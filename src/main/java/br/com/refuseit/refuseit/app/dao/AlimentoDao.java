package br.com.refuseit.refuseit.app.dao;

import br.com.refuseit.refuseit.app.entities.Alimento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlimentoDao {
    public List<Alimento> listar() throws SQLException {
        String sql = "SELECT id_alimento, nome, categoria, descricao, unidade_medida "
                + "FROM REFUSE_ALIMENTO ORDER BY nome";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Alimento> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        }
    }

    /** Procura pelo nome (sem diferenciar maiuscula de minuscula). Devolve null se nao achar. */
    public Alimento buscarPorNome(String nome) throws SQLException {
        String sql = "SELECT id_alimento, nome, categoria, descricao, unidade_medida "
                + "FROM REFUSE_ALIMENTO WHERE LOWER(nome) = LOWER(?)";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nome == null ? null : nome.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /** Insere um alimento novo e devolve o id gerado. */
    public int inserir(Alimento alimento) throws SQLException {
        String sql = "INSERT INTO REFUSE_ALIMENTO (nome, categoria, descricao, unidade_medida) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_ALIMENTO"})) {
            ps.setString(1, UsuarioDao.limitar(alimento.getNome(), 100));
            ps.setString(2, UsuarioDao.limitar(alimento.getCategoria(), 40));
            ps.setString(3, UsuarioDao.limitar(alimento.getDescricao(), 300));
            ps.setString(4, alimento.getUnidadeMedida());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Nao foi possivel obter o ID gerado do alimento.");
    }

    /** Le um alimento da linha atual (as colunas id_alimento, nome, categoria, descricao, unidade_medida). */
    static Alimento mapear(ResultSet rs) throws SQLException {
        Alimento a = new Alimento();
        a.setIdAlimento(rs.getInt("id_alimento"));
        a.setNome(rs.getString("nome"));
        a.setCategoria(rs.getString("categoria"));
        a.setDescricao(rs.getString("descricao"));
        a.setUnidadeMedida(rs.getString("unidade_medida"));
        return a;
    }
}