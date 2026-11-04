package tests;

import controller.ContaController;
import controller.TransferenciaController;
import model.Conta;
import model.Transferencia;

import java.util.List;

public class TesteTranferencia {
    public static void main(String[] args) {
        // Inicializa os controllers
        ContaController contaController = new ContaController();
        TransferenciaController transferenciaController = new TransferenciaController(contaController);

        // 1. Cria duas contas usando o controller
        Conta c1 = contaController.criarConta("Aron Eren", "10299977856");
        Conta c2 = contaController.criarConta("Mirian Acerman", "20388866654");

        System.out.println("--- ANTES DA TRANSFERÊNCIA ---");
        System.out.println("Saldo Conta 1 (" + c1.getNomeTitular() + "): R$ " + c1.getSaldoInicial());
        System.out.println("Saldo Conta 2 (" + c2.getNomeTitular() + "): R$ " + c2.getSaldoInicial());

        // 2. Realiza um Pix da Conta 1 para a Conta 2 (usando o documento como chave)
        transferenciaController.realizarPix(
                c1.getId(),
                "20388866654",
                150.00,
                "Pix de teste"
        );

        System.out.println("\n--- DEPOIS DA TRANSFERÊNCIA ---");
        System.out.println("Saldo Conta 1 (Esperado: 350.0): R$ " + c1.getSaldoInicial());
        System.out.println("Saldo Conta 2 (Esperado: 650.0): R$ " + c2.getSaldoInicial());

        // 3. Realiza um Pix da Conta 2 para a Conta 1 (usando o documento como chave)
        transferenciaController.realizarPix(
                c2.getId(),
                "10299977856",
                500.00,
                "Pix de teste 2"

        );

        System.out.println("\n--- DEPOIS DA TRANSFERÊNCIA 2 ---");
        System.out.println("Saldo Conta 1 (Esperado: 350.0): R$ " + c1.getSaldoInicial());
        System.out.println("Saldo Conta 2 (Esperado: 650.0): R$ " + c2.getSaldoInicial());

        // 4. Verifica o extrato da Conta 1
        System.out.println("\n--- EXTRATO DA CONTA 1 ---");
        List<Transferencia> extrato1 = transferenciaController.listarExtrato(c1.getId());
        for (Transferencia t : extrato1) {
            System.out.println("Tipo: " + t.getTipo() + " | Valor: R$ " + t.getValor() + " | Descrição: " + t.getDescricao());
        }

        // 5. Verifica o extrato da Conta 1
        System.out.println("\n--- EXTRATO DA CONTA 1 ---");
        List<Transferencia> extrato2 = transferenciaController.listarExtrato(c2.getId());
        for (Transferencia t : extrato2) {
            System.out.println("Tipo: " + t.getTipo() + " | Valor: R$ " + t.getValor() + " | Descrição: " + t.getDescricao());
        }

    }
}