package br.com.refuseit.refuseit.app.bo;

import br.com.refuseit.refuseit.app.dao.DoacaoDao;
import br.com.refuseit.refuseit.app.dao.EmpresaDao;
import br.com.refuseit.refuseit.app.dao.PessoaFisicaDao;
import br.com.refuseit.refuseit.app.entities.Alimento;
import br.com.refuseit.refuseit.app.entities.Doacao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class DoacaoBo {

    private static final ZoneId BRASIL = ZoneId.of("America/Sao_Paulo");

    // Descobre se quem doa é empresa ou pessoa física. Devolve {idEstabelecimento, idPessoaFisica}
    private Integer[] identificarDoador(int idUsuario) throws SQLException {
        EmpresaDao empresaDao = new EmpresaDao();
        PessoaFisicaDao pessoaFisicaDao = new PessoaFisicaDao();

        if (idUsuario <= 0) {
            throw new IllegalArgumentException("Id do usuário inválido.");
        }

        Integer idEstabelecimento = empresaDao.buscarIdPorUsuario(idUsuario);
        if (idEstabelecimento != null) {
            return new Integer[]{idEstabelecimento, null};
        }

        Integer idPessoaFisica = pessoaFisicaDao.buscarIdPorUsuario(idUsuario);
        if (idPessoaFisica != null) {
            return new Integer[]{null, idPessoaFisica};
        }

        throw new IllegalArgumentException("Apenas pessoas físicas e empresas podem publicar doações.");
    }

    // Inserir. Devolve o id da doação criada
    public int inserirBo(int idUsuario, Doacao doacao) throws SQLException {
        DoacaoDao doacaoDao = new DoacaoDao();
        AlimentoBo alimentoBo = new AlimentoBo();

        Integer[] doador = identificarDoador(idUsuario);

        if (doacao == null) {
            throw new IllegalArgumentException("Doação não pode ser nula.");
        }
        if (doacao.getAlimento() == null) {
            throw new IllegalArgumentException("Informe o alimento.");
        }
        if (doacao.getQuantidadeTotal() == null || doacao.getQuantidadeTotal().signum() <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }

        // Regras do banco: validade DEPOIS de hoje; retirada entre hoje e a validade
        LocalDate hoje = LocalDate.now(BRASIL);

        LocalDate validade = doacao.getDataValidade();
        if (validade == null || validade.isBefore(hoje.plusDays(1)) || validade.isAfter(hoje.plusYears(5))) {
            throw new IllegalArgumentException("A validade deve estar entre " + hoje.plusDays(1)
                    + " e " + hoje.plusYears(5) + " (formato aaaa-mm-dd).");
        }

        LocalDate limite = doacao.getDataLimiteRetirada();
        if (limite == null || limite.isBefore(hoje) || limite.isAfter(validade)) {
            throw new IllegalArgumentException("O limite de retirada deve estar entre " + hoje
                    + " e a data de validade (formato aaaa-mm-dd).");
        }

        Alimento alimento = alimentoBo.buscarPorIdBo(doacao.getAlimento().getIdAlimento());

        doacao.setIdEstabelecimento(doador[0]);
        doacao.setIdPessoaFisica(doador[1]);
        doacao.setAlimento(alimento);
        doacao.setDataPublicacao(LocalDateTime.now(BRASIL));

        return doacaoDao.inserir(doacao);
    }

    // Buscar as doações do doador
    public List<Doacao> buscarDoDoadorBo(int idUsuario) throws SQLException {
        DoacaoDao doacaoDao = new DoacaoDao();

        Integer[] doador = identificarDoador(idUsuario);
        return doacaoDao.listarDoDoador(doador[0], doador[1]);
    }

    // Buscar disponíveis (o que as ONGs podem reservar)
    public List<Doacao> buscarDisponiveisBo() throws SQLException {
        DoacaoDao doacaoDao = new DoacaoDao();

        return doacaoDao.listarDisponiveis(LocalDate.now(BRASIL));
    }

    // Cancelar
    public void cancelarBo(int idUsuario, int idDoacao) throws SQLException {
        DoacaoDao doacaoDao = new DoacaoDao();

        Integer[] doador = identificarDoador(idUsuario);

        if (!doacaoDao.cancelar(idDoacao, doador[0], doador[1])) {
            throw new IllegalArgumentException("Não foi possível cancelar: a doação não existe, não é sua "
                    + "ou o status mudou (doação com reserva não pode ser cancelada).");
        }
    }
}
