package br.com.refuseit.refuseit.app.controller;

import br.com.refuseit.refuseit.app.bo.UsuarioBo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    UsuarioBo usuarioBo = new UsuarioBo();

    // Login (body: email, senha e tipo = PESSOA_FISICA, EMPRESA ou ONG)
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> autenticarRs(@RequestBody Map<String, String> credenciais) throws SQLException {
        Map<String, Object> usuario = usuarioBo.autenticarBo(
                credenciais.get("email"), credenciais.get("senha"), credenciais.get("tipo"));

        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(usuario);
    }

    // Visualizar dados
    @GetMapping("/{idUsuario}")
    public String visualizarDadosRs(@PathVariable int idUsuario, @RequestParam String tipo) throws SQLException {
        return usuarioBo.visualizarDadosBo(idUsuario, tipo);
    }

    // Email existe
    @GetMapping("/email-existe")
    public boolean emailExisteRs(@RequestParam String email) throws SQLException {
        return usuarioBo.emailExisteBo(email);
    }

    // Desativar conta
    @DeleteMapping("/{idUsuario}")
    public ResponseEntity<Void> desativarRs(@PathVariable int idUsuario) throws SQLException {
        usuarioBo.desativarBo(idUsuario);
        return ResponseEntity.ok().build();
    }
}
