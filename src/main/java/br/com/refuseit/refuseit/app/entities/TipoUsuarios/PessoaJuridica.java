package br.com.refuseit.refuseit.app.entities.TipoUsuarios;

import br.com.refuseit.refuseit.app.api.Cnpj;
import br.com.refuseit.refuseit.app.api.Endereco;
import br.com.refuseit.refuseit.app.services.ReceitaWs;

import javax.swing.*;

public class PessoaJuridica {

    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String telefone;
    private String email;
    private String senha;
    private String dataAbertura;
    private String situacao;
    private String porte;

    private Endereco endereco = new Endereco();

    private ReceitaWs receitaWs = new ReceitaWs();

    public PessoaJuridica() {
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(String dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public String getPorte() {
        return porte;
    }

    public void setPorte(String porte) {
        this.porte = porte;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public boolean pegarDadosCnpj() {

        String digitado = receitaWs.pedirCnpj();
        Cnpj resposta = receitaWs.getCnpj(digitado);

        if (resposta == null || !resposta.isOk()) {
            return false;
        }

        this.razaoSocial = resposta.getNome();
        this.nomeFantasia = resposta.getFantasia();
        this.cnpj = resposta.getCnpj();
        this.dataAbertura = resposta.getAbertura();
        this.situacao = resposta.getSituacao();
        this.porte = resposta.getPorte();
        return true;
    }


    public void cadastroPJ() {
        String pedidaEmail = JOptionPane.showInputDialog("Digite seu email: ");
        if (pedidaEmail == null || !pedidaEmail.contains("@")) {
            JOptionPane.showMessageDialog(null, "EMAIL INVALIDO!", "ERRO", JOptionPane.WARNING_MESSAGE);
            return;
        }
        this.email = pedidaEmail;

        String pedidaSenha = JOptionPane.showInputDialog("Digite sua Senha");
        if (pedidaSenha == null) {
            return;
        }
        if (pedidaSenha.length() < 11) {
            JOptionPane.showMessageDialog(null, "SENHA CURTA, DIGITE AO MENOS 11 CARACTERES!", "ERRO", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (!pedidaSenha.matches(".*[!@#$%&*].*")) {
            JOptionPane.showMessageDialog(null, "SENHA FRACA, DIGITE AO MENOS 1 CARACTER ESPECIAL!", "ERRO", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (!pedidaSenha.matches(".*[A-Z].*")) {
            JOptionPane.showMessageDialog(null, "SENHA FRACA, DIGITE AO MENOS 1 LETRA MAIUSCULA!", "ERRO", JOptionPane.WARNING_MESSAGE);
            return;
        }
        this.senha = pedidaSenha;

        if (!pegarDadosCnpj()) {
            JOptionPane.showMessageDialog(null, "CNPJ INVALIDO OU NAO ENCONTRADO!", "ERRO", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pedidoTelefone = JOptionPane.showInputDialog("Digite seu telefone: ");
        if (pedidoTelefone == null || pedidoTelefone.isBlank()) {
            JOptionPane.showMessageDialog(null, "TELEFONE NAO PODE SER VAZIO!", "ERRO", JOptionPane.WARNING_MESSAGE);
            return;
        }
        this.telefone = pedidoTelefone;

        endereco.cadastroEndereco();
    }public void alterarDados() {

        String novaRazao = JOptionPane.showInputDialog(
                null, "Nova razão social:", razaoSocial);

        if (novaRazao != null && !novaRazao.isBlank()) {
            this.razaoSocial = novaRazao;
        }

        String novoNome = JOptionPane.showInputDialog(
                null, "Novo nome fantasia:", nomeFantasia);

        if (novoNome != null && !novoNome.isBlank()) {
            this.nomeFantasia = novoNome;
        }

        String novoEmail = JOptionPane.showInputDialog(
                null, "Novo email:", email);

        if (novoEmail != null && novoEmail.contains("@")) {
            this.email = novoEmail;
        }

        String novoTelefone = JOptionPane.showInputDialog(
                null, "Novo telefone:", telefone);

        if (novoTelefone != null && !novoTelefone.isBlank()) {
            this.telefone = novoTelefone;
        }

        String novaSenha = JOptionPane.showInputDialog(
                null, "Nova senha:");

        if (novaSenha != null && !novaSenha.isBlank()) {

            if (novaSenha.length() >= 11
                    && novaSenha.matches(".*[!@#$%&*].*")
                    && novaSenha.matches(".*[A-Z].*")) {

                this.senha = novaSenha;

            } else {
                JOptionPane.showMessageDialog(
                        null, "Senha não atende aos requisitos!");
            }
        }

        int opcao = JOptionPane.showConfirmDialog(
                null,
                "Deseja alterar o endereço?",
                "Alterar endereço",
                JOptionPane.YES_NO_OPTION
        );

        if (opcao == JOptionPane.YES_OPTION) {
            endereco.cadastroEndereco();
        }

        JOptionPane.showMessageDialog(
                null, "Dados atualizados com sucesso!");
    }


    @Override
    public String toString() {
        return
                "\nrazao social= " + razaoSocial +
                        "\nnome fantasia= " + nomeFantasia +
                        "\ncnpj= " + cnpj +
                        "\ntelefone= " + telefone +
                        "\nemail= " + email +
                        "\ndata de abertura= " + dataAbertura +
                        "\nsituacao= " + situacao +
                        "\nporte= " + porte +
                        "\n" + endereco;
    }
}