package br.com.refuseit.refuseit.app.dao;

import br.com.refuseit.refuseit.app.api.Endereco;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.Empresa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class EmpresaDao {



    public int inserir(Empresa empresa, String categoria) throws SQLException {
        Endereco end = empresa.getEndereco();
        if (end == null) {
            throw new SQLException("O endereco da empresa nao foi preenchido.");
        }

        String sql = "INSERT INTO REFUSE_ESTABELECIMENTO "
                + "(id_usuario, nome_fantasia, razao_social, cnpj, categoria, endereco, cidade, estado, cep) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = UsuarioDao.abrirConexao()) {
            con.setAutoCommit(false);   // so grava de verdade no commit()

            try {
                int idUsuario = UsuarioDao.inserirUsuario(
                        con,
                        UsuarioDao.nomeExibicao(empresa),
                        empresa.getEmail(),
                        empresa.getSenha(),
                        empresa.getTelefone(),
                        "DOADOR");

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idUsuario);
                    ps.setString(2, UsuarioDao.limitar(UsuarioDao.nomeExibicao(empresa), 120));
                    ps.setString(3, UsuarioDao.limitar(empresa.getRazaoSocial(), 150));
                    ps.setString(4, UsuarioDao.somenteNumeros(empresa.getCnpj()));
                    ps.setString(5, categoria);
                    ps.setString(6, UsuarioDao.montarEndereco(end));
                    ps.setString(7, UsuarioDao.limitar(end.getLocalidade(), 100));
                    ps.setString(8, end.getUf());
                    ps.setString(9, UsuarioDao.somenteNumeros(end.getCep()));
                    ps.executeUpdate();
                }

                con.commit();
                return idUsuario;

            } catch (SQLException e) {
                con.rollback();   // desfaz o usuario se o estabelecimento falhar
                throw e;
            }
        }
    }

    /** Devolve o id_estabelecimento do usuario, ou null se ele nao for empresa. */
    public Integer buscarIdPorUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT id_estabelecimento FROM REFUSE_ESTABELECIMENTO WHERE id_usuario = ?";

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
        return UsuarioDao.autenticarComPerfil("REFUSE_ESTABELECIMENTO", email, senha);
    }

    public boolean cnpjExiste(String cnpj) throws SQLException {
        String sql = "SELECT COUNT(*) FROM REFUSE_ESTABELECIMENTO WHERE cnpj = ?";

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
            Empresa empresa,
            String categoria
    ) throws SQLException {

        if (empresa.getEndereco() == null) {
            throw new SQLException("Endereco nao informado.");
        }

        String sql = "UPDATE REFUSE_ESTABELECIMENTO SET "
                + "nome_fantasia = ?, "
                + "razao_social = ?, "
                + "categoria = ?, "
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
                                UsuarioDao.nomeExibicao(empresa),
                                empresa.getEmail(),
                                empresa.getSenha(),
                                empresa.getTelefone()
                        );

                if (!usuarioAtualizado) {
                    throw new SQLException("Usuario nao encontrado.");
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    Endereco end = empresa.getEndereco();

                    ps.setString(1, UsuarioDao.limitar(
                            empresa.getNomeFantasia(), 120));

                    ps.setString(2, UsuarioDao.limitar(
                            empresa.getRazaoSocial(), 150));

                    ps.setString(3, categoria);
                    ps.setString(4, UsuarioDao.montarEndereco(end));

                    ps.setString(5, UsuarioDao.limitar(
                            end.getLocalidade(), 100));

                    ps.setString(6, end.getUf());

                    ps.setString(7, UsuarioDao.somenteNumeros(
                            end.getCep()));

                    ps.setInt(8, idUsuario);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException(
                                "Empresa nao encontrada.");
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