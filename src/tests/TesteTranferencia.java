package tests;

import controller.ContaController;
import controller.TransferenciaController;
import model.Conta;
import model.Transferencia;

import java.util.List;

public class TesteTranferencia {

    private static ContaController contaController;
    private static TransferenciaController transferenciaController;
    private static Conta origem; // id 1, saldo 500
    private static Conta destino; // id 2, saldo 500
    private static int ok = 0, falhas = 0;

    public static void main(String[] args) {
        // Validações básicas e limites de transferência
        rodar("pixComValorValidoAtualizaSaldosECriaExtratos", TesteTranferencia::pixComValorValidoAtualizaSaldosECriaExtratos);
        rodar("naoPermiteTransferirParaSiMesmo", TesteTranferencia::naoPermiteTransferirParaSiMesmo);
        rodar("naoPermitePixComSaldoInsuficiente", TesteTranferencia::naoPermitePixComSaldoInsuficiente);
        rodar("rejeitaValorAbaixoDoMinimo", TesteTranferencia::rejeitaValorAbaixoDoMinimo);
        rodar("rejeitaValorAcimaDoLimitePorTransferencia", TesteTranferencia::rejeitaValorAcimaDoLimitePorTransferencia);
        rodar("rejeitaValorComMaisDeDuasCasasDecimais", TesteTranferencia::rejeitaValorComMaisDeDuasCasasDecimais);
        rodar("rejeitaValoresInvalidosNaNInifinito", TesteTranferencia::rejeitaValoresInvalidosNaNInifinito);

        // Tratamento de chaves Pix e descrições
        rodar("normalizaChaveCpfRetirandoPontosETracos", TesteTranferencia::normalizaChaveCpfRetirandoPontosETracos);
        rodar("lancaExcecaoSeChaveNaoForEncontrada", TesteTranferencia::lancaExcecaoSeChaveNaoForEncontrada);
        rodar("rejeitaDescricaoAcimaDoTamanhoMaximo", TesteTranferencia::rejeitaDescricaoAcimaDoTamanhoMaximo);
        rodar("aceitaDescricaoVaziaOuNula", TesteTranferencia::aceitaDescricaoVaziaOuNula);

        // Extratos e busca de transações
        rodar("extratoRetornaTransacoesDoMaisRecenteParaMaisAntigo", TesteTranferencia::extratoRetornaTransacoesDoMaisRecenteParaMaisAntigo);
        rodar("permiteFiltrarExtratoPorTipoEnviadoOuRecebido", TesteTranferencia::permiteFiltrarExtratoPorTipoEnviadoOuRecebido);
        rodar("buscarTransacaoPorIdRetornaCorretamente", TesteTranferencia::buscarTransacaoPorIdRetornaCorretamente);
        rodar("buscarTransacaoInexistenteLancaExcecao", TesteTranferencia::buscarTransacaoInexistenteLancaExcecao);

        System.out.println("\n==== " + ok + " OK | " + falhas + " FALHAS ====");
        System.exit(falhas > 0 ? 1 : 0);
    }

    private static void rodar(String nome, Runnable teste) {
        try {
            prepararCenario();
            teste.run();
            ok++;
            System.out.println("[OK]     " + nome);
        } catch (Throwable e) {
            falhas++;
            System.out.println("[FALHA]  " + nome + " -> " + e);
        }
    }

    private static void prepararCenario() {
        contaController = new ContaController();
        transferenciaController = new TransferenciaController(contaController);
        origem = contaController.criarConta("Aron Eren", "102.999.778-56");
        destino = contaController.criarConta("Mikasa Ackerman", "20388866654");
    }

    // =====================================================================
    // TESTES DE TRANSFERÊNCIA (PIX)
    // =====================================================================

    private static void pixComValorValidoAtualizaSaldosECriaExtratos() {
        Transferencia t = transferenciaController.realizarPix(origem.getId(), "20388866654", 150.0, "Aluguel");

        assertEquals(350.0, origem.getSaldoInicial());
        assertEquals(650.0, destino.getSaldoInicial());
        assertEquals("PIX_ENVIADO", t.getTipo());
        assertEquals(150.0, t.getValor());

        List<Transferencia> extratoOrigem = transferenciaController.listarExtrato(origem.getId());
        assertEquals(1, extratoOrigem.size());
        assertEquals("PIX_ENVIADO", extratoOrigem.get(0).getTipo());

        List<Transferencia> extratoDestino = transferenciaController.listarExtrato(destino.getId());
        assertEquals(1, extratoDestino.size());
        assertEquals("PIX_RECEBIDO", extratoDestino.get(0).getTipo());
    }

