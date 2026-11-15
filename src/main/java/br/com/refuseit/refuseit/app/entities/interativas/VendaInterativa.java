package br.com.refuseit.refuseit.app.entities.interativas;

import br.com.refuseit.refuseit.app.dao.CompraDao;
import br.com.refuseit.refuseit.app.dao.EmpresaDao;
import br.com.refuseit.refuseit.app.dao.VendaDao;
import br.com.refuseit.refuseit.app.entities.Alimento;
import br.com.refuseit.refuseit.app.entities.Doacao;
import br.com.refuseit.refuseit.app.entities.Compra;
import br.com.refuseit.refuseit.app.entities.Venda;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class VendaInterativa {

    private final int idUsuario;
    private final String tipoUsuario;   // "PESSOA_FISICA", "EMPRESA" ou "ONG"

    private final VendaDao vendaDao = new VendaDao();
    private final CompraDao compraDao = new CompraDao();
    private final EmpresaDao empresaDao = new EmpresaDao();

    private Integer idEstabelecimento;

    public VendaInterativa(int idUsuario, String tipoUsuario) {
        this.idUsuario = idUsuario;
        this.tipoUsuario = tipoUsuario;
    }

    public void abrir() {
        try {
            if (!"EMPRESA".equals(tipoUsuario)) {
                Tela.aviso("Apenas empresas podem vender alimentos.");
                return;
            }

            idEstabelecimento = empresaDao.buscarIdPorUsuario(idUsuario);
            if (idEstabelecimento == null) {
                Tela.aviso("Não foi possível identificar o cadastro da sua empresa.");
                return;
            }

            menuVendas();
        } catch (SQLException e) {
            // Qualquer erro de banco mostra a mensagem e volta ao menu principal
            Tela.aviso("Erro no banco de dados:\n" + e.getMessage());
        }
    }

    private void menuVendas() throws SQLException {
        while (true) {
            switch (Tela.menu("Vendas", "Anunciar alimento", "Meus anúncios",
                    "Compras recebidas", "Cancelar anúncio", "Voltar")) {
                case 0:
                    anunciar();
                    break;
                case 1:
                    listarAnuncios();
                    break;
                case 2:
                    comprasRecebidas();
                    break;
                case 3:
                    cancelarAnuncio();
                    break;
                default:
                    return;   // Voltar (ou fechou a janela)
            }
        }
    }

    // ---------- ANUNCIAR ----------

    private void anunciar() throws SQLException {
        Alimento alimento = Tela.escolherAlimento();
        if (alimento == null) {
            return;
        }
        String un = alimento.getUnidadeMedida();

        BigDecimal quantidade = Tela.pedirNumero("Quantidade à venda (em " + un + "):");
        if (quantidade == null) {
            return;
        }

        BigDecimal preco = Tela.pedirNumero("Preço por " + un + " (R$):");
        if (preco == null) {
            return;
        }

        LocalDate hoje = LocalDate.now();
        LocalDate validade = Tela.pedirData("Data de validade (dd/mm/aaaa):",
                hoje.plusDays(1), hoje.plusYears(5));
        if (validade == null) {
            return;
        }

        Venda venda = new Venda();
        venda.setIdEstabelecimento(idEstabelecimento);
        venda.setAlimento(alimento);
        venda.setQuantidadeTotal(quantidade);
        venda.setPrecoUnitario(preco);
        venda.setDataPublicacao(LocalDateTime.now());   // do Java, para bater com a data acima
        venda.setDataValidade(validade);
        venda.setCondicaoArmazenamento(Tela.pedirTextoOpcional("Condição de armazenamento (opcional):"));

        int id = vendaDao.inserir(venda);
        Tela.info("Anúncio publicado com sucesso! (código " + id + ")");
    }

    // ---------- MEUS ANUNCIOS ----------

    private void listarAnuncios() throws SQLException {
        List<Venda> anuncios = vendaDao.listarDoVendedor(idEstabelecimento);

        if (anuncios.isEmpty()) {
            Tela.info("Você ainda não publicou nenhum anúncio.");
            return;
        }

        StringBuilder texto = new StringBuilder();
        for (Venda v : anuncios) {
            texto.append(v.detalhes()).append("\n\n");
        }
        Tela.mostrarTexto("Meus anúncios", texto.toString());
    }

    // ---------- COMPRAS RECEBIDAS ----------

    private void comprasRecebidas() throws SQLException {
        List<Compra> compras = compraDao.listarDoVendedor(idEstabelecimento);

        if (compras.isEmpty()) {
            Tela.info("Você ainda não recebeu nenhuma compra.");
            return;
        }

        Compra compra = (Compra) Tela.escolher(
                "Compras recebidas (escolha uma):",
                compras
        );

        if (compra == null) {
            return;
        }

        boolean atualizou;

        // A empresa ainda precisa aceitar ou recusar a compra.
        if ("PENDENTE".equals(compra.getStatus())) {

            int opcao = Tela.menu(
                    compra.detalhes(),
                    "Confirmar compra",
                    "Recusar compra",
                    "Voltar"
            );

            if (opcao == 0) {
                atualizou = compraDao.confirmar(
                        compra.getIdCompra(),
                        idEstabelecimento
                );

                if (atualizou) {
                    Tela.info("Compra confirmada!\n"
                            + "Agora aguarde o recebimento do pagamento.");
                } else {
                    Tela.aviso("Não foi possível confirmar a compra.");
                }

            } else if (opcao == 1) {
                atualizou = compraDao.recusar(
                        compra.getIdCompra(),
                        idEstabelecimento
                );

                if (atualizou) {
                    Tela.info("Compra recusada e pagamento cancelado.");
                } else {
                    Tela.aviso("Não foi possível recusar a compra.");
                }
            }

            return;
        }

        // A compra foi aceita, mas o pagamento ainda está pendente.
        if ("CONFIRMADA".equals(compra.getStatus())
                && "PENDENTE".equals(compra.getStatusPagamento())) {

            if (!Tela.confirmar(
                    "Você confirma que recebeu o pagamento?\n\n"
                            + compra.detalhes()
            )) {
                return;
            }

            atualizou = compraDao.confirmarPagamento(
                    compra.getIdCompra(),
                    idEstabelecimento
            );

            if (atualizou) {
                Tela.info("Pagamento confirmado com sucesso!\n"
                        + "Agora a compra pode ser concluída.");
            } else {
                Tela.aviso("Não foi possível confirmar o pagamento.");
            }

            return;
        }

        // O pagamento já foi recebido. A empresa pode concluir a compra.
        if ("CONFIRMADA".equals(compra.getStatus())
                && "CONFIRMADO".equals(compra.getStatusPagamento())) {

            if (!Tela.confirmar(
                    "Marcar esta compra como concluída?\n"
                            + "(Retirada ou entrega realizada)\n\n"
                            + compra.detalhes()
            )) {
                return;
            }

            atualizou = compraDao.concluir(
                    compra.getIdCompra(),
                    idEstabelecimento
            );

            if (atualizou) {
                Tela.info("Compra concluída com sucesso!");
            } else {
                Tela.aviso("Não foi possível concluir a compra.");
            }

            return;
        }

        Tela.info(compra.detalhes());
    }

    // ---------- CANCELAR ANUNCIO ----------

    private void cancelarAnuncio() throws SQLException {
        List<Venda> disponiveis = new ArrayList<>();
        for (Venda v : vendaDao.listarDoVendedor(idEstabelecimento)) {
            if ("DISPONIVEL".equals(v.getStatus())) {
                disponiveis.add(v);
            }
        }

        if (disponiveis.isEmpty()) {
            Tela.info("Você não tem anúncios disponíveis para cancelar.");
            return;
        }

        Venda venda = (Venda) Tela.escolher("Qual anúncio deseja cancelar?", disponiveis);
        if (venda == null) {
            return;
        }

        if (!Tela.confirmar("Cancelar este anúncio?\n(compras já feitas continuam valendo)\n\n"
                + venda.resumo())) {
            return;
        }

        if (vendaDao.cancelar(venda.getIdVenda(), idEstabelecimento)) {
            Tela.info("Anúncio cancelado.");
        } else {
            Tela.aviso("Não foi possível cancelar: o status do anúncio mudou.");
        }
    }
}