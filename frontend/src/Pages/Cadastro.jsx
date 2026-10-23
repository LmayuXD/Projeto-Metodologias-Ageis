import "../App.jsx";
import { useState } from "react";
import {Link} from "react-router-dom";

export default function Cadastro() {
    const [form, setForm] = useState({ nomeTitular: "", documento: "" });
    const [msg, setMsg] = useState("");
    const [erro, setErro] = useState(false);

    const handleChange = (e) =>
        setForm({ ...form, [e.target.name]: e.target.value });

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMsg("");

        try {
            const resp = await fetch("http://localhost:8080/api/contas", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(form),
            });

            const data = await resp.json();

            if (!resp.ok) {
                setErro(true);
                setMsg(data.erro);          // mensagem de validação vinda do Java
                return;
            }

            setErro(false);
            setMsg(`Conta nº ${data.id} criada! Saldo: R$ ${data.saldoInicial.toFixed(2)}`);
            setForm({ nomeTitular: "", documento: "" });
        } catch {
            setErro(true);
            setMsg("Não foi possível conectar ao servidor.");
        }
    };

    return (

        <div class="cadastro-page">

            <header class=" cabecalho-cadastro ">
                <a id="seta-voltar" href={"/"}> &lt; </a>
                {/* imagem do nekopay*/}

            </header>

            <h2>Abrir conta </h2>
            <p> Preencha os dados abaixo para abrir a sua conta NekoPay. </p>

            <form onSubmit={handleSubmit}>


             <label> Nome </label>
                <input class="form-input"

                    name="nomeTitular"
                    placeholder="Nome completo"
                    value={form.nomeTitular}
                    onChange={handleChange}
                    />
                <label> nº Documento </label>
                <input class="form-input"
                       name="documento"
                       placeholder="CPF ou CNPJ"
                       value={form.documento}
                       onChange={handleChange}
                />

                <button class="btn-cadastrar" type="submit">Cadastrar</button>

            </form>

                <footer class="cadastro-footer">
                    &copy; Todos os direitos reservados por Tech Girls.
                </footer>
        </div>

    );
}
