package br.com.refuseit.refuseit.app.controller;

import br.com.refuseit.refuseit.app.bo.DoacaoBo;
import br.com.refuseit.refuseit.app.entities.Doacao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/doacoes")
public class DoacaoController {

    DoacaoBo doacaoBo = new DoacaoBo();

    // Buscar disponíveis (o que as ONGs podem reservar)
    @GetMapping("/disponiveis")
    public List<Doacao> buscarDisponiveisRs() throws SQLException {
        return doacaoBo.buscarDisponiveisBo();
    }

    // Buscar as doações de um doador (pessoa física ou empresa)
    @GetMapping("/usuario/{idUsuario}")
    public List<Doacao> buscarDoDoadorRs(@PathVariable int idUsuario) throws SQLException {
        return doacaoBo.buscarDoDoadorBo(idUsuario);
    }

    // Inserir (body: alimento{idAlimento}, quantidadeTotal, dataValidade, dataLimiteRetirada, condicaoArmazenamento)
    @PostMapping("/usuario/{idUsuario}")
    public ResponseEntity<Void> inserirRs(@PathVariable int idUsuario,
                                          @RequestBody Doacao doacao) throws SQLException {
        int idDoacao = doacaoBo.inserirBo(idUsuario, doacao);

        URI location = URI.create("/doacoes/usuario/" + idUsuario);
        return ResponseEntity.created(location).header("X-Id-Doacao", String.valueOf(idDoacao)).build();
    }

    // Cancelar
    @PutMapping("/usuario/{idUsuario}/{idDoacao}/cancelar")
    public ResponseEntity<Void> cancelarRs(@PathVariable int idUsuario,
                                           @PathVariable int idDoacao) throws SQLException {
        doacaoBo.cancelarBo(idUsuario, idDoacao);
        return ResponseEntity.ok().build();
    }
}
