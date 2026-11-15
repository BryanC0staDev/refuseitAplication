package br.com.refuseit.refuseit.app.entities;

import br.com.refuseit.refuseit.app.entities.interativas.AlterarDadosInterativa;
import br.com.refuseit.refuseit.app.entities.interativas.DoacaoInterativa;
import br.com.refuseit.refuseit.app.entities.interativas.VisualizarDadosInterativa;
import br.com.refuseit.refuseit.app.entities.interativas.CompraInterativa;
import br.com.refuseit.refuseit.app.entities.interativas.VendaInterativa;
import javax.swing.*;

public class MenuPrincipal {

    private final int idUsuario;
    private final String tipoUsuario;   // "PESSOA_FISICA", "EMPRESA" ou "ONG"

    public MenuPrincipal(int idUsuario, String tipoUsuario) {
        this.idUsuario = idUsuario;
        this.tipoUsuario = tipoUsuario;
    }

    public void abrir() {
        boolean continuar = true;

        while (continuar) {
            if ("ONG".equals(tipoUsuario)) {
                continuar = menuOng();

            } else if ("EMPRESA".equals(tipoUsuario)) {
                continuar = menuEmpresa();

            } else {
                continuar = menuDoador();
            }
        }
    }


    private boolean menuEmpresa() {
        switch (menu(
                "Menu principal",
                "Vendas",
                "Doações",
                "Ver meus dados",
                "Alterar dados",
                "Sair"
        )) {
            case 0:
                new VendaInterativa(idUsuario, tipoUsuario).abrir();
                return true;

            case 1:
                new DoacaoInterativa(idUsuario, tipoUsuario).abrir();
                return true;

            case 2:
                new VisualizarDadosInterativa(idUsuario, tipoUsuario).abrir();
                return true;

            case 3:
                new AlterarDadosInterativa(idUsuario, tipoUsuario).abrir();
                return true;

            default:
                return false;
        }
    }

    private boolean menuDoador() {
        switch (menu("Menu principal", "Doações", "Ver meus dados", "Alterar dados", "Sair")) {
            case 0:
                new DoacaoInterativa(idUsuario, tipoUsuario).abrir();
                return true;
            case 1:
                new VisualizarDadosInterativa(idUsuario, tipoUsuario).abrir();
                return true;
            case 2:
                new AlterarDadosInterativa(idUsuario, tipoUsuario).abrir();
                return true;
            default:
                return false;   // Sair (ou fechou a janela)
        }
    }

    private boolean menuOng() {
        switch (menu("Menu principal", "Reservar Alimento", "Ver meus dados", "Alterar dados", "Sair")) {
            case 0:
                new CompraInterativa(idUsuario, tipoUsuario).abrir();
                return true;
            case 1:
                new VisualizarDadosInterativa(idUsuario, tipoUsuario).abrir();
                return true;
            case 2:
                new AlterarDadosInterativa(idUsuario, tipoUsuario).abrir();
                return true;
            default:
                return false;
        }
    }

    private int menu(String mensagem, String... opcoes) {
        return JOptionPane.showOptionDialog(null, mensagem, "Refuse It",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opcoes, opcoes[0]);
    }
}
