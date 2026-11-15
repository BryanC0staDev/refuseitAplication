package br.com.refuseit.refuseit.app.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reserva {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int idReserva;
    private int idDoacao;
    private Alimento alimento;
    private BigDecimal quantidadeSolicitada;
    private LocalDateTime dataReserva;
    private String status;   // PENDENTE, APROVADA, RECUSADA, CANCELADA, CONCLUIDA
    private String observacao;
    private String nomeDoador;
    private LocalDate dataLimiteRetirada;

    public Reserva() {
    }

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public int getIdDoacao() {
        return idDoacao;
    }

    public void setIdDoacao(int idDoacao) {
        this.idDoacao = idDoacao;
    }

    public Alimento getAlimento() {
        return alimento;
    }

    public void setAlimento(Alimento alimento) {
        this.alimento = alimento;
    }

    public BigDecimal getQuantidadeSolicitada() {
        return quantidadeSolicitada;
    }

    public void setQuantidadeSolicitada(BigDecimal quantidadeSolicitada) {
        this.quantidadeSolicitada = quantidadeSolicitada;
    }

    public LocalDateTime getDataReserva() {
        return dataReserva;
    }

    public void setDataReserva(LocalDateTime dataReserva) {
        this.dataReserva = dataReserva;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getNomeDoador() {
        return nomeDoador;
    }

    public void setNomeDoador(String nomeDoador) {
        this.nomeDoador = nomeDoador;
    }

    public LocalDate getDataLimiteRetirada() {
        return dataLimiteRetirada;
    }

    public void setDataLimiteRetirada(LocalDate dataLimiteRetirada) {
        this.dataLimiteRetirada = dataLimiteRetirada;
    }

    public String resumo() {
        return "#" + idReserva + " - " + alimento.getNome() + " - "
                + Doacao.formatar(quantidadeSolicitada) + " " + alimento.getUnidadeMedida()
                + " [" + status + "]";
    }

    public String detalhes() {
        return "Reserva #" + idReserva + "  [" + status + "]\n"
                + "Alimento: " + alimento.getNome() + "\n"
                + "Quantidade: " + Doacao.formatar(quantidadeSolicitada) + " " + alimento.getUnidadeMedida() + "\n"
                + "Doador: " + nomeDoador + "\n"
                + "Retirar até: " + dataLimiteRetirada.format(DATA) + "\n"
                + "Reservada em: " + dataReserva.format(DATA_HORA);
    }

    @Override
    public String toString() {
        return resumo();
    }
}