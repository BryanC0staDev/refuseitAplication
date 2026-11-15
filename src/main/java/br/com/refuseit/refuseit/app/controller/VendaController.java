package br.com.refuseit.refuseit.app.controller;

import br.com.refuseit.refuseit.app.bo.VendaBo;
import br.com.refuseit.refuseit.app.entities.Venda;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/vendas")
public class VendaController {

    VendaBo vendaBo = new VendaBo();

    // Buscar disponíveis (o que as ONGs podem comprar)
    @GetMapping("/disponiveis")
    public List<Venda> buscarDisponiveisRs() throws SQLException {
        return vendaBo.buscarDisponiveisBo();
    }

    // Buscar os anúncios de uma empresa
    @GetMapping("/usuario/{idUsuario}")
    public List<Venda> buscarDaEmpresaRs(@PathVariable int idUsuario) throws SQLException {
        return vendaBo.buscarDaEmpresaBo(idUsuario);
    }

    // Inserir (body: alimento{idAlimento}, quantidadeTotal, precoUnitario, dataValidade, condicaoArmazenamento)
    @PostMapping("/usuario/{idUsuario}")
    public ResponseEntity<Void> inserirRs(@PathVariable int idUsuario,
                                          @RequestBody Venda venda) throws SQLException {
        int idVenda = vendaBo.inserirBo(idUsuario, venda);

        URI location = URI.create("/vendas/usuario/" + idUsuario);
        return ResponseEntity.created(location).header("X-Id-Venda", String.valueOf(idVenda)).build();
    }

    // Cancelar
    @PutMapping("/usuario/{idUsuario}/{idVenda}/cancelar")
    public ResponseEntity<Void> cancelarRs(@PathVariable int idUsuario,
                                           @PathVariable int idVenda) throws SQLException {
        vendaBo.cancelarBo(idUsuario, idVenda);
        return ResponseEntity.ok().build();
    }
}
