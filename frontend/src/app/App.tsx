import { useEffect, useState } from 'react';
import type { MouseEvent } from 'react';
import { LoginPage } from '../features/auth/pages/LoginPage';
import { RegisterPage } from '../features/auth/pages/RegisterPage';

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

  useEffect(() => {
    function atualizarCaminho() {
      definirNavegacao(lerNavegacao());
    }

    window.addEventListener('popstate', atualizarCaminho);
    return () => window.removeEventListener('popstate', atualizarCaminho);
  }, []);

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
      />
    );
  }

  return (
    <main>
      <h1>Gitbook</h1>
      <p>Plataforma de escrita colaborativa.</p>
      <nav>
        <a href={caminhoLogin} onClick={seguirLink}>Entrar</a>{' · '}
        <a href={caminhoCadastro} onClick={seguirLink}>Criar conta</a>
      </nav>
    </main>
  );
}
