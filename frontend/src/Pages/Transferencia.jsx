import { useEffect, useState } from "react";
import { Link, NavLink, useLocation, useNavigate } from "react-router-dom";
import {
  useConta,
  requisicao,
  formatarMoeda,
  formatarDataHora,
  mascararDocumento,
  apenasDigitos,
  lerValor,
  extrairContraparte,
  sairDaConta,
} from "../services/api.js";
// Estilos: App.css, blocos "comp-*" (compartilhados) e "transferencia-*" (desta página).

// Mesmos limites do TransferenciaController
const LIMITE_POR_TRANSFERENCIA = 5000;
const TAMANHO_MAX_MENSAGEM = 140;
const VALORES_RAPIDOS = [10, 50, 100];

function formatarChave(chave) {
  if (chave.includes("@")) return chave;
  return mascararDocumento(chave);
}

const ITENS_DO_MENU = [
  { to: "/", rotulo: "Início", icone: "🏠", id: "layout-menu-item-inicio" },
  { to: "/transferencia", rotulo: "Transferência", icone: "⇄", id: "layout-menu-item-transferencia" },
  { to: "/poupanca", rotulo: "Poupança", icone: "🐖", id: "layout-menu-item-poupanca" },
  { to: "/extrato", rotulo: "Extrato", icone: "🧾", id: "layout-menu-item-extrato" },
  { to: "/perfil", rotulo: "Perfil", icone: "👤", id: "layout-menu-item-perfil" },
];

function LayoutPagina({ idPagina, titulo, subtitulo, voltarPara, children }) {
  const [menuAberto, setMenuAberto] = useState(false);
  const navigate = useNavigate();
  const { pathname } = useLocation();

  useEffect(() => {
    setMenuAberto(false);
  }, [pathname]);

  useEffect(() => {
    if (!menuAberto) return undefined;
    const aoTeclar = (e) => e.key === "Escape" && setMenuAberto(false);
    window.addEventListener("keydown", aoTeclar);
    return () => window.removeEventListener("keydown", aoTeclar);
  }, [menuAberto]);

  function sair() {
    sairDaConta();
    navigate("/");
  }

  return (
    <div id={idPagina} className="layout-pagina">
      <header id="layout-cabecalho" className="layout-cabecalho">
        <div className="layout-cabecalho-esquerda">
          {voltarPara && (
            <Link
              id="layout-botao-voltar"
              to={voltarPara}
              className="layout-botao-icone"
              aria-label="Voltar"
            >
              ‹
            </Link>
          )}
          <Link to="/" id="layout-marca" className="layout-marca" aria-label="NekoPay - ir para o início">
            <span className="layout-marca-logo" aria-hidden="true">🐱</span>
            <span className="layout-marca-nome">NekoPay</span>
          </Link>
        </div>
        <button
          type="button"
          id="layout-botao-abrir-menu"
          className="layout-botao-icone"
          aria-label="Abrir menu"
          aria-expanded={menuAberto}
          onClick={() => setMenuAberto(true)}
        >
          ☰
        </button>
      </header>

      {menuAberto && (
        <div id="layout-menu-fundo" className="layout-menu-fundo" onClick={() => setMenuAberto(false)}>
          <nav
            id="layout-menu-lateral"
            className="layout-menu-lateral"
            role="dialog"
            aria-modal="true"
            aria-label="Menu principal"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="layout-menu-topo">
              <span className="layout-marca">
                <span className="layout-marca-logo" aria-hidden="true">🐱</span>
                <span className="layout-marca-nome">NekoPay</span>
              </span>
              <button
                type="button"
                id="layout-botao-fechar-menu"
                className="layout-botao-icone layout-botao-icone-claro"
                aria-label="Fechar menu"
                onClick={() => setMenuAberto(false)}
              >
                ✕
              </button>
            </div>

            <ul className="layout-menu-lista">
              {ITENS_DO_MENU.map((item) => (
                <li key={item.to}>
                  <NavLink
                    id={item.id}
                    to={item.to}
                    end={item.to === "/"}
                    className={({ isActive }) =>
                      "layout-menu-item" + (isActive ? " layout-menu-item-ativo" : "")
                    }
                  >
                    <span aria-hidden="true">{item.icone}</span>
                    {item.rotulo}
                  </NavLink>
                </li>
              ))}
              <li>
                <button type="button" id="layout-menu-item-sair" className="layout-menu-item" onClick={sair}>
                  <span aria-hidden="true">↩</span>
                  Sair
                </button>
              </li>
            </ul>
          </nav>
        </div>
      )}

      <main id="layout-conteudo" className="layout-conteudo">
        {titulo && (
          <div className="layout-titulo-pagina">
            <h1 id="layout-titulo" className="layout-titulo">{titulo}</h1>
            {subtitulo && <p id="layout-subtitulo" className="layout-subtitulo">{subtitulo}</p>}
          </div>
        )}
        {children}
      </main>

      <footer id="layout-rodape" className="layout-rodape">
        <span aria-hidden="true">✿</span>
        <p className="layout-rodape-texto">NekoPay — Sorte e prosperidade sempre.</p>
      </footer>
    </div>
  );
}

