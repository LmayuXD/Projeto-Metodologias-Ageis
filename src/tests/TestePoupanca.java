package tests;

import controller.ContaController;
import controller.PoupancaController;
import controller.TransferenciaController;
import model.Conta;
import model.Poupanca;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes do módulo de poupança ("caixinha"): model.Poupanca e controller.PoupancaController.
 * Padrão: JUnit 5, igual ao TesteConta. Cada teste recebe controllers novos (@BeforeEach),
 * então nenhum teste depende de outro nem da ordem de execução.
 */
class TestePoupanca {

    private ContaController contaController;
    private PoupancaController poupancaController;
    private Conta conta1;   // id 1, saldo 500
    private Conta conta2;   // id 2, saldo 500

    @BeforeEach
    void prepararCenario() {
        contaController = new ContaController();
        poupancaController = new PoupancaController(contaController);
        conta1 = contaController.criarConta("Maria Silva", "12345678901");
        conta2 = contaController.criarConta("João Pedro", "98765432100");
    }

    // =====================================================================
    // model.Poupanca (classe isolada, sem controller)
    // =====================================================================

    // POUP-001
    @Test
    void novaPoupancaGuardaIdEComecaComSaldoZero() {
        Poupanca p = new Poupanca(7L);
        assertEquals(7L, p.getId());
        assertEquals(0.0, p.getSaldo());
    }

    // POUP-002
    @Test
    void depositarSomaAoSaldoERetornaONovoSaldo() {
        Poupanca p = new Poupanca(1L);
        double retorno = p.depositar(100.5);
        assertEquals(100.5, retorno);
        assertEquals(100.5, p.getSaldo());
    }

    // POUP-003
    @Test
    void depositosConsecutivosSeAcumulam() {
        Poupanca p = new Poupanca(1L);
        p.depositar(10);
        p.depositar(20.5);
        p.depositar(0.5);
        assertEquals(31.0, p.getSaldo());
    }

    // POUP-004
    @Test
    void sacarSubtraiDoSaldoERetornaONovoSaldo() {
        Poupanca p = new Poupanca(1L);
        p.depositar(100);
        double retorno = p.sacar(40);
        assertEquals(60.0, retorno);
        assertEquals(60.0, p.getSaldo());
    }

    // POUP-005
    @Test
    void sacarTodoOSaldoDeixaZero() {
        Poupanca p = new Poupanca(1L);
        p.depositar(75.25);
        p.sacar(75.25);
        assertEquals(0.0, p.getSaldo());
    }

