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

        validarValor(valor);

        Conta conta = buscarConta(contaID);
        Poupanca caixinha = consultar(contaID);

        double saldo = arredondar(conta.getSaldoInicial());
        double valorValido = arredondar(valor);

        if (saldo < valorValido) {
            throw new IllegalArgumentException(
                    "Saldo insuficiente na conta!"
            );
        }

        conta.setSaldoInicial(
                arredondar(saldo - valorValido)
        );

        caixinha.depositar(valorValido);

        return caixinha;
    }

    public Poupanca resgatar(Long contaID, double valor) {

        validarValor(valor);

        Conta conta = buscarConta(contaID);
        Poupanca caixinha = consultar(contaID);

        double valorValido = arredondar(valor);

        if (caixinha.getSaldo() < valorValido) {
            throw new IllegalArgumentException(
                    "Saldo insuficiente na poupança!"
            );
        }

        caixinha.sacar(valorValido);

        conta.setSaldoInicial(
                arredondar(conta.getSaldoInicial() + valorValido)
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

        // rejeita NaN e infinito
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException(
                    "O valor deve ser maior que 0."
            );
        }

        // rejeita zero e valores negativos
        if (valor <= 0) {
            throw new IllegalArgumentException(
                    "O valor deve ser maior que 0."
            );
        }

        BigDecimal valorDecimal = BigDecimal.valueOf(valor);

        // rejeita valores menores que R$ 0,01
        if (valorDecimal.compareTo(
                BigDecimal.valueOf(0.01)
        ) < 0) {
            throw new IllegalArgumentException(
                    "O valor deve ser maior que 0."
            );
        }

        // rejeita valores com mais de duas casas decimais
        if (valorDecimal.scale() > 2) {
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
