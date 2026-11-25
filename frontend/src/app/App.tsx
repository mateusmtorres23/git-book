import { useEffect, useState } from 'react';
import type { MouseEvent } from 'react';
import { RegisterPage } from '../features/auth/pages/RegisterPage';

const caminhoCadastro = '/cadastro';

export function App() {
  const [caminho, definirCaminho] = useState(() => window.location.pathname);

  useEffect(() => {
    function atualizarCaminho() {
      definirCaminho(window.location.pathname);
    }

    window.addEventListener('popstate', atualizarCaminho);
    return () => window.removeEventListener('popstate', atualizarCaminho);
  }, []);

  function abrirCadastro(evento: MouseEvent<HTMLAnchorElement>) {
    if (evento.button !== 0 || evento.ctrlKey || evento.metaKey || evento.shiftKey || evento.altKey) {
      return;
    }

    evento.preventDefault();
    window.history.pushState(null, '', caminhoCadastro);
    definirCaminho(caminhoCadastro);
  }

  if (caminho === caminhoCadastro) {
    return <RegisterPage />;
  }

  return (
    <main>
      <h1>Gitbook</h1>
      <p>Plataforma de escrita colaborativa.</p>
      <a href={caminhoCadastro} onClick={abrirCadastro}>Criar conta</a>
    </main>
  );
}
