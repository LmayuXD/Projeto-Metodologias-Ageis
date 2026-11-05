
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

    // CORREÇÃO: valida antes de arredondar
    private void validarValor(double valor) {
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException(
                "O valor deve ser de pelo menos R$ 0,01."
            );
        }

        BigDecimal quantia = BigDecimal.valueOf(valor);

        // Rejeita valores inferiores a um centavo
        if (quantia.compareTo(new BigDecimal("0.01")) < 0) {
            throw new IllegalArgumentException(
                "O valor deve ser de pelo menos R$ 0,01."
            );
        }

        // Rejeita valores com frações de centavo
        if (quantia.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                "O valor deve ter no máximo duas casas decimais."
            );
        }
    }

    private double arredondar(double valor) {
        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
