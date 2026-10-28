package controller;

import model.Conta;
import java.util.ArrayList;
import java.util.List;

public class ContaController {
    // cadastro, consulta de perfil e saldo

    // Lista temporária em memória para simular o banco de dados por enquanto
    private List<Conta> contas = new ArrayList<>();

    // saldo inicial padrão
    private static final double saldoInicial = 500.00;

    // 1. CREATE - Cadastrar uma nova conta com validação de dados obrigatórios
    public Conta criarConta(String nomeTitular, String documento) {

        // Cria uma nova conta
        Conta novaConta = new Conta(
                (long) (contas.size() + 1),
                nomeTitular,
                documento
        );

        // Validação: Verifica se o nome do titular está vazio ou nulo
        if (novaConta.getNomeTitular() == null || novaConta.getNomeTitular().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do titular é obrigatório!");
        }

        // Validação: Verifica se o documento está vazio ou nulo
        if (novaConta.getDocumento() == null || novaConta.getDocumento().trim().isEmpty()) {
            throw new IllegalArgumentException("O documento (CPF/CNPJ) é obrigatório!");
        }

        // ATEMÇAO EMELI-DA-VÂ E LETICIA SHSUHUSJI !!!!!!!!!!!!!!!!!
        //Documento duplicado: um banco normalmente não permite dois
        //cadastros com o mesmo CPF/CNPJ. Vale validar isso no criarConta.

        // Define o saldo inicial da conta
        novaConta.setSaldoInicial(saldoInicial);

        contas.add(novaConta);
        return novaConta;
    }

    // 2. READ ALL - Listar todas as contas
    public List<Conta> listarContas() {
        return contas;
    }

    // 3. READ ONE - Buscar conta específica por ID/Documento
    public Conta buscarPorId(Long id) {
        // Lógica para buscar uma conta específica na lista
        for (Conta conta : contas) {
            if (conta.getId() != null && conta.getId().equals(id)) {
                return conta; // Se encontrou a conta, retorna ela.
            }
        }
        return null; // Caso não tenha encontrado.
    }

    // 4. UPDATE - Atualizar dados da conta
    public Conta atualizarConta(Long id, Conta contaAtualizada) {
        for (int i = 0; i < contas.size(); i++) {
            Conta contaExistente = contas.get(i);

            if (contaExistente.getId() != null && contaExistente.getId().equals(id)) {

                // Atualiza os dados da conta encontrada
                if (contaAtualizada.getNomeTitular() != null) {
                    contaExistente.setNomeTitular(contaAtualizada.getNomeTitular());
                }

                if (contaAtualizada.getDocumento() != null) {
                    contaExistente.setDocumento(contaAtualizada.getDocumento());
                }

                return contaExistente; // Retorna a conta com os dados atualizados
            }
        }

        return null; // Caso a conta não seja encontrada para atualização
    }

    // 5. DELETE - Excluir uma conta
    public Boolean deletarConta(Long id) {
        for (int i = 0; i < contas.size(); i++) {
            Conta conta = contas.get(i);

            if (conta.getId() != null && conta.getId().equals(id)) {
                contas.remove(i); // Remove a conta da lista
                return true; // Sucesso na exclusão
            }
        }

        return false; // Retorna falso se não encontrou a conta para deletar
    }
}