package br.com.refuseit.refuseit.app.bo;

import br.com.refuseit.refuseit.app.dao.DoacaoDao;
import br.com.refuseit.refuseit.app.dao.OngDao;
import br.com.refuseit.refuseit.app.dao.ReservaDao;
import br.com.refuseit.refuseit.app.entities.Doacao;
import br.com.refuseit.refuseit.app.entities.Reserva;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class ReservaBo {

    private static final ZoneId BRASIL = ZoneId.of("America/Sao_Paulo");

    // Só ONG reserva
    private int identificarOng(int idUsuario) throws SQLException {
        OngDao ongDao = new OngDao();

        if (idUsuario <= 0) {
            throw new IllegalArgumentException("Id do usuário inválido.");
        }

        Integer idInstituicao = ongDao.buscarIdPorUsuario(idUsuario);
        if (idInstituicao == null) {
            throw new IllegalArgumentException("Apenas ONGs podem reservar doações.");
        }
        return idInstituicao;
    }

    // Inserir (reservar): usa idDoacao e quantidadeSolicitada da reserva recebida
    public void inserirBo(int idUsuario, Reserva reserva) throws SQLException {
        ReservaDao reservaDao = new ReservaDao();
        DoacaoDao doacaoDao = new DoacaoDao();

        int idInstituicao = identificarOng(idUsuario);

        if (reserva == null) {
            throw new IllegalArgumentException("Reserva não pode ser nula.");
        }
        if (reserva.getIdDoacao() <= 0) {
            throw new IllegalArgumentException("Informe a doação (idDoacao).");
        }
        if (reserva.getQuantidadeSolicitada() == null || reserva.getQuantidadeSolicitada().signum() <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }

        BigDecimal quantidade = reserva.getQuantidadeSolicitada().setScale(2, RoundingMode.HALF_UP);
        LocalDate hoje = LocalDate.now(BRASIL);

        Doacao doacao = null;
        for (Doacao d : doacaoDao.listarDisponiveis(hoje)) {
            if (d.getIdDoacao() == reserva.getIdDoacao()) {
                doacao = d;
                break;
            }
        }
        if (doacao == null) {
            throw new IllegalArgumentException("Essa doação não está mais disponível (acabou, venceu ou não existe).");
        }
        if (quantidade.compareTo(doacao.getQuantidadeDisponivel()) > 0) {
            throw new IllegalArgumentException("Quantidade maior que a disponível ("
                    + doacao.getQuantidadeDisponivel() + ").");
        }

        // O DAO confere tudo de novo e baixa o saldo numa instrução só (protege contra 2 ONGs ao mesmo tempo)
        if (!reservaDao.reservar(reserva.getIdDoacao(), idInstituicao, quantidade, hoje)) {
            throw new IllegalArgumentException("Não foi possível reservar: a doação acabou, venceu "
                    + "ou foi reservada por outra ONG.");
        }
    }

    // Buscar as reservas da ONG
    public List<Reserva> buscarDaOngBo(int idUsuario) throws SQLException {
        ReservaDao reservaDao = new ReservaDao();

        return reservaDao.listarDaInstituicao(identificarOng(idUsuario));
    }

    // Cancelar
    public void cancelarBo(int idUsuario, int idReserva) throws SQLException {
        ReservaDao reservaDao = new ReservaDao();

        int idInstituicao = identificarOng(idUsuario);

        if (!reservaDao.cancelar(idReserva, idInstituicao)) {
            throw new IllegalArgumentException("Não foi possível cancelar: a reserva não existe, "
                    + "não é sua ou o status mudou. (Só cancela PENDENTE ou APROVADA.)");
        }
    }
}
