package tests;

import controller.ContaController;
import controller.PoupancaController;
import controller.TransferenciaController;
import model.Conta;
import model.Poupanca;

/**
 * Testes do módulo de poupança ("caixinha"): model.Poupanca e controller.PoupancaController.
 * Padrão: Java puro com main, igual ao TesteConta. Cada teste recebe controllers novos
 * (prepararCenario), então nenhum teste depende de outro nem da ordem de execução.
 */
public class TestePoupanca {

    private static ContaController contaController;
    private static PoupancaController poupancaController;
    private static Conta conta1;   // id 1, saldo 500
    private static Conta conta2;   // id 2, saldo 500
    private static int ok = 0, falhas = 0;

    public static void main(String[] args) {
        // model.Poupanca
        rodar("novaPoupancaGuardaIdEComecaComSaldoZero", TestePoupanca::novaPoupancaGuardaIdEComecaComSaldoZero);
        rodar("depositarSomaAoSaldoERetornaONovoSaldo", TestePoupanca::depositarSomaAoSaldoERetornaONovoSaldo);
        rodar("depositosConsecutivosSeAcumulam", TestePoupanca::depositosConsecutivosSeAcumulam);
        rodar("sacarSubtraiDoSaldoERetornaONovoSaldo", TestePoupanca::sacarSubtraiDoSaldoERetornaONovoSaldo);
        rodar("sacarTodoOSaldoDeixaZero", TestePoupanca::sacarTodoOSaldoDeixaZero);
        rodar("sacarMaisQueOSaldoLancaExcecaoENaoAlteraOSaldo", TestePoupanca::sacarMaisQueOSaldoLancaExcecaoENaoAlteraOSaldo);
        rodar("sacarComSaldoZeroLancaExcecao", TestePoupanca::sacarComSaldoZeroLancaExcecao);

        // consultar
        rodar("consultarCriaCaixinhaComSaldoZeroLigadaAContaCerta", TestePoupanca::consultarCriaCaixinhaComSaldoZeroLigadaAContaCerta);
        rodar("consultarDuasVezesDevolveAMesmaCaixinha", TestePoupanca::consultarDuasVezesDevolveAMesmaCaixinha);
        rodar("consultarContaInexistenteLancaExcecao", TestePoupanca::consultarContaInexistenteLancaExcecao);
        rodar("consultarComIdNuloLancaExcecao", TestePoupanca::consultarComIdNuloLancaExcecao);
        rodar("consultarNaoMexeNoSaldoDaConta", TestePoupanca::consultarNaoMexeNoSaldoDaConta);

        // guardar
        rodar("guardarValorValidoMoveDinheiroDaContaParaACaixinha", TestePoupanca::guardarValorValidoMoveDinheiroDaContaParaACaixinha);
        rodar("guardarNaoAfetaOutraConta", TestePoupanca::guardarNaoAfetaOutraConta);
        rodar("guardarVariasVezesAcumulaNaCaixinha", TestePoupanca::guardarVariasVezesAcumulaNaCaixinha);
        rodar("guardarTodoOSaldoDaContaZeraAConta", TestePoupanca::guardarTodoOSaldoDaContaZeraAConta);
        rodar("guardarUmCentavoAMaisQueOSaldoEhRejeitadoESemEfeito", TestePoupanca::guardarUmCentavoAMaisQueOSaldoEhRejeitadoESemEfeito);
        rodar("guardarValorGiganteEhRejeitadoESemEfeito", TestePoupanca::guardarValorGiganteEhRejeitadoESemEfeito);
        rodar("guardarRejeitaValorInvalidoESemEfeito", TestePoupanca::guardarRejeitaValorInvalidoESemEfeito);
        rodar("guardarEmContaInexistenteLancaExcecao", TestePoupanca::guardarEmContaInexistenteLancaExcecao);
        rodar("guardarComIdNuloLancaExcecao", TestePoupanca::guardarComIdNuloLancaExcecao);
        rodar("guardarValorComCentavosFuncionaParaValoresExatos", TestePoupanca::guardarValorComCentavosFuncionaParaValoresExatos);
        rodar("somaDeCentavosNaCaixinhaEhExata", TestePoupanca::somaDeCentavosNaCaixinhaEhExata);

        // resgatar
        rodar("resgatarValorValidoDevolveDinheiroParaAConta", TestePoupanca::resgatarValorValidoDevolveDinheiroParaAConta);
        rodar("resgatarTudoDeixaCaixinhaZeradaEContaComOSaldoOriginal", TestePoupanca::resgatarTudoDeixaCaixinhaZeradaEContaComOSaldoOriginal);
        rodar("resgatesConsecutivosSomamNaConta", TestePoupanca::resgatesConsecutivosSomamNaConta);
        rodar("resgatarMaisQueACaixinhaEhRejeitadoESemEfeito", TestePoupanca::resgatarMaisQueACaixinhaEhRejeitadoESemEfeito);
        rodar("resgatarSemTerGuardadoNadaEhRejeitado", TestePoupanca::resgatarSemTerGuardadoNadaEhRejeitado);
        rodar("resgatarRejeitaValorInvalidoESemEfeito", TestePoupanca::resgatarRejeitaValorInvalidoESemEfeito);
        rodar("resgatarEmContaInexistenteLancaExcecao", TestePoupanca::resgatarEmContaInexistenteLancaExcecao);
        rodar("resgatarNaoAfetaCaixinhaDeOutraConta", TestePoupanca::resgatarNaoAfetaCaixinhaDeOutraConta);

        // consistência
        rodar("consultaRefleteOSaldoAtualDepoisDeVariasOperacoes", TestePoupanca::consultaRefleteOSaldoAtualDepoisDeVariasOperacoes);
        rodar("dinheiroTotalEConservadoEmUmaSequenciaDeOperacoes", TestePoupanca::dinheiroTotalEConservadoEmUmaSequenciaDeOperacoes);
        rodar("operacaoRejeitadaNaoMudaNenhumSaldoNaMesmaSequencia", TestePoupanca::operacaoRejeitadaNaoMudaNenhumSaldoNaMesmaSequencia);

        // integração
        rodar("dinheiroGuardadoNaCaixinhaNaoPodeSerUsadoNoPix", TestePoupanca::dinheiroGuardadoNaCaixinhaNaoPodeSerUsadoNoPix);
        rodar("depoisDeResgatarOPixPassaAFuncionar", TestePoupanca::depoisDeResgatarOPixPassaAFuncionar);
        rodar("pixRecebidoPermiteGuardarMaisNaCaixinha", TestePoupanca::pixRecebidoPermiteGuardarMaisNaCaixinha);
        rodar("depoisDeApagarAContaACaixinhaNaoPodeMaisSerConsultada", TestePoupanca::depoisDeApagarAContaACaixinhaNaoPodeMaisSerConsultada);

        System.out.println("\n==== " + ok + " OK | " + falhas + " FALHAS ====");
        System.exit(falhas > 0 ? 1 : 0);
    }

