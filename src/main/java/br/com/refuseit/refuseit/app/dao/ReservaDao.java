package br.com.refuseit.refuseit.app.dao;

import br.com.refuseit.refuseit.app.entities.Reserva;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class ReservaDao {

   
    public boolean reservar(int idDoacao, int idInstituicao, BigDecimal quantidade, LocalDate hoje)
            throws SQLException {

        // Uma unica instrucao confere TUDO e baixa a quantidade. Se duas ONGs
        // reservarem ao mesmo tempo, so uma passa (a outra atualiza 0 linhas).
        String baixa = "UPDATE REFUSE_DOACAO "
                + "SET quantidade_disponivel = quantidade_disponivel - ?, "
                + "status = CASE WHEN quantidade_disponivel - ? = 0 THEN 'ESGOTADA' "
                + "ELSE 'PARCIALMENTE_RESERVADA' END "
                + "WHERE id_doacao = ? AND status IN ('DISPONIVEL', 'PARCIALMENTE_RESERVADA') "
                + "AND quantidade_disponivel >= ? AND data_limite_retirada >= ?";

        String insere = "INSERT INTO REFUSE_RESERVA "
                + "(id_doacao, id_instituicao, quantidade_solicitada, data_reserva, status) "
                + "VALUES (?, ?, ?, ?, 'APROVADA')";

        try (Connection con = UsuarioDao.abrirConexao()) {
            con.setAutoCommit(false);

            try {
                try (PreparedStatement ps = con.prepareStatement(baixa)) {
                    ps.setBigDecimal(1, quantidade);
                    ps.setBigDecimal(2, quantidade);
                    ps.setInt(3, idDoacao);
                    ps.setBigDecimal(4, quantidade);
                    ps.setDate(5, Date.valueOf(hoje));

                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(insere)) {
                    ps.setInt(1, idDoacao);
                    ps.setInt(2, idInstituicao);
                    ps.setBigDecimal(3, quantidade);
                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    ps.executeUpdate();
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public List<Reserva> listarDaInstituicao(int idInstituicao) throws SQLException {
        String sql = "SELECT r.id_reserva, r.id_doacao, r.quantidade_solicitada, r.data_reserva, "
                + "r.status, r.observacao, d.data_limite_retirada, "
                + "a.id_alimento, a.nome, a.categoria, a.descricao, a.unidade_medida, "
                + "u.nome AS nome_doador "
                + "FROM REFUSE_RESERVA r "
                + "JOIN REFUSE_DOACAO d ON d.id_doacao = r.id_doacao "
                + "JOIN REFUSE_ALIMENTO a ON a.id_alimento = d.id_alimento "
                + DoacaoDao.JOIN_NOME_DOADOR
                + "WHERE r.id_instituicao = ? ORDER BY r.data_reserva DESC";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInstituicao);

            try (ResultSet rs = ps.executeQuery()) {
                List<Reserva> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        }
    }

 
    public boolean cancelar(int idReserva, int idInstituicao) throws SQLException {
        String busca = "SELECT id_doacao, quantidade_solicitada FROM REFUSE_RESERVA "
                + "WHERE id_reserva = ? AND id_instituicao = ? AND status IN ('PENDENTE', 'APROVADA')";

        String cancela = "UPDATE REFUSE_RESERVA SET status = 'CANCELADA' "
                + "WHERE id_reserva = ? AND status IN ('PENDENTE', 'APROVADA')";

        // Se a doacao ja foi cancelada ou expirou, nao mexe nela
        String devolve = "UPDATE REFUSE_DOACAO "
                + "SET quantidade_disponivel = quantidade_disponivel + ?, "
                + "status = CASE WHEN quantidade_disponivel + ? >= quantidade_total "
                + "THEN 'DISPONIVEL' ELSE 'PARCIALMENTE_RESERVADA' END "
                + "WHERE id_doacao = ? AND status IN ('DISPONIVEL', 'PARCIALMENTE_RESERVADA', 'ESGOTADA')";

        try (Connection con = UsuarioDao.abrirConexao()) {
            con.setAutoCommit(false);

            try {
                int idDoacao = 0;
                BigDecimal quantidade = null;

                try (PreparedStatement ps = con.prepareStatement(busca)) {
                    ps.setInt(1, idReserva);
                    ps.setInt(2, idInstituicao);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                        idDoacao = rs.getInt(1);
                        quantidade = rs.getBigDecimal(2);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(cancela)) {
                    ps.setInt(1, idReserva);

                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(devolve)) {
                    ps.setBigDecimal(1, quantidade);
                    ps.setBigDecimal(2, quantidade);
                    ps.setInt(3, idDoacao);
                    ps.executeUpdate();
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private Reserva mapear(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setIdReserva(rs.getInt("id_reserva"));
        r.setIdDoacao(rs.getInt("id_doacao"));
        r.setAlimento(AlimentoDao.mapear(rs));
        r.setQuantidadeSolicitada(rs.getBigDecimal("quantidade_solicitada"));
        r.setDataReserva(rs.getTimestamp("data_reserva").toLocalDateTime());
        r.setStatus(rs.getString("status"));
        r.setObservacao(rs.getString("observacao"));
        r.setNomeDoador(rs.getString("nome_doador"));
        r.setDataLimiteRetirada(rs.getDate("data_limite_retirada").toLocalDate());
        return r;
    }
}