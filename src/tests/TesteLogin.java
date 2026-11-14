package tests;

import controller.ContaController;
import model.Conta;
import java.util.List;

public class TesteLogin {
    private static ContaController controller;
    private static int ok = 0, falhas = 0;

    public static void main(String[] args) {
        rodar("criarContaComSucessoEDefineSaldo500", TesteLogin::criarContaComSucessoEDefineSaldo500);
        rodar("buscarPorIdRetornaContaCorreta", TesteLogin::buscarPorIdRetornaContaCorreta);
        rodar("buscarPorIdRetornaNuloSeNaoExistir", TesteLogin::buscarPorIdRetornaNuloSeNaoExistir);
        rodar("buscarPorDocumentoEncontraContaExata", TesteLogin::buscarPorDocumentoEncontraContaExata);
        rodar("buscarPorDocumentoEncontraContaIgnorandoFormatacao", TesteLogin::buscarPorDocumentoEncontraContaIgnorandoFormatacao);
        rodar("buscarPorDocumentoRetornaNuloSeNaoExistir", TesteLogin::buscarPorDocumentoRetornaNuloSeNaoExistir);
        rodar("buscarPorDocumentoLidaComNuloComSeguranca", TesteLogin::buscarPorDocumentoLidaComNuloComSeguranca);
        rodar("atualizarContaModificaDadosComSucesso", TesteLogin::atualizarContaModificaDadosComSucesso);
        rodar("deletarContaRemoveDaLista", TesteLogin::deletarContaRemoveDaLista);
        rodar("listarContasRetornaTodasAsContasCadastradas", TesteLogin::listarContasRetornaTodasAsContasCadastradas);

        System.out.println("\n==== " + ok + " OK | " + falhas + " FALHAS ====");
        System.exit(falhas > 0 ? 1 : 0);
    }

    // executa um teste com um controller novo (o que o @BeforeEach fazia)
    private static void rodar(String nome, Runnable teste) {
        try {
            inicializarController();
            teste.run();
            ok++;
            System.out.println("[OK]     " + nome);
        } catch (Throwable e) {
            falhas++;
            System.out.println("[FALHA]  " + nome + " -> " + e);
        }
    }

    private static void inicializarController() {
        controller = new ContaController();
    }

    // ---------------------------------------------------------------- criar conta
    private static void criarContaComSucessoEDefineSaldo500() {
        Conta c = controller.criarConta("Maria Silva", "12345678901");
        assertEquals(1L, c.getId());
        assertEquals("Maria Silva", c.getNomeTitular());
        assertEquals("12345678901", c.getDocumento());
        assertEquals(500.0, c.getSaldoInicial());
    }

    // ---------------------------------------------------------------- buscar por id
    private static void buscarPorIdRetornaContaCorreta() {
        controller.criarConta("Maria Silva", "12345678901");
        controller.criarConta("João Santos", "98765432100");

        Conta encontrada = controller.buscarPorId(2L);
        assertTrue(encontrada != null);
        assertEquals("João Santos", encontrada.getNomeTitular());
    }

    private static void buscarPorIdRetornaNuloSeNaoExistir() {
        Conta encontrada = controller.buscarPorId(99L);
        assertTrue(encontrada == null);
    }

    // ---------------------------------------------------------------- buscar por documento (Login)
    private static void buscarPorDocumentoEncontraContaExata() {
        controller.criarConta("Maria Silva", "12345678901");
        Conta encontrada = controller.buscarPorDocumento("12345678901");
        assertTrue(encontrada != null);
        assertEquals("Maria Silva", encontrada.getNomeTitular());
    }

    private static void buscarPorDocumentoEncontraContaIgnorandoFormatacao() {
        controller.criarConta("Carlos Souza", "123.456.789-01");

        // Deve encontrar tanto buscando limpo quanto formatado
        Conta encontrada1 = controller.buscarPorDocumento("12345678901");
        Conta encontrada2 = controller.buscarPorDocumento("123.456.789-01");

        assertTrue(encontrada1 != null);
        assertTrue(encontrada2 != null);
        assertEquals("Carlos Souza", encontrada1.getNomeTitular());
    }

    private static void buscarPorDocumentoRetornaNuloSeNaoExistir() {
        controller.criarConta("Maria Silva", "12345678901");
        Conta encontrada = controller.buscarPorDocumento("00000000000");
        assertTrue(encontrada == null);
    }

    private static void buscarPorDocumentoLidaComNuloComSeguranca() {
        Conta encontrada = controller.buscarPorDocumento(null);
        assertTrue(encontrada == null);
    }

    // ---------------------------------------------------------------- atualizar
    private static void atualizarContaModificaDadosComSucesso() {
        controller.criarConta("Maria Silva", "12345678901");
        Conta atualizacao = new Conta(1L, "Maria Silva Sauro", "12345678901");
        Conta atualizada = controller.atualizarConta(1L, atualizacao);

        assertTrue(atualizada != null);
        assertEquals("Maria Silva Sauro", atualizada.getNomeTitular());
    }

    // ---------------------------------------------------------------- deletar
    private static void deletarContaRemoveDaLista() {
        controller.criarConta("Maria Silva", "12345678901");
        boolean deletado = controller.deletarConta(1L);
        assertTrue(deletado);
        assertTrue(controller.listarContas().isEmpty());
    }

    // ---------------------------------------------------------------- listar
    private static void listarContasRetornaTodasAsContasCadastradas() {
        controller.criarConta("Ana", "11122233344");
        controller.criarConta("Bruno", "55566677788");
        List<Conta> lista = controller.listarContas();
        assertEquals(2, lista.size());
    }

    // ------------------------------------- auxiliares (substituem o JUnit)
    private interface Executavel {
        void executar();
    }

    private static void assertEquals(Object esperado, Object atual) {
        if (!esperado.equals(atual)) {
            throw new AssertionError("esperado <" + esperado + "> mas foi <" + atual + ">");
        }
    }

    private static void assertTrue(boolean condicao) {
        if (!condicao) {
            throw new AssertionError("a condição esperada era verdadeira");
        }
    }

    private static <T extends Throwable> T assertThrows(Class<T> tipo, Executavel codigo) {
        try {
            codigo.executar();
        } catch (Throwable e) {
            if (tipo.isInstance(e)) {
                return tipo.cast(e);
            }
            throw new AssertionError("esperava " + tipo.getSimpleName() + " mas lançou " + e);
        }
        throw new AssertionError("esperava " + tipo.getSimpleName() + " mas não lançou nada");
    }
}
