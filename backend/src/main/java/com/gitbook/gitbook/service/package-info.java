/**
 * Services de aplicação. O identificador do usuário recebido pelos métodos deve
 * vir da autenticação do futuro controller, e não de um campo livre do payload.
 *
 * <p>As gravações de versionamento mantêm o lock do livro durante a transação.
 * Services futuros de colaboradores e sugestões também precisam seguir esse
 * protocolo para serializar suas alterações com decisões de Pull Requests.
 * A criação do livro e a chamada a BranchService.criarMain devem compartilhar
 * a transação do futuro service de livros.
 *
 * <p>CommitPaiService oferece consultas; a escrita da ancestralidade faz parte
 * da criação do commit. A reconstrução segue o primeiro pai, e o delta de um
 * merge é calculado em relação a ele. Em destino vazio, a origem é o único pai
 * do novo commit da main. O merge textual trabalha por capítulo completo.
 * VersionamentoService centraliza a reconstrução, a comparação e a gravação
 * compartilhadas pelas operações de commits e Pull Requests.
 */
package com.gitbook.gitbook.service;
