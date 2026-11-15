package br.com.refuseit.refuseit.app.entities;

import br.com.refuseit.refuseit.app.entities.Alimento;
import br.com.refuseit.refuseit.app.entities.Doacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Venda {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int idVenda;
    private int idEstabelecimento;
    private Alimento alimento;
    private BigDecimal quantidadeTotal;
    private BigDecimal quantidadeDisponivel;
    private BigDecimal precoUnitario;
    private LocalDateTime dataPublicacao;
    private LocalDate dataValidade;
    private String condicaoArmazenamento;
    private String status;   // DISPONIVEL, ESGOTADA, EXPIRADA, CANCELADA
    private String nomeEstabelecimento;

    public Venda() {
    }

    public int getIdVenda() {
        return idVenda;
    }

    public void setIdVenda(int idVenda) {
        this.idVenda = idVenda;
    }

    public int getIdEstabelecimento() {
        return idEstabelecimento;
    }

    public void setIdEstabelecimento(int idEstabelecimento) {
        this.idEstabelecimento = idEstabelecimento;
    }

    public Alimento getAlimento() {
        return alimento;
    }

    public void setAlimento(Alimento alimento) {
        this.alimento = alimento;
    }

    public BigDecimal getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public void setQuantidadeTotal(BigDecimal quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    public BigDecimal getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public void setQuantidadeDisponivel(BigDecimal quantidadeDisponivel) {
        this.quantidadeDisponivel = quantidadeDisponivel;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public LocalDateTime getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(LocalDateTime dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }

    public String getCondicaoArmazenamento() {
        return condicaoArmazenamento;
    }

    public void setCondicaoArmazenamento(String condicaoArmazenamento) {
        this.condicaoArmazenamento = condicaoArmazenamento;
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
    public String resumo() {
        String un = alimento.getUnidadeMedida();
        return "#" + idVenda + " - " + alimento.getNome() + " - "
                + Doacao.formatar(quantidadeDisponivel) + " " + un
                + " a R$ " + Doacao.formatar(precoUnitario) + "/" + un
                + " - validade " + dataValidade.format(DATA)
                + (nomeEstabelecimento != null ? " - " + nomeEstabelecimento : "");
    }

    public String detalhes() {
        String un = alimento.getUnidadeMedida();
        StringBuilder sb = new StringBuilder();
        sb.append("Anúncio #").append(idVenda).append("  [").append(status).append("]\n");
        sb.append("Alimento: ").append(alimento.getNome()).append("\n");
        sb.append("À venda: ").append(Doacao.formatar(quantidadeDisponivel))
                .append(" de ").append(Doacao.formatar(quantidadeTotal)).append(" ").append(un).append("\n");
        sb.append("Preço: R$ ").append(Doacao.formatar(precoUnitario)).append(" por ").append(un).append("\n");
        sb.append("Validade: ").append(dataValidade.format(DATA));
        if (condicaoArmazenamento != null) {
            sb.append("\nArmazenamento: ").append(condicaoArmazenamento);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return resumo();
    }
}