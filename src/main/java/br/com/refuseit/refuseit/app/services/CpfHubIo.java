package br.com.refuseit.refuseit.app.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import br.com.refuseit.refuseit.app.api.Cpf;
import com.google.gson.Gson;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import javax.swing.*;


public class CpfHubIo {

    Dotenv dotenv = Dotenv.load();

    private static final String BASE_URL = "https://api.cpfhub.io/cpf/";
    private final String CPF_API_KEY = dotenv.get("CPF_API_KEY");


    public String pedirCpf() {
        String cpf = JOptionPane.showInputDialog("Digite seu Cpf!");

        if (cpf == null) {
            JOptionPane.showMessageDialog(null, "O CPF NAO PODE SER NULO.", "ERRO", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        cpf = cpf.replace(".", "").replace("-", "").trim();

        if (cpf.length() != 11) {
            JOptionPane.showMessageDialog(null, "ERRO DE DIGITAÇÃO, DIGITE UM CPF EXISTENTE", "ERRO", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        return cpf;
    }

    public Cpf getCpf(String cpf) {
        Cpf resposta = null;




        if (!cpfValido(cpf)) {
            return null;
        }

        HttpGet get = new HttpGet(BASE_URL + cpf);
        get.setHeader("x-api-key", CPF_API_KEY);
        get.setHeader("Accept", "application/json");

        try (CloseableHttpClient client = HttpClients.custom().build()) {

            String result = client.execute(get, response -> {
                int status = response.getCode();

                if (status == 200 && response.getEntity() != null) {
                    return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                }
                if (status != 400 && status != 404) {
                    System.out.println("Erro na API CPFHub.io. HTTP " + status);
                }
                return null;
            });

            if (result != null) {
                Gson gson = new Gson();
                resposta = gson.fromJson(result, Cpf.class);
            }

        } catch (ClientProtocolException e) {
            System.out.println("Erro detectado no client: " + e);
        } catch (IOException e) {
            System.out.println("Erro detectado no response: " + e);
        }

        return resposta;
    }


    public String getNome(Cpf cpf){
        return   cpf.getData().getName();
    }
    public String getAniversario(Cpf cpf){
        return cpf.getData().getBirthDate();
    }



    public boolean cpfExiste(String cpf) {
        Cpf r = getCpf(cpf);
        return r != null && r.isSuccess() && r.getData() != null;
    }


    public static boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        return digito(cpf, 9) == cpf.charAt(9) - '0'
                && digito(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int digito(String cpf, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (cpf.charAt(i) - '0') * (tamanho + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

}