package br.com.refuseit.refuseit.app.bo;

import br.com.refuseit.refuseit.app.dao.CompraDao;
import br.com.refuseit.refuseit.app.dao.EmpresaDao;
import br.com.refuseit.refuseit.app.dao.OngDao;
import br.com.refuseit.refuseit.app.dao.VendaDao;
import br.com.refuseit.refuseit.app.entities.Compra;
import br.com.refuseit.refuseit.app.entities.Venda;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/*
 * Lado da ONG: comprar, listar e cancelar.
 * Lado da empresa: confirmar/recusar a compra, confirmar o pagamento e concluir.
 *
 * Fluxo: PENDENTE -> CONFIRMADA -> (pagamento CONFIRMADO) -> CONCLUIDA
 *        PENDENTE -> RECUSADA (empresa)  |  PENDENTE -> CANCELADA (ONG)
 */
public class CompraBo {

    private static final ZoneId BRASIL = ZoneId.of("America/Sao_Paulo");
    private static final List<String> METODOS_PAGAMENTO = List.of("PIX", "DINHEIRO", "TRANSFERENCIA");

    private int identificarOng(int idUsuario) throws SQLException {
        OngDao ongDao = new OngDao();

        if (idUsuario <= 0) {
            throw new IllegalArgumentException("Id do usuário inválido.");
        }

        Integer idInstituicao = ongDao.buscarIdPorUsuario(idUsuario);
        if (idInstituicao == null) {
            throw new IllegalArgumentException("Apenas ONGs podem comprar alimentos.");
        }
        return idInstituicao;
    }

    private int identificarEmpresa(int idUsuario) throws SQLException {
        EmpresaDao empresaDao = new EmpresaDao();

        if (idUsuario <= 0) {
            throw new IllegalArgumentException("Id do usuário inválido.");
        }

        Integer idEstabelecimento = empresaDao.buscarIdPorUsuario(idUsuario);
        if (idEstabelecimento == null) {
            throw new IllegalArgumentException("Apenas empresas podem gerenciar compras recebidas.");
        }
        return idEstabelecimento;
    }

    // ---------- LADO DA ONG ----------

    // Inserir (comprar): usa idVenda, quantidade e metodoPagamento da compra recebida
    public void inserirBo(int idUsuario, Compra compra) throws SQLException {
        CompraDao compraDao = new CompraDao();
        VendaDao vendaDao = new VendaDao();

        int idInstituicao = identificarOng(idUsuario);

        if (compra == null) {
            throw new IllegalArgumentException("Compra não pode ser nula.");
        }
        if (compra.getIdVenda() <= 0) {
            throw new IllegalArgumentException("Informe o anúncio (idVenda).");
        }
        if (compra.getQuantidade() == null || compra.getQuantidade().signum() <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (compra.getMetodoPagamento() == null
                || !METODOS_PAGAMENTO.contains(compra.getMetodoPagamento().trim().toUpperCase())) {
            throw new IllegalArgumentException("Método de pagamento inválido. Use um destes: " + METODOS_PAGAMENTO);
        }

        BigDecimal quantidade = compra.getQuantidade().setScale(2, RoundingMode.HALF_UP);
        LocalDate hoje = LocalDate.now(BRASIL);

        Venda venda = null;
        for (Venda v : vendaDao.listarDisponiveis(hoje)) {
            if (v.getIdVenda() == compra.getIdVenda()) {
                venda = v;
                break;
            }
        }
        if (venda == null) {
            throw new IllegalArgumentException("Esse anúncio não está mais disponível (acabou, venceu ou não existe).");
        }
        if (quantidade.compareTo(venda.getQuantidadeDisponivel()) > 0) {
            throw new IllegalArgumentException("Quantidade maior que a disponível ("
                    + venda.getQuantidadeDisponivel() + ").");
        }

        // O valor total (preço x quantidade) é calculado pelo próprio DAO
        if (!compraDao.comprar(compra.getIdVenda(), idInstituicao, quantidade, hoje,
                compra.getMetodoPagamento().trim().toUpperCase())) {
            throw new IllegalArgumentException("Não foi possível comprar: o anúncio acabou, venceu, "
                    + "outra ONG comprou antes, ou o anúncio é da sua própria instituição.");
        }
    }

    // Buscar as compras da ONG
    public List<Compra> buscarDaOngBo(int idUsuario) throws SQLException {
        CompraDao compraDao = new CompraDao();

        return compraDao.listarDaInstituicao(identificarOng(idUsuario));
    }

    // Cancelar (só PENDENTE)
    public void cancelarBo(int idUsuario, int idCompra) throws SQLException {
        CompraDao compraDao = new CompraDao();

        int idInstituicao = identificarOng(idUsuario);

        if (!compraDao.cancelar(idCompra, idInstituicao)) {
            throw new IllegalArgumentException("Não foi possível cancelar: a compra não existe, não é sua "
                    + "ou o vendedor já respondeu (só cancela compra PENDENTE).");
        }
    }

    // ---------- LADO DA EMPRESA ----------

    // Buscar as compras recebidas
    public List<Compra> buscarRecebidasBo(int idUsuario) throws SQLException {
        CompraDao compraDao = new CompraDao();

        return compraDao.listarDoVendedor(identificarEmpresa(idUsuario));
    }

    // Confirmar a compra
    public void confirmarBo(int idUsuario, int idCompra) throws SQLException {
        CompraDao compraDao = new CompraDao();

        int idEstabelecimento = identificarEmpresa(idUsuario);

        if (!compraDao.confirmar(idCompra, idEstabelecimento)) {
            throw new IllegalArgumentException("Não foi possível confirmar: a compra não existe, "
                    + "não é da sua empresa ou não está PENDENTE.");
        }
    }

    // Recusar a compra
    public void recusarBo(int idUsuario, int idCompra) throws SQLException {
        CompraDao compraDao = new CompraDao();

        int idEstabelecimento = identificarEmpresa(idUsuario);

        if (!compraDao.recusar(idCompra, idEstabelecimento)) {
            throw new IllegalArgumentException("Não foi possível recusar: a compra não existe, "
                    + "não é da sua empresa ou não está PENDENTE.");
        }
    }

    // Confirmar o pagamento
    public void confirmarPagamentoBo(int idUsuario, int idCompra) throws SQLException {
        CompraDao compraDao = new CompraDao();

        int idEstabelecimento = identificarEmpresa(idUsuario);

        if (!compraDao.confirmarPagamento(idCompra, idEstabelecimento)) {
            throw new IllegalArgumentException("Não foi possível confirmar o pagamento: a compra precisa estar "
                    + "CONFIRMADA, ser da sua empresa e ter pagamento PENDENTE.");
        }
    }

    // Concluir
    public void concluirBo(int idUsuario, int idCompra) throws SQLException {
        CompraDao compraDao = new CompraDao();

        int idEstabelecimento = identificarEmpresa(idUsuario);

        if (!compraDao.concluir(idCompra, idEstabelecimento)) {
            throw new IllegalArgumentException("Não foi possível concluir: a compra precisa estar CONFIRMADA "
                    + "e com o pagamento CONFIRMADO.");
        }
    }
}
