import { useRef, useState } from 'react';
import type { FormEvent } from 'react';
import { cadastrar } from '../auth.api';
import type { DadosCadastro } from '../auth.api';
import { AuthLayout } from '../components/AuthLayout';

type CampoCadastro = keyof DadosCadastro;
type ErrosCadastro = Partial<Record<CampoCadastro, string>>;

function validarCadastro(dados: DadosCadastro, formatoEmailInvalido: boolean): ErrosCadastro {
  const erros: ErrosCadastro = {};

  if (!dados.nomeUsuario.trim()) {
    erros.nomeUsuario = 'Informe seu nome.';
  } else if (dados.nomeUsuario.length > 100) {
    erros.nomeUsuario = 'O nome deve ter até 100 caracteres.';
  }

  if (!dados.email.trim()) {
    erros.email = 'Informe seu e-mail.';
  } else if (dados.email.length > 100) {
    erros.email = 'O e-mail deve ter até 100 caracteres.';
  } else if (formatoEmailInvalido) {
    erros.email = 'Informe um e-mail válido.';
  }

  if (!dados.senha.trim()) {
    erros.senha = 'Informe sua senha.';
  } else if (dados.senha.length < 8) {
    erros.senha = 'A senha deve ter pelo menos oito caracteres.';
  } else if (new TextEncoder().encode(dados.senha).length > 72) {
    erros.senha = 'A senha deve ter até 72 bytes em UTF-8.';
  }

  return erros;
}

const campos = [
  { nome: 'nomeUsuario', rotulo: 'Nome', tipo: 'text', autocomplete: 'name' },
  { nome: 'email', rotulo: 'E-mail', tipo: 'email', autocomplete: 'email' },
  { nome: 'senha', rotulo: 'Senha', tipo: 'password', autocomplete: 'new-password' },
] as const;

export function RegisterPage() {
  const [erros, definirErros] = useState<ErrosCadastro>({});
  const [mensagens, definirMensagens] = useState<string[]>([]);
  const [enviando, definirEnviando] = useState(false);
  const [contaCriada, definirContaCriada] = useState(false);
  const envioEmAndamento = useRef(false);

  async function enviarCadastro(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    if (envioEmAndamento.current) return;

    const formulario = evento.currentTarget;
    const nome = formulario.querySelector<HTMLInputElement>('#cadastro-nomeUsuario')!;
    const email = formulario.querySelector<HTMLInputElement>('#cadastro-email')!;
    const senha = formulario.querySelector<HTMLInputElement>('#cadastro-senha')!;
    const dados = { nomeUsuario: nome.value, email: email.value, senha: senha.value };
    const errosLocais = validarCadastro(dados, email.validity.typeMismatch);
    definirErros(errosLocais);
    definirMensagens([]);

    const primeiroInvalido = campos.find(({ nome }) => errosLocais[nome]);
    if (primeiroInvalido) {
      formulario.querySelector<HTMLInputElement>(`#cadastro-${primeiroInvalido.nome}`)?.focus();
      return;
    }

    envioEmAndamento.current = true;
    definirEnviando(true);
    try {
      const resultado = await cadastrar(dados);
      switch (resultado.tipo) {
        case 'criado':
          senha.value = '';
          definirContaCriada(true);
          break;
        case 'emailDuplicado':
          definirErros({ email: resultado.mensagem });
          email.focus();
          break;
        case 'dadosInvalidos':
          definirMensagens(resultado.mensagens);
          break;
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
    <AuthLayout titulo="Criar conta">
      <div role="status">
        {contaCriada && <p className="auth-success">Conta criada com sucesso.</p>}
      </div>
      {!contaCriada && (
        <form className="auth-form" onSubmit={enviarCadastro} noValidate>
          {campos.map(({ nome, rotulo, tipo, autocomplete }) => (
            <div className="auth-field" key={nome}>
              <label htmlFor={`cadastro-${nome}`}>{rotulo}</label>
              <input
                id={`cadastro-${nome}`}
                name={nome}
                type={tipo}
                autoComplete={autocomplete}
                required
                readOnly={enviando}
                aria-invalid={Boolean(erros[nome])}
                aria-describedby={erros[nome] ? `cadastro-${nome}-erro` : undefined}
                onChange={() => definirErros((atuais) => ({ ...atuais, [nome]: undefined }))}
              />
              {erros[nome] && <p className="auth-error" id={`cadastro-${nome}-erro`} role="alert">{erros[nome]}</p>}
            </div>
          ))}
          {mensagens.length > 0 && (
            <ul className="auth-error" role="alert">
              {mensagens.map((mensagem, indice) => <li key={indice}>{mensagem}</li>)}
            </ul>
          )}
          <button type="submit" disabled={enviando}>{enviando ? 'Criando conta…' : 'Criar conta'}</button>
        </form>
      )}
    </AuthLayout>
  );
}
