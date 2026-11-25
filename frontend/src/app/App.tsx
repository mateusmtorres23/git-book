import { useEffect, useState } from 'react';
import type { MouseEvent } from 'react';
import { useAuth } from '../features/auth/AuthProvider';
import { LoginPage } from '../features/auth/pages/LoginPage';
import { RegisterPage } from '../features/auth/pages/RegisterPage';
import { HomePage } from '../pages/HomePage';

const caminhoCadastro = '/cadastro';
const caminhoLogin = '/login';

type CadastroConcluido = { email: string; contaCriada: true };

function lerNavegacao() {
  const caminho = window.location.pathname;
  const estado: unknown = window.history.state;
  const cadastro = caminho === caminhoLogin && estado && typeof estado === 'object'
    && 'contaCriada' in estado && estado.contaCriada === true
    && 'email' in estado && typeof estado.email === 'string'
    ? { email: estado.email, contaCriada: true } : null;

  return { caminho, cadastro };
}

export function App() {
  const [{ caminho, cadastro }, definirNavegacao] = useState(lerNavegacao);
  const { verificacao, recuperarSessao } = useAuth();
  const rotaProtegida = caminho !== caminhoCadastro && caminho !== caminhoLogin;
  const sessaoValida = verificacao.tipo === 'autenticado'
    && Date.parse(verificacao.sessao.expiraEm) > Date.now();

  useEffect(() => {
    function atualizarCaminho() {
      definirNavegacao(lerNavegacao());
    }

    window.addEventListener('popstate', atualizarCaminho);
    return () => window.removeEventListener('popstate', atualizarCaminho);
  }, []);

  useEffect(() => {
    if (!rotaProtegida || window.location.pathname !== caminho) return;

    if (verificacao.tipo === 'semSessao') {
      window.history.replaceState(null, '', caminhoLogin);
      definirNavegacao(lerNavegacao());
    } else if (verificacao.tipo === 'verificando' || (verificacao.tipo === 'autenticado' && !sessaoValida)) {
      void recuperarSessao();
    }
  }, [caminho, rotaProtegida, verificacao.tipo, sessaoValida, recuperarSessao]);

  function seguirLink(evento: MouseEvent<HTMLAnchorElement>) {
    if (evento.button !== 0 || evento.ctrlKey || evento.metaKey || evento.shiftKey || evento.altKey) {
      return;
    }

    evento.preventDefault();
    window.history.pushState(null, '', evento.currentTarget.href);
    definirNavegacao(lerNavegacao());
  }

  function abrirLoginAposCadastro(email: string) {
    if (window.location.pathname !== caminhoCadastro) return;

    const estado: CadastroConcluido = { email, contaCriada: true };
    window.history.replaceState(estado, '', caminhoLogin);
    definirNavegacao(lerNavegacao());
  }

  function abrirInicioAposLogin() {
    if (window.location.pathname !== caminhoLogin) return;

    window.history.replaceState(null, '', '/');
    definirNavegacao(lerNavegacao());
  }

  if (caminho === caminhoCadastro) {
    return <RegisterPage aoCadastrar={abrirLoginAposCadastro} />;
  }

  if (caminho === caminhoLogin) {
    return (
      <LoginPage
        key={cadastro?.email}
        emailInicial={cadastro?.email}
        contaCriada={cadastro?.contaCriada}
        aoAbrirCadastro={seguirLink}
        aoAutenticar={abrirInicioAposLogin}
      />
    );
  }

  if (sessaoValida) return <HomePage />;

  if (verificacao.tipo === 'erro') {
    return (
      <main>
        <p role="alert">{verificacao.mensagem}</p>
        <button type="button" onClick={() => void recuperarSessao()}>Tentar novamente</button>
      </main>
    );
  }

  return <main><p role="status">Verificando sessão…</p></main>;
}
