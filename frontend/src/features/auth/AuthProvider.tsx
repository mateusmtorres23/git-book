import { createContext, useCallback, useContext, useRef, useState } from 'react';
import type { ReactNode } from 'react';
import { renovarSessao, sair } from './auth.api';
import type { ResultadoLogout, SessaoAutenticada } from './auth.api';

type VerificacaoSessao =
  | { tipo: 'verificando' }
  | { tipo: 'autenticado'; sessao: SessaoAutenticada }
  | { tipo: 'semSessao' }
  | { tipo: 'erro'; mensagem: string };

type EstadoAuth = {
  sessao: SessaoAutenticada | null;
  verificacao: VerificacaoSessao;
  iniciarSessao: (sessao: SessaoAutenticada) => void;
  recuperarSessao: () => Promise<void>;
  encerrarSessao: () => Promise<ResultadoLogout>;
};

const AuthContext = createContext<EstadoAuth | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [verificacao, definirVerificacao] = useState<VerificacaoSessao>({ tipo: 'verificando' });
  const sessaoAtual = useRef<SessaoAutenticada | null>(null);
  const versaoSessao = useRef(0);
  const renovacaoEmAndamento = useRef<Promise<void> | null>(null);
  const logoutEmAndamento = useRef<Promise<ResultadoLogout> | null>(null);

  const iniciarSessao = useCallback((sessao: SessaoAutenticada) => {
    versaoSessao.current += 1;
    sessaoAtual.current = sessao;
    definirVerificacao({ tipo: 'autenticado', sessao });
  }, []);

  const recuperarSessao = useCallback((): Promise<void> => {
    if (logoutEmAndamento.current) {
      return logoutEmAndamento.current.then(() => undefined);
    }

    const sessao = sessaoAtual.current;
    if (sessao && Date.parse(sessao.expiraEm) > Date.now()) {
      return Promise.resolve();
    }

    if (renovacaoEmAndamento.current) {
      return renovacaoEmAndamento.current;
    }

    const versao = versaoSessao.current;
    sessaoAtual.current = null;
    definirVerificacao({ tipo: 'verificando' });

    const renovacao = renovarSessao().then((resultado) => {
      if (versao !== versaoSessao.current) return;

      switch (resultado.tipo) {
        case 'autenticado':
          iniciarSessao(resultado.sessao);
          break;
        case 'semSessao':
          definirVerificacao({ tipo: 'semSessao' });
          break;
        case 'falhaComunicacao':
          definirVerificacao({ tipo: 'erro', mensagem: resultado.mensagem });
          break;
      }
    }).finally(() => {
      renovacaoEmAndamento.current = null;
    });

    renovacaoEmAndamento.current = renovacao;
    return renovacao;
  }, [iniciarSessao]);

  const encerrarSessao = useCallback((): Promise<ResultadoLogout> => {
    if (logoutEmAndamento.current) return logoutEmAndamento.current;

    const logout = (async () => {
      await renovacaoEmAndamento.current;
      const versao = versaoSessao.current;
      const resultado = await sair();

      if (resultado.tipo === 'encerrado' && versao === versaoSessao.current) {
        versaoSessao.current += 1;
        sessaoAtual.current = null;
        definirVerificacao({ tipo: 'semSessao' });
      }

      return resultado;
    })().finally(() => {
      logoutEmAndamento.current = null;
    });

    logoutEmAndamento.current = logout;
    return logout;
  }, []);

  const sessao = verificacao.tipo === 'autenticado' ? verificacao.sessao : null;

  return (
    <AuthContext.Provider value={{ sessao, verificacao, iniciarSessao, recuperarSessao, encerrarSessao }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): EstadoAuth {
  const auth = useContext(AuthContext);
  if (auth === null) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider.');
  }
  return auth;
}
