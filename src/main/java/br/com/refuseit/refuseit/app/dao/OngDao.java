package br.com.refuseit.refuseit.app.dao;

import br.com.refuseit.refuseit.app.api.Endereco;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.Ong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class OngDao {

    public int inserir(Ong ong, String tipoInstituicao) throws SQLException {
        Endereco end = ong.getEndereco();
        if (end == null) {
            throw new SQLException("O endereco da ONG nao foi preenchido.");
        }

        String sql = "INSERT INTO REFUSE_INSTITUICAO "
                + "(id_usuario, nome, cnpj, tipo_instituicao, endereco, cidade, estado, cep) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = UsuarioDao.abrirConexao()) {
            con.setAutoCommit(false);

            try {
                int idUsuario = UsuarioDao.inserirUsuario(
                        con,
                        UsuarioDao.nomeExibicao(ong),
                        ong.getEmail(),
                        ong.getSenha(),
                        ong.getTelefone(),
                        "INSTITUICAO");

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idUsuario);
                    ps.setString(2, UsuarioDao.limitar(UsuarioDao.nomeExibicao(ong), 120));
                    ps.setString(3, UsuarioDao.somenteNumeros(ong.getCnpj()));
                    ps.setString(4, tipoInstituicao);
                    ps.setString(5, UsuarioDao.montarEndereco(end));
                    ps.setString(6, UsuarioDao.limitar(end.getLocalidade(), 100));
                    ps.setString(7, end.getUf());
                    ps.setString(8, UsuarioDao.somenteNumeros(end.getCep()));
                    ps.executeUpdate();
                }

                con.commit();
                return idUsuario;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public Integer buscarIdPorUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT id_instituicao FROM REFUSE_INSTITUICAO WHERE id_usuario = ?";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return null;
    }

    /** Login como ONG: devolve o id_usuario ou null. */
    public Integer autenticar(String email, String senha) throws SQLException {
        return UsuarioDao.autenticarComPerfil("REFUSE_INSTITUICAO", email, senha);
    }

    public boolean cnpjExiste(String cnpj) throws SQLException {
        String sql = "SELECT COUNT(*) FROM REFUSE_INSTITUICAO WHERE cnpj = ?";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, UsuarioDao.somenteNumeros(cnpj));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    public boolean atualizar(
            int idUsuario,
            Ong ong,
            String tipoInstituicao
    ) throws SQLException {

        if (ong.getEndereco() == null) {
            throw new SQLException("Endereco nao informado.");
        }

        String sql = "UPDATE REFUSE_INSTITUICAO SET "
                + "nome = ?, "
                + "tipo_instituicao = ?, "
                + "endereco = ?, cidade = ?, estado = ?, cep = ? "
                + "WHERE id_usuario = ?";

        try (Connection con = UsuarioDao.abrirConexao()) {

            con.setAutoCommit(false);

            try {

                UsuarioDao usuarioDao = new UsuarioDao();

                boolean usuarioAtualizado =
                        usuarioDao.atualizarDadosBasicos(
                                con,
                                idUsuario,
                                UsuarioDao.nomeExibicao(ong),
                                ong.getEmail(),
                                ong.getSenha(),
                                ong.getTelefone()
                        );

                if (!usuarioAtualizado) {
                    throw new SQLException("Usuario nao encontrado.");
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    Endereco end = ong.getEndereco();

                    ps.setString(1, UsuarioDao.limitar(
                            UsuarioDao.nomeExibicao(ong), 120));

                    ps.setString(2, tipoInstituicao);

                    ps.setString(3, UsuarioDao.montarEndereco(end));

                    ps.setString(4, UsuarioDao.limitar(
                            end.getLocalidade(), 100));

                    ps.setString(5, end.getUf());

                    ps.setString(6, UsuarioDao.somenteNumeros(
                            end.getCep()));

                    ps.setInt(7, idUsuario);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException(
                                "Instituicao nao encontrada.");
                    }
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }
}