import server.Servidor;
import tests.TesteServidor;

public class Main {
    //inicializa e testa o backend

    public static void main(String[] args) throws Exception {
        Servidor.main(new String[0]);   // sobe o servidor (a porta 8080 precisa estar livre)
        try {
            TesteServidor.testeCors();
            TesteServidor.testeContas();
            TesteServidor.testePoupanca();
            TesteServidor.testePix();
        } catch (Exception e) {
            TesteServidor.falhas++;
            System.out.println("[ERRO]   o teste parou por uma exceção: " + e);
        }
        System.out.println("\n==== " + TesteServidor.ok + " OK | " + TesteServidor.falhas + " FALHAS ====");
        System.exit(TesteServidor.falhas > 0 ? 1 : 0);   // encerra também o servidor, mesmo com erro
    }
}