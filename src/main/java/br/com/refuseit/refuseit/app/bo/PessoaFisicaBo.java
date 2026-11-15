package br.com.refuseit.refuseit.app.bo;

import br.com.refuseit.refuseit.app.api.Cpf;
import br.com.refuseit.refuseit.app.dao.PessoaFisicaDao;
import br.com.refuseit.refuseit.app.dao.UsuarioDao;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.PessoaFisica;
import br.com.refuseit.refuseit.app.services.CpfHubIo;

import java.sql.SQLException;

public class PessoaFisicaBo {

    // Inserir (nome e nascimento vêm do CPFHub; endereço do ViaCEP). Devolve o id_usuario criado
    public int inserirBo(PessoaFisica pf) throws SQLException {
        PessoaFisicaDao pessoaFisicaDao = new PessoaFisicaDao();
        UsuarioDao usuarioDao = new UsuarioDao();
        UsuarioBo usuarioBo = new UsuarioBo();

        if (pf == null) {
            throw new IllegalArgumentException("Pessoa física não pode ser nula.");
        }

        String email = usuarioBo.validarEmail(pf.getEmail());
        String senha = usuarioBo.validarSenha(pf.getSenha());
        String telefone = usuarioBo.validarObrigatorio(pf.getTelefone(), "telefone");

        String cpf = usuarioBo.validarObrigatorio(pf.getCpf(), "cpf").replaceAll("\\D", "");
        if (!CpfHubIo.cpfValido(cpf)) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        if (usuarioDao.emailExiste(email)) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado.");
        }
        if (pessoaFisicaDao.cpfExiste(cpf)) {
            throw new IllegalArgumentException("Este CPF já está cadastrado.");
        }

        Cpf consulta = new CpfHubIo().getCpf(cpf);
        if (consulta == null || !consulta.isSuccess() || consulta.getData() == null) {
            throw new IllegalArgumentException("CPF não encontrado na consulta.");
        }

        pf.setNome(consulta.getData().getName());
        pf.setAniversario(consulta.getData().getBirthDate());
        pf.setCpf(cpf);
        pf.setEmail(email);
        pf.setSenha(senha);
        pf.setTelefone(telefone);
        pf.setEndereco(usuarioBo.completarEndereco(pf.getEndereco()));

        return pessoaFisicaDao.inserir(pf);
    }

    // Atualizar
    public void atualizarBo(int idUsuario, PessoaFisica pf) throws SQLException {
        PessoaFisicaDao pessoaFisicaDao = new PessoaFisicaDao();
        UsuarioBo usuarioBo = new UsuarioBo();

        if (pf == null) {
            throw new IllegalArgumentException("Pessoa física não pode ser nula.");
        }
        if (pessoaFisicaDao.buscarIdPorUsuario(idUsuario) == null) {
            throw new IllegalArgumentException("Pessoa física não encontrada para este usuário.");
        }

        pf.setNome(usuarioBo.validarObrigatorio(pf.getNome(), "nome"));
        pf.setEmail(usuarioBo.validarEmail(pf.getEmail()));
        pf.setTelefone(usuarioBo.validarObrigatorio(pf.getTelefone(), "telefone"));
        pf.setSenha(usuarioBo.validarSenhaOpcional(pf.getSenha()));
        pf.setAniversario(usuarioBo.validarObrigatorio(pf.getAniversario(), "aniversario"));
        pf.setEndereco(usuarioBo.completarEndereco(pf.getEndereco()));

        if (!pessoaFisicaDao.atualizar(idUsuario, pf)) {
            throw new IllegalArgumentException("Pessoa física não encontrada para este usuário.");
        }
    }
}
