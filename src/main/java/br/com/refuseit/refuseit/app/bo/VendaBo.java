package br.com.refuseit.refuseit.app.bo;

import br.com.refuseit.refuseit.app.dao.EmpresaDao;
import br.com.refuseit.refuseit.app.dao.VendaDao;
import br.com.refuseit.refuseit.app.entities.Alimento;
import br.com.refuseit.refuseit.app.entities.Venda;

import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class VendaBo {

    private static final ZoneId BRASIL = ZoneId.of("America/Sao_Paulo");

    // Só empresa vende
    private int identificarEmpresa(int idUsuario) throws SQLException {
        EmpresaDao empresaDao = new EmpresaDao();

        if (idUsuario <= 0) {
            throw new IllegalArgumentException("Id do usuário inválido.");
        }

        Integer idEstabelecimento = empresaDao.buscarIdPorUsuario(idUsuario);
        if (idEstabelecimento == null) {
            throw new IllegalArgumentException("Apenas empresas podem vender alimentos.");
        }
        return idEstabelecimento;
    }

    // Inserir (anunciar). Devolve o id do anúncio criado
    public int inserirBo(int idUsuario, Venda venda) throws SQLException {
        VendaDao vendaDao = new VendaDao();
        AlimentoBo alimentoBo = new AlimentoBo();

        int idEstabelecimento = identificarEmpresa(idUsuario);

        if (venda == null) {
            throw new IllegalArgumentException("Anúncio não pode ser nulo.");
        }
        if (venda.getAlimento() == null) {
            throw new IllegalArgumentException("Informe o alimento.");
        }
        if (venda.getQuantidadeTotal() == null || venda.getQuantidadeTotal().signum() <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (venda.getPrecoUnitario() == null || venda.getPrecoUnitario().signum() <= 0) {
            throw new IllegalArgumentException("O preço unitário deve ser maior que zero.");
        }

        LocalDate hoje = LocalDate.now(BRASIL);
        LocalDate validade = venda.getDataValidade();
        if (validade == null || validade.isBefore(hoje.plusDays(1)) || validade.isAfter(hoje.plusYears(5))) {
            throw new IllegalArgumentException("A validade deve estar entre " + hoje.plusDays(1)
                    + " e " + hoje.plusYears(5) + " (formato aaaa-mm-dd).");
        }

        Alimento alimento = alimentoBo.buscarPorIdBo(venda.getAlimento().getIdAlimento());

        venda.setIdEstabelecimento(idEstabelecimento);
        venda.setAlimento(alimento);
        venda.setQuantidadeTotal(venda.getQuantidadeTotal().setScale(2, RoundingMode.HALF_UP));
        venda.setPrecoUnitario(venda.getPrecoUnitario().setScale(2, RoundingMode.HALF_UP));
        venda.setDataPublicacao(LocalDateTime.now(BRASIL));

        return vendaDao.inserir(venda);
    }

    // Buscar os anúncios da empresa
    public List<Venda> buscarDaEmpresaBo(int idUsuario) throws SQLException {
        VendaDao vendaDao = new VendaDao();

        return vendaDao.listarDoVendedor(identificarEmpresa(idUsuario));
    }

    // Buscar disponíveis (o que as ONGs podem comprar)
    public List<Venda> buscarDisponiveisBo() throws SQLException {
        VendaDao vendaDao = new VendaDao();

        return vendaDao.listarDisponiveis(LocalDate.now(BRASIL));
    }

    // Cancelar
    public void cancelarBo(int idUsuario, int idVenda) throws SQLException {
        VendaDao vendaDao = new VendaDao();

        int idEstabelecimento = identificarEmpresa(idUsuario);

        if (!vendaDao.cancelar(idVenda, idEstabelecimento)) {
            throw new IllegalArgumentException("Não foi possível cancelar: o anúncio não existe, "
                    + "não é seu ou o status mudou. (Compras já feitas continuam valendo.)");
        }
    }
}
