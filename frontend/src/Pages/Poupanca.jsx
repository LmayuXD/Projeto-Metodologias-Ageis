import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  useConta,
  poupancaService,
  formatarMoeda,
  lerValor,
} from "../Services/api.js";

import "./Poupanca.css";
import { LayoutPagina, Carregando, EstadoSemConta, ErroCarregar } from "../Components/Layout.jsx";
// Estilos: App.css, blocos "comp-*" (compartilhados) e estilos da aplicação.

export default function Poupanca() {
  const { contaId, conta, carregando, erro, recarregar } = useConta();

  const [saldoPoupanca, setSaldoPoupanca] = useState(0);
  const [valorTxt, setValorTxt] = useState("");
  const [operacao, setOperacao] = useState("guardar"); // "guardar" | "resgatar"
  const [enviando, setEnviando] = useState(false);
  const [mensagemErro, setMensagemErro] = useState("");
  const [mensagemSucesso, setMensagemSucesso] = useState("");

  const VALORES_RAPIDOS = [50, 100, 200];
  const valor = lerValor(valorTxt);

  async function consultarPoupanca() {
    if (!contaId) return;
    try {
      const dados = await poupancaService.consultar(contaId);
      setSaldoPoupanca(dados.saldo || 0);
    } catch (err) {
      setMensagemErro(err.message);
    }
  }

  useEffect(() => {
    if (contaId) {
      consultarPoupanca();
    }
  }, [contaId]);

  async function movimentar(ev) {
    ev.preventDefault();
    setMensagemErro("");
    setMensagemSucesso("");

    if (!valorTxt.trim()) {
      setMensagemErro("Informe o valor.");
      return;
    } else if (Number.isNaN(valor) || valor <= 0) {
      setMensagemErro("Valor inválido. Use no máximo 2 casas decimais (ex.: 25,90).");
      return;
    }

    if (operacao === "guardar" && conta && valor > conta.saldo) {
      setMensagemErro("Saldo insuficiente na conta para guardar este valor.");
      return;
    }

    if (operacao === "resgatar" && valor > saldoPoupanca) {
      setMensagemErro("Você não possui saldo suficiente na poupança para este resgate.");
      return;
    }

    if (enviando) return;
    setEnviando(true);

    try {
      let dados;
      if (operacao === "guardar") {
        dados = await poupancaService.guardar(Number(contaId), valor);
        setMensagemSucesso("Dinheiro guardado com sucesso!");
      } else {
        dados = await poupancaService.resgatar(Number(contaId), valor);
        setMensagemSucesso("Dinheiro resgatado com sucesso!");
      }
      setSaldoPoupanca(dados.saldo || 0);
      setValorTxt("");
      recarregar(); // Atualiza o saldo da conta principal no topo/contexto
    } catch (err) {
      setMensagemErro(err.message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <LayoutPagina
      idPagina="pagina-poupanca"
      titulo="Minha Poupança"
      subtitulo="Guarde hoje, colha amanhã."
      voltarPara="/"
    >
      {carregando && !conta && <Carregando />}
      {erro && !conta && <ErroCarregar mensagem={erro} aoTentarNovamente={recarregar} />}
      {!contaId && <EstadoSemConta />}

      {conta && (
        <>
          <div className="resumo-poupanca comp-pilha" style={{ display: "grid", gap: "1rem", gridTemplateColumns: "1fr 1fr" }}>
            <div className="comp-cartao comp-cartao-saldo">
              <div className="comp-saldo-rotulo"><span>Saldo da conta</span></div>
              <p className="comp-saldo-valor">{formatarMoeda(conta.saldo)}</p>
            </div>

            <div className="comp-cartao comp-cartao-saldo">
              <div className="comp-saldo-rotulo"><span>Dinheiro guardado</span></div>
              <p className="comp-saldo-valor">{formatarMoeda(saldoPoupanca)}</p>
            </div>
          </div>

          <div id="poupanca-abas" className="transferencia-abas" role="tablist" aria-label="Operação da poupança" style={{ margin: "1.5rem 0" }}>
            <button
              type="button"
              role="tab"
              aria-selected={operacao === "guardar"}
              className={`transferencia-aba ${operacao === "guardar" ? "ativo" : ""}`}
              onClick={() => {
                setOperacao("guardar");
                setMensagemErro("");
                setMensagemSucesso("");
              }}
            >
              Guardar
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={operacao === "resgatar"}
              className={`transferencia-aba ${operacao === "resgatar" ? "ativo" : ""}`}
              onClick={() => {
                setOperacao("resgatar");
                setMensagemErro("");
                setMensagemSucesso("");
              }}
            >
              Resgatar
            </button>
          </div>

          {mensagemErro && (
            <div className="comp-alerta comp-alerta-erro" role="alert">
              <span className="comp-alerta-icone" aria-hidden="true">!</span>
              <div>
                <strong className="comp-alerta-titulo">Atenção</strong>
                {mensagemErro}
              </div>
            </div>
          )}

          {mensagemSucesso && (
            <div className="comp-alerta comp-alerta-sucesso" role="alert" style={{ background: "#eef9f0", border: "1px solid #c3e6cb", color: "#155724", padding: "1rem", borderRadius: "8px", marginBottom: "1rem", display: "flex", gap: "0.75rem", alignItems: "center" }}>
              <span aria-hidden="true">✓</span>
              <div>{mensagemSucesso}</div>
            </div>
          )}

          <form onSubmit={movimentar} className="comp-pilha">
            <div className="comp-campo">
              <label htmlFor="poupanca-input-valor" className="comp-campo-rotulo">
                Valor para {operacao === "guardar" ? "guardar" : "resgatar"} (R$)
              </label>
              <div className="transferencia-valor-envoltorio">
                <span className="transferencia-valor-prefixo" aria-hidden="true">R$</span>
                <input
                  id="poupanca-input-valor"
                  className="comp-campo-input"
                  inputMode="decimal"
                  placeholder="0,00"
                  value={valorTxt}
                  onChange={(e) => setValorTxt(e.target.value)}
                  autoComplete="off"
                />
              </div>
              {/* Espaçamento aumentado entre os valores rápidos e o botão de submit */}
              <div className="transferencia-valores-rapidos" aria-label="Valores rápidos" style={{ display: "flex", gap: "0.5rem", marginTop: "0.75rem", marginBottom: "2rem" }}>
                {VALORES_RAPIDOS.map((v) => (
                  <button
                    key={v}
                    type="button"
                    className="comp-chip"
                    aria-pressed={valor === v}
                    onClick={() => setValorTxt(v.toFixed(2).replace(".", ","))}
                  >
                    {formatarMoeda(v)}
                  </button>
                ))}
              </div>
            </div>

            <button
              type="submit"
              className="comp-botao comp-botao-primario"
              disabled={enviando}
            >
              {enviando
                ? "Processando…"
                : operacao === "guardar"
                  ? "Guardar dinheiro"
                  : "Resgatar dinheiro"}
            </button>
          </form>
        </>
      )}
    </LayoutPagina>
  );
}