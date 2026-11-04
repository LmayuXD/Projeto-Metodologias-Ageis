package tests;

import model.Conta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TesteConta {

    private Conta conta;

    @BeforeEach
    void criarConta() {
        conta = new Conta(1L, "Maria Silva", "12345678901");
    }

    // ------------------------------------------------------------ construtor
    @Test
    void construtorGuardaOsDadosEComecaCom500() {
        assertEquals(1L, conta.getId());
        assertEquals("Maria Silva", conta.getNomeTitular());
        assertEquals("12345678901", conta.getDocumento());
        assertEquals(500.0, conta.getSaldoInicial());
    }

    @Test
    void construtorRepassaAsValidacoes() {
        assertThrows(IllegalArgumentException.class, () -> new Conta(0L, "Maria Silva", "12345678901"));
        assertThrows(IllegalArgumentException.class, () -> new Conta(1L, "A1", "12345678901"));
        assertThrows(IllegalArgumentException.class, () -> new Conta(1L, "Maria Silva", "123"));
    }

    // -------------------------------------------------------------------- id
    @ParameterizedTest
    @ValueSource(longs = {1, 42, Long.MAX_VALUE})
    void aceitaIdPositivo(long id) {
        conta.setId(id);
        assertEquals(id, conta.getId());
    }

    @Test
    void rejeitaIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> conta.setId(null));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -5, Long.MIN_VALUE})
    void rejeitaIdZeroOuNegativo(long id) {
        assertThrows(IllegalArgumentException.class, () -> conta.setId(id));
    }

    // ------------------------------------------------------------------ nome
    @ParameterizedTest
    @ValueSource(strings = {"Ana", "João Pedro", "Ângela da Conceição"})
    void aceitaNomeValido(String nome) {
        conta.setNomeTitular(nome);
        assertEquals(nome, conta.getNomeTitular());
    }

    @Test
    void removeEspacosDasPontasDoNome() {
        conta.setNomeTitular("  Carlos Lima  ");
        assertEquals("Carlos Lima", conta.getNomeTitular());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejeitaNomeVazioOuNulo(String nome) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> conta.setNomeTitular(nome));
        assertTrue(e.getMessage().contains("obrigat"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "Al", " Al "})
    void rejeitaNomeComMenosDe3Caracteres(String nome) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> conta.setNomeTitular(nome));
        assertTrue(e.getMessage().contains("3 caracteres"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Maria2", "12345", "Maria@Silva", "Dr. Silva", "Ana-Maria", "Maria\tSilva", "Maria 😀"})
    void rejeitaNomeComCaracteresInvalidos(String nome) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> conta.setNomeTitular(nome));
        assertTrue(e.getMessage().contains("letras"));
    }

    // ------------------------------------------------------------- documento
    @ParameterizedTest
    @ValueSource(strings = {"98765432100", "12345678000190", "123.456.789-01",
            "12.345.678/0001-90", "123 456 789 01"})
    void aceitaCpfOuCnpjEMantemOFormatoOriginal(String documento) {
        conta.setDocumento(documento);
        assertEquals(documento, conta.getDocumento());
    }

    @Test
    void removeEspacosDasPontasDoDocumento() {
        conta.setDocumento("  11122233344  ");
        assertEquals("11122233344", conta.getDocumento());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejeitaDocumentoVazioOuNulo(String documento) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> conta.setDocumento(documento));
        assertTrue(e.getMessage().contains("obrigat"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcdefghijk", "1234567890a", "..-.-/", "123,456,789,01"})
    void rejeitaDocumentoComCaracteresInvalidos(String documento) {
        assertThrows(IllegalArgumentException.class, () -> conta.setDocumento(documento));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "1234567890", "123456789012", "1234567890123", "123456789012345"})
    void rejeitaDocumentoComQuantidadeErradaDeDigitos(String documento) {
        assertThrows(IllegalArgumentException.class, () -> conta.setDocumento(documento));
    }

    // ----------------------------------------------------------------- saldo
    @Test
    void saldoPadraoE500() {
        assertEquals(500.0, conta.getSaldoInicial());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, 0.01, 1234.56, 1_000_000_000.0})
    void aceitaSaldoZeroOuPositivo(double saldo) {
        conta.setSaldoInicial(saldo);
        assertEquals(saldo, conta.getSaldoInicial());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, -0.01, Double.NEGATIVE_INFINITY, Double.NaN})
    void rejeitaSaldoInvalido(double saldo) {
        assertThrows(IllegalArgumentException.class, () -> conta.setSaldoInicial(saldo));
    }

    // ------------------------------------- setter inválido não muda o estado
    @Test
    void valorInvalidoNaoAlteraOsDadosAnteriores() {
        assertThrows(IllegalArgumentException.class, () -> conta.setId(-1L));
        assertThrows(IllegalArgumentException.class, () -> conta.setNomeTitular("X1"));
        assertThrows(IllegalArgumentException.class, () -> conta.setDocumento("123"));
        assertThrows(IllegalArgumentException.class, () -> conta.setSaldoInicial(-10));

        assertEquals(1L, conta.getId());
        assertEquals("Maria Silva", conta.getNomeTitular());
        assertEquals("12345678901", conta.getDocumento());
        assertEquals(500.0, conta.getSaldoInicial());
    }
}