    private static void naoPermiteTransferirParaSiMesmo() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> transferenciaController.realizarPix(origem.getId(), "102.999.778-56", 50.0, "Auto-pix"));
        assertTrue(e.getMessage().contains("própria conta"));
        assertEquals(500.0, origem.getSaldoInicial());
    }

    private static void naoPermitePixComSaldoInsuficiente() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> transferenciaController.realizarPix(origem.getId(), "20388866654", 600.0, "Sem saldo"));
        assertTrue(e.getMessage().contains("Saldo insuficiente"));
        assertEquals(500.0, origem.getSaldoInicial());
    }

    private static void rejeitaValorAbaixoDoMinimo() {
        assertThrows(IllegalArgumentException.class,
                () -> transferenciaController.realizarPix(origem.getId(), "20388866654", 0.0, "Zero"));
        assertThrows(IllegalArgumentException.class,
                () -> transferenciaController.realizarPix(origem.getId(), "20388866654", -10.0, "Negativo"));
    }

    private static void rejeitaValorAcimaDoLimitePorTransferencia() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> transferenciaController.realizarPix(origem.getId(), "20388866654", 6000.0, "Valor alto"));
        assertTrue(e.getMessage().contains("limite por transferência"));
    }

    private static void rejeitaValorComMaisDeDuasCasasDecimais() {
        assertThrows(IllegalArgumentException.class,
                () -> transferenciaController.realizarPix(origem.getId(), "20388866654", 10.555, "Fração centavo"));
    }

    private static void rejeitaValoresInvalidosNaNInifinito() {
        assertThrows(IllegalArgumentException.class,
                () -> transferenciaController.realizarPix(origem.getId(), "20388866654", Double.NaN, "NaN"));
        assertThrows(IllegalArgumentException.class,
                () -> transferenciaController.realizarPix(origem.getId(), "20388866654", Double.POSITIVE_INFINITY, "Infinito"));
    }

    private static void normalizaChaveCpfRetirandoPontosETracos() {
        Transferencia t = transferenciaController.realizarPix(origem.getId(), "203.888.666-54", 100.0, "Formatado");
        assertEquals(400.0, origem.getSaldoInicial());
        assertEquals(600.0, destino.getSaldoInicial());
    }

    private static void lancaExcecaoSeChaveNaoForEncontrada() {
        assertThrows(java.util.NoSuchElementException.class,
                () -> transferenciaController.realizarPix(origem.getId(), "99999999999", 50.0, "Chave inexistente"));
    }

    private static void rejeitaDescricaoAcimaDoTamanhoMaximo() {
        String descGrande = "a".repeat(141);
        assertThrows(IllegalArgumentException.class,
                () -> transferenciaController.realizarPix(origem.getId(), "20388866654", 50.0, descGrande));
    }

    private static void aceitaDescricaoVaziaOuNula() {
        Transferencia t1 = transferenciaController.realizarPix(origem.getId(), "20388866654", 10.0, null);
        Transferencia t2 = transferenciaController.realizarPix(origem.getId(), "20388866654", 10.0, "");
        assertTrue(t1.getId() != null);
        assertTrue(t2.getId() != null);
    }

    private static void extratoRetornaTransacoesDoMaisRecenteParaMaisAntigo() {
        transferenciaController.realizarPix(origem.getId(), "20388866654", 10.0, "Primeira");
        try { Thread.sleep(10); } catch (InterruptedException e) {} // Garante diferença de milissegundos na data
        transferenciaController.realizarPix(origem.getId(), "20388866654", 20.0, "Segunda");

        List<Transferencia> extrato = transferenciaController.listarExtrato(origem.getId());
        assertEquals(2, extrato.size());
        assertTrue(extrato.get(0).getDescricao().contains("Segunda"));
    }

    private static void permiteFiltrarExtratoPorTipoEnviadoOuRecebido() {
        transferenciaController.realizarPix(origem.getId(), "20388866654", 10.0, "Enviado 1");

        List<Transferencia> enviados = transferenciaController.listarExtrato(origem.getId(), TransferenciaController.PIX_ENVIADO);
        List<Transferencia> recebidos = transferenciaController.listarExtrato(origem.getId(), TransferenciaController.PIX_RECEBIDO);

        assertEquals(1, enviados.size());
        assertEquals(0, recebidos.size());
    }

    private static void buscarTransacaoPorIdRetornaCorretamente() {
        Transferencia tEnviada = transferenciaController.realizarPix(origem.getId(), "20388866654", 10.0, "Busca id");
        Transferencia tAchada = transferenciaController.buscarPorId(origem.getId(), tEnviada.getId());

        assertEquals(tEnviada.getId(), tAchada.getId());
        assertEquals(10.0, tAchada.getValor());
    }

    private static void buscarTransacaoInexistenteLancaExcecao() {
        assertThrows(java.util.NoSuchElementException.class,
                () -> transferenciaController.buscarPorId(origem.getId(), "id-inexistente-123"));
    }

    // =====================================================================
    // HELPERS DE ASSERÇÃO (substituem o JUnit)
    // =====================================================================

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