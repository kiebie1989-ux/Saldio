package com.bwa.controlling.benutzer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BenutzerRepository extends JpaRepository<Benutzer, String> {
    List<Benutzer> findAllByOrderByBenutzernameAsc();
}
