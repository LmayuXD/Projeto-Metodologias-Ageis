package model;

public class Transferencia {
    private String id;
    private String chaveDestino;
    private double valor;
    private String data;
    private String descricao;
    private String tipo; // Ex: "PIX_ENVIADO", "PIX_RECEBIDO"

    // Construtor vazio
    public Transferencia() {}

    // Construtor com parâmetros
    public Transferencia(String id, String chaveDestino, double valor, String data, String descricao, String tipo) {
        this.id = id;
        this.chaveDestino = chaveDestino;
        this.valor = valor;
        this.data = data;
        this.descricao = descricao;
        this.tipo = tipo;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChaveDestino() {
        return chaveDestino;
    }

    public void setChaveDestino(String chaveDestino) {
        this.chaveDestino = chaveDestino;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean realizarTransferencia(Conta contaOrigem, String chaveDestino, double valor) {
        // Validação de saldo suficiente
        if (contaOrigem.getSaldo() < valor) {
            System.out.println("Erro: Saldo insuficiente para realizar a transferência.");
            return false;
        }

        // Se tiver saldo, efetua a transação
        contaOrigem.setSaldo(contaOrigem.getSaldo() - valor);

        // Regista no extrato
        Transferencia novaTransacao = new Transferencia(
                java.util.UUID.randomUUID().toString(), // Cria um id exclusivo para a transação e converte para texto
                chaveDestino,
                valor,
                java.time.LocalDateTime.now().toString() // Pega a data e a hora em que a transação foi feita e converte para texto
                "Transferência Pix enviada",
                "PIX_ENVIADO"
        );

        contaOrigem.getExtrato().add(novaTransacao);
        return true;
    }
}