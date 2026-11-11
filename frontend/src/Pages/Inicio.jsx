import { Link } from "react-router-dom";
import { useState } from "react";
import { useConta, formatarMoeda } from "../Services/api.js";
import { LayoutPagina, Carregando, ErroCarregar } from "../Components/Layout.jsx";
// Estilos: App.css, blocos "comp-*" (compartilhados) e "inicio-*" (desta página).

const ATALHOS = [
    { to: "/transferencia", rotulo: "Transferir", id: "inicio-atalho-transferencia" },
    { to: "/poupanca", rotulo: "Caixinha", id: "inicio-atalho-poupanca" },
    { to: "/extrato", rotulo: "Extrato", id: "inicio-atalho-extrato" },
    { to: "/perfil", rotulo: "Perfil", id: "inicio-atalho-perfil" },
];

export default function Inicio() {
    const { contaId, conta, carregando, erro, recarregar } = useConta();
    const [saldoVisivel, setSaldoVisivel] = useState(true);

    // Sem conta ativa neste navegador: tela de boas-vindas
    if (!contaId) {
        return (
            <div>
                <h2> SEJA BEM-VINDO(A) </h2>

                <Link className="botao-inicio" to={"/cadastro"}>
                    <button>Iniciar em NekoPay</button>
                </Link>

                <footer className="cadastro-footer">
                    <p> &copy; Todos os direitos reservados por Tech Girls. </p>
                </footer>
            </div>
        );
    }

    const primeiroNome = conta?.nomeTitular?.split(" ")[0];

    return (
        <LayoutPagina
            idPagina="pagina-inicio"
            titulo={primeiroNome ? `Olá, ${primeiroNome}!` : "Olá!"}
            subtitulo="Bem-vindo(a) de volta ao NekoPay."
        >
            {carregando && !conta && <Carregando />}
            {erro && !conta && <ErroCarregar mensagem={erro} aoTentarNovamente={recarregar} />}

            {conta && (
                <>
                    <div id="inicio-cartao-saldo" className="comp-cartao comp-cartao-saldo">
                        <div className="comp-saldo-rotulo">
                            <span>Saldo disponível</span>
                            <button
                                type="button"
                                id="inicio-botao-saldo-visibilidade"
                                className="layout-botao-icone perfil-botao-saldo-visibilidade"
                                aria-label={saldoVisivel ? "Ocultar saldo" : "Mostrar saldo"}
                                aria-pressed={!saldoVisivel}
                                onClick={() => setSaldoVisivel((v) => !v)}
                            >
                                {saldoVisivel ? "👁" : "🙈"}
                            </button>
                        </div>
                        <p id="inicio-saldo-valor" className="comp-saldo-valor">
                            {saldoVisivel ? formatarMoeda(conta.saldo) : "R$ ••••••"}
                        </p>
                    </div>

                    <div id="inicio-atalhos" className="comp-botoes-coluna">
                        {ATALHOS.map((a) => (
                            <Link key={a.to} id={a.id} to={a.to} className="comp-botao comp-botao-suave">
                                {a.rotulo}
                            </Link>
                        ))}
                    </div>
                </>
            )}
        </LayoutPagina>
    );
}