function Carregando() {
  return (
    <div id="estado-carregando" className="estado-carregando" aria-busy="true" aria-live="polite">
      <div className="estado-esqueleto estado-esqueleto-cartao" />
      <div className="estado-esqueleto estado-esqueleto-linha" />
      <div className="estado-esqueleto estado-esqueleto-linha" />
      <span className="estado-so-leitor-de-tela">Carregando…</span>
    </div>
  );
}

function EstadoSemConta() {
  return (
    <div id="estado-sem-conta" className="comp-cartao estado-vazio">
      <span className="estado-vazio-icone" aria-hidden="true">🐾</span>
      <h2 className="comp-secao-titulo">Nenhuma conta selecionada</h2>
      <p className="estado-vazio-texto">Crie uma conta para começar a usar o NekoPay.</p>
      <Link id="estado-sem-conta-botao-criar" to="/cadastro" className="comp-botao comp-botao-primario">
        Criar conta
      </Link>
    </div>
  );
}

function ErroCarregar({ mensagem, aoTentarNovamente }) {
  return (
    <div id="estado-erro-carregar" className="estado-erro-carregar">
      <div className="comp-alerta comp-alerta-erro" role="alert">
        <span className="comp-alerta-icone" aria-hidden="true">!</span>
        <div>
          <strong className="comp-alerta-titulo">Não foi possível carregar</strong>
          <p>{mensagem}</p>
        </div>
      </div>
      <button
        type="button"
        id="estado-erro-carregar-botao-tentar-novamente"
        className="comp-botao comp-botao-suave"
        onClick={aoTentarNovamente}
      >
        Tentar novamente
      </button>
    </div>
  );
}


