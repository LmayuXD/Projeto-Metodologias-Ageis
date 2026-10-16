package controller;

import model.Conta;
import java.util.ArrayList;
import java.util.List;

public class ContaController {

    // Lista temporária em memória para simular o banco de dados por enquanto
    private List<Conta> contas = new ArrayList<>();

    // 1. CREATE - Cadastrar uma nova conta
    public Conta criarConta(Conta novaConta){
        contas.add(novaConta);
        return novaConta;
    }

    // 2. READ ALL - Listar todas as contas
    public List<Conta> listarContas(){
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
        // Lógica de atualização
        return contaAtualizada;
    }
    // 5. DELETE - Excluir uma conta
    public Boolean deletarConta(Long id) {
        // Lógica de remoção
        return true;
    }
}
