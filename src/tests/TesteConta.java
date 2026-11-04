package tests;

import model.Conta;

public class TesteConta {

    private static Conta conta;
    private static int ok = 0, falhas = 0;

    public static void main(String[] args) {
        rodar("construtorGuardaOsDadosEComecaCom500", TesteConta::construtorGuardaOsDadosEComecaCom500);
        rodar("construtorRepassaAsValidacoes", TesteConta::construtorRepassaAsValidacoes);
        rodar("aceitaIdPositivo", TesteConta::aceitaIdPositivo);
        rodar("rejeitaIdNulo", TesteConta::rejeitaIdNulo);
        rodar("rejeitaIdZeroOuNegativo", TesteConta::rejeitaIdZeroOuNegativo);
        rodar("aceitaNomeValido", TesteConta::aceitaNomeValido);
        rodar("removeEspacosDasPontasDoNome", TesteConta::removeEspacosDasPontasDoNome);
        rodar("rejeitaNomeVazioOuNulo", TesteConta::rejeitaNomeVazioOuNulo);
        rodar("rejeitaNomeComMenosDe3Caracteres", TesteConta::rejeitaNomeComMenosDe3Caracteres);
        rodar("rejeitaNomeComCaracteresInvalidos", TesteConta::rejeitaNomeComCaracteresInvalidos);
        rodar("aceitaCpfOuCnpjEMantemOFormatoOriginal", TesteConta::aceitaCpfOuCnpjEMantemOFormatoOriginal);
        rodar("removeEspacosDasPontasDoDocumento", TesteConta::removeEspacosDasPontasDoDocumento);
        rodar("rejeitaDocumentoVazioOuNulo", TesteConta::rejeitaDocumentoVazioOuNulo);
        rodar("rejeitaDocumentoComCaracteresInvalidos", TesteConta::rejeitaDocumentoComCaracteresInvalidos);
        rodar("rejeitaDocumentoComQuantidadeErradaDeDigitos", TesteConta::rejeitaDocumentoComQuantidadeErradaDeDigitos);
        rodar("saldoPadraoE500", TesteConta::saldoPadraoE500);
        rodar("aceitaSaldoZeroOuPositivo", TesteConta::aceitaSaldoZeroOuPositivo);
        rodar("rejeitaSaldoInvalido", TesteConta::rejeitaSaldoInvalido);
        rodar("valorInvalidoNaoAlteraOsDadosAnteriores", TesteConta::valorInvalidoNaoAlteraOsDadosAnteriores);

        System.out.println("\n==== " + ok + " OK | " + falhas + " FALHAS ====");
        System.exit(falhas > 0 ? 1 : 0);
    }

    // executa um teste com uma conta nova (o que o @BeforeEach fazia)
    private static void rodar(String nome, Runnable teste) {
        try {
            criarConta();
            teste.run();
            ok++;
            System.out.println("[OK]     " + nome);
        } catch (Throwable e) {
            falhas++;
            System.out.println("[FALHA]  " + nome + " -> " + e);
        }
    }

    private static void criarConta() {
        conta = new Conta(1L, "Maria Silva", "12345678901");
    }

    // ------------------------------------------------------------ construtor
    private static void construtorGuardaOsDadosEComecaCom500() {
        assertEquals(1L, conta.getId());
        assertEquals("Maria Silva", conta.getNomeTitular());
        assertEquals("12345678901", conta.getDocumento());
        assertEquals(500.0, conta.getSaldoInicial());
    }

    private static void construtorRepassaAsValidacoes() {
        assertThrows(IllegalArgumentException.class, () -> new Conta(0L, "Maria Silva", "12345678901"));
        assertThrows(IllegalArgumentException.class, () -> new Conta(1L, "A1", "12345678901"));
        assertThrows(IllegalArgumentException.class, () -> new Conta(1L, "Maria Silva", "123"));
    }

    // -------------------------------------------------------------------- id
    private static void aceitaIdPositivo() {
        for (long id : new long[]{1, 42, Long.MAX_VALUE}) {
            conta.setId(id);
            assertEquals(id, conta.getId());
        }
    }

