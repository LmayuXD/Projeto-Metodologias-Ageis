import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  useConta,
  requisicao,
  formatarMoeda,
  formatarDataHora,
  extrairContraparte,
  sairDaConta,
} from "../Services/api.js";

import "./Extrato.css"

import {LayoutPagina, Carregando, EstadoSemConta, ErroCarregar} from "../Components/Layout.jsx";
// Estilos: App.css, blocos "comp-*" (compartilhados) e "extrato-*" (desta página).

// ----- EXTRATO == MOSTRA O HISTORICO DE TRANSAÇAO DA CONTA (PIX, CAIXINHA E RECEBIDOS) -----

const FILTROS = [
  { id: "todos", rotulo: "Tudo" },
  { id: "PIX_ENVIADO", rotulo: "Pix enviados" },
  { id: "PIX_RECEBIDO", rotulo: "Pix recebidos" },
  { id: "POUPANCA", rotulo: "Poupança" },
];

const ehPoupanca = (tipo) => tipo.startsWith("POUPANCA");

// Converte um registro do backend em um item pronto para exibir.
function montarItem(t, conta) {
  const { nome, mensagem } = extrairContraparte(t.descricao);
  const base = { id: `${t.tipo}-${t.id}`, codigo: t.id, tipo: t.tipo, valor: t.valor, data: t.data, mensagem };

  switch (t.tipo) {
    case "PIX_ENVIADO":
      return {
        ...base, icone: "↗", classeIcone: "extrato-icone-pix-enviado",
        selo: "Pix", classeSelo: "extrato-selo-pix",
        titulo: "Pix enviado", subtitulo: nome ? `Para ${nome}` : "Pix enviado",
        sinal: "-", classeValor: "extrato-valor-saida",
        remetente: conta.nomeTitular, destinatario: nome || "—", chave: t.chaveDestino,
      };
    case "PIX_RECEBIDO":
      return {
        ...base, icone: "↙", classeIcone: "extrato-icone-pix-recebido",
        selo: "Pix", classeSelo: "extrato-selo-pix",
        titulo: "Pix recebido", subtitulo: nome ? `De ${nome}` : "Pix recebido",
        sinal: "+", classeValor: "extrato-valor-entrada",
        remetente: nome || "—", destinatario: conta.nomeTitular, chave: t.chaveDestino,
      };
    case "POUPANCA_GUARDAR":
      return {
        ...base, icone: "🐖", classeIcone: "extrato-icone-poupanca",
        selo: "Poupança", classeSelo: "extrato-selo-poupanca",
        titulo: "Guardado na poupança", subtitulo: "Da conta para a caixinha",
        sinal: "-", classeValor: "extrato-valor-poupanca",
        remetente: "Conta corrente", destinatario: "Caixinha (poupança)",
      };
    default: // POUPANCA_RESGATE
      return {
        ...base, icone: "🐖", classeIcone: "extrato-icone-poupanca",
        selo: "Poupança", classeSelo: "extrato-selo-poupanca",
        titulo: "Resgate da poupança", subtitulo: "Da caixinha para a conta",
        sinal: "+", classeValor: "extrato-valor-poupanca",
        remetente: "Caixinha (poupança)", destinatario: "Conta corrente",
      };
  }
}

function rotuloDia(iso) {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "Sem data";
  const hoje = new Date();
  const ontem = new Date();
  ontem.setDate(hoje.getDate() - 1);
  if (d.toDateString() === hoje.toDateString()) return "Hoje";
  if (d.toDateString() === ontem.toDateString()) return "Ontem";
  return d.toLocaleDateString("pt-BR", { day: "2-digit", month: "long", year: "numeric" });
}

const hora = (iso) => {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? "" : d.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
};

