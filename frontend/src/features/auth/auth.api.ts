export type DadosCadastro = {
  nomeUsuario: string;
  email: string;
  senha: string;
};

export type ResultadoCadastro =
  | { tipo: 'criado' }
  | { tipo: 'emailDuplicado'; mensagem: string }
  | { tipo: 'dadosInvalidos'; mensagens: string[] }
  | { tipo: 'falhaComunicacao'; mensagem: string };

export type DadosLogin = {
  email: string;
  senha: string;
};

export type SessaoAutenticada = {
  token: string;
  expiraEm: string;
};

export type ResultadoLogin =
  | { tipo: 'autenticado'; sessao: SessaoAutenticada }
  | { tipo: 'credenciaisInvalidas'; mensagem: string }
  | { tipo: 'dadosInvalidos'; mensagens: string[] }
  | { tipo: 'falhaComunicacao'; mensagem: string };

export type ResultadoRenovacao =
  | { tipo: 'autenticado'; sessao: SessaoAutenticada }
  | { tipo: 'semSessao' }
  | { tipo: 'falhaComunicacao'; mensagem: string };

export type ResultadoLogout =
  | { tipo: 'encerrado' }
  | { tipo: 'falhaComunicacao'; mensagem: string };

async function lerErrosDeValidacao(resposta: Response): Promise<string[]> {
  const problema: unknown = await resposta.json().catch(() => null);

  if (problema && typeof problema === 'object' && 'erros' in problema && Array.isArray(problema.erros)) {
    const mensagens = problema.erros.filter(
      (erro): erro is string => typeof erro === 'string' && erro.trim().length > 0,
    );
    if (mensagens.length > 0) {
      return mensagens;
    }
  }

  return ['Confira os dados informados e tente novamente.'];
}

export async function cadastrar(dados: DadosCadastro): Promise<ResultadoCadastro> {
  const resposta = await fetch('/api/users', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dados),
  }).catch(() => null);

  switch (resposta?.status) {
    case 201:
      return { tipo: 'criado' };
    case 409:
      return { tipo: 'emailDuplicado', mensagem: 'O e-mail já está cadastrado.' };
    case 400:
      return { tipo: 'dadosInvalidos', mensagens: await lerErrosDeValidacao(resposta) };
    default:
      return { tipo: 'falhaComunicacao', mensagem: 'Não foi possível concluir o cadastro. Tente novamente.' };
  }
}

function textoValido(valor: unknown): valor is string {
  return typeof valor === 'string' && valor.trim().length > 0;
}

async function obterCsrf(): Promise<{ headerName: string; token: string }> {
  const resposta = await fetch('/api/auth/csrf', { credentials: 'same-origin' });
  if (!resposta.ok) {
    throw new Error('Falha ao obter o token CSRF.');
  }

  const dados: unknown = await resposta.json();
  if (dados && typeof dados === 'object' && 'headerName' in dados && 'token' in dados
    && textoValido(dados.headerName) && textoValido(dados.token)) {
    return { headerName: dados.headerName, token: dados.token };
  }

  throw new Error('Resposta CSRF inválida.');
}

async function lerSessao(resposta: Response): Promise<SessaoAutenticada | null> {
  const sessao: unknown = await resposta.json().catch(() => null);
  if (sessao && typeof sessao === 'object' && 'token' in sessao && 'expiraEm' in sessao
    && textoValido(sessao.token) && textoValido(sessao.expiraEm)
    && Number.isFinite(Date.parse(sessao.expiraEm))) {
    return { token: sessao.token, expiraEm: sessao.expiraEm };
  }

  return null;
}

async function enviarOperacaoDeSessao(caminho: '/api/auth/refresh' | '/api/auth/logout') {
  const csrf = await obterCsrf();
  return fetch(caminho, {
    method: 'POST',
    credentials: 'same-origin',
    headers: { [csrf.headerName]: csrf.token },
  });
}

export async function entrar(dados: DadosLogin): Promise<ResultadoLogin> {
  const falha: ResultadoLogin = {
    tipo: 'falhaComunicacao',
    mensagem: 'Não foi possível concluir o login. Tente novamente.',
  };

  try {
    const csrf = await obterCsrf();
    const resposta = await fetch('/api/auth/login', {
      method: 'POST',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
      body: JSON.stringify(dados),
    });

    switch (resposta.status) {
      case 200: {
        const sessao = await lerSessao(resposta);
        return sessao ? { tipo: 'autenticado', sessao } : falha;
      }
      case 401:
        return { tipo: 'credenciaisInvalidas', mensagem: 'E-mail ou senha inválidos.' };
      case 400:
        return { tipo: 'dadosInvalidos', mensagens: await lerErrosDeValidacao(resposta) };
      default:
        return falha;
    }
  } catch {
    return falha;
  }
}

export async function renovarSessao(): Promise<ResultadoRenovacao> {
  const resposta = await enviarOperacaoDeSessao('/api/auth/refresh').catch(() => null);
  if (resposta?.status === 401) {
    return { tipo: 'semSessao' };
  }

  if (resposta?.status === 200) {
    const sessao = await lerSessao(resposta);
    if (sessao) {
      return { tipo: 'autenticado', sessao };
    }
  }

  return { tipo: 'falhaComunicacao', mensagem: 'Não foi possível recuperar a sessão. Tente novamente.' };
}

export async function sair(): Promise<ResultadoLogout> {
  const resposta = await enviarOperacaoDeSessao('/api/auth/logout').catch(() => null);
  if (resposta?.status === 204) {
    return { tipo: 'encerrado' };
  }

  return { tipo: 'falhaComunicacao', mensagem: 'Não foi possível sair. Tente novamente.' };
}
