import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { contasService, entrarNaConta } from "../Services/api.js";
import "./Login.css";
import "./Cadastro.css";

export default function Login() {
    const navigate = useNavigate();
    const [documento, setDocumento] = useState("");
    const [senha, setSenha] = useState("");
    const [erro, setErro] = useState("");
    const [enviando, setEnviando] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (enviando) return;
        setErro("");
        setEnviando(true);

        try {
            const conta = await contasService.login(documento, senha);
            entrarNaConta(conta.id);
            navigate("/");
        } catch (err) {
            setErro(err.message);
        } finally {
            setEnviando(false);
        }
    };

    return (
        <div className="cadastro-page">

             <header className=" cabecalho-cadastro ">
                 <Link id="seta-voltar" to="/"> &lt; </Link>
             </header>

            <h2>Entrar</h2>
            <p> Digite o documento e a senha da sua conta para acessar o NekoPay. </p>

            <form id="login-formulario" onSubmit={handleSubmit} noValidate>

                {erro && (
                    <div id="login-erro" className="comp-alerta comp-alerta-erro" role="alert">
                        <span className="comp-alerta-icone" aria-hidden="true">!</span>
                        <div>{erro}</div>
                    </div>
                )}

                <label htmlFor="login-input-documento"> N.º Documento </label>
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

                {/* Espaçamento reduzido aqui */}
                <label htmlFor="login-input-senha" style={{ marginTop: "0.5rem", display: "block" }}> Senha </label>
                <input
                    id="login-input-senha"
                    className="form-input"
                    name="senha"
                    type="password"
                    placeholder="Digite sua senha"
                    autoComplete="current-password"
                    value={senha}
                    onChange={(e) => setSenha(e.target.value)}
                    aria-invalid={Boolean(erro)}
                />

                <button id="login-botao-entrar" className="btn-cadastrar" type="submit" disabled={enviando} style={{ marginTop: "1.5rem" }}>
                    {enviando ? "Entrando…" : "Entrar"}
                </button>

            </form>

            <div className="login-alternativa">
                Ainda não tem conta? <Link id="login-link-cadastro" to="/cadastro">Abrir conta</Link>
            </div>

            <footer className="cadastro-footer">
                © Todos os direitos reservados por Tech Girls.
            </footer>
        </div>
    );
}