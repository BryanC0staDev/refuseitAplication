package br.com.refuseit.refuseit.app.entities;

import br.com.refuseit.refuseit.app.dao.EmpresaDao;
import br.com.refuseit.refuseit.app.dao.OngDao;
import br.com.refuseit.refuseit.app.dao.PessoaFisicaDao;
import br.com.refuseit.refuseit.app.dao.UsuarioDao;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.Empresa;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.Ong;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.PessoaFisica;

import javax.swing.*;
import java.sql.SQLException;


public class Login {

    private static final int REGISTRAR = 0;
    private static final int LOGIN = 1;

    private static final int PESSOA_FISICA = 0;
    private static final int EMPRESA = 1;
    private static final int ONG = 2;

    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final PessoaFisicaDao pessoaFisicaDao = new PessoaFisicaDao();
    private final EmpresaDao empresaDao = new EmpresaDao();
    private final OngDao ongDao = new OngDao();

    private Integer idUsuarioLogado;
    private String tipoLogado;   // "PESSOA_FISICA", "EMPRESA" ou "ONG"

    public Integer getIdUsuarioLogado() {
        return idUsuarioLogado;
    }

    public String getTipoLogado() {
        return tipoLogado;
    }


    public boolean iniciar() {
        while (true) {
            int acao = menu("O que você deseja fazer?", "Registrar-se", "Login");
            if (acao == JOptionPane.CLOSED_OPTION) {
                return false;   // fechou a janela
            }

            int tipo = menu("Qual é o seu tipo de usuário?", "Pessoa Física", "Empresa", "ONG");
            if (tipo == JOptionPane.CLOSED_OPTION) {
                continue;       // voltou: mostra o menu principal de novo
            }

            boolean entrou;
            try {
                entrou = (acao == REGISTRAR) ? registrar(tipo) : logar(tipo);
            } catch (SQLException e) {
                aviso("Erro no banco de dados:\n" + e.getMessage());
                entrou = false;
            }

            if (entrou) {
                return true;
            }
        }
    }

    // ---------- REGISTRAR-SE ----------

    private boolean registrar(int tipo) throws SQLException {
        switch (tipo) {
            case PESSOA_FISICA:
                return registrarPessoaFisica();
            case EMPRESA:
                return registrarEmpresa();
            default:
                return registrarOng();
        }
    }

    private boolean registrarPessoaFisica() throws SQLException {
        PessoaFisica pf = new PessoaFisica();
        pf.cadastroPF();

        if (pf.getEndereco().getCep() == null) {
            aviso("Cadastro cancelado ou incompleto.");
            return false;
        }
        if (usuarioDao.emailExiste(pf.getEmail())) {
            aviso("Este e-mail já está cadastrado.");
            return false;
        }
        if (pessoaFisicaDao.cpfExiste(pf.getCpf())) {
            aviso("Este CPF já está cadastrado.");
            return false;
        }

        int id = pessoaFisicaDao.inserir(pf);
        concluirRegistro(id, "PESSOA_FISICA");
        return true;
    }

    private boolean registrarEmpresa() throws SQLException {
        String[] categorias = {"RESTAURANTE", "MERCADO", "PADARIA", "PRODUTOR", "OUTRO"};
        String categoria = (String) JOptionPane.showInputDialog(null,
                "Categoria do estabelecimento:", "Refuse It",
                JOptionPane.QUESTION_MESSAGE, null, categorias, categorias[0]);
        if (categoria == null) {
            return false;
        }

        Empresa empresa = new Empresa();
        empresa.cadastroPJ();

        if (empresa.getEndereco().getCep() == null) {
            aviso("Cadastro cancelado ou incompleto.");
            return false;
        }
        if (usuarioDao.emailExiste(empresa.getEmail())) {
            aviso("Este e-mail já está cadastrado.");
            return false;
        }
        if (empresaDao.cnpjExiste(empresa.getCnpj())) {
            aviso("Este CNPJ já está cadastrado.");
            return false;
        }

        int id = empresaDao.inserir(empresa, categoria);
        concluirRegistro(id, "EMPRESA");
        return true;
    }

    private boolean registrarOng() throws SQLException {
        Ong ong = new Ong();
        ong.cadastroPJ();

        if (ong.getEndereco().getCep() == null) {
            aviso("Cadastro cancelado ou incompleto.");
            return false;
        }
        if (usuarioDao.emailExiste(ong.getEmail())) {
            aviso("Este e-mail já está cadastrado.");
            return false;
        }
        if (ongDao.cnpjExiste(ong.getCnpj())) {
            aviso("Este CNPJ já está cadastrado.");
            return false;
        }

        int id = ongDao.inserir(ong, "ONG");
        concluirRegistro(id, "ONG");
        return true;
    }

    private void concluirRegistro(int id, String tipo) {
        this.idUsuarioLogado = id;
        this.tipoLogado = tipo;
        JOptionPane.showMessageDialog(null, "Cadastro realizado com sucesso!", "Refuse It",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------- LOGIN ----------

    private boolean logar(int tipo) throws SQLException {
        String email = JOptionPane.showInputDialog("E-mail:");
        if (email == null) {
            return false;
        }

        String senha = pedirSenha();
        if (senha == null) {
            return false;
        }

        Integer id;
        String nomeTipo;
        switch (tipo) {
            case PESSOA_FISICA:
                id = pessoaFisicaDao.autenticar(email, senha);
                nomeTipo = "PESSOA_FISICA";
                break;
            case EMPRESA:
                id = empresaDao.autenticar(email, senha);
                nomeTipo = "EMPRESA";
                break;
            default:
                id = ongDao.autenticar(email, senha);
                nomeTipo = "ONG";
                break;
        }

        if (id == null) {
            aviso("E-mail ou senha incorretos, ou o tipo de usuário escolhido não confere.");
            return false;
        }

        this.idUsuarioLogado = id;
        this.tipoLogado = nomeTipo;
        JOptionPane.showMessageDialog(null, "Bem-vindo(a), " + usuarioDao.buscarNome(id) + "!",
                "Refuse It", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    // ---------- Auxiliares ----------

    private int menu(String mensagem, String... opcoes) {
        return JOptionPane.showOptionDialog(null, mensagem, "Refuse It",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opcoes, opcoes[0]);
    }

    private String pedirSenha() {
        JPasswordField campo = new JPasswordField();
        int resposta = JOptionPane.showConfirmDialog(null, campo, "Senha:",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resposta != JOptionPane.OK_OPTION) {
            return null;
        }
        return new String(campo.getPassword());
    }

    private void aviso(String texto) {
        JOptionPane.showMessageDialog(null, texto, "Refuse It", JOptionPane.WARNING_MESSAGE);
    }


}