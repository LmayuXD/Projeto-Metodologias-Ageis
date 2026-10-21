package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import controller.ContaController;
import model.Conta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Servidor {

    private static ContaController contaController = new ContaController();
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(8080), 0);

        servidor.createContext("/contas", exchange -> {
            // Configura os cabeçalhos de CORS
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                String method = exchange.getRequestMethod();

                if (method.equalsIgnoreCase("GET")) {
                    // RESPONDER DADOS EM JSON PARA O REACT
                    List<Conta> contas = contaController.listarContas();
                    String respostaJson = objectMapper.writeValueAsString(contas);
                    enviarResposta(exchange, 200, respostaJson);

                } else if (method.equalsIgnoreCase("POST")) {
                    // RECEBER DADOS EM JSON DO REACT
                    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String linha;
                    while ((linha = reader.readLine()) != null) {
                        sb.append(linha);
                    }

                    // Converte o JSON do React para objeto Conta usando o Jackson
                    Conta dadosRecebidos = objectMapper.readValue(sb.toString(), Conta.class);

                    Conta novaConta = contaController.criarConta(
                            dadosRecebidos.getNomeTitular(),
                            dadosRecebidos.getDocumento()
                    );

                    String respostaJson = objectMapper.writeValueAsString(novaConta);
                    enviarResposta(exchange, 201, respostaJson);
                } else {
                    enviarResposta(exchange, 405, "Método não permitido");
                }

            } catch (Exception e) {
                String erroJson = "{\"erro\": \"" + e.getMessage() + "\"}";
                enviarResposta(exchange, 400, erroJson);
            }
        });

        servidor.start();
        System.out.println("Servidor rodando em http://localhost:8080");
    }

    private static void enviarResposta(HttpExchange exchange, int statusCode, String resposta) throws IOException {
        byte[] bytes = resposta.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}