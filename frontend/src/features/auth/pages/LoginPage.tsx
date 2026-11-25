import { useRef, useState } from 'react';
import type { FormEvent, MouseEventHandler } from 'react';
import { entrar } from '../auth.api';
import type { DadosLogin } from '../auth.api';
import { useAuth } from '../AuthProvider';
import { AuthLayout } from '../components/AuthLayout';

type ErrosLogin = Partial<Record<keyof DadosLogin, string>>;

function validarLogin({ email, senha }: DadosLogin, formatoEmailInvalido: boolean): ErrosLogin {
  const erros: ErrosLogin = {};

  if (!email.trim()) {
    erros.email = 'Informe seu e-mail.';
  } else if (email.length > 100) {
    erros.email = 'O e-mail deve ter até 100 caracteres.';
  } else if (formatoEmailInvalido) {
    erros.email = 'Informe um e-mail válido.';
  }

  if (!senha.trim()) {
    erros.senha = 'Informe sua senha.';
  } else if (new TextEncoder().encode(senha).length > 72) {
    erros.senha = 'A senha deve ter até 72 bytes em UTF-8.';
  }

  return erros;
}

const campos = [
  { nome: 'email', rotulo: 'E-mail', tipo: 'email', autocomplete: 'username' },
  { nome: 'senha', rotulo: 'Senha', tipo: 'password', autocomplete: 'current-password' },
] as const;

type LoginPageProps = {
  emailInicial?: string;
  contaCriada?: boolean;
  aoAbrirCadastro: MouseEventHandler<HTMLAnchorElement>;
};

export function LoginPage({ emailInicial, contaCriada, aoAbrirCadastro }: LoginPageProps) {
  const { iniciarSessao } = useAuth();
  const [erros, definirErros] = useState<ErrosLogin>({});
  const [mensagens, definirMensagens] = useState<string[]>([]);
  const [enviando, definirEnviando] = useState(false);
  const [loginConfirmado, definirLoginConfirmado] = useState(false);
  const envioEmAndamento = useRef(false);

  async function enviarLogin(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    if (envioEmAndamento.current) return;

    const formulario = evento.currentTarget;
    const email = formulario.querySelector<HTMLInputElement>('#login-email')!;
    const senha = formulario.querySelector<HTMLInputElement>('#login-senha')!;
    const dados = { email: email.value, senha: senha.value };
    const errosLocais = validarLogin(dados, email.validity.typeMismatch);
    definirErros(errosLocais);
    definirMensagens([]);

    if (errosLocais.email || errosLocais.senha) {
      (errosLocais.email ? email : senha).focus();
      return;
    }

    envioEmAndamento.current = true;
    definirEnviando(true);
    try {
      const resultado = await entrar(dados);
      switch (resultado.tipo) {
        case 'autenticado':
          iniciarSessao(resultado.sessao);
          senha.value = '';
          definirLoginConfirmado(true);
          break;
        case 'dadosInvalidos':
          definirMensagens(resultado.mensagens);
          break;
        case 'credenciaisInvalidas':
        case 'falhaComunicacao':
          definirMensagens([resultado.mensagem]);
          break;
      }
    } finally {
      envioEmAndamento.current = false;
      definirEnviando(false);
    }
  }

  return (
    <AuthLayout titulo="Entrar">
      <div role="status">
        {loginConfirmado && <p className="auth-success">Login realizado com sucesso.</p>}
        {!loginConfirmado && contaCriada && <p className="auth-success">Conta criada com sucesso.</p>}
      </div>
      {!loginConfirmado && (
        <form className="auth-form" onSubmit={enviarLogin} noValidate>
          {campos.map(({ nome, rotulo, tipo, autocomplete }) => (
            <div className="auth-field" key={nome}>
              <label htmlFor={`login-${nome}`}>{rotulo}</label>
              <input
                id={`login-${nome}`}
                name={nome}
                type={tipo}
                autoComplete={autocomplete}
                defaultValue={nome === 'email' ? emailInicial : undefined}
                required
                readOnly={enviando}
                aria-invalid={Boolean(erros[nome])}
                aria-describedby={erros[nome] ? `login-${nome}-erro` : undefined}
                onChange={() => definirErros((atuais) => ({ ...atuais, [nome]: undefined }))}
              />
              {erros[nome] && <p className="auth-error" id={`login-${nome}-erro`} role="alert">{erros[nome]}</p>}
            </div>
          ))}
          {mensagens.length > 0 && (
            <ul className="auth-error" role="alert">
              {mensagens.map((mensagem, indice) => <li key={indice}>{mensagem}</li>)}
            </ul>
          )}
          <button type="submit" disabled={enviando}>{enviando ? 'Entrando…' : 'Entrar'}</button>
          <a href="/cadastro" onClick={aoAbrirCadastro}>Criar conta</a>
        </form>
      )}
    </AuthLayout>
  );
}
