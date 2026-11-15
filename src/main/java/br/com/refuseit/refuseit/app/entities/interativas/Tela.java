package br.com.refuseit.refuseit.app.entities.interativas;

import br.com.refuseit.refuseit.app.dao.AlimentoDao;
import br.com.refuseit.refuseit.app.entities.Alimento;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;


public class Tela {

    // "uuuu" + STRICT: rejeita datas que nao existem, como 31/02/2026
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final String NOVO_ALIMENTO = "➕ Cadastrar novo alimento";
    private static final AlimentoDao ALIMENTO_DAO = new AlimentoDao();

    // ---------- Mensagens ----------

    public static void info(String texto) {
        JOptionPane.showMessageDialog(null, texto, "Refuse It", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void aviso(String texto) {
        JOptionPane.showMessageDialog(null, texto, "Refuse It", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirmar(String texto) {
        return JOptionPane.showConfirmDialog(null, texto, "Refuse It",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }


    public static int menu(String mensagem, String... opcoes) {
        return JOptionPane.showOptionDialog(null, mensagem, "Refuse It",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opcoes, opcoes[0]);
    }

    public static Object escolher(String mensagem, List<?> itens) {
        return JOptionPane.showInputDialog(null, mensagem, "Refuse It",
                JOptionPane.QUESTION_MESSAGE, null, itens.toArray(), itens.get(0));
    }

    public static void mostrarTexto(String titulo, String texto) {
        JTextArea area = new JTextArea(texto, 18, 50);
        area.setEditable(false);
        JOptionPane.showMessageDialog(null, new JScrollPane(area), titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------- Entradas ----------

    public static String pedirTextoOpcional(String mensagem) {
        String texto = JOptionPane.showInputDialog(mensagem);
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    public static BigDecimal pedirNumero(String mensagem) {
        while (true) {
            String texto = JOptionPane.showInputDialog(mensagem);
            if (texto == null) {
                return null;
            }

            try {
                BigDecimal numero = new BigDecimal(texto.trim().replace(",", "."))
                        .setScale(2, RoundingMode.HALF_UP);

                if (numero.signum() > 0) {
                    return numero;
                }
                aviso("O valor deve ser maior que zero.");
            } catch (NumberFormatException e) {
                aviso("Número inválido. Exemplo: 10 ou 2,5");
            }
        }
    }


    public static LocalDate pedirData(String mensagem, LocalDate minima, LocalDate maxima) {
        while (true) {
            String texto = JOptionPane.showInputDialog(mensagem);
            if (texto == null) {
                return null;
            }

            try {
                LocalDate data = LocalDate.parse(texto.trim(), FORMATO_DATA);

                if (data.isBefore(minima) || data.isAfter(maxima)) {
                    aviso("A data deve ficar entre " + minima.format(FORMATO_DATA)
                            + " e " + maxima.format(FORMATO_DATA) + ".");
                } else {
                    return data;
                }
            } catch (DateTimeParseException e) {
                aviso("Data inválida. Use o formato dd/mm/aaaa (ex: 25/12/2026).");
            }
        }
    }


    public static Alimento escolherAlimento() throws SQLException {
        List<Alimento> alimentos = ALIMENTO_DAO.listar();

        Object[] opcoes = new Object[alimentos.size() + 1];
        for (int i = 0; i < alimentos.size(); i++) {
            opcoes[i] = alimentos.get(i);
        }
        opcoes[alimentos.size()] = NOVO_ALIMENTO;

        Object escolha = JOptionPane.showInputDialog(null, "Qual alimento?", "Refuse It",
                JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);

        if (escolha == null) {
            return null;
        }
        if (escolha instanceof Alimento) {
            return (Alimento) escolha;
        }
        return cadastrarNovoAlimento();
    }

    private static Alimento cadastrarNovoAlimento() throws SQLException {
        String nome = JOptionPane.showInputDialog("Nome do alimento:");
        if (nome == null || nome.isBlank()) {
            return null;
        }
        nome = nome.trim();

        // Se ja existe no catalogo, usa o que ja esta la (evita "Arroz" e "arroz")
        Alimento existente = ALIMENTO_DAO.buscarPorNome(nome);
        if (existente != null) {
            info("Esse alimento já existe no catálogo (unidade: " + existente.getUnidadeMedida() + ").");
            return existente;
        }

        String[] unidades = {"KG", "GRAMA", "LITRO", "ML", "UNIDADE"};
        String unidade = (String) JOptionPane.showInputDialog(null, "Unidade de medida:", "Refuse It",
                JOptionPane.QUESTION_MESSAGE, null, unidades, unidades[0]);
        if (unidade == null) {
            return null;
        }

        Alimento alimento = new Alimento();
        alimento.setNome(nome);
        alimento.setCategoria("Outros");
        alimento.setUnidadeMedida(unidade);
        alimento.setIdAlimento(ALIMENTO_DAO.inserir(alimento));
        return alimento;
    }
}