package model;

/**
 * Registro de uma transação Pix no extrato.
 * Esta classe só guarda dados; as regras de negócio ficam no TransferenciaController.
 */
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
}
