package controller;

import model.Conta;
import model.Poupanca;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

// guardar e resgatar dinheiro

public class PoupancaController {

    private final ContaController contaController;
    private final Map<Long, Poupanca> caixinhas = new HashMap<>();

    public PoupancaController(ContaController contaController) {
        this.contaController = contaController;
    }

    public Poupanca consultar(Long contaId) {
        buscarConta(contaId);
        return caixinhas.computeIfAbsent(contaId, Poupanca::new);
    }

    public Poupanca guardar(Long contaID, double valor) {

        // valida o valor antes de alterar qualquer saldo
        validarValor(valor);

        Conta conta = buscarConta(contaID);
        Poupanca caixinha = consultar(contaID);

        double saldoConta = arredondar(conta.getSaldoInicial());
        double valorArredondado = arredondar(valor);

        // verifica se a conta possui saldo suficiente
        if (saldoConta < valorArredondado) {
            throw new IllegalArgumentException(
                    "Saldo insuficiente na conta!"
            );
        }

        // tira o dinheiro da conta
        conta.setSaldoInicial(
                arredondar(saldoConta - valorArredondado)
        );

        // coloca o dinheiro na caixinha
        caixinha.depositar(valorArredondado);

        return caixinha;
    }

    public Poupanca resgatar(Long contaID, double valor) {

        // valida o valor antes de alterar qualquer saldo
        validarValor(valor);

        Conta conta = buscarConta(contaID);
        Poupanca caixinha = consultar(contaID);

        double valorArredondado = arredondar(valor);

        // verifica se a caixinha possui saldo suficiente
        if (caixinha.getSaldo() < valorArredondado) {
            throw new IllegalArgumentException(
                    "Saldo insuficiente na poupança!"
            );
        }

        // tira o dinheiro da caixinha
        caixinha.sacar(valorArredondado);

        // devolve o dinheiro para a conta
        conta.setSaldoInicial(
                arredondar(conta.getSaldoInicial() + valorArredondado)
        );

        return caixinha;
    }

    private Conta buscarConta(Long contaID) {

        Conta conta = contaController.buscarPorId(contaID);

        if (conta == null) {
            throw new IllegalArgumentException(
                    "Conta não encontrada!"
            );
        }

        return conta;
    }

    private void validarValor(double valor) {

        // rejeita valores negativos, zero, NaN e infinito
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException(
                    "O valor deve ser maior que zero."
            );
        }

        BigDecimal quantia = BigDecimal.valueOf(valor);

        // rejeita valores menores que R$ 0,01
        if (quantia.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            throw new IllegalArgumentException(
                    "O valor mínimo é R$ 0,01."
            );
        }

        // rejeita valores com mais de duas casas decimais
        if (quantia.scale() > 2) {
            throw new IllegalArgumentException(
                    "O valor deve possuir no máximo duas casas decimais."
            );
        }
    }

    private double arredondar(double valor) {
        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
