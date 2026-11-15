package br.com.refuseit.refuseit.app.entities.TipoUsuarios;

/**
 * Dados basicos de qualquer usuario (tabela REFUSE_USUARIO).
 * Usado na tela "Meu cadastro" para mostrar e alterar os dados.
 */

public class Usuario {

    private int idUsuario;
    private String nome;
    private String email;
    private String telefone;

    public Usuario() {
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}