package br.com.refuseit.refuseit.app.api;

public class Cpf {

    private boolean success;
    private Data data;

    public boolean isSuccess() {
        return success;
    }

    public Data getData() {
        return data;
    }

    public static class Data {
        private String cpf;
        private String name;
        private String birthDate;

        public String getCpf() {
            return cpf;
        }

        public String getName() {
            return name;
        }
        public String getBirthDate() {
            return birthDate;
        }

        public void setCpf(String cpf) {
            this.cpf = cpf;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setBirthDate(String birthDate) {
            this.birthDate = birthDate;
        }

        public Data(String cpf, String name, String birthDate) {
            this.cpf = cpf;
            this.name = name;
            this.birthDate = birthDate;
        }

        public Data() {
        }
    }

    public Cpf(boolean success, Data data) {
        this.success = success;
        this.data = data;
    }

    public Cpf() {
    }

}