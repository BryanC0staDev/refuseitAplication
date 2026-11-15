package br.com.refuseit.refuseit.app.controller;

import br.com.refuseit.refuseit.app.bo.EmpresaBo;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.Empresa;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.SQLException;

@RestController
@RequestMapping("/empresas")
public class EmpresaController {

    EmpresaBo empresaBo = new EmpresaBo();

    // Inserir (body: cnpj, email, senha, telefone, endereco{cep, numero}; query: ?categoria=RESTAURANTE)
    @PostMapping
    public ResponseEntity<Void> inserirRs(@RequestBody Empresa empresa,
                                          @RequestParam String categoria) throws SQLException {
        int idUsuario = empresaBo.inserirBo(empresa, categoria);

        URI location = URI.create("/usuarios/" + idUsuario);
        return ResponseEntity.created(location).build();
    }

    // Atualizar (body: nomeFantasia, razaoSocial, email, senha opcional, telefone, endereco{cep, numero}; query: ?categoria=)
    @PutMapping("/{idUsuario}")
    public ResponseEntity<Void> atualizarRs(@PathVariable int idUsuario,
                                            @RequestBody Empresa empresa,
                                            @RequestParam String categoria) throws SQLException {
        empresaBo.atualizarBo(idUsuario, empresa, categoria);
        return ResponseEntity.ok().build();
    }
}
