package br.com.refuseit.refuseit.app.api;


public class Cnpj {

    private String status;
    private String message;
    private String cnpj;
    private String nome;               // razao social
    private String fantasia;           // nome fantasia
    private String abertura;           // dd/mm/aaaa
    private String situacao;           // ATIVA, BAIXADA, SUSPENSA...
    private String tipo;               // MATRIZ ou FILIAL
    private String porte;
    private String natureza_juridica;
    private String telefone;
    private String email;

    public boolean isOk() {
        return "OK".equals(status);
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getNome() {
        return nome;
    }

    public String getFantasia() {
        return fantasia;
    }

    public String getAbertura() {
        return abertura;
    }

    public String getSituacao() {
        return situacao;
    }

    public String getTipo() {
        return tipo;
    }

    public String getPorte() {
        return porte;
    }

    public String getNaturezaJuridica() {
        return natureza_juridica;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }
}