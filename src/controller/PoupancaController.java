
package controller;

import model.Conta;
import model.Poupanca;

import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
        valor = arredondar(valor);

        Conta conta = buscarConta(contaID);
        Poupanca caixinha = consultar(contaID);

        if (arredondar(conta.getSaldoInicial()) < valor) {
            throw new IllegalArgumentException("Saldo insuficiente na conta!");
        }

        conta.setSaldoInicial(arredondar(conta.getSaldoInicial() - valor));
        caixinha.depositar(valor);

        return caixinha;
    }

    public Poupanca resgatar(Long contaID, double valor) {
        validarValor(valor);
        valor = arredondar(valor);

        Conta conta = buscarConta(contaID);
        Poupanca caixinha = consultar(contaID);

        caixinha.sacar(valor);
        conta.setSaldoInicial(arredondar(conta.getSaldoInicial() + valor));

        return caixinha;
    }

    private Conta buscarConta(Long contaID) {
        Conta conta = contaController.buscarPorId(contaID);
        if (conta == null) {
            throw new IllegalArgumentException("Conta não encontrada!");
        }
        return conta;
    }

    private void validarValor(double valor) {
        if (Double.isNaN(valor) || Double.isInfinite(valor)
                || valor <= 0 || arredondar(valor) <= 0) {
            throw new IllegalArgumentException("O valor deve ser de pelo menos R$ 0,01.");
        }
    }

    private double arredondar(double valor) {
        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
