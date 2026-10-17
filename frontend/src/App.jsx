import { useState } from "react";
import "./App.css";

function App() {
  const [contas, setContas] = useState([
    {
      id: 1,
      nomeTitular: "Ana Silva",
      documento: "123.456.789-00",
      saldo: 500.00
    }
  ]);

  const [nome, setNome] = useState("");
  const [documento, setDocumento] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [tipoMensagem, setTipoMensagem] = useState("");

  function cadastrarConta(event) {
    event.preventDefault();

    // Verifica se os campos foram preenchidos
    if (nome.trim() === "" || documento.trim() === "") {
      setMensagem("Preencha o nome e o documento.");
      setTipoMensagem("erro");
      return;
    }

    // Cria uma nova conta seguindo a estrutura padrão
    const novaConta = {
      id: Date.now(),
      nomeTitular: nome.trim(),
      documento: documento.trim(),
      saldo: 0
    };

    // Adiciona a conta à lista
    setContas([...contas, novaConta]);

    // Limpa os campos
    setNome("");
    setDocumento("");

    // Exibe mensagem de sucesso
    setMensagem("Conta cadastrada com sucesso!");
    setTipoMensagem("sucesso");
  }

  function excluirConta(id) {
    const novaLista = contas.filter(
      (conta) => conta.id !== id
    );

    setContas(novaLista);
    setMensagem("Conta removida da lista.");
    setTipoMensagem("sucesso");
  }

  return (
    <main className="pagina">
      <header className="cabecalho">
        <h1>NekoPay</h1>
        <p>Gerenciamento de contas</p>
      </header>

      <section className="conteudo">
        <div className="formulario">
          <h2>Abrir conta</h2>

          <form onSubmit={cadastrarConta}>
            <label htmlFor="nome">Nome do titular</label>
            <input
              id="nome"
              type="text"
              placeholder="Digite o nome completo"
              value={nome}
              onChange={(event) => setNome(event.target.value)}
            />

            <label htmlFor="documento">CPF ou CNPJ</label>
            <input
              id="documento"
              type="text"
              placeholder="Digite o documento"
              value={documento}
              onChange={(event) => setDocumento(event.target.value)}
            />

            <button type="submit">
              Cadastrar conta
            </button>
          </form>

          {/* Exibe mensagem (sucesso, erro) ao criar conta */}
          {mensagem && (
            <p className={`mensagem ${tipoMensagem}`}>
              {mensagem}
            </p>
          )}
        </div>

        <section className="secao-contas">
          <h2>Contas cadastradas</h2>

          {contas.length === 0 ? (
            <p>Nenhuma conta cadastrada.</p>
          ) : (
            <div className="lista-contas">
              {contas.map((conta) => (
                <article
                  className="cartao-conta"
                  key={conta.id}
                >
                  <h3>{conta.nomeTitular}</h3>

                  <p>
                    <strong>Documento:</strong>{" "}
                    {conta.documento}
                  </p>

                  <p>
                    <strong>Saldo inicial:</strong>{" "}
                    {conta.saldo.toLocaleString("pt-BR", {
                      style: "currency",
                      currency: "BRL"
                    })}
                  </p>

                  <button
                    type="button"
                    onClick={() => excluirConta(conta.id)}
                  >
                    Excluir
                  </button>
                </article>
              ))}
            </div>
          )}
        </section>
      </section>
    </main>
  );
}

export default App;