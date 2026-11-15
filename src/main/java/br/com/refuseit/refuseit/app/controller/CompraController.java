package br.com.refuseit.refuseit.app.controller;

import br.com.refuseit.refuseit.app.bo.CompraBo;
import br.com.refuseit.refuseit.app.entities.Compra;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/compras")
public class CompraController {

    CompraBo compraBo = new CompraBo();

    // ---------- ONG (compra) ----------

    // Buscar as compras de uma ONG
    @GetMapping("/ong/{idUsuario}")
    public List<Compra> buscarDaOngRs(@PathVariable int idUsuario) throws SQLException {
        return compraBo.buscarDaOngBo(idUsuario);
    }

    // Inserir (body: idVenda, quantidade, metodoPagamento = PIX/DINHEIRO/TRANSFERENCIA)
    @PostMapping("/ong/{idUsuario}")
    public ResponseEntity<Void> inserirRs(@PathVariable int idUsuario,
                                          @RequestBody Compra compra) throws SQLException {
        compraBo.inserirBo(idUsuario, compra);

        URI location = URI.create("/compras/ong/" + idUsuario);
        return ResponseEntity.created(location).build();
    }

    // Cancelar (só PENDENTE)
    @PutMapping("/ong/{idUsuario}/{idCompra}/cancelar")
    public ResponseEntity<Void> cancelarRs(@PathVariable int idUsuario,
                                           @PathVariable int idCompra) throws SQLException {
        compraBo.cancelarBo(idUsuario, idCompra);
        return ResponseEntity.ok().build();
    }

    // ---------- Empresa (vende) ----------

    // Buscar as compras recebidas
    @GetMapping("/empresa/{idUsuario}")
    public List<Compra> buscarRecebidasRs(@PathVariable int idUsuario) throws SQLException {
        return compraBo.buscarRecebidasBo(idUsuario);
    }

    // Confirmar a compra
    @PutMapping("/empresa/{idUsuario}/{idCompra}/confirmar")
    public ResponseEntity<Void> confirmarRs(@PathVariable int idUsuario,
                                            @PathVariable int idCompra) throws SQLException {
        compraBo.confirmarBo(idUsuario, idCompra);
        return ResponseEntity.ok().build();
    }

    // Recusar a compra
    @PutMapping("/empresa/{idUsuario}/{idCompra}/recusar")
    public ResponseEntity<Void> recusarRs(@PathVariable int idUsuario,
                                          @PathVariable int idCompra) throws SQLException {
        compraBo.recusarBo(idUsuario, idCompra);
        return ResponseEntity.ok().build();
    }

    // Confirmar o pagamento
    @PutMapping("/empresa/{idUsuario}/{idCompra}/confirmar-pagamento")
    public ResponseEntity<Void> confirmarPagamentoRs(@PathVariable int idUsuario,
                                                     @PathVariable int idCompra) throws SQLException {
        compraBo.confirmarPagamentoBo(idUsuario, idCompra);
        return ResponseEntity.ok().build();
    }

    // Concluir
    @PutMapping("/empresa/{idUsuario}/{idCompra}/concluir")
    public ResponseEntity<Void> concluirRs(@PathVariable int idUsuario,
                                           @PathVariable int idCompra) throws SQLException {
        compraBo.concluirBo(idUsuario, idCompra);
        return ResponseEntity.ok().build();
    }
}
