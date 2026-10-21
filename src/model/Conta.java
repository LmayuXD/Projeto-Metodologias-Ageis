package model;

public class Conta {

    // recebe dados da conta como ID, nome, documento e saldo
    private Long id;
    private String nomeTitular;
    private String documento;
    private double saldo = 500.00;

    //não é necessário ter saldo no construtor, já que o usuário não pode editar o saldo, e terá um saldo inicial de 500
    public Conta(Long id, String nomeTitular, String documento) {
        setId(id);
        setNomeTitular(nomeTitular);
        setDocumento(documento);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if (id != null && id > 0) {
            this.id = id;
        } else {
            throw new IllegalArgumentException("O id deve ser um número positivo!");
        }
    }

    public String getNomeTitular() {
        return nomeTitular;
    }

    public void setNomeTitular(String nomeTitular) {
        if (nomeTitular == null || nomeTitular.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do titular é obrigatório!");
        }

        String nome = nomeTitular.trim();

        if (nome.length() < 3) {
            throw new IllegalArgumentException("O nome deve ter pelo menos 3 caracteres!");
        }

        if (!nome.matches("[\\p{L} ]+")) {
            throw new IllegalArgumentException("O nome deve conter apenas letras e espaços!");
        }

        this.nomeTitular = nome;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            throw new IllegalArgumentException("O documento (CPF/CNPJ) é obrigatório!");
        }

        // tira pontos, traço, barra e espaços
        String apenasDigitos = documento.replaceAll("[.\\-/\\s]", "");

        if (!apenasDigitos.matches("\\d+")) {
            throw new IllegalArgumentException("O documento deve conter apenas números!");
        }

        if (apenasDigitos.length() != 11 && apenasDigitos.length() != 14) {
            throw new IllegalArgumentException("CPF deve ter 11 dígitos e CNPJ deve ter 14!");
        }

        this.documento = documento.trim();
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        if (saldo >= 0) {
            this.saldo = saldo;
        } else {
            throw new IllegalArgumentException("O saldo não pode ser negativo!");
        }
    }
}
