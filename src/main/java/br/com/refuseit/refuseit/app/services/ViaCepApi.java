package br.com.refuseit.refuseit.app.services;


import br.com.refuseit.refuseit.app.api.Endereco;
import com.google.gson.Gson;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;

public class ViaCepApi {

    public Endereco getEndereco(String cep) {
        Endereco endereco = null;
        try {

            HttpGet get = new HttpGet("https://viacep.com.br/ws/" + cep + "/json/");

            CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling().build();

            CloseableHttpResponse response = client.execute(get);

            HttpEntity entity = response.getEntity();

            if (entity != null) {


                String result = EntityUtils.toString(entity);

                Gson gson = new Gson();

                endereco = gson.fromJson(result, Endereco.class);
            }


        } catch (ClientProtocolException e) {
            System.out.println("Erro detectado no client: " + e);
        } catch (IOException e) {
            System.out.println("Erro detectado no response: " + e);
        } catch (Error e) {
            System.out.println("Erro desconhecido detectado: " + e);
        } catch (ParseException e) {
            System.out.println(e);
        }


        return endereco;

    }
}
