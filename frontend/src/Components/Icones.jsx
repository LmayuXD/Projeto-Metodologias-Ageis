// Ícones em SVG (traço, estilo moderno). Herdam a cor do texto (currentColor).

const CAMINHOS = {
  inicio: (
    <>
      <path d="M3 10.5 12 3l9 7.5" />
      <path d="M5 9.5V20a1 1 0 0 0 1 1h4v-6h4v6h4a1 1 0 0 0 1-1V9.5" />
    </>
  ),
  transferencia: (
    <>
      <path d="M4 8h14" />
      <path d="m14 4 4 4-4 4" />
      <path d="M20 16H6" />
      <path d="m10 12-4 4 4 4" />
    </>
  ),
  poupanca: (
    <>
      <path d="M19 10.5c1.2.6 2 1.8 2 3.2V15h-2l-1 2.5V20h-3v-1.5H9V20H6v-2.5C4.2 16.5 3 14.6 3 12.5 3 8.9 6.6 6 11 6h4.5l2-2v3.2c.6.4 1.1.9 1.5 1.5" />
      <circle cx="15.5" cy="11" r=".6" fill="currentColor" />
      <path d="M9 3.5c.8-.6 1.8-.6 2.6 0" />
    </>
  ),
  extrato: (
    <>
      <path d="M6 3h12v18l-3-2-3 2-3-2-3 2z" />
      <path d="M9 8h6" />
      <path d="M9 12h6" />
    </>
  ),
  perfil: (
    <>
      <circle cx="12" cy="8" r="4" />
      <path d="M4 21c.6-4 3.8-6 8-6s7.4 2 8 6" />
    </>
  ),
  sair: (
    <>
      <path d="M10 4H6a1 1 0 0 0-1 1v14a1 1 0 0 0 1 1h4" />
      <path d="M15 8l4 4-4 4" />
      <path d="M19 12H9" />
    </>
  ),
  menu: (
    <>
      <path d="M4 7h16" />
      <path d="M4 12h16" />
      <path d="M4 17h16" />
    </>
  ),
  fechar: (
    <>
      <path d="M6 6l12 12" />
      <path d="M18 6 6 18" />
    </>
  ),
  voltar: <path d="m15 5-7 7 7 7" />,
};

export function Icone({ nome, tamanho = 22 }) {
  return (
    <svg
      width={tamanho}
      height={tamanho}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
      style={{ flexShrink: 0 }}
    >
      {CAMINHOS[nome]}
    </svg>
  );
}
