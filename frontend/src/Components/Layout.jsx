import { useEffect, useState } from "react";
import { Link, NavLink, useLocation, useNavigate } from "react-router-dom";
import { sairDaConta } from "../Services/api.js";
// Pedaços de tela compartilhados (cabeçalho, menu lateral e estados de carregamento/erro).
// Estilos: App.css, blocos "layout-*", "estado-*" e "comp-*".

const ITENS_DO_MENU = [
  { to: "/", rotulo: "Início", icone: "🏠", id: "layout-menu-item-inicio" },
  { to: "/transferencia", rotulo: "Transferência", icone: "⇄", id: "layout-menu-item-transferencia" },
  { to: "/poupanca", rotulo: "Poupança", icone: "🐖", id: "layout-menu-item-poupanca" },
  { to: "/extrato", rotulo: "Extrato", icone: "🧾", id: "layout-menu-item-extrato" },
  { to: "/perfil", rotulo: "Perfil", icone: "👤", id: "layout-menu-item-perfil" },
];

export function LayoutPagina({ idPagina, titulo, subtitulo, voltarPara, children }) {
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

export function Carregando() {
  return (
    <div id="estado-carregando" className="estado-carregando" aria-busy="true" aria-live="polite">
      <div className="estado-esqueleto estado-esqueleto-cartao" />
      <div className="estado-esqueleto estado-esqueleto-linha" />
      <div className="estado-esqueleto estado-esqueleto-linha" />
      <span className="estado-so-leitor-de-tela">Carregando…</span>
    </div>
  );
}

export function EstadoSemConta() {
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

export function ErroCarregar({ mensagem, aoTentarNovamente }) {
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
