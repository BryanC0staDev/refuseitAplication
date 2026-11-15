package br.com.refuseit.refuseit.app.dao;

import br.com.refuseit.refuseit.app.entities.Venda;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class VendaDao {

    private static final String SELECT_VENDA =
            "SELECT v.id_venda, v.id_estabelecimento, v.quantidade_total, v.quantidade_disponivel, "
                    + "v.preco_unitario, v.data_publicacao, v.data_validade, v.condicao_armazenamento, v.status, "
                    + "a.id_alimento, a.nome, a.categoria, a.descricao, a.unidade_medida, "
                    + "e.nome_fantasia AS nome_estabelecimento "
                    + "FROM REFUSE_VENDA v "
                    + "JOIN REFUSE_ALIMENTO a ON a.id_alimento = v.id_alimento "
                    + "JOIN REFUSE_ESTABELECIMENTO e ON e.id_estabelecimento = v.id_estabelecimento ";

    public int inserir(Venda v) throws SQLException {
        // status nao entra: o banco usa DEFAULT 'DISPONIVEL'
        String sql = "INSERT INTO REFUSE_VENDA "
                + "(id_estabelecimento, id_alimento, quantidade_total, quantidade_disponivel, "
                + "preco_unitario, data_publicacao, data_validade, condicao_armazenamento) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_VENDA"})) {
            ps.setInt(1, v.getIdEstabelecimento());
            ps.setInt(2, v.getAlimento().getIdAlimento());
            ps.setBigDecimal(3, v.getQuantidadeTotal());
            ps.setBigDecimal(4, v.getQuantidadeTotal());   // ao publicar, tudo esta disponivel
            ps.setBigDecimal(5, v.getPrecoUnitario());
            ps.setTimestamp(6, Timestamp.valueOf(v.getDataPublicacao()));
            ps.setDate(7, Date.valueOf(v.getDataValidade()));
            ps.setString(8, UsuarioDao.limitar(v.getCondicaoArmazenamento(), 200));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Nao foi possivel obter o ID gerado do anuncio.");
    }

    public List<Venda> listarDoVendedor(int idEstabelecimento) throws SQLException {
        String sql = SELECT_VENDA + "WHERE v.id_estabelecimento = ? ORDER BY v.data_publicacao DESC";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEstabelecimento);
            return executar(ps);
        }
    }


    public List<Venda> listarDisponiveis(LocalDate hoje) throws SQLException {
        String sql = SELECT_VENDA + "WHERE v.status = 'DISPONIVEL' AND v.quantidade_disponivel > 0 "
                + "AND v.data_validade >= ? ORDER BY v.data_validade";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(hoje));
            return executar(ps);
        }
    }


    public boolean cancelar(int idVenda, int idEstabelecimento) throws SQLException {
        String sql = "UPDATE REFUSE_VENDA SET status = 'CANCELADA' "
                + "WHERE id_venda = ? AND id_estabelecimento = ? AND status = 'DISPONIVEL'";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenda);
            ps.setInt(2, idEstabelecimento);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Venda> executar(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            List<Venda> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        }
    }

    private Venda mapear(ResultSet rs) throws SQLException {
        Venda v = new Venda();
        v.setIdVenda(rs.getInt("id_venda"));
        v.setIdEstabelecimento(rs.getInt("id_estabelecimento"));
        v.setAlimento(AlimentoDao.mapear(rs));
        v.setQuantidadeTotal(rs.getBigDecimal("quantidade_total"));
        v.setQuantidadeDisponivel(rs.getBigDecimal("quantidade_disponivel"));
        v.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
        v.setDataPublicacao(rs.getTimestamp("data_publicacao").toLocalDateTime());
        v.setDataValidade(rs.getDate("data_validade").toLocalDate());
        v.setCondicaoArmazenamento(rs.getString("condicao_armazenamento"));
        v.setStatus(rs.getString("status"));
        v.setNomeEstabelecimento(rs.getString("nome_estabelecimento"));
        return v;
    }
}