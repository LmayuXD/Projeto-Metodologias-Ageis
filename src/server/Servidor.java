package server;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Servidor {

    public static void main(String[] args) throws Exception {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(8080), 0);

        servidor.createContext("/", exchange -> {
            byte[] resposta = "Servidor Nekopay funcionando!".getBytes();
            exchange.sendResponseHeaders(200, resposta.length);
            exchange.getResponseBody().write(resposta);
            exchange.close();
        });

        servidor.start();
        System.out.println("Servidor rodando em http://localhost:8080");
    }
}