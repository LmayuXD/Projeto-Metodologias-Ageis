public class Main {
    //inicializa e testa o backend

     import com.sun.net.httpserver.HttpServer;
     import java.net.InetSocketAddress;
     import controller.ContaController;
     import controller.PoupancaController;
     import controller.TransferenciaController;

     //import controller.ContaHandler;

    public class Main {
        public static void main(String[] args) throws Exception {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/api/contas", new ContaController());
            server.start();
            System.out.println("Servidor rodando em http://localhost:8080");
        }
    }
}
