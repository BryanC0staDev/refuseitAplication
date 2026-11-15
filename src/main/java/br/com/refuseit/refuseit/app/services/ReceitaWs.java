package br.com.refuseit.refuseit.app.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import br.com.refuseit.refuseit.app.api.Cnpj;
import com.google.gson.Gson;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import javax.swing.*;


public class ReceitaWs {

    private static final String BASE_URL = "https://receitaws.com.br/v1/cnpj/";


    public String pedirCnpj() {
        String cnpj = JOptionPane.showInputDialog("Digite o CNPJ!");

        if (cnpj == null) {
            JOptionPane.showMessageDialog(null, "O CNPJ NAO PODE SER NULO.", "ERRO", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        cnpj = cnpj.replace(".", "").replace("/", "").replace("-", "").trim();

        if (cnpj.length() != 14) {
            JOptionPane.showMessageDialog(null, "ERRO DE DIGITAÇÃO, DIGITE UM CNPJ EXISTENTE", "ERRO", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        return cnpj;
    }


    public Cnpj getCnpj(String cnpj) {
        Cnpj resposta = null;

        if (cnpj == null) {
            return null;
        }

        cnpj = cnpj.replace(".", "").replace("/", "").replace("-", "");

        if (!cnpjValido(cnpj)) {
            return null;
        }

        HttpGet get = new HttpGet(BASE_URL + cnpj);
        get.setHeader("Accept", "application/json");

        try (CloseableHttpClient client = HttpClients.custom().build()) {

            String result = client.execute(get, response -> {
                int status = response.getCode();

                if (status == 200 && response.getEntity() != null) {
                    return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                }
                if (status == 429) {
                    System.out.println("Limite de consultas da ReceitaWS atingido. Aguarde 1 minuto.");
                } else {
                    System.out.println("Erro na ReceitaWS. HTTP " + status);
                }
                return null;
            });

            if (result != null) {
                Gson gson = new Gson();
                resposta = gson.fromJson(result, Cnpj.class);
            }

        } catch (ClientProtocolException e) {
            System.out.println("Erro detectado no client: " + e);
        } catch (IOException e) {
            System.out.println("Erro detectado no response: " + e);
        }

        return resposta;
    }

    public boolean cnpjExiste(String cnpj) {
        Cnpj r = getCnpj(cnpj);
        return r != null && r.isOk();
    }

    public String getRazaoSocial(String cnpj) {
        Cnpj r = getCnpj(cnpj);
        if (r != null && r.isOk()) {
            return r.getNome();
        }
        return null;
    }


    // ---------- Validacao local dos digitos verificadores do CNPJ ----------

    public static boolean cnpjValido(String cnpj) {
        if (cnpj == null || !cnpj.matches("\\d{14}") || cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }
        return digito(cnpj, 12) == cnpj.charAt(12) - '0'
                && digito(cnpj, 13) == cnpj.charAt(13) - '0';
    }


    private static int digito(String cnpj, int tamanho) {
        int soma = 0;
        int peso = 2;
        for (int i = tamanho - 1; i >= 0; i--) {
            soma += (cnpj.charAt(i) - '0') * peso;
            peso++;
            if (peso > 9) {
                peso = 2;
            }
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}