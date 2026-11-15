package br.com.refuseit.refuseit.app.controller;

import br.com.refuseit.refuseit.app.bo.PessoaFisicaBo;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.PessoaFisica;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.SQLException;

@RestController
@RequestMapping("/pessoas-fisicas")
public class PessoaFisicaController {

    PessoaFisicaBo pessoaFisicaBo = new PessoaFisicaBo();

    // Inserir (body: cpf, email, senha, telefone, endereco{cep, numero})
    @PostMapping
    public ResponseEntity<Void> inserirRs(@RequestBody PessoaFisica pessoaFisica) throws SQLException {
        int idUsuario = pessoaFisicaBo.inserirBo(pessoaFisica);

        URI location = URI.create("/usuarios/" + idUsuario);
        return ResponseEntity.created(location).build();
    }

    // Atualizar (body: nome, email, senha opcional, telefone, aniversario, endereco{cep, numero})
    @PutMapping("/{idUsuario}")
    public ResponseEntity<Void> atualizarRs(@PathVariable int idUsuario,
                                            @RequestBody PessoaFisica pessoaFisica) throws SQLException {
        pessoaFisicaBo.atualizarBo(idUsuario, pessoaFisica);
        return ResponseEntity.ok().build();
    }
}
