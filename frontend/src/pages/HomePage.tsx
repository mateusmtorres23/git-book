import { useState } from 'react';
import { useAuth } from '../features/auth/AuthProvider';

export function HomePage() {
  const { encerrarSessao } = useAuth();
  const [saindo, definirSaindo] = useState(false);
  const [erro, definirErro] = useState<string | null>(null);

  async function solicitarSaida() {
    definirSaindo(true);
    definirErro(null);
    try {
      const resultado = await encerrarSessao();
      if (resultado.tipo === 'falhaComunicacao') definirErro(resultado.mensagem);
    } finally {
      definirSaindo(false);
    }
  }

  return (
    <main>
      <h1>Gitbook</h1>
      <button type="button" onClick={solicitarSaida} disabled={saindo}>
        {saindo ? 'Saindo…' : 'Sair'}
      </button>
      {erro && <p role="alert">{erro}</p>}
    </main>
  );
}
