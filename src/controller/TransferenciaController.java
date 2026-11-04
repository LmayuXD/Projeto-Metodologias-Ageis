package controller;

import model.Conta;
import model.Transferencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class TransferenciaController {

    // ---------- Constantes ----------
    public static final String PIX_ENVIADO = "PIX_ENVIADO";
    public static final String PIX_RECEBIDO = "PIX_RECEBIDO";

    private static final double VALOR_MINIMO = 0.01;
    private static final double LIMITE_POR_TRANSFERENCIA = 5000.00; // limite simulado, ajustem como quiserem
    private static final int TAMANHO_MAX_DESCRICAO = 140;           // mesmo limite da mensagem do Pix real

    // ---------- Dependências ----------
    // As contas vivem no ContaController (nosso "banco de dados" em memória)
    private final ContaController contaController;

    public TransferenciaController(ContaController contaController) {
        this.contaController = contaController;
    }

      /**
     * Realiza um Pix da conta de origem para a conta dona da chave informada.
     * Retorna o registro do remetente (serve como comprovante).
     *
     * synchronized: o HttpServer atende várias requisições ao mesmo tempo.
     * Sem isso, dois Pix simultâneos poderiam gastar o mesmo saldo duas vezes.
     */
    public synchronized Transferencia realizarPix(Long contaOrigemId, String chaveDestino,
                                                  double valor, String descricao) {

        // 1) Validações de entrada (nada é alterado até aqui)
        validarValor(valor);
        String chave = normalizarChave(chaveDestino);
        String descricaoUsuario = normalizarDescricao(descricao);

        // 2) Localiza as duas contas
        Conta origem = buscarContaOuFalhar(contaOrigemId);
        Conta destino = localizarContaPorChave(chave);

        // 3) Regras de negócio
        if (origem.getId().equals(destino.getId())) {
            throw new IllegalArgumentException("Não é possível transferir para a própria conta!");
        }

        if (origem.getSaldo() < valor) {
            throw new IllegalArgumentException("Saldo insuficiente para realizar a transferência!");
        }

        // 4) Monta os DOIS registros (saída na origem, entrada no destino)
        //    Mesmo id e mesma data nos dois: é a mesma operação vista por dois lados.
        String id = UUID.randomUUID().toString();
        String data = LocalDateTime.now().toString();

        Transferencia registroEnviado = new Transferencia(
                id, chave, valor, data,
                montarDescricao("Pix enviado para " + destino.getNomeTitular(), descricaoUsuario),
                PIX_ENVIADO
        );

        Transferencia registroRecebido = new Transferencia(
                id, chave, valor, data,
                montarDescricao("Pix recebido de " + origem.getNomeTitular(), descricaoUsuario),
                PIX_RECEBIDO
        );

        // 5) Só agora mexe nos saldos e nos extratos (tudo ou nada)
        origem.debitar(valor);
        destino.creditar(valor);
        origem.getExtrato().add(registroEnviado);
        destino.getExtrato().add(registroRecebido);

        return registroEnviado;
    }

    /** Extrato completo da conta, do mais recente para o mais antigo. */
    public List<Transferencia> listarExtrato(Long contaId) {
        return listarExtrato(contaId, null);
    }

    /**
     * Extrato filtrado por tipo (PIX_ENVIADO / PIX_RECEBIDO).
     * tipo nulo ou vazio = sem filtro.
     */
    public List<Transferencia> listarExtrato(Long contaId, String tipo) {
        Conta conta = buscarContaOuFalhar(contaId);

        boolean filtrar = tipo != null && !tipo.trim().isEmpty();
        if (filtrar && !tipo.equalsIgnoreCase(PIX_ENVIADO) && !tipo.equalsIgnoreCase(PIX_RECEBIDO)) {
            throw new IllegalArgumentException("Tipo inválido! Use " + PIX_ENVIADO + " ou " + PIX_RECEBIDO + ".");
        }

        // Copia a lista: quem chama não consegue mexer no extrato original
        List<Transferencia> resultado = new ArrayList<>();
        for (Transferencia t : conta.getExtrato()) {
            if (!filtrar || t.getTipo().equalsIgnoreCase(tipo)) {
                resultado.add(t);
            }
        }

        // data em formato ISO -> ordenar como texto já ordena cronologicamente
        // (para ordem crescente, tire o .reversed())
        resultado.sort(Comparator.comparing(Transferencia::getData).reversed());
        return resultado;
    }

    /** Busca uma transação específica do extrato (útil para tela de comprovante). */
    public Transferencia buscarPorId(Long contaId, String transferenciaId) {
        Conta conta = buscarContaOuFalhar(contaId);

        for (Transferencia t : conta.getExtrato()) {
            if (t.getId().equals(transferenciaId)) {
                return t;
            }
        }
        throw new NoSuchElementException("Transação não encontrada!");
    }

    // =====================================================================
    // MÉTODOS AUXILIARES (privados)
    // =====================================================================

    private void validarValor(double valor) {
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            throw new IllegalArgumentException("Valor inválido!");
        }
        if (valor < VALOR_MINIMO) {
            throw new IllegalArgumentException("O valor da transferência deve ser maior que zero!");
        }
        // Dinheiro não tem fração de centavo (ex: 10.005 é rejeitado)
        if (BigDecimal.valueOf(valor).stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("O valor deve ter no máximo 2 casas decimais!");
        }
        if (valor > LIMITE_POR_TRANSFERENCIA) {
            throw new IllegalArgumentException(
                    String.format("O limite por transferência é de R$ %.2f!", LIMITE_POR_TRANSFERENCIA));
        }
    }

    /** Padroniza a chave para comparar: e-mail em minúsculo; CPF/CNPJ/telefone só com dígitos. */
    private String normalizarChave(String chave) {
        if (chave == null || chave.trim().isEmpty()) {
            throw new IllegalArgumentException("A chave de destino é obrigatória!");
        }
        String c = chave.trim();
        if (c.contains("@")) {
            return c.toLowerCase();
        }
        return c.replaceAll("[.\\-/\\s]", "");
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            return "";
        }
        String d = descricao.trim();
        if (d.length() > TAMANHO_MAX_DESCRICAO) {
            throw new IllegalArgumentException(
                    "A descrição deve ter no máximo " + TAMANHO_MAX_DESCRICAO + " caracteres!");
        }
        return d;
    }

    private String montarDescricao(String padrao, String descricaoUsuario) {
        return descricaoUsuario.isEmpty() ? padrao : padrao + " - " + descricaoUsuario;
    }

    private Conta buscarContaOuFalhar(Long contaId) {
        Conta conta = contaController.buscarPorId(contaId);
        if (conta == null) {
            throw new NoSuchElementException("Conta não encontrada!");
        }
        return conta;
    }

    /**
     * Por enquanto a chave Pix é o documento (CPF/CNPJ) da conta.
     * Quando a Conta ganhar o campo chavePix (e-mail, telefone, aleatória),
     * é só trocar o getDocumento() por getChavePix() aqui.
     */
    private Conta localizarContaPorChave(String chaveNormalizada) {
        for (Conta conta : contaController.listarContas()) {
            if (normalizarChave(conta.getDocumento()).equals(chaveNormalizada)) {
                return conta;
            }
        }
        throw new NoSuchElementException("Chave Pix não encontrada!");
    }
}
