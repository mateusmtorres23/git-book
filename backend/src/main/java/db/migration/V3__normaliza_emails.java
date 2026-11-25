package db.migration;

import com.gitbook.gitbook.service.support.EmailUsuario;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class V3__normaliza_emails extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws SQLException {
        var conexao = context.getConnection();
        try (var lock = conexao.createStatement()) {
            lock.execute("LOCK TABLE usuario IN SHARE ROW EXCLUSIVE MODE");
        }

        Map<String, UUID> contasPorEmail = new HashMap<>();
        Map<UUID, String> alteracoes = new LinkedHashMap<>();
        try (var consulta = conexao.prepareStatement("SELECT id_usuario, email FROM usuario ORDER BY id_usuario");
             var contas = consulta.executeQuery()) {
            while (contas.next()) {
                UUID idUsuario = contas.getObject("id_usuario", UUID.class);
                String email = contas.getString("email");
                String normalizado = EmailUsuario.normalizar(email);
                UUID outraConta = contasPorEmail.putIfAbsent(normalizado, idUsuario);
                if (outraConta != null) {
                    throw new SQLException("A normalização de e-mail gera uma colisão entre as contas "
                            + outraConta + " e " + idUsuario + ".");
                }
                if (!email.equals(normalizado)) {
                    alteracoes.put(idUsuario, normalizado);
                }
            }
        }

        try (var atualizacao = conexao.prepareStatement("UPDATE usuario SET email = ? WHERE id_usuario = ?")) {
            for (var alteracao : alteracoes.entrySet()) {
                atualizacao.setString(1, alteracao.getValue());
                atualizacao.setObject(2, alteracao.getKey());
                atualizacao.addBatch();
            }
            atualizacao.executeBatch();
        }
    }
}
