import { useCallback, useEffect, useState } from "react";

export const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

// ---------- Requisição base ----------
export async function requisicao(caminho, opcoes = {}) {
    let resposta;
    try {
        resposta = await fetch(`${API_URL}${caminho}`, {
            ...opcoes,
            headers: { "Content-Type": "application/json", ...opcoes.headers },
        });
    } catch {
        const erro = new Error("Não foi possível conectar ao servidor.");
        erro.status = 0;
        throw erro;
    }

    let dados = null;
    try { dados = await resposta.json(); } catch { /* sem corpo */ }

    if (!resposta.ok) {
        const erro = new Error(dados?.erro || `Erro inesperado (${resposta.status}).`);
        erro.status = resposta.status;
        throw erro;
    }
    return dados;
}

const json = (corpo) => JSON.stringify(corpo);

// ---------- Serviços ----------
export const contasService = {
    cadastrar: (nomeTitular, documento) =>
        requisicao("/api/contas", { method: "POST", body: json({ nomeTitular, documento }) }),
    buscar: (id) => requisicao(`/api/contas/${id}`),
    atualizar: (id, nomeTitular, documento) =>
        requisicao(`/api/contas/${id}`, { method: "PUT", body: json({ nomeTitular, documento }) }),
    encerrar: (id) => requisicao(`/api/contas/${id}`, { method: "DELETE" }),
    extrato: (id, tipo) =>
        requisicao(`/api/contas/${id}/extrato${tipo ? `?tipo=${encodeURIComponent(tipo)}` : ""}`),
};

export const poupancaService = {
    consultar: (contaId) => requisicao(`/api/poupanca/${contaId}`),
    guardar: (contaId, valor) =>
        requisicao(`/api/poupanca/${contaId}/guardar`, { method: "POST", body: json({ valor }) }),
    resgatar: (contaId, valor) =>
        requisicao(`/api/poupanca/${contaId}/resgatar`, { method: "POST", body: json({ valor }) }),
};

export const pixService = {
    enviar: (contaOrigemId, chaveDestino, valor, descricao) =>
        requisicao("/api/pix", {
            method: "POST",
            body: json({ contaOrigemId, chaveDestino, valor, descricao }),
        }),
};

// ---------- Sessão (conta ativa neste navegador) ----------
const CHAVE_SESSAO = "nekopay.contaId";

export function lerContaId() {
    try { return localStorage.getItem(CHAVE_SESSAO) || null; } catch { return null; }
}
export function entrarNaConta(id) {
    try { localStorage.setItem(CHAVE_SESSAO, String(id)); } catch { /* storage bloqueado */ }
}
export function sairDaConta() {
    try { localStorage.removeItem(CHAVE_SESSAO); } catch { /* nada a fazer */ }
}

// backend: "saldoInicial" + extrato em outra rota; telas: conta.saldo e conta.extrato
function montarConta(conta, extrato) {
    return { ...conta, saldo: conta.saldoInicial, extrato: extrato ?? conta.extrato ?? [] };
}

// ---------- Hook: carrega a conta da sessão ----------
export function useConta() {
    const [contaId, setContaId] = useState(lerContaId);
    const [conta, setContaState] = useState(null);
    const [carregando, setCarregando] = useState(Boolean(contaId));
    const [erro, setErro] = useState("");
    const [tentativa, setTentativa] = useState(0);

    useEffect(() => {
        if (!contaId) return undefined;
        let cancelado = false;
        setCarregando(true);
        setErro("");

        Promise.all([contasService.buscar(contaId), contasService.extrato(contaId)])
            .then(([dados, extrato]) => {
                if (!cancelado) setContaState(montarConta(dados, extrato));
            })
            .catch((e) => {
                if (cancelado) return;
                if (e.status === 404) {       // conta não existe mais (servidor reiniciado)
                    sairDaConta();
                    setContaId(null);
                    setContaState(null);
                } else {
                    setErro(e.message);
                }
            })
            .finally(() => !cancelado && setCarregando(false));

        return () => { cancelado = true; };
    }, [contaId, tentativa]);

    const recarregar = useCallback(() => setTentativa((n) => n + 1), []);
    const setConta = useCallback(
        (nova) => setContaState((atual) => montarConta(nova, atual?.extrato)),
        []
    );

    return { contaId, conta, setConta, carregando, erro, recarregar };
}

// ---------- Formatação ----------
const moeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
export const formatarMoeda = (valor) => moeda.format(Number(valor) || 0);

export function formatarDataHora(iso) {
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return "—";
    return d.toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
}

export const apenasDigitos = (texto = "") => String(texto).replace(/\D/g, "");

export function mascararDocumento(documento = "") {
    const d = apenasDigitos(documento);
    if (d.length === 11) return `***.${d.slice(3, 6)}.${d.slice(6, 9)}-**`;
    if (d.length === 14) return `**.***.${d.slice(5, 8)}/${d.slice(8, 12)}-**`;
    return documento;
}

// "25,90", "1.234,56", "R$ 10" -> número; NaN se inválido ou com mais de 2 casas
export function lerValor(texto) {
    let t = String(texto ?? "").replace(/R\$|\s/g, "");
    if (!t) return NaN;
    if (t.includes(",")) t = t.replace(/\./g, "").replace(",", ".");
    else if (/^\d{1,3}(\.\d{3})+$/.test(t)) t = t.replace(/\./g, "");
    return /^\d+(\.\d{1,2})?$/.test(t) ? Number(t) : NaN;
}

// "Pix enviado para NOME - msg" / "Pix recebido de NOME"
export function extrairContraparte(descricao = "") {
    const m = /^Pix (?:enviado para|recebido de) (.*?)(?: - ([\s\S]*))?$/.exec(descricao || "");
    return { nome: m?.[1] ?? "", mensagem: m?.[2] ?? "" };
}