package br.com.refuseit.refuseit.app.entities.interativas;

import br.com.refuseit.refuseit.app.dao.CompraDao;
import br.com.refuseit.refuseit.app.dao.DoacaoDao;
import br.com.refuseit.refuseit.app.dao.OngDao;
import br.com.refuseit.refuseit.app.dao.ReservaDao;
import br.com.refuseit.refuseit.app.dao.VendaDao;
import br.com.refuseit.refuseit.app.entities.Doacao;
import br.com.refuseit.refuseit.app.entities.Reserva;
import br.com.refuseit.refuseit.app.entities.Compra;
import br.com.refuseit.refuseit.app.entities.Venda;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class CompraInterativa {

    private final int idUsuario;
    private final String tipoUsuario;   // "PESSOA_FISICA", "EMPRESA" ou "ONG"

    private final DoacaoDao doacaoDao = new DoacaoDao();
    private final ReservaDao reservaDao = new ReservaDao();
    private final VendaDao vendaDao = new VendaDao();
    private final CompraDao compraDao = new CompraDao();
    private final OngDao ongDao = new OngDao();

    private Integer idInstituicao;

    public CompraInterativa(int idUsuario, String tipoUsuario) {
        this.idUsuario = idUsuario;
        this.tipoUsuario = tipoUsuario;
    }

    public void abrir() {
        try {
            if (!"ONG".equals(tipoUsuario)) {
                Tela.aviso("Apenas ONGs podem receber doações e comprar alimentos.");
                return;
            }

            idInstituicao = ongDao.buscarIdPorUsuario(idUsuario);
            if (idInstituicao == null) {
                Tela.aviso("Não foi possível identificar o cadastro da sua ONG.");
                return;
            }

            menuCompras();
        } catch (SQLException e) {
            Tela.aviso("Erro no banco de dados:\n" + e.getMessage());
        }
    }

    private void menuCompras() throws SQLException {
        while (true) {
            switch (Tela.menu("Receber e comprar", "Receber doação", "Minhas reservas",
                    "Comprar alimento", "Minhas compras", "Voltar")) {
                case 0:
                    receberDoacao();
                    break;
                case 1:
                    minhasReservas();
                    break;
                case 2:
                    comprarAlimento();
                    break;
                case 3:
                    minhasCompras();
                    break;
                default:
                    return;   // Voltar (ou fechou a janela)
            }
        }
    }

    // ---------- RECEBER DOACAO (reserva) ----------

    private void receberDoacao() throws SQLException {
        LocalDate hoje = LocalDate.now();
        List<Doacao> disponiveis = doacaoDao.listarDisponiveis(hoje);

        if (disponiveis.isEmpty()) {
            Tela.info("Nenhuma doação disponível no momento.");
            return;
        }

        Doacao doacao = (Doacao) Tela.escolher("Qual doação você quer receber?", disponiveis);
        if (doacao == null) {
            return;
        }
        String un = doacao.getAlimento().getUnidadeMedida();

        BigDecimal quantidade = Tela.pedirNumero("Quantidade desejada (disponível: "
                + Doacao.formatar(doacao.getQuantidadeDisponivel()) + " " + un + "):");
        if (quantidade == null) {
            return;
        }
        if (quantidade.compareTo(doacao.getQuantidadeDisponivel()) > 0) {
            Tela.aviso("Quantidade maior que a disponível.");
            return;
        }

        if (reservaDao.reservar(doacao.getIdDoacao(), idInstituicao, quantidade, hoje)) {
            Tela.info("Reserva feita! Veja o prazo de retirada em \"Minhas reservas\".");
        } else {
            Tela.aviso("Não foi possível reservar: a doação acabou, venceu ou foi reservada por outra ONG.");
        }
    }

    // ---------- MINHAS RESERVAS ----------

    private void minhasReservas() throws SQLException {
        List<Reserva> reservas = reservaDao.listarDaInstituicao(idInstituicao);

        if (reservas.isEmpty()) {
            Tela.info("Você ainda não tem reservas.");
            return;
        }

        Reserva reserva = (Reserva) Tela.escolher("Suas reservas:", reservas);
        if (reserva == null) {
            return;
        }

        boolean cancelavel = "PENDENTE".equals(reserva.getStatus()) || "APROVADA".equals(reserva.getStatus());
        if (!cancelavel) {
            Tela.info(reserva.detalhes());
            return;
        }

        if (Tela.menu(reserva.detalhes(), "Cancelar reserva", "Voltar") != 0) {
            return;
        }
        if (!Tela.confirmar("Tem certeza que deseja cancelar esta reserva?")) {
            return;
        }

        if (reservaDao.cancelar(reserva.getIdReserva(), idInstituicao)) {
            Tela.info("Reserva cancelada. A quantidade voltou para a doação.");
        } else {
            Tela.aviso("Não foi possível cancelar: o status da reserva mudou.");
        }
    }

    // ---------- COMPRAR ALIMENTO ----------

    private void comprarAlimento() throws SQLException {
        LocalDate hoje = LocalDate.now();
        List<Venda> anuncios = vendaDao.listarDisponiveis(hoje);

        if (anuncios.isEmpty()) {
            Tela.info("Nenhum alimento à venda no momento.");
            return;
        }

        Venda venda = (Venda) Tela.escolher("O que você quer comprar?", anuncios);
        if (venda == null) {
            return;
        }
        String un = venda.getAlimento().getUnidadeMedida();

        BigDecimal quantidade = Tela.pedirNumero("Quantidade (disponível: "
                + Doacao.formatar(venda.getQuantidadeDisponivel()) + " " + un + "):");
        if (quantidade == null) {
            return;
        }
        if (quantidade.compareTo(venda.getQuantidadeDisponivel()) > 0) {
            Tela.aviso("Quantidade maior que a disponível.");
            return;
        }

        BigDecimal total = venda.getPrecoUnitario().multiply(quantidade).setScale(2, RoundingMode.HALF_UP);

        int opcaoPagamento = Tela.menu(
                "Escolha o método de pagamento:",
                "PIX",
                "Dinheiro",
                "Transferência",
                "Voltar"
        );

        String metodoPagamento;

        switch (opcaoPagamento) {
            case 0:
                metodoPagamento = "PIX";
                break;
            case 1:
                metodoPagamento = "DINHEIRO";
                break;
            case 2:
                metodoPagamento = "TRANSFERENCIA";
                break;
            default:
                return;
        }

        if (!Tela.confirmar("Confirmar a compra?\n\n"
                + venda.getAlimento().getNome() + " - "
                + Doacao.formatar(quantidade) + " " + un + "\n"
                + "Total: R$ " + Doacao.formatar(total) + "\n"
                + "Pagamento: " + metodoPagamento)) {
            return;
        }

        if (compraDao.comprar(
                venda.getIdVenda(),
                idInstituicao,
                quantidade,
                hoje,
                metodoPagamento
        )) {
            Tela.info("Compra registrada com sucesso!\n\n"
                    + "Pagamento: " + metodoPagamento + "\n"
                    + "Status: PENDENTE\n\n"
                    + "Aguarde a empresa confirmar o recebimento do pagamento.");
        } else {
            Tela.aviso("Não foi possível comprar: o anúncio acabou, venceu, "
                    + "ou outra ONG comprou antes.");
        }


    }

    // ---------- MINHAS COMPRAS ----------

    private void minhasCompras() throws SQLException {
        List<Compra> compras = compraDao.listarDaInstituicao(idInstituicao);

        if (compras.isEmpty()) {
            Tela.info("Você ainda não fez nenhuma compra.");
            return;
        }

        Compra compra = (Compra) Tela.escolher("Suas compras:", compras);
        if (compra == null) {
            return;
        }

        if (!"PENDENTE".equals(compra.getStatus())) {
            Tela.info(compra.detalhes());
            return;
        }

        if (Tela.menu(compra.detalhes(), "Cancelar compra", "Voltar") != 0) {
            return;
        }
        if (!Tela.confirmar("Tem certeza que deseja cancelar esta compra?")) {
            return;
        }

        if (compraDao.cancelar(compra.getIdCompra(), idInstituicao)) {
            Tela.info("Compra cancelada.");
        } else {
            Tela.aviso("Não foi possível cancelar: o vendedor já respondeu a compra.");
        }
    }
}