    private static void rejeitaIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> conta.setId(null));
    }

    private static void rejeitaIdZeroOuNegativo() {
        for (long id : new long[]{0, -5, Long.MIN_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> conta.setId(id));
        }
    }

    // ------------------------------------------------------------------ nome
    private static void aceitaNomeValido() {
        for (String nome : new String[]{"Ana", "João Pedro", "Ângela da Conceição"}) {
            conta.setNomeTitular(nome);
            assertEquals(nome, conta.getNomeTitular());
        }
    }

    private static void removeEspacosDasPontasDoNome() {
        conta.setNomeTitular("  Carlos Lima  ");
        assertEquals("Carlos Lima", conta.getNomeTitular());
    }

    private static void rejeitaNomeVazioOuNulo() {
        for (String nome : new String[]{null, "", "   "}) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> conta.setNomeTitular(nome));
            assertTrue(e.getMessage().contains("obrigat"));
        }
    }

    private static void rejeitaNomeComMenosDe3Caracteres() {
        for (String nome : new String[]{"A", "Al", " Al "}) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> conta.setNomeTitular(nome));
            assertTrue(e.getMessage().contains("3 caracteres"));
        }
    }

    private static void rejeitaNomeComCaracteresInvalidos() {
        for (String nome : new String[]{"Maria2", "12345", "Maria@Silva", "Dr. Silva", "Ana-Maria", "Maria\tSilva", "Maria 😀"}) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> conta.setNomeTitular(nome));
            assertTrue(e.getMessage().contains("letras"));
        }
    }

    // ------------------------------------------------------------- documento
    private static void aceitaCpfOuCnpjEMantemOFormatoOriginal() {
        for (String documento : new String[]{"98765432100", "12345678000190", "123.456.789-01",
                "12.345.678/0001-90", "123 456 789 01"}) {
            conta.setDocumento(documento);
            assertEquals(documento, conta.getDocumento());
        }
    }

    private static void removeEspacosDasPontasDoDocumento() {
        conta.setDocumento("  11122233344  ");
        assertEquals("11122233344", conta.getDocumento());
    }

    private static void rejeitaDocumentoVazioOuNulo() {
        for (String documento : new String[]{null, "", "   "}) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> conta.setDocumento(documento));
            assertTrue(e.getMessage().contains("obrigat"));
        }
    }

    private static void rejeitaDocumentoComCaracteresInvalidos() {
        for (String documento : new String[]{"abcdefghijk", "1234567890a", "..-.-/", "123,456,789,01"}) {
            assertThrows(IllegalArgumentException.class, () -> conta.setDocumento(documento));
        }
    }

    private static void rejeitaDocumentoComQuantidadeErradaDeDigitos() {
        for (String documento : new String[]{"1", "1234567890", "123456789012", "1234567890123", "123456789012345"}) {
            assertThrows(IllegalArgumentException.class, () -> conta.setDocumento(documento));
        }
    }

    // ----------------------------------------------------------------- saldo
    private static void saldoPadraoE500() {
        assertEquals(500.0, conta.getSaldoInicial());
    }

    private static void aceitaSaldoZeroOuPositivo() {
        for (double saldo : new double[]{0, 0.01, 1234.56, 1_000_000_000.0}) {
            conta.setSaldoInicial(saldo);
            assertEquals(saldo, conta.getSaldoInicial());
        }
    }

    private static void rejeitaSaldoInvalido() {
        for (double saldo : new double[]{-1, -0.01, Double.NEGATIVE_INFINITY, Double.NaN}) {
            assertThrows(IllegalArgumentException.class, () -> conta.setSaldoInicial(saldo));
        }
    }

    // ------------------------------------- setter inválido não muda o estado
    private static void valorInvalidoNaoAlteraOsDadosAnteriores() {
        assertThrows(IllegalArgumentException.class, () -> conta.setId(-1L));
        assertThrows(IllegalArgumentException.class, () -> conta.setNomeTitular("X1"));
        assertThrows(IllegalArgumentException.class, () -> conta.setDocumento("123"));
        assertThrows(IllegalArgumentException.class, () -> conta.setSaldoInicial(-10));

        assertEquals(1L, conta.getId());
        assertEquals("Maria Silva", conta.getNomeTitular());
        assertEquals("12345678901", conta.getDocumento());
        assertEquals(500.0, conta.getSaldoInicial());
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
