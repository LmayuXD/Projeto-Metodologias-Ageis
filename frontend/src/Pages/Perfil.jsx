import { useEffect, useState } from "react";
import { Link, NavLink, useLocation, useNavigate } from "react-router-dom";
import {
  useConta,
  requisicao,
  formatarMoeda,
  mascararDocumento,
  sairDaConta,
} from "../services/api.js";
// Estilos: App.css, blocos "comp-*" (compartilhados) e "perfil-*" (desta página).

function validar(nome, documento) {
  const erros = {};
  const n = nome.trim();
  if (!n) erros.nome = "Informe o nome completo.";
  else if (n.length < 3) erros.nome = "O nome deve ter pelo menos 3 caracteres.";
  else if (!/^[\p{L} ]+$/u.test(n)) erros.nome = "Use apenas letras e espaços.";

  const d = documento.trim();
  if (!d) erros.documento = "Informe o CPF ou CNPJ.";
  else {
    const digitos = d.replace(/[.\-/\s]/g, "");
    if (!/^\d+$/.test(digitos)) erros.documento = "O documento deve conter apenas números.";
    else if (digitos.length !== 11 && digitos.length !== 14)
      erros.documento = "CPF tem 11 dígitos e CNPJ tem 14.";
  }
  return erros;
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


export default function Perfil() {
  const navigate = useNavigate();
  const { contaId, conta, setConta, carregando, erro, recarregar } = useConta();

  const [editando, setEditando] = useState(false);
  const [nome, setNome] = useState("");
  const [documento, setDocumento] = useState("");
  const [erros, setErros] = useState({});
  const [salvando, setSalvando] = useState(false);
  const [aviso, setAviso] = useState(null); // { tipo: "sucesso" | "erro", texto }
  const [saldoVisivel, setSaldoVisivel] = useState(true);

  const [encerrando, setEncerrando] = useState(false);
  const [confirmacao, setConfirmacao] = useState("");
  const [excluindo, setExcluindo] = useState(false);

  if (!contaId) {
    return (
      <LayoutPagina idPagina="pagina-perfil" titulo="Meu perfil" voltarPara="/">
        <EstadoSemConta />
      </LayoutPagina>
    );
  }

  function iniciarEdicao() {
    setNome(conta.nomeTitular);
    setDocumento(conta.documento);
    setErros({});
    setAviso(null);
    setEditando(true);
  }

  function cancelarEdicao() {
    setEditando(false);
    setErros({});
  }

  async function salvar(e) {
    e.preventDefault();
    const e2 = validar(nome, documento);
    setErros(e2);
    if (Object.keys(e2).length > 0) return;

    setSalvando(true);
    setAviso(null);
    try {
      const atualizada = await requisicao(`/api/contas/${contaId}`, {
        method: "PUT",
        body: JSON.stringify({ nomeTitular: nome.trim(), documento: documento.trim() }),
      });
      setConta(atualizada);
      setEditando(false);
      setAviso({ tipo: "sucesso", texto: "Dados atualizados com sucesso!" });
    } catch (err) {
      setAviso({ tipo: "erro", texto: err.message });
    } finally {
      setSalvando(false);
    }
  }

  async function encerrarConta() {
    setExcluindo(true);
    setAviso(null);
    try {
      await requisicao(`/api/contas/${contaId}`, { method: "DELETE" });
      sairDaConta();
      navigate("/cadastro");
    } catch (err) {
      setAviso({ tipo: "erro", texto: err.message });
      setExcluindo(false);
    }
  }

  function sair() {
    sairDaConta();
    navigate("/");
  }

  const semAlteracao =
    conta && nome.trim() === conta.nomeTitular && documento.trim() === conta.documento;

  return (
    <LayoutPagina
      idPagina="pagina-perfil"
      titulo="Meu perfil"
      subtitulo="Seus dados e configurações da conta."
      voltarPara="/"
    >
      {carregando && !conta && <Carregando />}
      {erro && !conta && <ErroCarregar mensagem={erro} aoTentarNovamente={recarregar} />}

      {conta && (
        <>
          {aviso && (
            <div
              id="perfil-aviso"
              className={`comp-alerta ${aviso.tipo === "sucesso" ? "comp-alerta-sucesso" : "comp-alerta-erro"}`}
              role={aviso.tipo === "erro" ? "alert" : "status"}
            >
              <span className="comp-alerta-icone" aria-hidden="true">
                {aviso.tipo === "sucesso" ? "✓" : "!"}
              </span>
              <div>{aviso.texto}</div>
            </div>
          )}

          {/* ---------- Resumo: avatar, nome, documento e saldo ---------- */}
          <section id="perfil-cartao-resumo" className="comp-cartao comp-pilha" aria-label="Resumo da conta">
            <div className="perfil-cabecalho-usuario">
              <span id="perfil-avatar" className="perfil-avatar" aria-hidden="true">
                {conta.nomeTitular.trim().charAt(0).toUpperCase()}
              </span>
              <div>
                <p id="perfil-nome-exibido" className="perfil-nome-exibido">{conta.nomeTitular}</p>
                <p id="perfil-documento-mascarado" className="perfil-documento-mascarado">
                  {conta.documento.replace(/\D/g, "").length === 14 ? "CNPJ" : "CPF"} •{" "}
                  {mascararDocumento(conta.documento)}
                </p>
              </div>
            </div>

            <div id="perfil-cartao-saldo" className="comp-cartao comp-cartao-saldo">
              <div className="comp-saldo-rotulo">
                <span>Saldo da conta</span>
                <button
                  type="button"
                  id="perfil-botao-saldo-visibilidade"
                  className="layout-botao-icone perfil-botao-saldo-visibilidade"
                  aria-label={saldoVisivel ? "Ocultar saldo" : "Mostrar saldo"}
                  aria-pressed={!saldoVisivel}
                  onClick={() => setSaldoVisivel((v) => !v)}
                >
                  {saldoVisivel ? "👁" : "🙈"}
                </button>
              </div>
              <p id="perfil-saldo-valor" className="comp-saldo-valor">
                {saldoVisivel ? formatarMoeda(conta.saldo) : "R$ ••••••"}
              </p>
            </div>
          </section>

          {/* ---------- Dados pessoais (visualizar / editar) ---------- */}
          <section id="perfil-secao-dados" className="comp-cartao comp-pilha" aria-label="Dados pessoais">
            <div className="comp-linha-espacada">
              <h2 className="comp-secao-titulo">Dados pessoais</h2>
              {!editando && (
                <button
                  type="button"
                  id="perfil-botao-editar"
                  className="comp-botao comp-botao-contorno comp-botao-pequeno"
                  onClick={iniciarEdicao}
                >
                  ✎ Editar
                </button>
              )}
            </div>

            {!editando ? (
              <dl id="perfil-lista-dados" className="comp-detalhes">
                <div className="comp-detalhes-linha">
                  <dt>Nome completo</dt>
                  <dd>{conta.nomeTitular}</dd>
                </div>
                <div className="comp-detalhes-linha">
                  <dt>CPF ou CNPJ</dt>
                  <dd>{conta.documento}</dd>
                </div>
                <div className="comp-detalhes-linha">
                  <dt>Número da conta</dt>
                  <dd>{String(conta.id).padStart(6, "0")}</dd>
                </div>
              </dl>
            ) : (
              <form id="perfil-formulario-edicao" onSubmit={salvar} noValidate className="comp-pilha">
                <div className="comp-campo">
                  <label htmlFor="perfil-input-nome" className="comp-campo-rotulo">Nome completo</label>
                  <input
                    id="perfil-input-nome"
                    className="comp-campo-input"
                    value={nome}
                    onChange={(e) => setNome(e.target.value)}
                    autoComplete="name"
                    aria-invalid={Boolean(erros.nome)}
                    aria-describedby={erros.nome ? "perfil-erro-nome" : undefined}
                  />
                  {erros.nome && <span id="perfil-erro-nome" className="comp-campo-erro">{erros.nome}</span>}
                </div>

                <div className="comp-campo">
                  <label htmlFor="perfil-input-documento" className="comp-campo-rotulo">CPF ou CNPJ</label>
                  <input
                    id="perfil-input-documento"
                    className="comp-campo-input"
                    value={documento}
                    onChange={(e) => setDocumento(e.target.value)}
                    inputMode="numeric"
                    aria-invalid={Boolean(erros.documento)}
                    aria-describedby={erros.documento ? "perfil-erro-documento" : "perfil-ajuda-documento"}
                  />
                  {erros.documento ? (
                    <span id="perfil-erro-documento" className="comp-campo-erro">{erros.documento}</span>
                  ) : (
                    <span id="perfil-ajuda-documento" className="comp-campo-ajuda">
                      Este documento é a sua chave Pix. Alterá-lo muda a chave que outras pessoas usam para te pagar.
                    </span>
                  )}
                </div>

                <div className="comp-botoes-par">
                  <button
                    type="button"
                    id="perfil-botao-cancelar-edicao"
                    className="comp-botao comp-botao-contorno"
                    onClick={cancelarEdicao}
                    disabled={salvando}
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    id="perfil-botao-salvar"
                    className="comp-botao comp-botao-primario"
                    disabled={salvando || semAlteracao}
                  >
                    {salvando ? "Salvando…" : "Salvar"}
                  </button>
                </div>
              </form>
            )}
          </section>

          {/* ---------- Atalhos ---------- */}
          <section id="perfil-secao-atalhos" className="comp-cartao comp-pilha" aria-label="Atalhos">
            <h2 className="comp-secao-titulo">Atalhos</h2>
            <div className="comp-botoes-par">
              <Link id="perfil-atalho-extrato" to="/extrato" className="comp-botao comp-botao-suave">
                🧾 Extrato
              </Link>
              <Link id="perfil-atalho-pix" to="/transferencia" className="comp-botao comp-botao-suave">
                ⇄ Pix
              </Link>
            </div>
            <button
              type="button"
              id="perfil-botao-sair"
              className="comp-botao comp-botao-contorno"
              onClick={sair}
            >
              ↩ Sair da conta
            </button>
          </section>

          {/* ---------- Encerrar conta (ação perigosa, pede confirmação) ---------- */}
          <section
            id="perfil-secao-encerrar-conta"
            className="comp-cartao perfil-secao-encerrar-conta comp-pilha"
            aria-label="Encerrar conta"
          >
            <h2 className="comp-secao-titulo perfil-encerrar-titulo">Encerrar conta</h2>
            {!encerrando ? (
              <>
                <p className="perfil-encerrar-explicacao">
                  Esta ação apaga sua conta, seu extrato e sua poupança. Não é possível desfazer.
                </p>
                <button
                  type="button"
                  id="perfil-botao-abrir-encerramento"
                  className="comp-botao comp-botao-perigo"
                  onClick={() => setEncerrando(true)}
                >
                  Encerrar minha conta
                </button>
              </>
            ) : (
              <div className="comp-pilha">
                <div className="comp-alerta comp-alerta-erro" role="alert">
                  <span className="comp-alerta-icone" aria-hidden="true">!</span>
                  <div>
                    <strong className="comp-alerta-titulo">Tem certeza?</strong>
                    {conta.saldo > 0
                      ? `Você ainda tem ${formatarMoeda(conta.saldo)} na conta, que serão perdidos.`
                      : "Todos os seus dados serão apagados."}
                  </div>
                </div>
                <div className="comp-campo">
                  <label htmlFor="perfil-input-confirmacao" className="comp-campo-rotulo">
                    Digite ENCERRAR para confirmar
                  </label>
                  <input
                    id="perfil-input-confirmacao"
                    className="comp-campo-input"
                    value={confirmacao}
                    onChange={(e) => setConfirmacao(e.target.value)}
                    autoComplete="off"
                  />
                </div>
                <div className="comp-botoes-par">
                  <button
                    type="button"
                    id="perfil-botao-voltar-encerramento"
                    className="comp-botao comp-botao-contorno"
                    disabled={excluindo}
                    onClick={() => {
                      setEncerrando(false);
                      setConfirmacao("");
                    }}
                  >
                    Voltar
                  </button>
                  <button
                    type="button"
                    id="perfil-botao-confirmar-encerramento"
                    className="comp-botao comp-botao-primario"
                    disabled={excluindo || confirmacao.trim().toUpperCase() !== "ENCERRAR"}
                    onClick={encerrarConta}
                  >
                    {excluindo ? "Encerrando…" : "Encerrar"}
                  </button>
                </div>
              </div>
            )}
          </section>
        </>
      )}
    </LayoutPagina>
  );
}
