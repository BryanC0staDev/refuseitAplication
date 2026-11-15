package br.com.refuseit.refuseit.app.entities;

import br.com.refuseit.refuseit.app.entities.Alimento;
import br.com.refuseit.refuseit.app.entities.Doacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Compra {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int idCompra;
    private int idVenda;
    private int idInstituicao;
    private Alimento alimento;
    private BigDecimal quantidade;
    private BigDecimal valorTotal;
    private LocalDateTime dataCompra;
    private String status;   // PENDENTE, CONFIRMADA, CONCLUIDA, RECUSADA, CANCELADA
    private String nomeEstabelecimento;
    private String nomeInstituicao;
    private String metodoPagamento;
    private String statusPagamento;
    private LocalDateTime dataRegistroPagamento;
    private LocalDateTime dataConfirmacaoPagamento;

    public Compra() {
    }
    public String getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public String getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(String statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    public LocalDateTime getDataRegistroPagamento() {
        return dataRegistroPagamento;
    }

    public void setDataRegistroPagamento(LocalDateTime dataRegistroPagamento) {
        this.dataRegistroPagamento = dataRegistroPagamento;
    }

    public LocalDateTime getDataConfirmacaoPagamento() {
        return dataConfirmacaoPagamento;
    }

    public void setDataConfirmacaoPagamento(LocalDateTime dataConfirmacaoPagamento) {
        this.dataConfirmacaoPagamento = dataConfirmacaoPagamento;
    }
    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public int getIdVenda() {
        return idVenda;
    }

    public void setIdVenda(int idVenda) {
        this.idVenda = idVenda;
    }

    public int getIdInstituicao() {
        return idInstituicao;
    }

    public void setIdInstituicao(int idInstituicao) {
        this.idInstituicao = idInstituicao;
    }

    public Alimento getAlimento() {
        return alimento;
    }

    public void setAlimento(Alimento alimento) {
        this.alimento = alimento;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public LocalDateTime getDataCompra() {
        return dataCompra;
    }

    public void setDataCompra(LocalDateTime dataCompra) {
        this.dataCompra = dataCompra;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNomeEstabelecimento() {
        return nomeEstabelecimento;
    }

    public void setNomeEstabelecimento(String nomeEstabelecimento) {
        this.nomeEstabelecimento = nomeEstabelecimento;
    }

    public String getNomeInstituicao() {
        return nomeInstituicao;
    }

    public void setNomeInstituicao(String nomeInstituicao) {
        this.nomeInstituicao = nomeInstituicao;
    }

    public String resumo() {
        return "#" + idCompra + " - " + alimento.getNome() + " - "
                + Doacao.formatar(quantidade) + " " + alimento.getUnidadeMedida()
                + " - R$ " + Doacao.formatar(valorTotal) + " [" + status + "]";
    }

    public String detalhes() {
        return "Compra #" + idCompra + "  [" + status + "]\n"
                + "Alimento: " + alimento.getNome() + "\n"
                + "Quantidade: " + Doacao.formatar(quantidade) + " " + alimento.getUnidadeMedida() + "\n"
                + "Valor total: R$ " + Doacao.formatar(valorTotal) + "\n"
                + "Vendedor: " + nomeEstabelecimento + "\n"
                + "Comprador: " + nomeInstituicao + "\n"
                + "Comprada em: " + dataCompra.format(DATA_HORA) + "\n"
                + "Método de pagamento: " + metodoPagamento + "\n"
                + "Status do pagamento: " + statusPagamento + "\n"
                + "Pagamento confirmado em: "
                + (dataConfirmacaoPagamento == null
                ? "Aguardando confirmação"
                : dataConfirmacaoPagamento.format(DATA_HORA));
    }
    @Override
    public String toString() {
        return resumo();
    }
}