package controller;

import model.Conta;
import model.Poupanca;
import model.Transferencia;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// guardar e resgatar dinheiro

public class PoupancaController {

    // ---------- Constantes exatamente iguais ao que o Front-end espera ----------
    public static final String POUPANCA_GUARDAR = "POUPANCA_GUARDAR";
    public static final String POUPANCA_RESGATE = "POUPANCA_RESGATE";

    private static final double VALOR_MINIMO = 0.01;

    // ---------- Dependências ----------
    private final ContaController contaController;
    private final Map<Long, Poupanca> caixinhas = new HashMap<>();

    public PoupancaController(ContaController contaController) {
        this.contaController = contaController;
    }

    // apaga a caixinha de uma conta encerrada
    public synchronized void removerCaixinha(Long contaId) {
        caixinhas.remove(contaId);
    }

    public Poupanca consultar(Long contaId) {
        buscarConta(contaId);
        return caixinhas.computeIfAbsent(contaId, Poupanca::new);
    }

    public synchronized Poupanca guardar(Long contaId, double valor) {

        validarValor(valor);
        Conta conta = buscarConta(contaId);
        Poupanca caixinha = consultar(contaId);

        double saldo = arredondar(conta.getSaldoInicial());
        double valorValido = arredondar(valor);

        if (saldo < valorValido) {
            throw new IllegalArgumentException(
                    "Saldo insuficiente na conta!"
            );
        }

        // 1) Atualiza o saldo da conta
        conta.setSaldoInicial(arredondar(saldo - valorValido));

        // 2) Atualiza a caixinha
        caixinha.depositar(valorValido);

        // 3) Monta o registro de extrato (EXATAMENTE como o Pix faz)
        String id = UUID.randomUUID().toString();
        String data = LocalDateTime.now().toString();

        Transferencia registroGuardado = new Transferencia(
                id, "Caixinha NekoPay", valorValido, data,
                "Guardado na caixinha", POUPANCA_GUARDAR
        );

        // 4) ADICIONA DIRETAMENTE NO EXTRATO DA CONTA (o segredo que faltava!)
        conta.getExtrato().add(registroGuardado);

        return caixinha;
    }

    public synchronized Poupanca resgatar(Long contaId, double valor) {

        validarValor(valor);

        Conta conta = buscarConta(contaId);
        Poupanca caixinha = consultar(contaId);

        double valorValido = arredondar(valor);

        if (caixinha.getSaldo() < valorValido) {
            throw new IllegalArgumentException(
                    "Saldo insuficiente na poupança!"
            );
        }

        // 1) Atualiza a caixinha
        caixinha.sacar(valorValido);

        // 2) Atualiza o saldo da conta
        conta.setSaldoInicial(
                arredondar(conta.getSaldoInicial() + valorValido)
        );

        // 3) Monta o registro de extrato
        String id = UUID.randomUUID().toString();
        String data = LocalDateTime.now().toString();

        Transferencia registroResgatado = new Transferencia(
                id, "Caixinha NekoPay", valorValido, data,
                "Resgatado da caixinha", POUPANCA_RESGATE
        );

        // 4) ADICIONA DIRETAMENTE NO EXTRATO DA CONTA
        conta.getExtrato().add(registroResgatado);

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
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException(
                    "O valor deve ser maior que 0."
            );
        }

        BigDecimal valorDecimal = BigDecimal.valueOf(valor);

        if (valorDecimal.compareTo(
                BigDecimal.valueOf(0.01)
        ) < 0) {
            throw new IllegalArgumentException(
                    "O valor deve ser maior que 0."
            );
        }

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