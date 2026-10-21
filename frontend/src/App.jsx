<<<<<<< HEAD
=======
import { useState } from "react";
>>>>>>> ce7255fdc0e625b86bf8befef459436e82f09527
import "./App.css";

function App() {
//FUNCAO DE CADASTRO
  function cadastrarConta(event) {
    event.preventDefault();

    // Verifica se os campos foram preenchidos
    if (nome.trim() === "" || documento.trim() === "") {
      setMensagem("Preencha o nome e o documento.");
      setTipoMensagem("erro");
      return;
    }

    // Cria uma conta seguindo a estrutura padrão
    const novaConta = {
      nomeTitular: nome.trim(),
      documento: documento.trim(),
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

        {/*EXIBE AS MENSAGENS NO CONSOLE*/}
  return (
    <main className="pagina">

      <header className="cabecalho">
        <h1>NekoPay</h1>
        <p>Gerenciamento de contas</p>
      </header>

      <section className="conteudo">

           {/*FORMULARIO DE ABRIR CONTA NO BANCO*/}

        <div className="formulario">
          <h2>Abrir conta</h2>

            {/*CRIAÇAO DO FORMULARIO*/}

          <form onSubmit={cadastrarConta}>

              {/*PEDE O NOME*/}
            <label htmlFor="nome">Nome do titular</label>
            <input
              id="nome"
              type="text"
              placeholder="Digite o nome completo"
              value={nome}
              onChange={(event) => setNome(event.target.value)}
            />
               {/*PEDE O DOCUMENTO*/}
            <label htmlFor="documento">CPF ou CNPJ</label>
            <input
              id="documento"
              type="text"
              placeholder="Digite o documento"
              value={documento}
              onChange={(event) => setDocumento(event.target.value)}
            />
                {/*LITERALMENTE UM BOTAO*/}

            <button type="submit">
              Cadastrar conta
            </button>
          </form> {/*FIM DO FORMS*/}

          {/* Exibe mensagem (sucesso, erro) ao criar conta */}
          {mensagem && (
            <p className={`mensagem ${tipoMensagem}`}>
              {mensagem}
            </p>
          )}
        </div> {/*FECHA A PRIMEIRA DIV DO FORMULARIO*/}

        <section className="secao-contas"> {/*SEGUNDA SEÇAO DE CONTAS*/}
          <h2>Contas cadastradas</h2>

          {contas.length === 0 ? ( //VERIFICA SE A QUANTIDADE DA ARAY CONTA É IGUAL A 0, SE FOR T = NENHUMA CONTA CADASTRADA
            <p>Nenhuma conta cadastrada.</p>
          ) : (
            <div className="lista-contas">  {/*SE FOR F = MOSTRA O CARD DA CONTA COM INFORMAÇOES*/}
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

                  <button //BOTAO NAO FUNCIONAL (AINDA)
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