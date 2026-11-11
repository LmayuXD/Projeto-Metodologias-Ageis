import { useState, useEffect } from "react";
import "../App.css";

export default function Poupanca({ contaId, onVoltar }) {

    const [saldoPoupanca, setSaldoPoupanca] = useState(0);
    const [valor, setValor] = useState("");

    const [mensagemErro, setMensagemErro] = useState("");
    const [mensagemSucesso, setMensagemSucesso] = useState("");

    // Consultar saldo da poupança
    async function consultarPoupanca() {
        try {
            const resposta = await fetch(
                `http://localhost:8080/poupanca/${contaId}`
            );

            const dados = await resposta.json();

            if (!resposta.ok) {
                throw new Error(dados.erro || "Erro ao consultar poupança.");
            }

            setSaldoPoupanca(dados.saldo);

        } catch (erro) {
            setMensagemErro(erro.message);
        }
    }

    // Guardar dinheiro
    async function guardarDinheiro() {
        try {
            setMensagemErro("");
            setMensagemSucesso("");

            if (valor === "" || Number(valor) <= 0) {
                setMensagemErro("Digite um valor válido.");
                return;
            }

            const resposta = await fetch(
                "http://localhost:8080/poupanca/guardar",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        contaId: contaId,
                        valor: Number(valor)
                    })
                }
            );
            const dados = await resposta.json();

            if (!resposta.ok) {
                throw new Error(dados.erro || "Erro ao guardar dinheiro.");
            }

            setSaldoPoupanca(dados.saldo);
            setValor("");

            setMensagemSucesso("Dinheiro guardado com sucesso!");

        } catch (erro) {
            setMensagemErro(erro.message);
        }
    }

    // Resgatar dinheiro
    async function resgatarDinheiro() {
        try {
            setMensagemErro("");
            setMensagemSucesso("");

            // Verifica se o valor foi preenchido
            if (valor === "" || Number(valor) <= 0) {
                setMensagemErro("Digite um valor válido.");
                return;
            }

            // Envia a solicitação para o backend
            const resposta = await fetch(
                "http://localhost:8080/poupanca/resgatar",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        contaId: contaId,
                        valor: Number(valor)
                    })
                }
            );

            const dados = await resposta.json();

            // Verifica se ocorreu algum erro
            if (!resposta.ok) {
                throw new Error(
                    dados.erro || "Erro ao resgatar dinheiro."
                );
            }

            // Atualiza o saldo da poupança
            setSaldoPoupanca(dados.saldo);

            // Limpa o campo de valor
            setValor("");

            setMensagemSucesso("Dinheiro resgatado com sucesso!");

        } catch (erro) {
            setMensagemErro(erro.message);
        }
    }

    // Consultar poupança ao abrir a tela
    useEffect(() => {
        consultarPoupanca();
    }, []);

    return (
        <main className="pagina-poupanca">

            <header className="cabecalho-poupanca">
                <div className="logo-nekopay">
                    🐱 NekoPay
                </div>

                {onVoltar && (
                    <button
                        className="botao-voltar"
                        onClick={onVoltar}
                    >
                        ☰
                    </button>
                )}
            </header>

            <section className="titulo-poupanca">
                <h2>Minha Poupança</h2>
                <p>Guarde hoje, colha amanhã.</p>
            </section>

            {/* Cartões de saldo */}
            <section className="resumo-poupanca">

                <div className="cartao-saldo">
                    <span>♧</span>
                    <p>Saldo da conta</p>
                    <h3>{formatarMoeda(saldoConta)}</h3>
                </div>

                <div className="cartao-saldo dinheiro-guardado">
                    <span>🐱</span>
                    <p>Dinheiro guardado</p>
                    <h3>{formatarMoeda(saldoPoupanca)}</h3>
                </div>

            </section>

            {/* Área de movimentação */}
            <section className="movimentacao-poupanca">

                <h3>Movimentar</h3>

                <div className="botoes-operacao">

                    <button
                        type="button"
                        className={`botao-operacao guardar ${operacao === "guardar" ? "selecionado" : ""}`}
                        onClick={() => {
                            setOperacao("guardar");
                            setMensagemErro("");
                            setMensagemSucesso("");
                        }}
                    >
                        Guardar dinheiro
                    </button>

                    <button
                        type="button"
                        className={`botao-operacao resgatar ${operacao === "resgatar" ? "selecionado" : ""}`}
                        onClick={() => {
                            setOperacao("resgatar");
                            setMensagemErro("");
                            setMensagemSucesso("");
                        }}
                    >
                        Retirar dinheiro
                    </button>

                </div>

                <form onSubmit={movimentarDinheiro}>

                    <label htmlFor="valor-poupanca">
                        ◉ Valor (R$)
                    </label>

                    <input
                        id="valor-poupanca"
                        type="text"
                        inputMode="decimal"
                        placeholder="Digite o valor"
                        value={valor}
                        onChange={(e) => setValor(e.target.value)}
                    />

                    <button
                        type="submit"
                        className={`botao-movimentar ${operacao}`}
                        disabled={carregando}
                    >
                        {carregando
                            ? "Aguarde..."
                            : operacao === "guardar"
                                ? "♧ Guardar"
                                : "↩ Retirar dinheiro"
                        }
                    </button>

                </form>

                {/* Mensagens */}
                {mensagemErro && (
                    <div className="mensagem-poupanca erro">
                        ⚠️ {mensagemErro}
                    </div>
                )}

                {mensagemSucesso && (
                    <div className="mensagem-poupanca sucesso">
                        ✓ {mensagemSucesso}
                    </div>
                )}

            </section>

            <footer className="rodape-poupanca">
                🐾
                <p>NekoPay — Sorte e prosperidade sempre.</p>
            </footer>

        </main>
    );
}
