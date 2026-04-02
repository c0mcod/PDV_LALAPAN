package com.pdv.lalapan.controllers;

import com.pdv.lalapan.dto.usuario.UsuarioAtualizadoDTO;
import com.pdv.lalapan.dto.usuario.UsuarioCreateDTO;
import com.pdv.lalapan.dto.usuario.UsuarioResponseDTO;
import com.pdv.lalapan.services.ExcelExportService;
import com.pdv.lalapan.services.ProdutoService;
import com.pdv.lalapan.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Usuários",
        description = "Operações relacionadas ao usuário (CRUD usuário, em breve será adicionado autenticação)"
)
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService userService;

    public UsuarioController(UsuarioService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Criar um usuário",
            description = "Criar um novo usuário"
    )
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> create(@RequestBody UsuarioCreateDTO dto) {
        UsuarioResponseDTO response = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Listar todos os usuários ativos",
            description = "Listar todos os usuários ativos no sistema sem páginação"
    )
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuariosAtivos() {
        return ResponseEntity.ok(userService.listarTodosUsuarios());
    }

    @Operation(
            summary = "Buscar um usuário por ID",
            description = "Encontrar um usuário cadastrado por ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(userService.buscarPorId(id));
    }

    @Operation(
            summary = "Atualizar um usuário",
            description = "Atualizar um usuário cadastrado"
    )
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioAtualizadoDTO> updateUser(@PathVariable Long id, @RequestBody UsuarioAtualizadoDTO dto) {
        UsuarioAtualizadoDTO response = userService.updateUser(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Reativar usuário",
            description = "Reativar um usuário anteriormente desativado"
    )
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativarUsuario(@PathVariable Long id) {
        userService.ativar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Desativar um usuário",
            description = "Desativar um usuário cadastrado"
    )
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        userService.desativar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
