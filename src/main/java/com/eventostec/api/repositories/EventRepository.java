package com.eventostec.api.repositories;

import com.eventostec.api.domain.event.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    // Aqui no repository vamos declarar um novo metodo onde a gente precisa declarar somente a sua assinatura. E agora
    // a gente vai ter que colocar a Query exata do SQL que a gente quer fazer nesse metodo. Então, é selecione o evento
    // da tabela Event onde a data do evento é maior ou igual a data atual.
    @Query("SELECT e FROM Event e WHERE e.date >= :currentDate")
    Page<Event> findUpcomingEvents(@Param("currentDate")Date currentDate, Pageable pageable);
}
