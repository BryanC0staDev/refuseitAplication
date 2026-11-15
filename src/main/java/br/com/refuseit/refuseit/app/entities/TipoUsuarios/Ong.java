package br.com.refuseit.refuseit.app.entities.TipoUsuarios;

public class Ong extends PessoaJuridica {

    public Ong() {
    }

    @Override
    public String toString() {
        return "\n=== ONG ===" + super.toString();
    }
}