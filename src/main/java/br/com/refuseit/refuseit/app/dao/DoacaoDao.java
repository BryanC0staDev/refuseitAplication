package br.com.refuseit.refuseit.app.dao;

import br.com.refuseit.refuseit.app.entities.Alimento;
import br.com.refuseit.refuseit.app.entities.Doacao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class DoacaoDao {


    static final String JOIN_NOME_DOADOR =
            "LEFT JOIN REFUSE_ESTABELECIMENTO e ON e.id_estabelecimento = d.id_estabelecimento "
                    + "LEFT JOIN REFUSE_PESSOA_FISICA p ON p.id_pessoa_fisica = d.id_pessoa_fisica "
                    + "LEFT JOIN REFUSE_USUARIO u ON u.id_usuario = COALESCE(e.id_usuario, p.id_usuario) ";

    public int inserir(Doacao d) throws SQLException {
        // status nao entra: o banco usa DEFAULT 'DISPONIVEL'
        String sql = "INSERT INTO REFUSE_DOACAO "
                + "(id_estabelecimento, id_pessoa_fisica, id_alimento, quantidade_total, "
                + "quantidade_disponivel, data_publicacao, data_validade, data_limite_retirada, "
                + "condicao_armazenamento) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_DOACAO"})) {

            setIntOuNulo(ps, 1, d.getIdEstabelecimento());
            setIntOuNulo(ps, 2, d.getIdPessoaFisica());
            ps.setInt(3, d.getAlimento().getIdAlimento());
            ps.setBigDecimal(4, d.getQuantidadeTotal());
            ps.setBigDecimal(5, d.getQuantidadeTotal());   // ao publicar, tudo esta disponivel
            ps.setTimestamp(6, Timestamp.valueOf(d.getDataPublicacao()));
            ps.setDate(7, Date.valueOf(d.getDataValidade()));
            ps.setDate(8, Date.valueOf(d.getDataLimiteRetirada()));
            ps.setString(9, UsuarioDao.limitar(d.getCondicaoArmazenamento(), 200));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Nao foi possivel obter o ID gerado da doacao.");
    }

    /** Todas as doacoes do doador, da mais nova para a mais antiga. */
    public List<Doacao> listarDoDoador(Integer idEstabelecimento, Integer idPessoaFisica) throws SQLException {
        String sql = "SELECT d.id_doacao, d.id_estabelecimento, d.id_pessoa_fisica, d.id_alimento, "
                + "d.quantidade_total, d.quantidade_disponivel, d.data_publicacao, d.data_validade, "
                + "d.data_limite_retirada, d.condicao_armazenamento, d.status, "
                + "a.nome, a.categoria, a.descricao, a.unidade_medida "
                + "FROM REFUSE_DOACAO d JOIN REFUSE_ALIMENTO a ON a.id_alimento = d.id_alimento "
                + "WHERE d." + colunaDoador(idEstabelecimento, idPessoaFisica) + " = ? "
                + "ORDER BY d.data_publicacao DESC";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDoador(idEstabelecimento, idPessoaFisica));

            try (ResultSet rs = ps.executeQuery()) {
                List<Doacao> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        }
    }

    /**
     * Cancela a doacao. So funciona se ela for DESSE doador e estiver DISPONIVEL
     * (se ja tem reserva, nao da para cancelar daqui).
     *
     * @return true se cancelou; false se nao encontrou nenhuma doacao nessas condicoes
     */
    public boolean cancelar(int idDoacao, Integer idEstabelecimento, Integer idPessoaFisica) throws SQLException {
        String sql = "UPDATE REFUSE_DOACAO SET status = 'CANCELADA' "
                + "WHERE id_doacao = ? AND status = 'DISPONIVEL' "
                + "AND " + colunaDoador(idEstabelecimento, idPessoaFisica) + " = ?";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDoacao);
            ps.setInt(2, idDoador(idEstabelecimento, idPessoaFisica));
            return ps.executeUpdate() > 0;
        }
    }

    /** O que uma ONG pode reservar: doacoes com saldo e dentro do prazo de retirada. */
    public List<Doacao> listarDisponiveis(LocalDate hoje) throws SQLException {
        String sql = "SELECT d.id_doacao, d.id_estabelecimento, d.id_pessoa_fisica, d.id_alimento, "
                + "d.quantidade_total, d.quantidade_disponivel, d.data_publicacao, d.data_validade, "
                + "d.data_limite_retirada, d.condicao_armazenamento, d.status, "
                + "a.nome, a.categoria, a.descricao, a.unidade_medida, u.nome AS nome_doador "
                + "FROM REFUSE_DOACAO d JOIN REFUSE_ALIMENTO a ON a.id_alimento = d.id_alimento "
                + JOIN_NOME_DOADOR
                + "WHERE d.status IN ('DISPONIVEL', 'PARCIALMENTE_RESERVADA') "
                + "AND d.quantidade_disponivel > 0 AND d.data_limite_retirada >= ? "
                + "ORDER BY d.data_limite_retirada";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(hoje));

            try (ResultSet rs = ps.executeQuery()) {
                List<Doacao> lista = new ArrayList<>();
                while (rs.next()) {
                    Doacao d = mapear(rs);
                    d.setNomeDoador(rs.getString("nome_doador"));
                    lista.add(d);
                }
                return lista;
            }
        }
    }

    // ---------- Auxiliares ----------

    /**
     * Nome da coluna do doador. So devolve constantes daqui
     * (por isso e seguro concatenar no SQL).
     */
    private static String colunaDoador(Integer idEstabelecimento, Integer idPessoaFisica) throws SQLException {
        if (idEstabelecimento != null) {
            return "id_estabelecimento";
        }
        if (idPessoaFisica != null) {
            return "id_pessoa_fisica";
        }
        throw new SQLException("Doador nao informado.");
    }

    private static int idDoador(Integer idEstabelecimento, Integer idPessoaFisica) {
        return idEstabelecimento != null ? idEstabelecimento : idPessoaFisica;
    }

    private static void setIntOuNulo(PreparedStatement ps, int indice, Integer valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.INTEGER);
        } else {
            ps.setInt(indice, valor);
        }
    }

    private static Integer inteiroOuNulo(ResultSet rs, String coluna) throws SQLException {
        int valor = rs.getInt(coluna);
        return rs.wasNull() ? null : valor;
    }

    private Doacao mapear(ResultSet rs) throws SQLException {
        Alimento a = new Alimento();
        a.setIdAlimento(rs.getInt("id_alimento"));
        a.setNome(rs.getString("nome"));
        a.setCategoria(rs.getString("categoria"));
        a.setDescricao(rs.getString("descricao"));
        a.setUnidadeMedida(rs.getString("unidade_medida"));

        Doacao d = new Doacao();
        d.setIdDoacao(rs.getInt("id_doacao"));
        d.setIdEstabelecimento(inteiroOuNulo(rs, "id_estabelecimento"));
        d.setIdPessoaFisica(inteiroOuNulo(rs, "id_pessoa_fisica"));
        d.setAlimento(a);
        d.setQuantidadeTotal(rs.getBigDecimal("quantidade_total"));
        d.setQuantidadeDisponivel(rs.getBigDecimal("quantidade_disponivel"));
        d.setDataPublicacao(rs.getTimestamp("data_publicacao").toLocalDateTime());
        d.setDataValidade(rs.getDate("data_validade").toLocalDate());
        d.setDataLimiteRetirada(rs.getDate("data_limite_retirada").toLocalDate());
        d.setCondicaoArmazenamento(rs.getString("condicao_armazenamento"));
        d.setStatus(rs.getString("status"));
        return d;
    }
}