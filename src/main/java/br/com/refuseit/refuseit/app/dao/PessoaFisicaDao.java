package br.com.refuseit.refuseit.app.dao;

import br.com.refuseit.refuseit.app.api.Endereco;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.PessoaFisica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class PessoaFisicaDao {


    public int inserir(PessoaFisica pf) throws SQLException {
        Endereco end = pf.getEndereco();
        if (end == null) {
            throw new SQLException("O endereco da pessoa fisica nao foi preenchido.");
        }

        String sql = "INSERT INTO REFUSE_PESSOA_FISICA "
                + "(id_usuario, cpf, data_nascimento, endereco, cidade, estado, cep) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = UsuarioDao.abrirConexao()) {
            con.setAutoCommit(false);

            try {
                int idUsuario = UsuarioDao.inserirUsuario(
                        con,
                        pf.getNome(),
                        pf.getEmail(),
                        pf.getSenha(),
                        pf.getTelefone(),
                        "DOADOR");

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idUsuario);
                    ps.setString(2, UsuarioDao.somenteNumeros(pf.getCpf()));
                    ps.setString(3, pf.getAniversario());
                    ps.setString(4, UsuarioDao.montarEndereco(end));
                    ps.setString(5, UsuarioDao.limitar(end.getLocalidade(), 100));
                    ps.setString(6, end.getUf());
                    ps.setString(7, UsuarioDao.somenteNumeros(end.getCep()));
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
        String sql = "SELECT id_pessoa_fisica FROM REFUSE_PESSOA_FISICA WHERE id_usuario = ?";

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

    public Integer autenticar(String email, String senha) throws SQLException {
        return UsuarioDao.autenticarComPerfil("REFUSE_PESSOA_FISICA", email, senha);
    }

    public boolean cpfExiste(String cpf) throws SQLException {
        String sql = "SELECT COUNT(*) FROM REFUSE_PESSOA_FISICA WHERE cpf = ?";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, UsuarioDao.somenteNumeros(cpf));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    public boolean atualizar(int idUsuario, PessoaFisica pf)
            throws SQLException {

        if (pf.getEndereco() == null) {
            throw new SQLException("Endereco nao informado.");
        }

        String sql = "UPDATE REFUSE_PESSOA_FISICA SET "
                + "data_nascimento = ?, "
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
                                pf.getNome(),
                                pf.getEmail(),
                                pf.getSenha(),
                                pf.getTelefone()
                        );

                if (!usuarioAtualizado) {
                    throw new SQLException("Usuario nao encontrado.");
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    Endereco end = pf.getEndereco();

                    ps.setString(1, pf.getAniversario());
                    ps.setString(2, UsuarioDao.montarEndereco(end));
                    ps.setString(3, UsuarioDao.limitar(
                            end.getLocalidade(), 100));
                    ps.setString(4, end.getUf());
                    ps.setString(5, UsuarioDao.somenteNumeros(end.getCep()));
                    ps.setInt(6, idUsuario);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException(
                                "Pessoa fisica nao encontrada.");
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