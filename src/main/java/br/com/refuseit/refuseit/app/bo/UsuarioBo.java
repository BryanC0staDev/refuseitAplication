package br.com.refuseit.refuseit.app.bo;

import br.com.refuseit.refuseit.app.api.Cnpj;
import br.com.refuseit.refuseit.app.api.Endereco;
import br.com.refuseit.refuseit.app.dao.EmpresaDao;
import br.com.refuseit.refuseit.app.dao.OngDao;
import br.com.refuseit.refuseit.app.dao.PessoaFisicaDao;
import br.com.refuseit.refuseit.app.dao.UsuarioDao;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.PessoaJuridica;
import br.com.refuseit.refuseit.app.services.ReceitaWs;
import br.com.refuseit.refuseit.app.services.ViaCepApi;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UsuarioBo {

    //Validações (também usadas pelas outras BOs de cadastro)//

    public String validarObrigatorio(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo " + campo + " é obrigatório.");
        }
        return valor.trim();
    }

    public String validarEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido.");
        }
        return email.trim().toLowerCase();
    }

    // 11+ caracteres, 1 especial e 1 maiúscula (mesma regra do cadastro original)
    public String validarSenha(String senha) {
        if (senha == null || senha.length() < 11) {
            throw new IllegalArgumentException("Senha curta: use ao menos 11 caracteres.");
        }
        if (!senha.matches(".*[!@#$%&*].*")) {
            throw new IllegalArgumentException("Senha fraca: use ao menos 1 caractere especial (!@#$%&*).");
        }
        if (!senha.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Senha fraca: use ao menos 1 letra maiúscula.");
        }
        return senha;
    }

    // Na alteração de dados, senha em branco significa "manter a atual"
    public String validarSenhaOpcional(String senha) {
        if (senha == null || senha.trim().isEmpty()) {
            return null;
        }
        return validarSenha(senha);
    }

    // Recebe cep + numero e completa rua, bairro, cidade e UF pelo ViaCEP
    public Endereco completarEndereco(Endereco entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException("Endereço é obrigatório.");
        }

        String cep = entrada.getCep() == null ? "" : entrada.getCep().replaceAll("\\D", "");
        if (cep.length() != 8) {
            throw new IllegalArgumentException("CEP inválido: informe 8 dígitos.");
        }
        if (entrada.getNumero() == null || entrada.getNumero().trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o número do endereço.");
        }

        Endereco encontrado;
        try {
            encontrado = new ViaCepApi().getEndereco(cep);
        } catch (RuntimeException e) {
            encontrado = null; // o ViaCEP devolve algo que não é JSON quando o CEP é malformado
        }

        // CEP que não existe volta {"erro": true}: cidade e UF ficam nulos
        if (encontrado == null || encontrado.getLocalidade() == null || encontrado.getUf() == null) {
            throw new IllegalArgumentException("CEP não encontrado.");
        }

        encontrado.setCep(cep);
        encontrado.setNumero(entrada.getNumero().trim());
        return encontrado;
    }

    // Valida o CNPJ e preenche razão social, fantasia, abertura, situação e porte pela ReceitaWS
    public void preencherPelaReceita(PessoaJuridica pj, String cnpjDigitado) {
        String cnpj = validarObrigatorio(cnpjDigitado, "cnpj").replaceAll("\\D", "");

        if (!ReceitaWs.cnpjValido(cnpj)) {
            throw new IllegalArgumentException("CNPJ inválido.");
        }

        Cnpj resposta = new ReceitaWs().getCnpj(cnpj);
        if (resposta == null || !resposta.isOk()) {
            throw new IllegalArgumentException("Não foi possível confirmar o CNPJ na Receita "
                    + "(inexistente ou limite de consultas atingido). Tente novamente em 1 minuto.");
        }

        pj.setCnpj(cnpj);
        pj.setRazaoSocial(resposta.getNome());
        pj.setNomeFantasia(resposta.getFantasia());
        pj.setDataAbertura(resposta.getAbertura());
        pj.setSituacao(resposta.getSituacao());
        pj.setPorte(resposta.getPorte());
    }

    //Metodos Bo//

    // Login: devolve idUsuario, tipo e nome (ou null se email/senha/tipo não conferem)
    public Map<String, Object> autenticarBo(String email, String senha, String tipo) throws SQLException {
        UsuarioDao usuarioDao = new UsuarioDao();

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o email.");
        }
        if (senha == null || senha.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe a senha.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Informe o tipo: PESSOA_FISICA, EMPRESA ou ONG.");
        }

        Integer id;
        switch (tipo.trim().toUpperCase()) {
            case "PESSOA_FISICA":
                id = new PessoaFisicaDao().autenticar(email, senha);
                break;
            case "EMPRESA":
                id = new EmpresaDao().autenticar(email, senha);
                break;
            case "ONG":
                id = new OngDao().autenticar(email, senha);
                break;
            default:
                throw new IllegalArgumentException("Tipo inválido. Use PESSOA_FISICA, EMPRESA ou ONG.");
        }

        if (id == null) {
            return null;
        }

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("idUsuario", id);
        resposta.put("tipo", tipo.trim().toUpperCase());
        resposta.put("nome", usuarioDao.buscarNome(id));
        return resposta;
    }

    // Visualizar dados
    public String visualizarDadosBo(int idUsuario, String tipo) throws SQLException {
        UsuarioDao usuarioDao = new UsuarioDao();

        if (idUsuario <= 0) {
            throw new IllegalArgumentException("Id do usuário inválido.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Informe o tipo: PESSOA_FISICA, EMPRESA ou ONG.");
        }
        // o DAO lança SQLException se não achar; conferimos antes
        if (usuarioDao.buscarNome(idUsuario) == null) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        return usuarioDao.visualizarDados(idUsuario, tipo.trim().toUpperCase());
    }

    // Email existe
    public boolean emailExisteBo(String email) throws SQLException {
        UsuarioDao usuarioDao = new UsuarioDao();

        return usuarioDao.emailExiste(validarEmail(email));
    }

    // Desativar
    public void desativarBo(int idUsuario) throws SQLException {
        UsuarioDao usuarioDao = new UsuarioDao();

        if (!usuarioDao.desativar(idUsuario)) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
    }
}
