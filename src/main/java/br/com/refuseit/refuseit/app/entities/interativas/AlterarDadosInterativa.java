package br.com.refuseit.refuseit.app.entities.interativas;

import br.com.refuseit.refuseit.app.api.Endereco;
import br.com.refuseit.refuseit.app.dao.EmpresaDao;
import br.com.refuseit.refuseit.app.dao.OngDao;
import br.com.refuseit.refuseit.app.dao.PessoaFisicaDao;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.Empresa;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.Ong;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.PessoaFisica;

import javax.swing.*;
import java.sql.SQLException;

public class AlterarDadosInterativa {

    private final int idUsuario;
    private final String tipoUsuario;

    private final PessoaFisicaDao pessoaFisicaDao = new PessoaFisicaDao();
    private final EmpresaDao empresaDao = new EmpresaDao();
    private final OngDao ongDao = new OngDao();

    public AlterarDadosInterativa(int idUsuario, String tipoUsuario) {
        this.idUsuario = idUsuario;
        this.tipoUsuario = tipoUsuario;
    }

    public void abrir() {
        try {
            boolean atualizado;
            if ("PESSOA_FISICA".equals(tipoUsuario)) {
                atualizado = alterarPessoaFisica();
            } else if ("EMPRESA".equals(tipoUsuario)) {
                atualizado = alterarEmpresa();
            } else if ("ONG".equals(tipoUsuario)) {
                atualizado = alterarOng();
            } else {
                aviso("Tipo de usuário não reconhecido.");
                return;
            }

            if (atualizado) {
                JOptionPane.showMessageDialog(null, "Dados atualizados com sucesso!", "Refuse It",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            aviso("Não foi possível atualizar os dados:\n" + e.getMessage());
        } catch (CancelamentoEdicao e) {
            // O usuário cancelou; volta ao menu sem alterar o banco.
        }
    }

    private boolean alterarPessoaFisica() throws SQLException {
        PessoaFisica pf = new PessoaFisica();
        pf.setNome(textoObrigatorio("Nome completo:"));
        pf.setEmail(email());
        pf.setTelefone(textoObrigatorio("Telefone:"));
        pf.setSenha(senhaOpcional());
        pf.setAniversario(textoObrigatorio("Data de nascimento (formato usado no cadastro):"));
        pf.setEndereco(endereco());
        return pessoaFisicaDao.atualizar(idUsuario, pf);
    }

    private boolean alterarEmpresa() throws SQLException {
        Empresa empresa = new Empresa();
        empresa.setNomeFantasia(textoObrigatorio("Nome fantasia:"));
        empresa.setRazaoSocial(textoObrigatorio("Razão social:"));
        empresa.setEmail(email());
        empresa.setTelefone(textoObrigatorio("Telefone:"));
        empresa.setSenha(senhaOpcional());
        String[] categorias = {"RESTAURANTE", "MERCADO", "PADARIA", "PRODUTOR", "OUTRO"};
        String categoria = (String) JOptionPane.showInputDialog(null, "Categoria do estabelecimento:",
                "Refuse It", JOptionPane.QUESTION_MESSAGE, null, categorias, categorias[0]);
        if (categoria == null) throw new CancelamentoEdicao();
        empresa.setEndereco(endereco());
        return empresaDao.atualizar(idUsuario, empresa, categoria);
    }

    private boolean alterarOng() throws SQLException {
        Ong ong = new Ong();
        ong.setNomeFantasia(textoObrigatorio("Nome da instituição:"));
        ong.setEmail(email());
        ong.setTelefone(textoObrigatorio("Telefone:"));
        ong.setSenha(senhaOpcional());
        ong.setEndereco(endereco());
        return ongDao.atualizar(idUsuario, ong, "ONG");
    }

    private String email() {
        String valor = textoObrigatorio("E-mail:");
        if (!valor.contains("@")) {
            aviso("Informe um e-mail válido.");
            throw new CancelamentoEdicao();
        }
        return valor;
    }

    private String senhaOpcional() {
        String senha = JOptionPane.showInputDialog(null,
                "Nova senha (deixe em branco para manter a senha atual):", "Refuse It",
                JOptionPane.QUESTION_MESSAGE);
        if (senha == null) throw new CancelamentoEdicao();
        return senha.isBlank() ? null : senha;
    }

    private Endereco endereco() {
        Endereco end = new Endereco();
        end.setCep(textoObrigatorio("CEP (somente números ou com hífen):"));
        end.setLogradouro(textoObrigatorio("Logradouro (rua/avenida):"));
        end.setNumero(textoObrigatorio("Número:"));
        end.setBairro(textoObrigatorio("Bairro:"));
        end.setLocalidade(textoObrigatorio("Cidade:"));
        end.setUf(textoObrigatorio("Estado (UF):").toUpperCase());
        return end;
    }

    private String textoObrigatorio(String mensagem) {
        String valor = JOptionPane.showInputDialog(null, mensagem, "Alterar dados",
                JOptionPane.QUESTION_MESSAGE);
        if (valor == null) throw new CancelamentoEdicao();
        if (valor.isBlank()) {
            aviso("Este campo não pode ficar vazio.");
            throw new CancelamentoEdicao();
        }
        return valor.trim();
    }

    private void aviso(String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem, "Refuse It", JOptionPane.WARNING_MESSAGE);
    }

    private static class CancelamentoEdicao extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
