package br.com.refuseit.refuseit.app.controller;

import br.com.refuseit.refuseit.app.bo.ReservaBo;
import br.com.refuseit.refuseit.app.entities.Reserva;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    ReservaBo reservaBo = new ReservaBo();

    // Buscar as reservas de uma ONG
    @GetMapping("/usuario/{idUsuario}")
    public List<Reserva> buscarDaOngRs(@PathVariable int idUsuario) throws SQLException {
        return reservaBo.buscarDaOngBo(idUsuario);
    }

    // Inserir (body: idDoacao, quantidadeSolicitada)
    @PostMapping("/usuario/{idUsuario}")
    public ResponseEntity<Void> inserirRs(@PathVariable int idUsuario,
                                          @RequestBody Reserva reserva) throws SQLException {
        reservaBo.inserirBo(idUsuario, reserva);

        URI location = URI.create("/reservas/usuario/" + idUsuario);
        return ResponseEntity.created(location).build();
    }

    // Cancelar
    @PutMapping("/usuario/{idUsuario}/{idReserva}/cancelar")
    public ResponseEntity<Void> cancelarRs(@PathVariable int idUsuario,
                                           @PathVariable int idReserva) throws SQLException {
        reservaBo.cancelarBo(idUsuario, idReserva);
        return ResponseEntity.ok().build();
    }
}
