package br.com.refuseit.refuseit.app.dao;

import br.com.refuseit.refuseit.app.entities.Compra;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CompraDao {

    private static final String SELECT_COMPRA =
            "SELECT c.id_compra, c.id_venda, c.id_instituicao, c.quantidade, c.valor_total, "
                    + "c.data_compra, c.status, "
                    + "a.id_alimento, a.nome, a.categoria, a.descricao, a.unidade_medida, "
                    + "e.nome_fantasia AS nome_estabelecimento, i.nome AS nome_instituicao, "
                    + "p.metodo_pagamento, p.status AS status_pagamento, "
                    + "p.data_registro, p.data_confirmacao "
                    + "FROM REFUSE_COMPRA c "
                    + "JOIN REFUSE_VENDA v ON v.id_venda = c.id_venda "
                    + "JOIN REFUSE_ALIMENTO a ON a.id_alimento = v.id_alimento "
                    + "JOIN REFUSE_ESTABELECIMENTO e ON e.id_estabelecimento = v.id_estabelecimento "
                    + "JOIN REFUSE_INSTITUICAO i ON i.id_instituicao = c.id_instituicao "
                    + "LEFT JOIN REFUSE_PAGAMENTO p ON p.id_compra = c.id_compra ";


    public boolean comprar(int idVenda, int idInstituicao,
                           BigDecimal quantidade, LocalDate hoje,
                           String metodoPagamento) throws SQLException {

        if (!List.of("PIX", "DINHEIRO", "TRANSFERENCIA").contains(metodoPagamento)) {
            throw new IllegalArgumentException("Método de pagamento inválido.");
        }

        String baixaEstoque =
                "UPDATE REFUSE_VENDA "
                        + "SET quantidade_disponivel = quantidade_disponivel - ?, "
                        + "status = CASE WHEN quantidade_disponivel - ? = 0 "
                        + "THEN 'ESGOTADA' ELSE 'DISPONIVEL' END "
                        + "WHERE id_venda = ? AND status = 'DISPONIVEL' "
                        + "AND quantidade_disponivel >= ? AND data_validade >= ? "
                        + "AND NOT EXISTS ("
                        + " SELECT 1 FROM REFUSE_VENDA v "
                        + " JOIN REFUSE_ESTABELECIMENTO e ON e.id_estabelecimento = v.id_estabelecimento "
                        + " JOIN REFUSE_INSTITUICAO i ON i.id_usuario = e.id_usuario "
                        + " WHERE v.id_venda = ? AND i.id_instituicao = ?"
                        + ")";

        String buscaPreco =
                "SELECT preco_unitario FROM REFUSE_VENDA WHERE id_venda = ?";

        String insereCompra =
                "INSERT INTO REFUSE_COMPRA "
                        + "(id_venda, id_instituicao, quantidade, valor_total) "
                        + "VALUES (?, ?, ?, ?)";

        String inserePagamento =
                "INSERT INTO REFUSE_PAGAMENTO "
                        + "(id_compra, valor, metodo_pagamento, status) "
                        + "VALUES (?, ?, ?, 'PENDENTE')";

        try (Connection con = UsuarioDao.abrirConexao()) {
            con.setAutoCommit(false);

            try {
                // 1. Baixa o estoque, verificando disponibilidade e compra própria.
                try (PreparedStatement ps = con.prepareStatement(baixaEstoque)) {
                    ps.setBigDecimal(1, quantidade);
                    ps.setBigDecimal(2, quantidade);
                    ps.setInt(3, idVenda);
                    ps.setBigDecimal(4, quantidade);
                    ps.setDate(5, Date.valueOf(hoje));
                    ps.setInt(6, idVenda);
                    ps.setInt(7, idInstituicao);

                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }

                // 2. Consulta o preço do anúncio.
                BigDecimal preco;

                try (PreparedStatement ps = con.prepareStatement(buscaPreco)) {
                    ps.setInt(1, idVenda);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                        preco = rs.getBigDecimal("preco_unitario");
                    }
                }

                BigDecimal valorTotal =
                        preco.multiply(quantidade).setScale(2, RoundingMode.HALF_UP);

                // 3. Registra a compra e recupera seu ID.
                int idCompra;

                try (PreparedStatement ps = con.prepareStatement(
                        insereCompra, new String[]{"ID_COMPRA"})) {

                    ps.setInt(1, idVenda);
                    ps.setInt(2, idInstituicao);
                    ps.setBigDecimal(3, quantidade);
                    ps.setBigDecimal(4, valorTotal);
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                        idCompra = rs.getInt(1);
                    }
                }

                // 4. Registra o pagamento como PENDENTE.
                try (PreparedStatement ps = con.prepareStatement(inserePagamento)) {
                    ps.setInt(1, idCompra);
                    ps.setBigDecimal(2, valorTotal);
                    ps.setString(3, metodoPagamento);
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

    public List<Compra> listarDaInstituicao(int idInstituicao) throws SQLException {
        return listar("c.id_instituicao", idInstituicao);
    }

    public List<Compra> listarDoVendedor(int idEstabelecimento) throws SQLException {
        return listar("v.id_estabelecimento", idEstabelecimento);
    }

    // ---------- AÇÕES DA EMPRESA ----------

    public boolean confirmar(int idCompra, int idEstabelecimento) throws SQLException {
        return mudarStatus(idCompra, idEstabelecimento, "PENDENTE", "CONFIRMADA");
    }


    public boolean confirmarPagamento(int idCompra, int idEstabelecimento)
            throws SQLException {

        String sql =
                "UPDATE REFUSE_PAGAMENTO p "
                        + "SET status = 'CONFIRMADO', p.data_confirmacao = SYSTIMESTAMP "
                        + "WHERE p.id_compra = ? AND p.status = 'PENDENTE' "
                        + "AND EXISTS ("
                        + " SELECT 1 FROM REFUSE_COMPRA c "
                        + " JOIN REFUSE_VENDA v ON v.id_venda = c.id_venda "
                        + " WHERE c.id_compra = p.id_compra "
                        + " AND c.status = 'CONFIRMADA' "
                        + " AND v.id_estabelecimento = ?"
                        + ")";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCompra);
            ps.setInt(2, idEstabelecimento);

            return ps.executeUpdate() > 0;
        }
    }


    public boolean concluir(int idCompra, int idEstabelecimento) throws SQLException {

        String sql =
                "UPDATE REFUSE_COMPRA c SET status = 'CONCLUIDA' "
                        + "WHERE c.id_compra = ? AND c.status = 'CONFIRMADA' "
                        + "AND EXISTS ("
                        + " SELECT 1 FROM REFUSE_VENDA v "
                        + " WHERE v.id_venda = c.id_venda "
                        + " AND v.id_estabelecimento = ?"
                        + ") "
                        + "AND EXISTS ("
                        + " SELECT 1 FROM REFUSE_PAGAMENTO p "
                        + " WHERE p.id_compra = c.id_compra "
                        + " AND p.status = 'CONFIRMADO'"
                        + ")";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCompra);
            ps.setInt(2, idEstabelecimento);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean recusar(int idCompra, int idEstabelecimento) throws SQLException {
        String busca =
                "SELECT c.id_venda, c.quantidade FROM REFUSE_COMPRA c "
                        + "JOIN REFUSE_VENDA v ON v.id_venda = c.id_venda "
                        + "WHERE c.id_compra = ? AND v.id_estabelecimento = ? "
                        + "AND c.status = 'PENDENTE'";

        return encerrar(idCompra, busca, idEstabelecimento, "RECUSADA");
    }

    // ---------- AÇÕES DA ONG ----------

    public boolean cancelar(int idCompra, int idInstituicao) throws SQLException {
        String busca =
                "SELECT id_venda, quantidade FROM REFUSE_COMPRA "
                        + "WHERE id_compra = ? AND id_instituicao = ? "
                        + "AND status = 'PENDENTE'";

        return encerrar(idCompra, busca, idInstituicao, "CANCELADA");
    }

    // ---------- MÉTODOS AUXILIARES ----------

    private boolean mudarStatus(int idCompra, int idEstabelecimento,
                                String de, String para) throws SQLException {

        String sql =
                "UPDATE REFUSE_COMPRA SET status = ? "
                        + "WHERE id_compra = ? AND status = ? "
                        + "AND id_venda IN ("
                        + " SELECT id_venda FROM REFUSE_VENDA "
                        + " WHERE id_estabelecimento = ?"
                        + ")";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, para);
            ps.setInt(2, idCompra);
            ps.setString(3, de);
            ps.setInt(4, idEstabelecimento);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Recusa ou cancela a compra, cancela o pagamento e devolve o estoque.
     */
    private boolean encerrar(int idCompra, String busca,
                             int idDono, String novoStatus) throws SQLException {

        String atualizaCompra =
                "UPDATE REFUSE_COMPRA SET status = ? "
                        + "WHERE id_compra = ? AND status = 'PENDENTE'";

        String cancelaPagamento =
                "UPDATE REFUSE_PAGAMENTO SET status = 'CANCELADO' "
                        + "WHERE id_compra = ? AND status = 'PENDENTE'";

        String devolveEstoque =
                "UPDATE REFUSE_VENDA "
                        + "SET quantidade_disponivel = quantidade_disponivel + ?, "
                        + "status = CASE WHEN status = 'ESGOTADA' "
                        + "THEN 'DISPONIVEL' ELSE status END "
                        + "WHERE id_venda = ? AND status IN ('DISPONIVEL', 'ESGOTADA')";

        try (Connection con = UsuarioDao.abrirConexao()) {
            con.setAutoCommit(false);

            try {
                int idVenda;
                BigDecimal quantidade;

                try (PreparedStatement ps = con.prepareStatement(busca)) {
                    ps.setInt(1, idCompra);
                    ps.setInt(2, idDono);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }

                        idVenda = rs.getInt("id_venda");
                        quantidade = rs.getBigDecimal("quantidade");
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(atualizaCompra)) {
                    ps.setString(1, novoStatus);
                    ps.setInt(2, idCompra);

                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(cancelaPagamento)) {
                    ps.setInt(1, idCompra);

                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(devolveEstoque)) {
                    ps.setBigDecimal(1, quantidade);
                    ps.setInt(2, idVenda);

                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
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

    private List<Compra> listar(String colunaFiltro, int id) throws SQLException {
        String sql = SELECT_COMPRA
                + "WHERE " + colunaFiltro + " = ? ORDER BY c.data_compra DESC";

        try (Connection con = UsuarioDao.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                List<Compra> lista = new ArrayList<>();

                while (rs.next()) {
                    lista.add(mapear(rs));
                }

                return lista;
            }
        }
    }

    private Compra mapear(ResultSet rs) throws SQLException {
        Compra c = new Compra();

        c.setIdCompra(rs.getInt("id_compra"));
        c.setIdVenda(rs.getInt("id_venda"));
        c.setIdInstituicao(rs.getInt("id_instituicao"));
        c.setAlimento(AlimentoDao.mapear(rs));
        c.setQuantidade(rs.getBigDecimal("quantidade"));
        c.setValorTotal(rs.getBigDecimal("valor_total"));
        c.setDataCompra(rs.getTimestamp("data_compra").toLocalDateTime());
        c.setStatus(rs.getString("status"));
        c.setNomeEstabelecimento(rs.getString("nome_estabelecimento"));
        c.setNomeInstituicao(rs.getString("nome_instituicao"));

        c.setMetodoPagamento(rs.getString("metodo_pagamento"));
        c.setStatusPagamento(rs.getString("status_pagamento"));

        Timestamp registro = rs.getTimestamp("data_registro");
        if (registro != null) {
            c.setDataRegistroPagamento(registro.toLocalDateTime());
        }

        Timestamp confirmacao = rs.getTimestamp("data_confirmacao");
        if (confirmacao != null) {
            c.setDataConfirmacaoPagamento(confirmacao.toLocalDateTime());
        }

        return c;
    }
}