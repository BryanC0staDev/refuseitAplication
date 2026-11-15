package br.com.refuseit.refuseit.app.conexoes;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    Dotenv dotenv = Dotenv.load();


    private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
    private  final String CONEXAO = dotenv.get("DB_CONNECTION");
    private  final String USUARIO = dotenv.get("DB_USER");
    private  final String SENHA = dotenv.get("DB_PASSWORD");

    public Connection cn() {

        try {
            Class.forName(DRIVER);

            return DriverManager.getConnection(CONEXAO, USUARIO, SENHA);



        } catch (ClassNotFoundException e) {
            System.out.println("Erro ao encontrar o driver do banco de dados!");


        } catch (SQLException e) {
            System.out.println("Erro ao acessar o Banco de Dados. Verifique URL, usuário e senha.");

        }catch (Exception e){
            System.out.println("Erro Desconhecido");
            e.printStackTrace();
        }

        return null;
    }
}