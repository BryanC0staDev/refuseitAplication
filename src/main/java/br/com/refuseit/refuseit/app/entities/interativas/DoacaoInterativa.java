package br.com.refuseit.refuseit.app.entities.interativas;

import br.com.refuseit.refuseit.app.dao.DoacaoDao;
import br.com.refuseit.refuseit.app.dao.EmpresaDao;
import br.com.refuseit.refuseit.app.dao.PessoaFisicaDao;
import br.com.refuseit.refuseit.app.entities.Alimento;
import br.com.refuseit.refuseit.app.entities.Doacao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class DoacaoInterativa {

    private final int idUsuario;
    private final String tipoUsuario;   // "PESSOA_FISICA", "EMPRESA" ou "ONG"

    private final DoacaoDao doacaoDao = new DoacaoDao();
    private final EmpresaDao empresaDao = new EmpresaDao();
    private final PessoaFisicaDao pessoaFisicaDao = new PessoaFisicaDao();

    // Um dos dois fica preenchido, conforme o tipo do usuario
    private Integer idEstabelecimento;
    private Integer idPessoaFisica;

    public DoacaoInterativa(int idUsuario, String tipoUsuario) {
        this.idUsuario = idUsuario;
        this.tipoUsuario = tipoUsuario;
    }

    /** Mostra o menu de doacoes ate o usuario escolher "Voltar". */
    public void abrir() {
        try {
            if (identificarDoador()) {
                menuDoacoes();
            }
        } catch (SQLException e) {
            // Qualquer erro de banco mostra a mensagem e volta ao menu principal
            Tela.aviso("Erro no banco de dados:\n" + e.getMessage());
        }
    }

    private void menuDoacoes() throws SQLException {
        while (true) {
            switch (Tela.menu("Doações", "Publicar doação", "Minhas doações", "Cancelar doação", "Voltar")) {
                case 0:
                    publicar();
                    break;
                case 1:
                    listar();
                    break;
                case 2:
                    cancelar();
                    break;
                default:
                    return;   // Voltar (ou fechou a janela)
            }
        }
    }

    /** Descobre se o usuario e um estabelecimento ou uma pessoa fisica doadora. */
    private boolean identificarDoador() throws SQLException {
        if ("EMPRESA".equals(tipoUsuario)) {
            idEstabelecimento = empresaDao.buscarIdPorUsuario(idUsuario);
        } else if ("PESSOA_FISICA".equals(tipoUsuario)) {
            idPessoaFisica = pessoaFisicaDao.buscarIdPorUsuario(idUsuario);
        } else {
            Tela.aviso("Apenas pessoas físicas e empresas podem publicar doações.");
            return false;
        }

        if (idEstabelecimento == null && idPessoaFisica == null) {
            Tela.aviso("Não foi possível identificar o seu cadastro de doador.");
            return false;
        }
        return true;
    }

    // ---------- PUBLICAR ----------

    private void publicar() throws SQLException {
        Alimento alimento = Tela.escolherAlimento();
        if (alimento == null) {
            return;
        }

        BigDecimal quantidade = Tela.pedirNumero(
                "Quantidade total a doar (em " + alimento.getUnidadeMedida() + "):");
        if (quantidade == null) {
            return;
        }

        // O banco exige: validade DEPOIS de hoje; retirada entre hoje e a validade
        LocalDate hoje = LocalDate.now();

        LocalDate validade = Tela.pedirData("Data de validade (dd/mm/aaaa):",
                hoje.plusDays(1), hoje.plusYears(5));
        if (validade == null) {
            return;
        }

        LocalDate limiteRetirada = Tela.pedirData("Data limite para retirada (dd/mm/aaaa):",
                hoje, validade);
        if (limiteRetirada == null) {
            return;
        }

        Doacao doacao = new Doacao();
        doacao.setIdEstabelecimento(idEstabelecimento);
        doacao.setIdPessoaFisica(idPessoaFisica);
        doacao.setAlimento(alimento);
        doacao.setQuantidadeTotal(quantidade);
        doacao.setDataPublicacao(LocalDateTime.now());   // do Java, para bater com as datas acima
        doacao.setDataValidade(validade);
        doacao.setDataLimiteRetirada(limiteRetirada);
        doacao.setCondicaoArmazenamento(Tela.pedirTextoOpcional("Condição de armazenamento (opcional):"));

        int id = doacaoDao.inserir(doacao);
        Tela.info("Doação publicada com sucesso! (código " + id + ")");
    }

    // ---------- MINHAS DOACOES ----------

    private void listar() throws SQLException {
        List<Doacao> doacoes = doacaoDao.listarDoDoador(idEstabelecimento, idPessoaFisica);

        if (doacoes.isEmpty()) {
            Tela.info("Você ainda não publicou nenhuma doação.");
            return;
        }

        StringBuilder texto = new StringBuilder();
        for (Doacao d : doacoes) {
            texto.append(d.detalhes()).append("\n\n");
        }
        Tela.mostrarTexto("Minhas doações", texto.toString());
    }

    // ---------- CANCELAR ----------

    private void cancelar() throws SQLException {
        List<Doacao> disponiveis = new ArrayList<>();
        for (Doacao d : doacaoDao.listarDoDoador(idEstabelecimento, idPessoaFisica)) {
            if ("DISPONIVEL".equals(d.getStatus())) {
                disponiveis.add(d);
            }
        }

        if (disponiveis.isEmpty()) {
            Tela.info("Você não tem doações disponíveis para cancelar.");
            return;
        }

        Doacao doacao = (Doacao) Tela.escolher("Qual doação deseja cancelar?", disponiveis);
        if (doacao == null) {
            return;
        }

        if (!Tela.confirmar("Cancelar esta doação?\n\n" + doacao.resumo())) {
            return;
        }

        if (doacaoDao.cancelar(doacao.getIdDoacao(), idEstabelecimento, idPessoaFisica)) {
            Tela.info("Doação cancelada.");
        } else {
            Tela.aviso("Não foi possível cancelar: o status da doação mudou.");
        }
    }
}