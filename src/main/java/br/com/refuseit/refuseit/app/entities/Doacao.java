package br.com.refuseit.refuseit.app.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Doacao {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int idDoacao;
    private Integer idEstabelecimento;
    private Integer idPessoaFisica;
    private Alimento alimento;
    private BigDecimal quantidadeTotal;
    private BigDecimal quantidadeDisponivel;
    private LocalDateTime dataPublicacao;
    private LocalDate dataValidade;
    private LocalDate dataLimiteRetirada;
    private String condicaoArmazenamento;
    private String status;   // DISPONIVEL, PARCIALMENTE_RESERVADA, ESGOTADA, EXPIRADA, CANCELADA
    private String nomeDoador;   // so vem preenchido na lista de doacoes disponiveis (para as ONGs)

    public Doacao() {
    }

    public int getIdDoacao() {
        return idDoacao;
    }

    public void setIdDoacao(int idDoacao) {
        this.idDoacao = idDoacao;
    }

    public Integer getIdEstabelecimento() {
        return idEstabelecimento;
    }

    public void setIdEstabelecimento(Integer idEstabelecimento) {
        this.idEstabelecimento = idEstabelecimento;
    }

    public Integer getIdPessoaFisica() {
        return idPessoaFisica;
    }

    public void setIdPessoaFisica(Integer idPessoaFisica) {
        this.idPessoaFisica = idPessoaFisica;
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

    public LocalDate getDataLimiteRetirada() {
        return dataLimiteRetirada;
    }

    public void setDataLimiteRetirada(LocalDate dataLimiteRetirada) {
        this.dataLimiteRetirada = dataLimiteRetirada;
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

    public String getNomeDoador() {
        return nomeDoador;
    }

    public void setNomeDoador(String nomeDoador) {
        this.nomeDoador = nomeDoador;
    }

    /** 12,50 em vez de 12.50 */
    public static String formatar(BigDecimal quantidade) {
        return quantidade == null ? "" : quantidade.toPlainString().replace('.', ',');
    }

    /** Uma linha so: usada nas listas de escolha. */
    public String resumo() {
        return "#" + idDoacao + " - " + alimento.getNome() + " - "
                + formatar(quantidadeDisponivel) + " " + alimento.getUnidadeMedida()
                + " - validade " + dataValidade.format(DATA)
                + (nomeDoador != null ? " - doador: " + nomeDoador : "");
    }

    /** Varias linhas: usada na tela "Minhas doacoes". */
    public String detalhes() {
        StringBuilder sb = new StringBuilder();
        sb.append("Doação #").append(idDoacao).append("  [").append(status).append("]\n");
        sb.append("Alimento: ").append(alimento.getNome()).append("\n");
        sb.append("Quantidade: ").append(formatar(quantidadeDisponivel))
                .append(" disponíveis de ").append(formatar(quantidadeTotal))
                .append(" ").append(alimento.getUnidadeMedida()).append("\n");
        sb.append("Validade: ").append(dataValidade.format(DATA)).append("\n");
        sb.append("Retirar até: ").append(dataLimiteRetirada.format(DATA)).append("\n");
        if (condicaoArmazenamento != null) {
            sb.append("Armazenamento: ").append(condicaoArmazenamento).append("\n");
        }
        sb.append("Publicada em: ").append(dataPublicacao.format(DATA_HORA));
        return sb.toString();
    }

    @Override
    public String toString() {
        return resumo();
    }
}