package br.com.refuseit.refuseit.app.bo;

import br.com.refuseit.refuseit.app.dao.OngDao;
import br.com.refuseit.refuseit.app.dao.UsuarioDao;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.Ong;

import java.sql.SQLException;

public class OngBo {

    private static final String TIPO_INSTITUICAO = "ONG";

    // Inserir (dados da instituição vêm da ReceitaWS; endereço do ViaCEP). Devolve o id_usuario criado
    public int inserirBo(Ong ong) throws SQLException {
        OngDao ongDao = new OngDao();
        UsuarioDao usuarioDao = new UsuarioDao();
        UsuarioBo usuarioBo = new UsuarioBo();

        if (ong == null) {
            throw new IllegalArgumentException("ONG não pode ser nula.");
        }

        String email = usuarioBo.validarEmail(ong.getEmail());
        String senha = usuarioBo.validarSenha(ong.getSenha());
        String telefone = usuarioBo.validarObrigatorio(ong.getTelefone(), "telefone");

        // valida e consulta o CNPJ na Receita (preenche os dados da ONG)
        usuarioBo.preencherPelaReceita(ong, ong.getCnpj());

        if (usuarioDao.emailExiste(email)) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado.");
        }
        if (ongDao.cnpjExiste(ong.getCnpj())) {
            throw new IllegalArgumentException("Este CNPJ já está cadastrado.");
        }

        ong.setEmail(email);
        ong.setSenha(senha);
        ong.setTelefone(telefone);
        ong.setEndereco(usuarioBo.completarEndereco(ong.getEndereco()));

        return ongDao.inserir(ong, TIPO_INSTITUICAO);
    }

    // Atualizar
    public void atualizarBo(int idUsuario, Ong ong) throws SQLException {
        OngDao ongDao = new OngDao();
        UsuarioBo usuarioBo = new UsuarioBo();

        if (ong == null) {
            throw new IllegalArgumentException("ONG não pode ser nula.");
        }
        if (ongDao.buscarIdPorUsuario(idUsuario) == null) {
            throw new IllegalArgumentException("ONG não encontrada para este usuário.");
        }

        ong.setNomeFantasia(usuarioBo.validarObrigatorio(ong.getNomeFantasia(), "nomeFantasia"));
        ong.setEmail(usuarioBo.validarEmail(ong.getEmail()));
        ong.setTelefone(usuarioBo.validarObrigatorio(ong.getTelefone(), "telefone"));
        ong.setSenha(usuarioBo.validarSenhaOpcional(ong.getSenha()));
        ong.setEndereco(usuarioBo.completarEndereco(ong.getEndereco()));

        if (!ongDao.atualizar(idUsuario, ong, TIPO_INSTITUICAO)) {
            throw new IllegalArgumentException("ONG não encontrada para este usuário.");
        }
    }
}