    // executa um teste com um cenário novo (o que o @BeforeEach fazia)
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
        poupancaController = new PoupancaController(contaController);
        conta1 = contaController.criarConta("Maria Silva", "12345678901");
        conta2 = contaController.criarConta("João Pedro", "98765432100");
    }

    // valores que guardar/resgatar devem rejeitar
    private static double[] valoresInvalidos() {
        return new double[]{0, -0.0, -0.01, -5, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
    }

    // =====================================================================
    // model.Poupanca (classe isolada, sem controller)
    // =====================================================================

    // POUP-001
    private static void novaPoupancaGuardaIdEComecaComSaldoZero() {
        Poupanca p = new Poupanca(7L);
        assertEquals(7L, p.getId());
        assertEquals(0.0, p.getSaldo());
    }

    // POUP-002
    private static void depositarSomaAoSaldoERetornaONovoSaldo() {
        Poupanca p = new Poupanca(1L);
        double retorno = p.depositar(100.5);
        assertEquals(100.5, retorno);
        assertEquals(100.5, p.getSaldo());
    }

    // POUP-003
    private static void depositosConsecutivosSeAcumulam() {
        Poupanca p = new Poupanca(1L);
        p.depositar(10);
        p.depositar(20.5);
        p.depositar(0.5);
        assertEquals(31.0, p.getSaldo());
    }

    // POUP-004
    private static void sacarSubtraiDoSaldoERetornaONovoSaldo() {
        Poupanca p = new Poupanca(1L);
        p.depositar(100);
        double retorno = p.sacar(40);
        assertEquals(60.0, retorno);
        assertEquals(60.0, p.getSaldo());
    }

    // POUP-005
    private static void sacarTodoOSaldoDeixaZero() {
        Poupanca p = new Poupanca(1L);
        p.depositar(75.25);
        p.sacar(75.25);
        assertEquals(0.0, p.getSaldo());
    }

    // POUP-006
    private static void sacarMaisQueOSaldoLancaExcecaoENaoAlteraOSaldo() {
        Poupanca p = new Poupanca(1L);
        p.depositar(50);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> p.sacar(50.01));
        assertTrue(e.getMessage().contains("Saldo insuficiente"));
        assertEquals(50.0, p.getSaldo());
    }

    // POUP-007
    private static void sacarComSaldoZeroLancaExcecao() {
        Poupanca p = new Poupanca(1L);
        assertThrows(IllegalArgumentException.class, () -> p.sacar(0.01));
        assertEquals(0.0, p.getSaldo());
    }

    // =====================================================================
    // PoupancaController.consultar
    // =====================================================================

    // POUP-010
    private static void consultarCriaCaixinhaComSaldoZeroLigadaAContaCerta() {
        Poupanca p = poupancaController.consultar(1L);
        assertEquals(1L, p.getId());
        assertEquals(0.0, p.getSaldo());
    }

    // POUP-011
    private static void consultarDuasVezesDevolveAMesmaCaixinha() {
        Poupanca primeira = poupancaController.consultar(1L);
        Poupanca segunda = poupancaController.consultar(1L);
        assertSame(primeira, segunda);
    }

    // POUP-012
    private static void consultarContaInexistenteLancaExcecao() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> poupancaController.consultar(99L));
        assertTrue(e.getMessage().contains("Conta não encontrada"));
    }

    // POUP-013
    private static void consultarComIdNuloLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> poupancaController.consultar(null));
    }

    // POUP-014
    private static void consultarNaoMexeNoSaldoDaConta() {
        poupancaController.consultar(1L);
        assertEquals(500.0, conta1.getSaldoInicial());
    }

    // =====================================================================
    // PoupancaController.guardar
    // =====================================================================

    // POUP-020
    private static void guardarValorValidoMoveDinheiroDaContaParaACaixinha() {
        Poupanca p = poupancaController.guardar(1L, 200);
        assertEquals(200.0, p.getSaldo());
        assertEquals(300.0, conta1.getSaldoInicial());
    }

    // POUP-021
    private static void guardarNaoAfetaOutraConta() {
        poupancaController.guardar(1L, 200);
        assertEquals(500.0, conta2.getSaldoInicial());
        assertEquals(0.0, poupancaController.consultar(2L).getSaldo());
    }

    // POUP-022
    private static void guardarVariasVezesAcumulaNaCaixinha() {
        poupancaController.guardar(1L, 100);
        poupancaController.guardar(1L, 50);
        Poupanca p = poupancaController.guardar(1L, 25);
        assertEquals(175.0, p.getSaldo());
        assertEquals(325.0, conta1.getSaldoInicial());
    }

    // POUP-023 (fronteira: exatamente todo o saldo)
    private static void guardarTodoOSaldoDaContaZeraAConta() {
        Poupanca p = poupancaController.guardar(1L, 500);
        assertEquals(500.0, p.getSaldo());
        assertEquals(0.0, conta1.getSaldoInicial());
    }

    // POUP-024 (fronteira: um centavo a mais que o saldo)
    private static void guardarUmCentavoAMaisQueOSaldoEhRejeitadoESemEfeito() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> poupancaController.guardar(1L, 500.01));
        assertTrue(e.getMessage().contains("Saldo insuficiente"));
        assertEquals(500.0, conta1.getSaldoInicial());
        assertEquals(0.0, poupancaController.consultar(1L).getSaldo());
    }

    // POUP-025
    private static void guardarValorGiganteEhRejeitadoESemEfeito() {
        assertThrows(IllegalArgumentException.class,
                () -> poupancaController.guardar(1L, Double.MAX_VALUE));
        assertEquals(500.0, conta1.getSaldoInicial());
    }

    // POUP-026
    private static void guardarRejeitaValorInvalidoESemEfeito() {
        for (double valor : valoresInvalidos()) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> poupancaController.guardar(1L, valor));
            assertTrue(e.getMessage().contains("maior que 0"));
            assertEquals(500.0, conta1.getSaldoInicial());
            assertEquals(0.0, poupancaController.consultar(1L).getSaldo());
        }
    }

    // POUP-027
    private static void guardarEmContaInexistenteLancaExcecao() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> poupancaController.guardar(99L, 10));
        assertTrue(e.getMessage().contains("Conta não encontrada"));
    }

    // POUP-028
    private static void guardarComIdNuloLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> poupancaController.guardar(null, 10));
    }

    // POUP-029
    private static void guardarValorComCentavosFuncionaParaValoresExatos() {
        Poupanca p = poupancaController.guardar(1L, 0.5);
        assertEquals(0.5, p.getSaldo());
        assertEquals(499.5, conta1.getSaldoInicial());
    }

    // POUP-030 (precisão financeira)
    // Este teste verifica que 0,10 + 0,20 na caixinha resulta em exatamente 0,30.
    // Como o saldo é double, o Java soma 0.30000000000000004. Se falhar, o defeito
    // está na representação do dinheiro (double) e não no teste.
    private static void somaDeCentavosNaCaixinhaEhExata() {
        poupancaController.guardar(1L, 0.10);
        Poupanca p = poupancaController.guardar(1L, 0.20);
        assertEquals(0.30, p.getSaldo());
    }

    // =====================================================================
    // PoupancaController.resgatar
    // =====================================================================

    // POUP-040
    private static void resgatarValorValidoDevolveDinheiroParaAConta() {
        poupancaController.guardar(1L, 200);
        Poupanca p = poupancaController.resgatar(1L, 50);
        assertEquals(150.0, p.getSaldo());
        assertEquals(350.0, conta1.getSaldoInicial());
    }

    // POUP-041
    private static void resgatarTudoDeixaCaixinhaZeradaEContaComOSaldoOriginal() {
        poupancaController.guardar(1L, 200);
        Poupanca p = poupancaController.resgatar(1L, 200);
        assertEquals(0.0, p.getSaldo());
        assertEquals(500.0, conta1.getSaldoInicial());
    }

    // POUP-042
    private static void resgatesConsecutivosSomamNaConta() {
        poupancaController.guardar(1L, 300);
        poupancaController.resgatar(1L, 100);
        Poupanca p = poupancaController.resgatar(1L, 50);
        assertEquals(150.0, p.getSaldo());
        assertEquals(350.0, conta1.getSaldoInicial());
    }

    // POUP-043
    private static void resgatarMaisQueACaixinhaEhRejeitadoESemEfeito() {
        poupancaController.guardar(1L, 100);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> poupancaController.resgatar(1L, 100.01));
        assertTrue(e.getMessage().contains("Saldo insuficiente"));
        assertEquals(100.0, poupancaController.consultar(1L).getSaldo());
        assertEquals(400.0, conta1.getSaldoInicial());
    }

    // POUP-044
    private static void resgatarSemTerGuardadoNadaEhRejeitado() {
        assertThrows(IllegalArgumentException.class, () -> poupancaController.resgatar(1L, 10));
        assertEquals(500.0, conta1.getSaldoInicial());
        assertEquals(0.0, poupancaController.consultar(1L).getSaldo());
    }

    // POUP-045
    private static void resgatarRejeitaValorInvalidoESemEfeito() {
        poupancaController.guardar(1L, 100);
        for (double valor : valoresInvalidos()) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> poupancaController.resgatar(1L, valor));
            assertTrue(e.getMessage().contains("maior que 0"));
            assertEquals(100.0, poupancaController.consultar(1L).getSaldo());
            assertEquals(400.0, conta1.getSaldoInicial());
        }
    }

    // POUP-046
    private static void resgatarEmContaInexistenteLancaExcecao() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> poupancaController.resgatar(99L, 10));
        assertTrue(e.getMessage().contains("Conta não encontrada"));
    }

    // POUP-047
    private static void resgatarNaoAfetaCaixinhaDeOutraConta() {
        poupancaController.guardar(1L, 100);
        poupancaController.guardar(2L, 300);
        poupancaController.resgatar(1L, 100);
        assertEquals(300.0, poupancaController.consultar(2L).getSaldo());
        assertEquals(200.0, conta2.getSaldoInicial());
    }

    // =====================================================================
    // Consistência e transições de estado
    // =====================================================================

    // POUP-050
    private static void consultaRefleteOSaldoAtualDepoisDeVariasOperacoes() {
        poupancaController.guardar(1L, 200);
        poupancaController.resgatar(1L, 50);
        poupancaController.guardar(1L, 25);
        poupancaController.resgatar(1L, 75);
        assertEquals(100.0, poupancaController.consultar(1L).getSaldo());
    }

    // POUP-051 (dinheiro total conservado: conta + caixinha = 500)
    private static void dinheiroTotalEConservadoEmUmaSequenciaDeOperacoes() {
        poupancaController.guardar(1L, 123);
        poupancaController.guardar(1L, 77);
        poupancaController.resgatar(1L, 50);
        poupancaController.guardar(1L, 10);
        poupancaController.resgatar(1L, 100);
        double total = conta1.getSaldoInicial() + poupancaController.consultar(1L).getSaldo();
        assertEquals(500.0, total, 1e-9);
    }

    // POUP-052
    private static void operacaoRejeitadaNaoMudaNenhumSaldoNaMesmaSequencia() {
        poupancaController.guardar(1L, 200);
        assertThrows(IllegalArgumentException.class, () -> poupancaController.guardar(1L, 9999));
        assertThrows(IllegalArgumentException.class, () -> poupancaController.resgatar(1L, 9999));
        assertThrows(IllegalArgumentException.class, () -> poupancaController.guardar(1L, -1));
        assertEquals(300.0, conta1.getSaldoInicial());
        assertEquals(200.0, poupancaController.consultar(1L).getSaldo());
    }

    // =====================================================================
    // Integração com outros módulos
    // =====================================================================

    // POUP-060: dinheiro na caixinha não pode ser usado no Pix
    private static void dinheiroGuardadoNaCaixinhaNaoPodeSerUsadoNoPix() {
        TransferenciaController pix = new TransferenciaController(contaController);
        poupancaController.guardar(1L, 400);   // conta fica com 100

        assertThrows(IllegalArgumentException.class,
                () -> pix.realizarPix(1L, "98765432100", 150.00, "teste"));

        assertEquals(100.0, conta1.getSaldoInicial());
        assertEquals(500.0, conta2.getSaldoInicial());
        assertEquals(400.0, poupancaController.consultar(1L).getSaldo());
    }

    // POUP-061: resgatar devolve o dinheiro e libera o Pix
    private static void depoisDeResgatarOPixPassaAFuncionar() {
        TransferenciaController pix = new TransferenciaController(contaController);
        poupancaController.guardar(1L, 400);
        poupancaController.resgatar(1L, 400);

        pix.realizarPix(1L, "98765432100", 150.00, "teste");

        assertEquals(350.0, conta1.getSaldoInicial());
        assertEquals(650.0, conta2.getSaldoInicial());
    }

    // POUP-062: Pix recebido aumenta o que pode ser guardado
    private static void pixRecebidoPermiteGuardarMaisNaCaixinha() {
        TransferenciaController pix = new TransferenciaController(contaController);
        pix.realizarPix(2L, "12345678901", 200.00, "teste");   // conta1 passa a ter 700

        Poupanca p = poupancaController.guardar(1L, 700);

        assertEquals(700.0, p.getSaldo());
        assertEquals(0.0, conta1.getSaldoInicial());
    }

    // POUP-063: conta apagada deixa a caixinha inacessível
    private static void depoisDeApagarAContaACaixinhaNaoPodeMaisSerConsultada() {
        poupancaController.guardar(1L, 100);
        contaController.deletarConta(1L);
        assertThrows(IllegalArgumentException.class, () -> poupancaController.consultar(1L));
        assertThrows(IllegalArgumentException.class, () -> poupancaController.guardar(1L, 10));
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

    private static void assertEquals(double esperado, double atual, double margem) {
        if (Math.abs(esperado - atual) > margem) {
            throw new AssertionError("esperado <" + esperado + "> mas foi <" + atual + ">");
        }
    }

    private static void assertSame(Object esperado, Object atual) {
        if (esperado != atual) {
            throw new AssertionError("os objetos deveriam ser o mesmo");
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
