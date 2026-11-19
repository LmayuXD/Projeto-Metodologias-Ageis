import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { contasService, entrarNaConta } from "../Services/api.js";
import logoNekoPay from "../assets/logo-nekopay.png";
import "./Cadastro.css";

// ----- CADASTRO = FORMULARIO QUE PEDE NOME, DOCUMENTO E SENHA DO NOVO USUARIO -----

export default function Cadastro() {
    const navigate = useNavigate();
    const [form, setForm] = useState({ nomeTitular: "", documento: "", senha: "" });
    const [erro, setErro] = useState("");
    const [enviando, setEnviando] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (enviando) return; // evita clique duplo
        setErro("");
        setEnviando(true);

        try {
            // Envia explicitamente os três campos do estado para o serviço
            const conta = await contasService.cadastrar(form.nomeTitular, form.documento, form.senha);
            entrarNaConta(conta.id);   // a conta recém-criada vira a conta ativa
            navigate("/");             // a tela inicial já mostra o saldo
        } catch (err) {
            setErro(err.message);      // mensagem de validação vinda do Java
        } finally {
            setEnviando(false);
        }
    };

    return (
        <div className="cadastro-page">

            <header className="cabecalho-cadastro">
                <img className="auth-logo" src={logoNekoPay} alt="Logo NekoPay" />
                <h2>Abrir conta</h2>
                <p>Preencha os dados abaixo para abrir a sua conta NekoPay.</p>
            </header>


            {erro && (
                <div id="cadastro-erro" className="comp-alerta comp-alerta-erro" role="alert">
                    <span className="comp-alerta-icone" aria-hidden="true">!</span>
                    <div>{erro}</div>
                </div>
            )}

            <form className="auth-form" onSubmit={handleSubmit}>

                <label htmlFor="cadastro-input-nome">Nome</label>
                <input
                    id="cadastro-input-nome"
                    className="form-input"
                    name="nomeTitular"
                    autoComplete="name"
                    placeholder="Nome completo"
                    value={form.nomeTitular}
                    onChange={handleChange}
                />

                <label htmlFor="cadastro-input-documento">N.º Documento</label>
                <input
                    id="cadastro-input-documento"
                    className="form-input"
                    name="documento"
                    inputMode="numeric"
                    placeholder="CPF ou CNPJ"
                    value={form.documento}
                    onChange={handleChange}
                />

                <label htmlFor="cadastro-input-senha">Senha</label>
                <input
                    id="cadastro-input-senha"
                    className="form-input"
                    name="senha"
                    autoComplete="new-password"
                    type="password"
                    placeholder="Mínimo de 8 caracteres"
                    value={form.senha}
                    onChange={handleChange}
                />

                <button className="btn-cadastrar" type="submit" disabled={enviando}>
                    {enviando ? "Cadastrando…" : "Cadastrar"}
                </button>
            </form>

            <div className="login-alternativa">
                Já tem conta? <Link id="cadastro-link-login" to="/login">Entrar</Link>
            </div>

            <footer className="cadastro-footer">
                © Todos os direitos reservados por Tech Girls.
            </footer>
        </div>
    );
}