import { Link } from "react-router-dom";
import { useState } from "react";
import { useConta, formatarMoeda } from "../Services/api.js";
import { LayoutPagina, Carregando, ErroCarregar } from "../Components/Layout.jsx";
// Estilos: App.css, blocos "comp-*" (compartilhados) e "inicio-*" (desta página).

// ----- INICIO == PERGUNTA SE O USUARIO DESEJA CRIAR OU ENTRAR EM UMA CONTA EXISTENTE  -----


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
                <Link className="botao-inicio" to={"/login"}>
                    <button>Já tenho conta</button>
                </Link>

                <footer className="cadastro-footer">
                    <p> &copy; Todos os direitos reservados por Tech Girls. </p>
                </footer>
            </div>
        );
    }
