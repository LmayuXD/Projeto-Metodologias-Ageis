package controller;

import model.Conta;
import model.Poupanca;

import java.util.HashMap;
import java.util.Map;

//guardar e resgatar dinheiro

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

        if(conta.getSaldoInicial() < valor) {
            throw new IllegalArgumentException("Saldo insuficiente na conta!");
        }
        conta.setSaldoInicial(conta.getSaldoInicial() - valor);
        caixinha.depositar(valor);

        return caixinha;
    }

    public Poupanca resgatar(Long contaID, double valor) {
        validarValor(contaID);
        Conta conta = buscarConta(contaID);
        Poupanca caixinha = consultar(contaID);

        caixinha.sacar(valor);
        conta.setSaldoInicial(conta.getSaldoInicial() + valor);

        return caixinha;
    }

    private Conta buscarConta(Long contaID) {
        Conta conta = contaController.buscarPorId(contaID);
        if(conta == null) {
            throw new IllegalArgumentException("Conta não encontrada!");
        }
        return conta;
    }

    private void validarValor(double valor) {
        if(Double.isNaN(valor) || Double.isInfinite(valor) || valor <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que 0.");
        }
    }
}