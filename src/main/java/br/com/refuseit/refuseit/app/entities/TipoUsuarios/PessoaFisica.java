package br.com.refuseit.refuseit.app.entities.TipoUsuarios;

import br.com.refuseit.refuseit.app.api.Cpf;
import br.com.refuseit.refuseit.app.api.Endereco;
import br.com.refuseit.refuseit.app.services.CpfHubIo;

import javax.swing.*;

public class PessoaFisica {

    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private String senha;
    private String aniversario;

    Endereco endereco = new Endereco();

    CpfHubIo cpfHubIo = new CpfHubIo();

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public PessoaFisica(String telefone, String email) {
        this.telefone = telefone;
        this.email = email;
    }

    public PessoaFisica(Endereco endereco) {
        this.endereco = endereco;
    }

    public PessoaFisica() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAniversario() {
        return aniversario;
    }

    public void setAniversario(String aniversario) {
        this.aniversario = aniversario;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public boolean pegarDadosCpf() {

        String digitado = cpfHubIo.pedirCpf();
        Cpf resposta = cpfHubIo.getCpf(digitado);

        if (resposta == null || !resposta.isSuccess() || resposta.getData() == null) {
            return false;
        }

        this.nome = resposta.getData().getName();
        this.cpf = resposta.getData().getCpf();
        this.aniversario = resposta.getData().getBirthDate();
        return true;
    }


    public void cadastroPF(){
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

        if (!pegarDadosCpf()) {
            JOptionPane.showMessageDialog(null, "CPF INVALIDO OU NAO ENCONTRADO!", "ERRO", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pedidoTelefone = JOptionPane.showInputDialog("Digite seu telefone: ");
        if (pedidoTelefone == null || pedidoTelefone.isBlank()) {
            JOptionPane.showMessageDialog(null, "TELEFONE NAO PODE SER VAZIO!", "ERRO", JOptionPane.WARNING_MESSAGE);
            return;
        }
        this.telefone = pedidoTelefone;

        endereco.cadastroEndereco();
    }

    public void alterarDados() {

        String novoNome = JOptionPane.showInputDialog(
                null, "Novo nome:", nome);

        if (novoNome != null && !novoNome.isBlank()) {
            this.nome = novoNome;
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
                "\nnome= " + nome +
                "\ncpf= " + cpf +
                "\ntelefone=" + telefone +
                "\nemail= " + email +
                "\nData de Aniversario = "+ aniversario+endereco;

    }
}
