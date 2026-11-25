import { createContext, useContext, useState } from 'react';
import type { ReactNode } from 'react';
import type { SessaoAutenticada } from './auth.api';

type EstadoAuth = {
  sessao: SessaoAutenticada | null;
  iniciarSessao: (sessao: SessaoAutenticada) => void;
};

const AuthContext = createContext<EstadoAuth | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sessao, definirSessao] = useState<SessaoAutenticada | null>(null);

  return (
    <AuthContext.Provider value={{ sessao, iniciarSessao: definirSessao }}>
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