    // POUP-006
    @Test
    void sacarMaisQueOSaldoLancaExcecaoENaoAlteraOSaldo() {
        Poupanca p = new Poupanca(1L);
        p.depositar(50);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> p.sacar(50.01));
        assertTrue(e.getMessage().contains("Saldo insuficiente"));
        assertEquals(50.0, p.getSaldo());
    }

    // POUP-007
    @Test
    void sacarComSaldoZeroLancaExcecao() {
        Poupanca p = new Poupanca(1L);
        assertThrows(IllegalArgumentException.class, () -> p.sacar(0.01));
        assertEquals(0.0, p.getSaldo());
    }

    // =====================================================================
    // PoupancaController.consultar
    // =====================================================================

    // POUP-010
    @Test
    void consultarCriaCaixinhaComSaldoZeroLigadaAContaCerta() {
        Poupanca p = poupancaController.consultar(1L);
        assertEquals(1L, p.getId());
        assertEquals(0.0, p.getSaldo());
    }

    // POUP-011
    @Test
    void consultarDuasVezesDevolveAMesmaCaixinha() {
        Poupanca primeira = poupancaController.consultar(1L);
        Poupanca segunda = poupancaController.consultar(1L);
        assertSame(primeira, segunda);
    }

    // POUP-012
    @Test
    void consultarContaInexistenteLancaExcecao() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> poupancaController.consultar(99L));
        assertTrue(e.getMessage().contains("Conta não encontrada"));
    }

    // POUP-013
    @Test
    void consultarComIdNuloLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> poupancaController.consultar(null));
    }

    // POUP-014
    @Test
    void consultarNaoMexeNoSaldoDaConta() {
        poupancaController.consultar(1L);
        assertEquals(500.0, conta1.getSaldoInicial());
    }

    // =====================================================================
    // PoupancaController.guardar
    // =====================================================================

    // POUP-020
    @Test
    void guardarValorValidoMoveDinheiroDaContaParaACaixinha() {
        Poupanca p = poupancaController.guardar(1L, 200);
        assertEquals(200.0, p.getSaldo());
        assertEquals(300.0, conta1.getSaldoInicial());
    }

    // POUP-021
    @Test
    void guardarNaoAfetaOutraConta() {
        poupancaController.guardar(1L, 200);
        assertEquals(500.0, conta2.getSaldoInicial());
        assertEquals(0.0, poupancaController.consultar(2L).getSaldo());
    }

    // POUP-022
    @Test
    void guardarVariasVezesAcumulaNaCaixinha() {
        poupancaController.guardar(1L, 100);
        poupancaController.guardar(1L, 50);
        Poupanca p = poupancaController.guardar(1L, 25);
        assertEquals(175.0, p.getSaldo());
        assertEquals(325.0, conta1.getSaldoInicial());
    }

    // POUP-023 (fronteira: exatamente todo o saldo)
    @Test
    void guardarTodoOSaldoDaContaZeraAConta() {
        Poupanca p = poupancaController.guardar(1L, 500);
        assertEquals(500.0, p.getSaldo());
        assertEquals(0.0, conta1.getSaldoInicial());
    }

    // POUP-024 (fronteira: um centavo a mais que o saldo)
    @Test
    void guardarUmCentavoAMaisQueOSaldoEhRejeitadoESemEfeito() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> poupancaController.guardar(1L, 500.01));
        assertTrue(e.getMessage().contains("Saldo insuficiente"));
        assertEquals(500.0, conta1.getSaldoInicial());
        assertEquals(0.0, poupancaController.consultar(1L).getSaldo());
    }

    // POUP-025
    @Test
    void guardarValorGiganteEhRejeitadoESemEfeito() {
        assertThrows(IllegalArgumentException.class,
                () -> poupancaController.guardar(1L, Double.MAX_VALUE));
        assertEquals(500.0, conta1.getSaldoInicial());
    }

    // POUP-026
    @ParameterizedTest
    @ValueSource(doubles = {0, -0.0, -0.01, -5, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void guardarRejeitaValorInvalidoESemEfeito(double valor) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> poupancaController.guardar(1L, valor));
        assertTrue(e.getMessage().contains("maior que 0"));
        assertEquals(500.0, conta1.getSaldoInicial());
        assertEquals(0.0, poupancaController.consultar(1L).getSaldo());
    }

    // POUP-027
    @Test
    void guardarEmContaInexistenteLancaExcecao() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> poupancaController.guardar(99L, 10));
        assertTrue(e.getMessage().contains("Conta não encontrada"));
    }

    // POUP-028
    @Test
    void guardarComIdNuloLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> poupancaController.guardar(null, 10));
    }

    // POUP-029
    @Test
    void guardarValorComCentavosFuncionaParaValoresExatos() {
        Poupanca p = poupancaController.guardar(1L, 0.5);
        assertEquals(0.5, p.getSaldo());
        assertEquals(499.5, conta1.getSaldoInicial());
    }

    // POUP-030 (precisão financeira)
    // Este teste verifica que 0,10 + 0,20 na caixinha resulta em exatamente 0,30.
    // Como o saldo é double, o Java soma 0.30000000000000004. Se falhar, o defeito
    // está na representação do dinheiro (double) e não no teste.
    @Test
    void somaDeCentavosNaCaixinhaEhExata() {
        poupancaController.guardar(1L, 0.10);
        Poupanca p = poupancaController.guardar(1L, 0.20);
        assertEquals(0.30, p.getSaldo());
    }

    // =====================================================================
    // PoupancaController.resgatar
    // =====================================================================

    // POUP-040
    @Test
    void resgatarValorValidoDevolveDinheiroParaAConta() {
        poupancaController.guardar(1L, 200);
        Poupanca p = poupancaController.resgatar(1L, 50);
        assertEquals(150.0, p.getSaldo());
        assertEquals(350.0, conta1.getSaldoInicial());
    }

    // POUP-041
    @Test
    void resgatarTudoDeixaCaixinhaZeradaEContaComOSaldoOriginal() {
        poupancaController.guardar(1L, 200);
        Poupanca p = poupancaController.resgatar(1L, 200);
        assertEquals(0.0, p.getSaldo());
        assertEquals(500.0, conta1.getSaldoInicial());
    }

    // POUP-042
    @Test
    void resgatesConsecutivosSomamNaConta() {
        poupancaController.guardar(1L, 300);
        poupancaController.resgatar(1L, 100);
        Poupanca p = poupancaController.resgatar(1L, 50);
        assertEquals(150.0, p.getSaldo());
        assertEquals(350.0, conta1.getSaldoInicial());
    }

    // POUP-043
    @Test
    void resgatarMaisQueACaixinhaEhRejeitadoESemEfeito() {
        poupancaController.guardar(1L, 100);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> poupancaController.resgatar(1L, 100.01));
        assertTrue(e.getMessage().contains("Saldo insuficiente"));
        assertEquals(100.0, poupancaController.consultar(1L).getSaldo());
        assertEquals(400.0, conta1.getSaldoInicial());
    }

    // POUP-044
    @Test
    void resgatarSemTerGuardadoNadaEhRejeitado() {
        assertThrows(IllegalArgumentException.class, () -> poupancaController.resgatar(1L, 10));
        assertEquals(500.0, conta1.getSaldoInicial());
        assertEquals(0.0, poupancaController.consultar(1L).getSaldo());
    }

    // POUP-045
    @ParameterizedTest
    @ValueSource(doubles = {0, -0.0, -0.01, -5, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void resgatarRejeitaValorInvalidoESemEfeito(double valor) {
        poupancaController.guardar(1L, 100);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> poupancaController.resgatar(1L, valor));
        assertTrue(e.getMessage().contains("maior que 0"));
        assertEquals(100.0, poupancaController.consultar(1L).getSaldo());
        assertEquals(400.0, conta1.getSaldoInicial());
    }

    // POUP-046
    @Test
    void resgatarEmContaInexistenteLancaExcecao() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> poupancaController.resgatar(99L, 10));
        assertTrue(e.getMessage().contains("Conta não encontrada"));
    }

    // POUP-047
    @Test
    void resgatarNaoAfetaCaixinhaDeOutraConta() {
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
    @Test
    void consultaRefleteOSaldoAtualDepoisDeVariasOperacoes() {
        poupancaController.guardar(1L, 200);
        poupancaController.resgatar(1L, 50);
        poupancaController.guardar(1L, 25);
        poupancaController.resgatar(1L, 75);
        assertEquals(100.0, poupancaController.consultar(1L).getSaldo());
    }

    // POUP-051 (dinheiro total conservado: conta + caixinha = 500)
    @Test
    void dinheiroTotalEConservadoEmUmaSequenciaDeOperacoes() {
        poupancaController.guardar(1L, 123);
        poupancaController.guardar(1L, 77);
        poupancaController.resgatar(1L, 50);
        poupancaController.guardar(1L, 10);
        poupancaController.resgatar(1L, 100);
        double total = conta1.getSaldoInicial() + poupancaController.consultar(1L).getSaldo();
        assertEquals(500.0, total, 1e-9);
    }

    // POUP-052
    @Test
    void operacaoRejeitadaNaoMudaNenhumSaldoNaMesmaSequencia() {
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
    @Test
    void dinheiroGuardadoNaCaixinhaNaoPodeSerUsadoNoPix() {
        TransferenciaController pix = new TransferenciaController(contaController);
        poupancaController.guardar(1L, 400);   // conta fica com 100

        assertThrows(IllegalArgumentException.class,
                () -> pix.realizarPix(1L, "98765432100", 150.00, "teste"));

        assertEquals(100.0, conta1.getSaldoInicial());
        assertEquals(500.0, conta2.getSaldoInicial());
        assertEquals(400.0, poupancaController.consultar(1L).getSaldo());
    }

    // POUP-061: resgatar devolve o dinheiro e libera o Pix
    @Test
    void depoisDeResgatarOPixPassaAFuncionar() {
        TransferenciaController pix = new TransferenciaController(contaController);
        poupancaController.guardar(1L, 400);
        poupancaController.resgatar(1L, 400);

        pix.realizarPix(1L, "98765432100", 150.00, "teste");

        assertEquals(350.0, conta1.getSaldoInicial());
        assertEquals(650.0, conta2.getSaldoInicial());
    }

    // POUP-062: Pix recebido aumenta o que pode ser guardado
    @Test
    void pixRecebidoPermiteGuardarMaisNaCaixinha() {
        TransferenciaController pix = new TransferenciaController(contaController);
        pix.realizarPix(2L, "12345678901", 200.00, "teste");   // conta1 passa a ter 700

        Poupanca p = poupancaController.guardar(1L, 700);

        assertEquals(700.0, p.getSaldo());
        assertEquals(0.0, conta1.getSaldoInicial());
    }

    // POUP-063: conta apagada deixa a caixinha inacessível
    @Test
    void depoisDeApagarAContaACaixinhaNaoPodeMaisSerConsultada() {
        poupancaController.guardar(1L, 100);
        contaController.deletarConta(1L);
        assertThrows(IllegalArgumentException.class, () -> poupancaController.consultar(1L));
        assertThrows(IllegalArgumentException.class, () -> poupancaController.guardar(1L, 10));
    }
}