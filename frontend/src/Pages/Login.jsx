import { Link } from "react-router-dom";
import {useConta} from "../Services/api.js";



export default function login() {

// ----- LOGIN  == CADASTRO DE >>NOVOS<<  USUARIOS!  -----



    return (
        <div className="cadastro-page">

            <header className="cabecalho-cadastro">
                <Link id="seta-voltar" to="/"> &lt; </Link>
            </header>

            <h2>Entrar</h2>
            <p> Digite o documento da sua conta para acessar o NekoPay. </p>

            <form id="login-formulario" onSubmit={handleSubmit} noValidate>

                {erro && (
                    <div id="login-erro" className="comp-alerta comp-alerta-erro" role="alert">
                        <span className="comp-alerta-icone" aria-hidden="true">!</span>
                        <div>{erro}</div>
                    </div>
                )}

                <label htmlFor="login-input-documento"> nº Documento </label>
                <input
                    id="login-input-documento"
                    className="form-input"
                    name="documento"
                    placeholder="CPF ou CNPJ"
                    inputMode="numeric"
                    autoComplete="username"
                    value={documento}
                    onChange={(e) => setDocumento(e.target.value)}
                    aria-invalid={Boolean(erro)}
                />
                <span className="login-dica">É o mesmo CPF ou CNPJ usado para abrir a conta.</span>

                <button id="login-botao-entrar" className="btn-cadastrar" type="submit" disabled={enviando}>
                    {enviando ? "Entrando…" : "Entrar"}
                </button>

            </form>

            <div className="login-alternativa">
                Ainda não tem conta? <Link id="login-link-cadastro" to="/cadastro">Abrir conta</Link>
            </div>

            <footer className="cadastro-footer">
                &copy; Todos os direitos reservados por Tech Girls.
            </footer>
        </div>
    );
}