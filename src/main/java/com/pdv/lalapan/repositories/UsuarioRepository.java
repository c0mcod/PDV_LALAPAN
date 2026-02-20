package com.pdv.lalapan.repositories;

import com.pdv.lalapan.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>{

    Optional<Usuario> findByUsername(String Username);

    List<Usuario> findByAtivoTrue();
}
