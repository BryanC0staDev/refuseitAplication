package br.com.refuseit.refuseit.app.entities.interativas;

import br.com.refuseit.refuseit.app.dao.UsuarioDao;

import javax.swing.*;
import java.sql.SQLException;

public class VisualizarDadosInterativa {

    private final int idUsuario;
    private final String tipoUsuario;
    private final UsuarioDao usuarioDao = new UsuarioDao();

    public VisualizarDadosInterativa(int idUsuario, String tipoUsuario) {
        this.idUsuario = idUsuario;
        this.tipoUsuario = tipoUsuario;
    }

    public void abrir() {
        try {
            String dados = usuarioDao.visualizarDados(idUsuario, tipoUsuario);
            JTextArea area = new JTextArea(dados, 18, 42);
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setCaretPosition(0);

            JScrollPane rolagem = new JScrollPane(area);
            JOptionPane.showMessageDialog(null, rolagem, "Meus dados",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Nao foi possivel consultar seus dados:\n" + e.getMessage(),
                    "Refuse It", JOptionPane.ERROR_MESSAGE);
        }
    }
}