export default function Extrato() {
  const { contaId, conta, carregando, erro, recarregar } = useConta();
  const [historicoPoupanca, setHistoricoPoupanca] = useState([]);
  const [filtro, setFiltro] = useState("todos");
  const [aberto, setAberto] = useState(null);

  // O histórico da poupança vem em /api/poupanca/{id} (campo "historico").
  // Se o backend ainda não enviar esse campo, a lista simplesmente fica vazia.
  useEffect(() => {
    if (!contaId) return undefined;
    let cancelado = false;
    requisicao(`/api/poupanca/${contaId}`)
      .then((p) => !cancelado && setHistoricoPoupanca(Array.isArray(p?.historico) ? p.historico : []))
      .catch(() => !cancelado && setHistoricoPoupanca([]));
    return () => {
      cancelado = true;
    };
  }, [contaId]);

  const itens = useMemo(() => {
    if (!conta) return [];
    return [...(conta.extrato || []), ...historicoPoupanca]
      .map((t) => montarItem(t, conta))
      .sort((a, b) => new Date(b.data) - new Date(a.data));
  }, [conta, historicoPoupanca]);

  const filtrados = useMemo(
    () =>
      itens.filter((i) =>
        filtro === "todos" ? true : filtro === "POUPANCA" ? ehPoupanca(i.tipo) : i.tipo === filtro
      ),
    [itens, filtro]
  );

  const totais = useMemo(
    () =>
      filtrados.reduce(
        (acc, i) => {
          if (i.sinal === "+") acc.entrou += i.valor;
          else acc.saiu += i.valor;
          return acc;
        },
        { entrou: 0, saiu: 0 }
      ),
    [filtrados]
  );

  const grupos = useMemo(() => {
    const mapa = new Map();
    filtrados.forEach((i) => {
      const dia = rotuloDia(i.data);
      if (!mapa.has(dia)) mapa.set(dia, []);
      mapa.get(dia).push(i);
    });
    return [...mapa.entries()];
  }, [filtrados]);

  if (!contaId) {
    return (
      <LayoutPagina idPagina="pagina-extrato" titulo="Extrato" voltarPara="/">
        <EstadoSemConta />
      </LayoutPagina>
    );
  }

  return (
    <LayoutPagina
      idPagina="pagina-extrato"
      titulo="Extrato"
      subtitulo="Tudo o que entrou e saiu da sua conta."
      voltarPara="/"
    >
      {carregando && !conta && <Carregando />}
      {erro && !conta && <ErroCarregar mensagem={erro} aoTentarNovamente={recarregar} />}

      {conta && (
        <>
          <div id="extrato-cartao-saldo" className="comp-cartao comp-cartao-saldo">
            <div className="comp-saldo-rotulo"><span>Saldo da conta</span></div>
            <p id="extrato-saldo-valor" className="comp-saldo-valor">{formatarMoeda(conta.saldo)}</p>
          </div>

          <div id="extrato-filtros" className="extrato-filtros" role="group" aria-label="Filtrar movimentações">
            {FILTROS.map((f) => (
              <button
                key={f.id}
                type="button"
                id={`extrato-filtro-${f.id.toLowerCase().replace("_", "-")}`}
                className="comp-chip"
                aria-pressed={filtro === f.id}
                onClick={() => {
                  setFiltro(f.id);
                  setAberto(null);
                }}
              >
                {f.rotulo}
              </button>
            ))}
          </div>

          {filtrados.length > 0 && (
            <div id="extrato-resumo" className="extrato-resumo-grade">
              <div id="extrato-resumo-entrada" className="comp-cartao extrato-resumo-item">
                <span className="extrato-resumo-rotulo">Entrou na conta</span>
                <strong className="extrato-resumo-valor extrato-resumo-valor-entrada">
                  {formatarMoeda(totais.entrou)}
                </strong>
              </div>
              <div id="extrato-resumo-saida" className="comp-cartao extrato-resumo-item">
                <span className="extrato-resumo-rotulo">Saiu da conta</span>
                <strong className="extrato-resumo-valor extrato-resumo-valor-saida">
                  {formatarMoeda(totais.saiu)}
                </strong>
              </div>
            </div>
          )}

          {filtrados.length === 0 ? (
            <div id="extrato-vazio" className="comp-cartao estado-vazio">
              <span className="estado-vazio-icone" aria-hidden="true">🐾</span>
              <h2 className="comp-secao-titulo">Nenhuma movimentação</h2>
              <p className="estado-vazio-texto">
                {filtro === "todos"
                  ? "Quando você fizer um Pix ou mexer na poupança, tudo aparece aqui."
                  : "Não há movimentações neste filtro."}
              </p>
              <Link id="extrato-botao-fazer-pix" to="/transferencia" className="comp-botao comp-botao-primario">
                Fazer um Pix
              </Link>
            </div>
          ) : (
            grupos.map(([dia, lista]) => (
              <section key={dia} className="extrato-grupo-dia" aria-label={dia}>
                <h2 className="extrato-dia-titulo">{dia}</h2>
                <ul className="extrato-lista">
                  {lista.map((i) => {
                    const expandido = aberto === i.id;
                    return (
                      <li key={i.id} className="extrato-item">
                        <button
                          type="button"
                          className="extrato-item-botao"
                          aria-expanded={expandido}
                          onClick={() => setAberto(expandido ? null : i.id)}
                        >
                          <span className={`extrato-item-icone ${i.classeIcone}`} aria-hidden="true">
                            {i.icone}
                          </span>
                          <span className="extrato-item-texto">
                            <span className="extrato-item-titulo">
                              {i.titulo}
                              <span className={`extrato-selo ${i.classeSelo}`}>{i.selo}</span>
                            </span>
                            <span className="extrato-item-subtitulo">{i.subtitulo}</span>
                          </span>
                          <span className="extrato-item-direita">
                            <span className={`extrato-item-valor ${i.classeValor}`}>
                              {i.sinal === "+" ? "+ " : "− "}
                              {formatarMoeda(i.valor)}
                            </span>
                            <span className="extrato-item-hora">{hora(i.data)}</span>
                          </span>
                        </button>

                        {expandido && (
                          <dl className="comp-detalhes extrato-item-detalhes">
                            <div className="comp-detalhes-linha"><dt>Tipo</dt><dd>{i.titulo}</dd></div>
                            <div className="comp-detalhes-linha"><dt>Remetente</dt><dd>{i.remetente}</dd></div>
                            <div className="comp-detalhes-linha"><dt>Destinatário</dt><dd>{i.destinatario}</dd></div>
                            {i.chave && (
                              <div className="comp-detalhes-linha"><dt>Chave Pix</dt><dd>{i.chave}</dd></div>
                            )}
                            <div className="comp-detalhes-linha"><dt>Valor</dt><dd>{formatarMoeda(i.valor)}</dd></div>
                            <div className="comp-detalhes-linha">
                              <dt>Data e hora</dt>
                              <dd>{formatarDataHora(i.data)}</dd>
                            </div>
                            {i.mensagem && (
                              <div className="comp-detalhes-linha"><dt>Mensagem</dt><dd>{i.mensagem}</dd></div>
                            )}
                            <div className="comp-detalhes-linha">
                              <dt>Código</dt>
                              <dd className="comp-detalhes-codigo">{i.codigo}</dd>
                            </div>
                          </dl>
                        )}
                      </li>
                    );
                  })}
                </ul>
              </section>
            ))
          )}
        </>
      )}
    </LayoutPagina>
  );
}
