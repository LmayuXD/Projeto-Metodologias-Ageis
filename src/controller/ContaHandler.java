package controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Conta;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ContaHandler implements HttpHandler {

    private final ContaController controller;
    private final Gson gson = new Gson();

    public ContaHandler(ContaController controller) {
        this.controller = controller;
    }

    // Formato do JSON que o React envia
    private static class CadastroRequest {
        String nomeTitular;
        String documento;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        // CORS (o React roda em outra porta)
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        String metodo = ex.getRequestMethod();

        if (metodo.equals("OPTIONS")) {          // preflight do navegador
            ex.sendResponseHeaders(204, -1);
            return;
        }

        try {
            if (metodo.equals("POST")) {
                cadastrar(ex);
            } else if (metodo.equals("GET")) {
                listarOuBuscar(ex);
            } else {
                responder(ex, 405, erro("Método não permitido"));
            }
        } catch (IllegalArgumentException e) {
            // erros de validação da Conta/Controller viram 400
            responder(ex, 400, erro(e.getMessage()));
        } catch (JsonSyntaxException e) {
            responder(ex, 400, erro("JSON inválido"));
        } catch (Exception e) {
            responder(ex, 500, erro("Erro interno no servidor"));
        }
    }

    private void cadastrar(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        CadastroRequest req = gson.fromJson(body, CadastroRequest.class);

        if (req == null) {
            throw new IllegalArgumentException("Corpo da requisição vazio");
        }

        Conta conta = controller.criarConta(req.nomeTitular, req.documento);
        responder(ex, 201, gson.toJson(conta));
    }

    // GET /api/contas       -> lista todas
    // GET /api/contas/3     -> busca a conta de id 3
    private void listarOuBuscar(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String[] partes = path.split("/");   // ["", "api", "contas", "3"]

        if (partes.length >= 4) {
            Conta conta = controller.buscarPorId(Long.parseLong(partes[3]));
            if (conta == null) {
                responder(ex, 404, erro("Conta não encontrada"));
            } else {
                responder(ex, 200, gson.toJson(conta));
            }
        } else {
            responder(ex, 200, gson.toJson(controller.listarContas()));
        }
    }

    private String erro(String mensagem) {
        return gson.toJson(java.util.Map.of("erro", mensagem));
    }

    private void responder(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}