package br.com.refuseit.refuseit.app.dao;

import br.com.refuseit.refuseit.app.api.Endereco;
import br.com.refuseit.refuseit.app.conexoes.ConnectionFactory;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.PessoaJuridica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class UsuarioDao {

    // ---------- Usados pelas outras DAOs ----------


    static Connection abrirConexao() throws SQLException {
        Connection con = new ConnectionFactory().cn();
        if (con == null) {
            throw new SQLException("Nao foi possivel conectar ao banco. "
                    + "Verifique DB_CONNECTION, DB_USER e DB_PASSWORD no .env");
        }
        return con;
    }


    static int inserirUsuario(Connection con, String nome, String email, String senha,
                              String telefone, String tipoUsuario) throws SQLException {

        String sql = "INSERT INTO REFUSE_USUARIO "
                + "(nome, email, senha, telefone, tipo_usuario) "
                + "VALUES (?, ?, ?, ?, ?)";

        // O 2o parametro pede ao Oracle para devolver o ID gerado
        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_USUARIO"})) {
            ps.setString(1, limitar(nome, 120));
            ps.setString(2, email == null ? null : email.trim().toLowerCase());
            ps.setString(3, senha);
            ps.setString(4, telefone);
            ps.setString(5, tipoUsuario);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Nao foi possivel obter o ID gerado do usuario.");
    }

    static String somenteNumeros(String texto) {
        return texto == null ? null : texto.replaceAll("\\D", "");
    }

    static String limitar(String texto, int max) {
        if (texto == null) {
            return null;
        }
        return texto.length() > max ? texto.substring(0, max) : texto;
    }


    static String nomeExibicao(PessoaJuridica pj) {
        String fantasia = pj.getNomeFantasia();
        if (fantasia == null || fantasia.isBlank()) {
            return pj.getRazaoSocial();
        }
        return fantasia;
    }


    static String montarEndereco(Endereco e) {
        String rua = aparar(e.getLogradouro());
        String numero = aparar(e.getNumero());
        String bairro = aparar(e.getBairro());

        StringBuilder sb = new StringBuilder();
        if (rua != null) {
            sb.append(rua);
        }
        if (numero != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(numero);
        }
        if (bairro != null) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(bairro);
        }
        return sb.length() == 0 ? null : limitar(sb.toString(), 250);
    }

    private static String aparar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    // ---------- Consultas ----------

    public boolean emailExiste(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM REFUSE_USUARIO WHERE email = ?";

        try (Connection con = abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email == null ? null : email.trim().toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }


    public Integer autenticar(String email, String senha) throws SQLException {
        String sql = "SELECT id_usuario FROM REFUSE_USUARIO "
                + "WHERE email = ? AND senha = ? AND status = 'ATIVO'";

        try (Connection con = abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email == null ? null : email.trim().toLowerCase());
            ps.setString(2, senha);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return null;
    }

    static Integer autenticarComPerfil(String tabelaPerfil, String email, String senha) throws SQLException {
        String sql = "SELECT u.id_usuario FROM REFUSE_USUARIO u "
                + "JOIN " + tabelaPerfil + " p ON p.id_usuario = u.id_usuario "
                + "WHERE u.email = ? AND u.senha = ? AND u.status = 'ATIVO'";

        try (Connection con = abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email == null ? null : email.trim().toLowerCase());
            ps.setString(2, senha);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return null;
    }

    public String buscarNome(int idUsuario) throws SQLException {
        String sql = "SELECT nome FROM REFUSE_USUARIO WHERE id_usuario = ?";

        try (Connection con = abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    public boolean atualizarDadosBasicos(
            Connection con,
            int idUsuario,
            String nome,
            String email,
            String senha,
            String telefone
    ) throws SQLException {

        String sql;

        if (senha == null || senha.isBlank()) {
            sql = "UPDATE REFUSE_USUARIO SET "
                    + "nome = ?, email = ?, telefone = ? "
                    + "WHERE id_usuario = ?";
        } else {
            sql = "UPDATE REFUSE_USUARIO SET "
                    + "nome = ?, email = ?, senha = ?, telefone = ? "
                    + "WHERE id_usuario = ?";
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, limitar(nome, 120));
            ps.setString(2, email.trim().toLowerCase());

            if (senha == null || senha.isBlank()) {
                ps.setString(3, telefone);
                ps.setInt(4, idUsuario);
            } else {
                ps.setString(3, senha);
                ps.setString(4, telefone);
                ps.setInt(5, idUsuario);
            }

            return ps.executeUpdate() > 0;
        }
    }


    /**
     * Consulta os dados do usuario logado, incluindo os dados especificos
     * do seu perfil. A senha nunca e retornada.
     */
    public String visualizarDados(int idUsuario, String tipoUsuario) throws SQLException {
        String sql;
        if ("PESSOA_FISICA".equals(tipoUsuario)) {
            sql = "SELECT u.nome, u.email, u.telefone, u.status, "
                    + "p.cpf, p.data_nascimento, p.endereco, p.cidade, p.estado, p.cep "
                    + "FROM REFUSE_USUARIO u "
                    + "JOIN REFUSE_PESSOA_FISICA p ON p.id_usuario = u.id_usuario "
                    + "WHERE u.id_usuario = ?";
        } else if ("EMPRESA".equals(tipoUsuario)) {
            sql = "SELECT u.nome, u.email, u.telefone, u.status, "
                    + "e.nome_fantasia, e.razao_social, e.cnpj, e.categoria, "
                    + "e.endereco, e.cidade, e.estado, e.cep "
                    + "FROM REFUSE_USUARIO u "
                    + "JOIN REFUSE_ESTABELECIMENTO e ON e.id_usuario = u.id_usuario "
                    + "WHERE u.id_usuario = ?";
        } else if ("ONG".equals(tipoUsuario)) {
            sql = "SELECT u.nome, u.email, u.telefone, u.status, "
                    + "i.nome, i.cnpj, i.tipo_instituicao, "
                    + "i.endereco, i.cidade, i.estado, i.cep "
                    + "FROM REFUSE_USUARIO u "
                    + "JOIN REFUSE_INSTITUICAO i ON i.id_usuario = u.id_usuario "
                    + "WHERE u.id_usuario = ?";
        } else {
            throw new SQLException("Tipo de usuario nao reconhecido.");
        }

        try (Connection con = abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Nao foram encontrados dados para este usuario.");
                }

                StringBuilder dados = new StringBuilder();
                dados.append("=== MEUS DADOS ===\n")
                        .append("Nome: ").append(valor(rs.getString(1))).append("\n")
                        .append("E-mail: ").append(valor(rs.getString(2))).append("\n")
                        .append("Telefone: ").append(valor(rs.getString(3))).append("\n")
                        .append("Status: ").append(valor(rs.getString(4))).append("\n");

                if ("PESSOA_FISICA".equals(tipoUsuario)) {
                    dados.append("\n=== PESSOA FISICA ===\n")
                            .append("CPF: ").append(valor(rs.getString(5))).append("\n")
                            .append("Data de nascimento: ").append(valor(rs.getString(6))).append("\n");
                    adicionarEndereco(dados, rs.getString(7), rs.getString(8),
                            rs.getString(9), rs.getString(10));
                } else if ("EMPRESA".equals(tipoUsuario)) {
                    dados.append("\n=== EMPRESA ===\n")
                            .append("Nome fantasia: ").append(valor(rs.getString(5))).append("\n")
                            .append("Razao social: ").append(valor(rs.getString(6))).append("\n")
                            .append("CNPJ: ").append(valor(rs.getString(7))).append("\n")
                            .append("Categoria: ").append(valor(rs.getString(8))).append("\n");
                    adicionarEndereco(dados, rs.getString(9), rs.getString(10),
                            rs.getString(11), rs.getString(12));
                } else {
                    dados.append("\n=== ONG ===\n")
                            .append("Nome: ").append(valor(rs.getString(5))).append("\n")
                            .append("CNPJ: ").append(valor(rs.getString(6))).append("\n")
                            .append("Tipo de instituicao: ").append(valor(rs.getString(7))).append("\n");
                    adicionarEndereco(dados, rs.getString(8), rs.getString(9),
                            rs.getString(10), rs.getString(11));
                }
                return dados.toString();
            }
        }
    }

    private static String valor(String texto) {
        return texto == null || texto.isBlank() ? "Nao informado" : texto;
    }

    private static void adicionarEndereco(StringBuilder dados, String endereco,
                                          String cidade, String estado, String cep) {
        dados.append("\n=== ENDERECO ===\n")
                .append("Endereco: ").append(valor(endereco)).append("\n")
                .append("Cidade: ").append(valor(cidade)).append("\n")
                .append("Estado: ").append(valor(estado)).append("\n")
                .append("CEP: ").append(valor(cep)).append("\n");
    }

    public boolean desativar(int idUsuario) throws SQLException {
        String sql = "UPDATE REFUSE_USUARIO SET status = 'INATIVO' WHERE id_usuario = ?";

        try (Connection con = abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }
}