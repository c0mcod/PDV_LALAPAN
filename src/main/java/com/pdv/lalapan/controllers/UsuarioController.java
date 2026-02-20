package com.pdv.lalapan.controllers;

import com.pdv.lalapan.dto.usuario.UsuarioAtualizadoDTO;
import com.pdv.lalapan.dto.usuario.UsuarioCreateDTO;
import com.pdv.lalapan.dto.usuario.UsuarioResponseDTO;
import com.pdv.lalapan.services.ExcelExportService;
import com.pdv.lalapan.services.ProdutoService;
import com.pdv.lalapan.services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService userService;

    public UsuarioController(UsuarioService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> create(@RequestBody UsuarioCreateDTO dto) {
        UsuarioResponseDTO response = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuariosAtivos() {
        return ResponseEntity.ok(userService.listarTodosAtivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(userService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioAtualizadoDTO> updateUser(@PathVariable Long id, @RequestBody UsuarioAtualizadoDTO dto) {
        UsuarioAtualizadoDTO response = userService.updateUser(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativarUsuario(@PathVariable Long id) {
        userService.ativar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        userService.desativar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
