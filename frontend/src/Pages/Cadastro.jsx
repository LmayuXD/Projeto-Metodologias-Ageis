import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { contasService, entrarNaConta } from "../Services/api.js";

export default function Cadastro() {
    const navigate = useNavigate();
    const [form, setForm] = useState({ nomeTitular: "", documento: "" });
    const [erro, setErro] = useState("");
    const [enviando, setEnviando] = useState(false);

    const handleChange = (e) =>
        setForm({ ...form, [e.target.name]: e.target.value });

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (enviando) return; // evita clique duplo
        setErro("");
        setEnviando(true);

        try {
            const conta = await contasService.cadastrar(form.nomeTitular, form.documento);
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

            <header className=" cabecalho-cadastro ">
                <Link id="seta-voltar" to="/"> &lt; </Link>
                {/* imagem do nekopay*/}

            </header>

            <h2>Abrir conta </h2>
            <p> Preencha os dados abaixo para abrir a sua conta NekoPay. </p>

            <form onSubmit={handleSubmit}>

                {erro && (
                    <div id="cadastro-erro" className="comp-alerta comp-alerta-erro" role="alert">
                        <span className="comp-alerta-icone" aria-hidden="true">!</span>
                        <div>{erro}</div>
                    </div>
                )}

                <label> Nome </label>
                <input className="form-input"
                       name="nomeTitular"
                       placeholder="Nome completo"
                       value={form.nomeTitular}
                       onChange={handleChange}
                />
                <label> nº Documento </label>
                <input className="form-input"
                       name="documento"
                       placeholder="CPF ou CNPJ"
                       value={form.documento}
                       onChange={handleChange}
                />

                <button className="btn-cadastrar" type="submit" disabled={enviando}>
                    {enviando ? "Cadastrando…" : "Cadastrar"}
                </button>

            </form>

            <footer className="cadastro-footer">
                &copy; Todos os direitos reservados por Tech Girls.
            </footer>
        </div>

    );
}
