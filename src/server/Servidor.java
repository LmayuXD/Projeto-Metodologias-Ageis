package server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import controller.ContaController;
import controller.PoupancaController;
import controller.TransferenciaController;
import model.Conta;
import model.Transferencia;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class Servidor {

    // Formato do JSON que o React envia
    private record CadastroRequest(String nomeTitular, String documento) {}
    private record LoginRequest(String documento) {}
    private record ValorRequest(double valor) {}
    private record PixRequest(Long contaOrigemId, String chaveDestino, double valor, String descricao) {}

    // Esconde o extrato do JSON da conta: ele só sai pela rota /api/contas/{id}/extrato
    private abstract static class ContaSemExtrato {
        @JsonIgnore
        abstract List<Transferencia> getExtrato();
    }

    private static final ContaController contaController = new ContaController();
    private static final PoupancaController poupancaController = new PoupancaController(contaController);
    private static final TransferenciaController transferenciaController = new TransferenciaController(contaController);
    private static final ObjectMapper json = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .addMixIn(Conta.class, ContaSemExtrato.class);

    @FunctionalInterface
    private interface Rota {
        void tratar(HttpExchange ex, String metodo, String[] partes) throws Exception;
    }

    public static void main(String[] args) throws Exception {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(8080), 0);

        servidor.createContext("/api/contas", ex -> executar(ex, Servidor::rotaContas));
        servidor.createContext("/api/login", ex -> executar(ex, Servidor::rotaLogin));
        servidor.createContext("/api/poupanca", ex -> executar(ex, Servidor::rotaPoupanca));
        servidor.createContext("/api/pix", ex -> executar(ex, Servidor::rotaPix));

        servidor.start();
        System.out.println("Servidor rodando em http://localhost:8080");
    }

    // ---------------------------------------------------------------- núcleo
    // CORS + preflight + tratamento de erros, comum a todas as rotas
    private static void executar(HttpExchange ex, Rota rota) throws IOException {
        // CORS (o React roda em outra porta)
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "http://localhost:5173");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        String metodo = ex.getRequestMethod();
        if (metodo.equalsIgnoreCase("OPTIONS")) {            // preflight do navegador
            ex.sendResponseHeaders(204, -1);
            return;
        }

        try {
            String[] partes = ex.getRequestURI().getPath().split("/"); // ["", "api", "contas", "3"]
            rota.tratar(ex, metodo.toUpperCase(), partes);
        } catch (IllegalArgumentException e) {
            // validação das regras de negócio (e NumberFormatException) -> 400
            responder(ex, 400, erro(e.getMessage()));
        } catch (NoSuchElementException e) {
            // conta, chave Pix ou transação que não existe -> 404
            responder(ex, 404, erro(e.getMessage()));
        } catch (JsonProcessingException e) {
            responder(ex, 400, erro("JSON inválido"));
        } catch (Exception e) {
            e.printStackTrace();
            responder(ex, 500, erro("Erro interno no servidor"));
        }
    }

    // ----------------------------------------------------------------- contas
    // POST /api/contas        -> cadastra
    // GET  /api/contas        -> lista todas
    // GET  /api/contas/{id}   -> busca uma
    // GET  /api/contas/{id}/extrato[?tipo=PIX_ENVIADO|PIX_RECEBIDO] -> extrato, do mais recente ao mais antigo
    private static void rotaContas(HttpExchange ex, String metodo, String[] partes) throws Exception {
        boolean colecao = partes.length == 3;
        boolean item = partes.length == 4;

        if (metodo.equals("POST") && colecao) {
            CadastroRequest req = json.readValue(ex.getRequestBody(), CadastroRequest.class);
            Conta conta = contaController.criarConta(req.nomeTitular(), req.documento());
            responder(ex, 201, json.writeValueAsString(conta));

        } else if (metodo.equals("GET") && colecao) {
            responder(ex, 200, json.writeValueAsString(contaController.listarContas()));

        } else if (metodo.equals("GET") && item) {
            Conta conta = contaController.buscarPorId(Long.parseLong(partes[3]));
            if (conta == null) {
                responder(ex, 404, erro("Conta não encontrada"));
            } else {
                responder(ex, 200, json.writeValueAsString(conta));
            }

        } else if (metodo.equals("GET") && partes.length == 5 && partes[4].equals("extrato")) {
            Long contaId = Long.parseLong(partes[3]);
            String tipo = parametro(ex, "tipo");
            responder(ex, 200, json.writeValueAsString(transferenciaController.listarExtrato(contaId, tipo)));

        } else {
            responder(ex, 404, erro("Rota não encontrada"));
        }
    }

    // -------------------------------------------------------------------- pix
    // POST /api/pix -> {"contaOrigemId":1,"chaveDestino":"98765432100","valor":50,"descricao":"almoço"}
    private static void rotaPix(HttpExchange ex, String metodo, String[] partes) throws Exception {
        if (metodo.equals("POST") && partes.length == 3) {
            PixRequest req = json.readValue(ex.getRequestBody(), PixRequest.class);
            Transferencia comprovante = transferenciaController.realizarPix(
                    req.contaOrigemId(), req.chaveDestino(), req.valor(), req.descricao());
            responder(ex, 201, json.writeValueAsString(comprovante));
        } else {
            responder(ex, 404, erro("Rota não encontrada"));
        }
    }

    // --------------------------------------------------------------- caixinha
    // GET  /api/poupanca/{contaId}            -> consulta a caixinha
    // POST /api/poupanca/{contaId}/guardar    -> {"valor": 200}
    // POST /api/poupanca/{contaId}/resgatar   -> {"valor": 50}
    private static void rotaPoupanca(HttpExchange ex, String metodo, String[] partes) throws Exception {
        if (partes.length < 4) {
            responder(ex, 404, erro("Rota não encontrada"));
            return;
        }
        Long contaId = Long.parseLong(partes[3]);

        if (metodo.equals("GET") && partes.length == 4) {
            responder(ex, 200, json.writeValueAsString(poupancaController.consultar(contaId)));

        } else if (metodo.equals("POST") && partes.length == 5 && partes[4].equals("guardar")) {
            ValorRequest req = json.readValue(ex.getRequestBody(), ValorRequest.class);
            responder(ex, 200, json.writeValueAsString(poupancaController.guardar(contaId, req.valor())));

        } else if (metodo.equals("POST") && partes.length == 5 && partes[4].equals("resgatar")) {
            ValorRequest req = json.readValue(ex.getRequestBody(), ValorRequest.class);
            responder(ex, 200, json.writeValueAsString(poupancaController.resgatar(contaId, req.valor())));

        } else {
            responder(ex, 404, erro("Rota não encontrada"));
        }
    }

    // ---------------------------------------------------------------- helpers
    // lê um parâmetro da query string (?nome=valor); null se não existir
    private static String parametro(HttpExchange ex, String nome) {
        String query = ex.getRequestURI().getQuery();
        if (query == null) {
            return null;
        }
        for (String par : query.split("&")) {
            String[] kv = par.split("=", 2);
            if (kv[0].equals(nome) && kv.length == 2) {
                return kv[1];
            }
        }
        return null;
    }

    private static String erro(String mensagem) throws IOException {
        return json.writeValueAsString(Map.of("erro", mensagem));
    }

    private static void responder(HttpExchange ex, int status, String corpo) throws IOException {
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    // ----------------------------------------------------------------- login
    // POST /api/login -> {"documento": "12345678901"}
    private static void rotaLogin(HttpExchange ex, String metodo, String[] partes) throws Exception {
        if (metodo.equals("POST") && partes.length == 3) {
            LoginRequest req = json.readValue(ex.getRequestBody(), LoginRequest.class);

            if (req.documento() == null || req.documento().trim().isEmpty()) {
                throw new IllegalArgumentException("O documento é obrigatório para o login!");
            }

            Conta conta = contaController.buscarPorDocumento(req.documento());

            if (conta == null) {
                responder(ex, 404, erro("Conta não encontrada para o documento informado."));
            } else {
                responder(ex, 200, json.writeValueAsString(conta));
            }
        } else {
            responder(ex, 404, erro("Rota não encontrada"));
        }
    }
}