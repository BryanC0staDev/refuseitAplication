package br.com.refuseit.refuseit.app.controller;

import br.com.refuseit.refuseit.app.bo.AlimentoBo;
import br.com.refuseit.refuseit.app.entities.Alimento;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/alimentos")
public class AlimentoController {

    AlimentoBo alimentoBo = new AlimentoBo();

    // Buscar todos
    @GetMapping
    public List<Alimento> buscarTodosRs() throws SQLException {
        return alimentoBo.buscarTodosBo();
    }

    // Buscar por id
    @GetMapping("/{idAlimento}")
    public Alimento buscarPorIdRs(@PathVariable int idAlimento) throws SQLException {
        return alimentoBo.buscarPorIdBo(idAlimento);
    }

    // Inserir (body: nome, unidadeMedida = KG/GRAMA/LITRO/ML/UNIDADE, categoria e descricao opcionais)
    @PostMapping
    public ResponseEntity<Alimento> inserirRs(@RequestBody Alimento alimento) throws SQLException {
        Alimento criado = alimentoBo.inserirBo(alimento);

        URI location = URI.create("/alimentos/" + criado.getIdAlimento());
        return ResponseEntity.created(location).body(criado);
    }
}
