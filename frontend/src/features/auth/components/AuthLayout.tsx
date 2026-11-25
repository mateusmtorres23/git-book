import type { ReactNode } from 'react';

type AuthLayoutProps = {
  titulo: string;
  children: ReactNode;
};

export function AuthLayout({ titulo, children }: AuthLayoutProps) {
  return (
    <main className="auth-layout">
      <section className="auth-card" aria-labelledby="auth-titulo">
        <p className="auth-brand">Gitbook</p>
        <h1 id="auth-titulo">{titulo}</h1>
        {children}
      </section>
    </main>
  );
}
