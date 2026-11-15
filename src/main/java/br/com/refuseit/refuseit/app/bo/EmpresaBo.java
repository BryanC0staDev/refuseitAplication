package br.com.refuseit.refuseit.app.bo;

import br.com.refuseit.refuseit.app.dao.EmpresaDao;
import br.com.refuseit.refuseit.app.dao.UsuarioDao;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.Empresa;

import java.sql.SQLException;
import java.util.List;

public class EmpresaBo {

    private static final List<String> CATEGORIAS = List.of("RESTAURANTE", "MERCADO", "PADARIA", "PRODUTOR", "OUTRO");

    private String validarCategoria(String categoria) {
        if (categoria == null || !CATEGORIAS.contains(categoria.trim().toUpperCase())) {
            throw new IllegalArgumentException("Categoria inválida. Use uma destas: " + CATEGORIAS);
        }
        return categoria.trim().toUpperCase();
    }

    // Inserir (razão social e fantasia vêm da ReceitaWS; endereço do ViaCEP). Devolve o id_usuario criado
    public int inserirBo(Empresa empresa, String categoria) throws SQLException {
        EmpresaDao empresaDao = new EmpresaDao();
        UsuarioDao usuarioDao = new UsuarioDao();
        UsuarioBo usuarioBo = new UsuarioBo();

        if (empresa == null) {
            throw new IllegalArgumentException("Empresa não pode ser nula.");
        }

        String email = usuarioBo.validarEmail(empresa.getEmail());
        String senha = usuarioBo.validarSenha(empresa.getSenha());
        String telefone = usuarioBo.validarObrigatorio(empresa.getTelefone(), "telefone");
        String categoriaValida = validarCategoria(categoria);

        // valida e consulta o CNPJ na Receita (preenche os dados da empresa)
        usuarioBo.preencherPelaReceita(empresa, empresa.getCnpj());

        if (usuarioDao.emailExiste(email)) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado.");
        }
        if (empresaDao.cnpjExiste(empresa.getCnpj())) {
            throw new IllegalArgumentException("Este CNPJ já está cadastrado.");
        }

        empresa.setEmail(email);
        empresa.setSenha(senha);
        empresa.setTelefone(telefone);
        empresa.setEndereco(usuarioBo.completarEndereco(empresa.getEndereco()));

        return empresaDao.inserir(empresa, categoriaValida);
    }

    // Atualizar
    public void atualizarBo(int idUsuario, Empresa empresa, String categoria) throws SQLException {
        EmpresaDao empresaDao = new EmpresaDao();
        UsuarioBo usuarioBo = new UsuarioBo();

        if (empresa == null) {
            throw new IllegalArgumentException("Empresa não pode ser nula.");
        }
        if (empresaDao.buscarIdPorUsuario(idUsuario) == null) {
            throw new IllegalArgumentException("Empresa não encontrada para este usuário.");
        }

        empresa.setNomeFantasia(usuarioBo.validarObrigatorio(empresa.getNomeFantasia(), "nomeFantasia"));
        empresa.setRazaoSocial(usuarioBo.validarObrigatorio(empresa.getRazaoSocial(), "razaoSocial"));
        empresa.setEmail(usuarioBo.validarEmail(empresa.getEmail()));
        empresa.setTelefone(usuarioBo.validarObrigatorio(empresa.getTelefone(), "telefone"));
        empresa.setSenha(usuarioBo.validarSenhaOpcional(empresa.getSenha()));
        empresa.setEndereco(usuarioBo.completarEndereco(empresa.getEndereco()));

        if (!empresaDao.atualizar(idUsuario, empresa, validarCategoria(categoria))) {
            throw new IllegalArgumentException("Empresa não encontrada para este usuário.");
        }
    }
}
