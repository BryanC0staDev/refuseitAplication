package br.com.refuseit.refuseit.app.entities.TipoUsuarios;

public class Empresa extends PessoaJuridica {

    public Empresa() {
    }

    @Override
    public String toString() {
        return "\n=== EMPRESA ===" + super.toString();
    }
}