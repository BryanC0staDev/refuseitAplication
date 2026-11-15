package br.com.refuseit.refuseit.app.Testes;


import br.com.refuseit.refuseit.app.entities.Login;
import br.com.refuseit.refuseit.app.entities.MenuPrincipal;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.PessoaFisica;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.PessoaJuridica;
import br.com.refuseit.refuseit.app.entities.interativas.Tela;

public class testes {
    public static void main(String[] args) {

        Login login = new Login();

        while (login.iniciar()) {
            new MenuPrincipal(login.getIdUsuarioLogado(), login.getTipoLogado()).abrir();
        }
    }




    }

