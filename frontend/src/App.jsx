import { useState } from "react";
import "./App.css"


  return (


 <main className="pagina">

      <header className="cabecalho">
        <h1>NekoPay</h1>
        <p>Gerenciamento de contas</p>
      </header>

      <section className="conteudo">
        <h2>Contas cadastradas</h2>

        {contas.length === 0 ? (
          <p>Nenhuma conta cadastrada.</p>
        ) : (
          <div className="lista-contas">
            {contas.map((conta) => (
              <article className="cartao-conta" key={conta.id}>
                <h3>{conta.nome}</h3>

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
                  onClick={() => excluirConta(conta.id)}
                >
                  Excluir
                </button>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}

export default App