export default function Transferencia() {
  const { contaId, conta, carregando, erro, recarregar } = useConta();

  const [etapa, setEtapa] = useState("form"); // "form" | "revisar" | "sucesso"
  const [chave, setChave] = useState("");
  const [valorTxt, setValorTxt] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [erros, setErros] = useState({});
  const [enviando, setEnviando] = useState(false);
  const [erroEnvio, setErroEnvio] = useState("");
  const [comprovante, setComprovante] = useState(null);

  if (!contaId) {
    return (
      <LayoutPagina idPagina="pagina-transferencia" titulo="Transferências" voltarPara="/">
        <EstadoSemConta />
      </LayoutPagina>
    );
  }

  const valor = lerValor(valorTxt);

  function validar() {
    const e = {};
    const c = chave.trim();
    if (!c) {
      e.chave = "Informe a chave Pix do destinatário.";
    } else if (!c.includes("@")) {
      const d = apenasDigitos(c);
      if (d.length !== 11 && d.length !== 14) {
        e.chave = "Informe um CPF (11 dígitos) ou CNPJ (14 dígitos) válido.";
      } else if (conta && d === apenasDigitos(conta.documento)) {
        e.chave = "Você não pode transferir para a sua própria conta.";
      }
    }

    if (!valorTxt.trim()) {
      e.valor = "Informe o valor da transferência.";
    } else if (Number.isNaN(valor)) {
      e.valor = "Valor inválido. Use no máximo 2 casas decimais (ex.: 25,90).";
    } else if (valor < 0.01) {
      e.valor = "O valor deve ser maior que zero.";
    } else if (valor > LIMITE_POR_TRANSFERENCIA) {
      e.valor = `O limite por transferência é de ${formatarMoeda(LIMITE_POR_TRANSFERENCIA)}.`;
    } else if (conta && valor > conta.saldo) {
      e.valor = "Saldo insuficiente para esta transferência.";
    }

    if (mensagem.trim().length > TAMANHO_MAX_MENSAGEM) {
      e.mensagem = `A mensagem deve ter no máximo ${TAMANHO_MAX_MENSAGEM} caracteres.`;
    }
    return e;
  }

  function continuar(ev) {
    ev.preventDefault();
    const e = validar();
    setErros(e);
    setErroEnvio("");
    if (Object.keys(e).length === 0) setEtapa("revisar");
  }

  async function confirmar() {
    if (enviando) return; // evita clique duplo
    setEnviando(true);
    setErroEnvio("");
    try {
      const resposta = await requisicao("/api/transferencias", {
        method: "POST",
        body: JSON.stringify({
          contaOrigemId: Number(contaId),
          chaveDestino: chave.trim(),
          valor,
          descricao: mensagem.trim(),
        }),
      });
      setComprovante(resposta);
      setEtapa("sucesso");
      recarregar(); // atualiza o saldo
    } catch (err) {
      setErroEnvio(err.message);
      setEtapa("form");
    } finally {
      setEnviando(false);
    }
  }

  function novaTransferencia() {
    setChave("");
    setValorTxt("");
    setMensagem("");
    setErros({});
    setErroEnvio("");
    setComprovante(null);
    setEtapa("form");
  }

  // ============================== ETAPA 3: COMPROVANTE ==============================
  if (etapa === "sucesso" && comprovante) {
    const { nome, mensagem: mensagemEnviada } = extrairContraparte(comprovante.descricao);
    return (
      <LayoutPagina idPagina="pagina-transferencia" titulo="Comprovante" voltarPara="/">
        <section id="transferencia-comprovante" className="comp-cartao comp-pilha" aria-label="Comprovante do Pix">
          <div className="transferencia-comprovante-topo" role="status">
            <span className="transferencia-comprovante-check" aria-hidden="true">✓</span>
            <h2 className="comp-secao-titulo">Transferência realizada com sucesso!</h2>
            <p className="comp-campo-ajuda">O valor foi enviado para o destino com êxito.</p>
          </div>

          <div id="transferencia-comprovante-valor" className="transferencia-destaque-valor">
            <span className="transferencia-destaque-valor-rotulo">Valor enviado</span>
            <strong className="transferencia-destaque-valor-numero">{formatarMoeda(comprovante.valor)}</strong>
          </div>

          <dl id="transferencia-comprovante-detalhes" className="comp-detalhes">
            <div className="comp-detalhes-linha"><dt>Para</dt><dd>{nome || "—"}</dd></div>
            <div className="comp-detalhes-linha"><dt>Chave Pix</dt><dd>{formatarChave(comprovante.chaveDestino)}</dd></div>
            <div className="comp-detalhes-linha"><dt>De</dt><dd>{conta?.nomeTitular || "—"}</dd></div>
            <div className="comp-detalhes-linha"><dt>Data e hora</dt><dd>{formatarDataHora(comprovante.data)}</dd></div>
            {mensagemEnviada && (
              <div className="comp-detalhes-linha"><dt>Mensagem</dt><dd>{mensagemEnviada}</dd></div>
            )}
            <div className="comp-detalhes-linha">
              <dt>Código</dt>
              <dd className="comp-detalhes-codigo">{comprovante.id}</dd>
            </div>
          </dl>
        </section>

        {conta && (
          <p id="transferencia-saldo-atual" className="transferencia-saldo-atual">
            Saldo atual: <strong>{formatarMoeda(conta.saldo)}</strong>
          </p>
        )}

        <div className="comp-botoes-coluna">
          <Link id="transferencia-botao-ver-extrato" to="/extrato" className="comp-botao comp-botao-primario">
            Ver extrato
          </Link>
          <button
            type="button"
            id="transferencia-botao-nova-transferencia"
            className="comp-botao comp-botao-suave"
            onClick={novaTransferencia}
          >
            Nova transferência
          </button>
          <Link id="transferencia-botao-voltar-inicio" to="/" className="comp-botao comp-botao-contorno">
            Voltar ao início
          </Link>
        </div>
      </LayoutPagina>
    );
  }

  // ============================== ETAPA 2: REVISAR ==============================
  if (etapa === "revisar") {
    return (
      <LayoutPagina
        idPagina="pagina-transferencia"
        titulo="Confirme os dados"
        subtitulo="Confira tudo antes de enviar."
        voltarPara="/"
      >
        <section id="transferencia-revisao" className="comp-cartao comp-pilha" aria-label="Revisão do Pix">
          <div id="transferencia-revisao-valor" className="transferencia-destaque-valor">
            <span className="transferencia-destaque-valor-rotulo">Você vai enviar</span>
            <strong className="transferencia-destaque-valor-numero">{formatarMoeda(valor)}</strong>
          </div>
          <dl className="comp-detalhes">
            <div className="comp-detalhes-linha"><dt>Tipo</dt><dd>Pix</dd></div>
            <div className="comp-detalhes-linha">
              <dt>Chave do destinatário</dt>
              <dd>{formatarChave(chave.trim())}</dd>
            </div>
            {mensagem.trim() && (
              <div className="comp-detalhes-linha"><dt>Mensagem</dt><dd>{mensagem.trim()}</dd></div>
            )}
            <div className="comp-detalhes-linha"><dt>Saldo atual</dt><dd>{formatarMoeda(conta.saldo)}</dd></div>
            <div className="comp-detalhes-linha">
              <dt>Saldo após o envio</dt>
              <dd>{formatarMoeda(conta.saldo - valor)}</dd>
            </div>
          </dl>
        </section>

        <div id="transferencia-aviso-irreversivel" className="comp-alerta comp-alerta-info">
          <span className="comp-alerta-icone" aria-hidden="true">i</span>
          <div>Pix é instantâneo e não pode ser desfeito. Confirme se a chave está correta.</div>
        </div>

        <div className="comp-botoes-coluna">
          <button
            type="button"
            id="transferencia-botao-confirmar"
            className="comp-botao comp-botao-primario"
            onClick={confirmar}
            disabled={enviando}
          >
            {enviando ? "Enviando…" : "Confirmar Pix"}
          </button>
          <button
            type="button"
            id="transferencia-botao-voltar-editar"
            className="comp-botao comp-botao-contorno"
            onClick={() => setEtapa("form")}
            disabled={enviando}
          >
            Voltar e editar
          </button>
        </div>
      </LayoutPagina>
    );
  }

  // ============================== ETAPA 1: FORMULÁRIO ==============================
  return (
    <LayoutPagina
      idPagina="pagina-transferencia"
      titulo="Transferências"
      subtitulo="Seu dinheiro, onde você precisar."
      voltarPara="/"
    >
      {carregando && !conta && <Carregando />}
      {erro && !conta && <ErroCarregar mensagem={erro} aoTentarNovamente={recarregar} />}

      {conta && (
        <>
          <div id="transferencia-cartao-saldo" className="comp-cartao comp-cartao-saldo">
            <div className="comp-saldo-rotulo"><span>Saldo disponível</span></div>
            <p id="transferencia-saldo-disponivel" className="comp-saldo-valor">
              {formatarMoeda(conta.saldo)}
            </p>
          </div>

          <div id="transferencia-abas" className="transferencia-abas" role="tablist" aria-label="Tipo de transferência">
            <button
              type="button"
              id="transferencia-aba-pix"
              role="tab"
              aria-selected="true"
              className="transferencia-aba"
            >
              Pix
            </button>
            <button
              type="button"
              id="transferencia-aba-bancaria"
              role="tab"
              aria-selected="false"
              className="transferencia-aba"
              disabled
            >
              Transferência bancária <span className="transferencia-aba-selo">em breve</span>
            </button>
          </div>

          {erroEnvio && (
            <div id="transferencia-erro-envio" className="comp-alerta comp-alerta-erro" role="alert">
              <span className="comp-alerta-icone" aria-hidden="true">!</span>
              <div>
                <strong className="comp-alerta-titulo">Não foi possível transferir</strong>
                {erroEnvio}
              </div>
            </div>
          )}

          <form id="transferencia-formulario" onSubmit={continuar} noValidate className="comp-pilha">
            <div className="comp-campo">
              <label htmlFor="transferencia-input-chave" className="comp-campo-rotulo">
                Chave Pix do destinatário
              </label>
              <input
                id="transferencia-input-chave"
                className="comp-campo-input"
                placeholder="Digite a chave Pix"
                value={chave}
                onChange={(e) => setChave(e.target.value)}
                autoComplete="off"
                aria-invalid={Boolean(erros.chave)}
                aria-describedby={erros.chave ? "transferencia-erro-chave" : "transferencia-ajuda-chave"}
              />
              {erros.chave ? (
                <span id="transferencia-erro-chave" className="comp-campo-erro">{erros.chave}</span>
              ) : (
                <span id="transferencia-ajuda-chave" className="comp-campo-ajuda">
                  Por enquanto, a chave Pix é o CPF ou CNPJ da conta.
                </span>
              )}
            </div>

            <div className="comp-campo">
              <label htmlFor="transferencia-input-valor" className="comp-campo-rotulo">Valor (R$)</label>
              <div className="transferencia-valor-envoltorio">
                <span className="transferencia-valor-prefixo" aria-hidden="true">R$</span>
                <input
                  id="transferencia-input-valor"
                  className="comp-campo-input"
                  inputMode="decimal"
                  placeholder="0,00"
                  value={valorTxt}
                  onChange={(e) => setValorTxt(e.target.value)}
                  autoComplete="off"
                  aria-invalid={Boolean(erros.valor)}
                  aria-describedby={erros.valor ? "transferencia-erro-valor" : undefined}
                />
              </div>
              {erros.valor && <span id="transferencia-erro-valor" className="comp-campo-erro">{erros.valor}</span>}
              <div id="transferencia-valores-rapidos" className="transferencia-valores-rapidos" aria-label="Valores rápidos">
                {VALORES_RAPIDOS.map((v) => (
                  <button
                    key={v}
                    type="button"
                    id={`transferencia-valor-rapido-${v}`}
                    className="comp-chip"
                    aria-pressed={valor === v}
                    onClick={() => setValorTxt(v.toFixed(2).replace(".", ","))}
                  >
                    {formatarMoeda(v)}
                  </button>
                ))}
              </div>
              <span className="comp-campo-ajuda">
                Limite por transferência: {formatarMoeda(LIMITE_POR_TRANSFERENCIA)}.
              </span>
            </div>

            <div className="comp-campo">
              <label htmlFor="transferencia-input-mensagem" className="comp-campo-rotulo">
                Mensagem (opcional)
              </label>
              <textarea
                id="transferencia-input-mensagem"
                className="comp-campo-input comp-campo-textarea"
                placeholder="Ex.: aluguel de novembro"
                value={mensagem}
                maxLength={TAMANHO_MAX_MENSAGEM + 20}
                onChange={(e) => setMensagem(e.target.value)}
                aria-invalid={Boolean(erros.mensagem)}
              />
              <div className="comp-campo-ajuda-linha">
                {erros.mensagem ? (
                  <span id="transferencia-erro-mensagem" className="comp-campo-erro">{erros.mensagem}</span>
                ) : (
                  <span />
                )}
                <span id="transferencia-contador-mensagem" className="comp-campo-ajuda">
                  {mensagem.length}/{TAMANHO_MAX_MENSAGEM}
                </span>
              </div>
            </div>

            <button type="submit" id="transferencia-botao-continuar" className="comp-botao comp-botao-primario">
              Continuar →
            </button>
          </form>
        </>
      )}
    </LayoutPagina>
  );
}
