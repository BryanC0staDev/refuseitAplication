package br.com.refuseit.refuseit.app.bo;

import br.com.refuseit.refuseit.app.dao.AlimentoDao;
import br.com.refuseit.refuseit.app.entities.Alimento;

import java.sql.SQLException;
import java.util.List;

public class AlimentoBo {

    private static final List<String> UNIDADES = List.of("KG", "GRAMA", "LITRO", "ML", "UNIDADE");

    // Buscar todos
    public List<Alimento> buscarTodosBo() throws SQLException {
        AlimentoDao alimentoDao = new AlimentoDao();

        return alimentoDao.listar();
    }

    public Alimento buscarPorIdBo(int idAlimento) throws SQLException {
        AlimentoDao alimentoDao = new AlimentoDao();

        if (idAlimento <= 0) {
            throw new IllegalArgumentException("Id do alimento inválido.");
        }

        for (Alimento a : alimentoDao.listar()) {
            if (a.getIdAlimento() == idAlimento) {
                return a;
            }
        }
        throw new IllegalArgumentException("Alimento " + idAlimento + " não encontrado.");
    }

    // Inserir
    public Alimento inserirBo(Alimento alimento) throws SQLException {
        AlimentoDao alimentoDao = new AlimentoDao();

        if (alimento == null) {
            throw new IllegalArgumentException("Alimento não pode ser nulo.");
        }
        if (alimento.getNome() == null || alimento.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
        if (alimento.getNome().trim().length() > 100) {
            throw new IllegalArgumentException("O nome pode ter no máximo 100 caracteres.");
        }
        if (alimento.getUnidadeMedida() == null
                || !UNIDADES.contains(alimento.getUnidadeMedida().trim().toUpperCase())) {
            throw new IllegalArgumentException("Unidade de medida inválida. Use uma destas: " + UNIDADES);
        }

        // evita "Arroz" e "arroz" duplicados (o DAO compara sem diferenciar maiúscula)
        Alimento existente = alimentoDao.buscarPorNome(alimento.getNome());
        if (existente != null) {
            throw new IllegalArgumentException("Esse alimento já existe no catálogo (id "
                    + existente.getIdAlimento() + ").");
        }

        alimento.setNome(alimento.getNome().trim());
        alimento.setUnidadeMedida(alimento.getUnidadeMedida().trim().toUpperCase());
        if (alimento.getCategoria() == null || alimento.getCategoria().trim().isEmpty()) {
            alimento.setCategoria("Outros");
        }

        alimento.setIdAlimento(alimentoDao.inserir(alimento));
        return alimento;
    }
}
