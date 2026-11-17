package br.com.refuseit.refuseit.app.main;

import br.com.refuseit.refuseit.app.entities.Login;
import br.com.refuseit.refuseit.app.entities.MenuPrincipal;

public class refuseit_App {

    public static void main(String[] args) {
        Login login = new Login();

        while (login.iniciar()) {
            new MenuPrincipal(login.getIdUsuarioLogado(), login.getTipoLogado()).abrir();
        }
    }

}

