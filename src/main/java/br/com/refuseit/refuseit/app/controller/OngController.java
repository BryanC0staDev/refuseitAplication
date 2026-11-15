package br.com.refuseit.refuseit.app.controller;

import br.com.refuseit.refuseit.app.bo.OngBo;
import br.com.refuseit.refuseit.app.entities.TipoUsuarios.Ong;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.SQLException;

@RestController
@RequestMapping("/ongs")
public class OngController {

    OngBo ongBo = new OngBo();

    // Inserir (body: cnpj, email, senha, telefone, endereco{cep, numero})
    @PostMapping
    public ResponseEntity<Void> inserirRs(@RequestBody Ong ong) throws SQLException {
        int idUsuario = ongBo.inserirBo(ong);

        URI location = URI.create("/usuarios/" + idUsuario);
        return ResponseEntity.created(location).build();
    }

    // Atualizar (body: nomeFantasia, razaoSocial opcional, email, senha opcional, telefone, endereco{cep, numero})
    @PutMapping("/{idUsuario}")
    public ResponseEntity<Void> atualizarRs(@PathVariable int idUsuario,
                                            @RequestBody Ong ong) throws SQLException {
        ongBo.atualizarBo(idUsuario, ong);
        return ResponseEntity.ok().build();
    }
}
