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
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.address a WHERE e.date >= :currentDate")
    Page<Event> findUpcomingEvents(@Param("currentDate")Date currentDate, Pageable pageable);

    // Podemos adicionar um novo metodo pra fazer o filtro por data, local e título do evento. Como é que a gente vai
    // fazer esse filtro primeiro a gente vai ter que fazer um join das duas tabelas, então a gente vai fazer uma
    // consulta não só na tabela de Event mas também na tabela Address porque a gente vai estar procurando por cidade e
    // estado também pra juntar os resultados da tabela de Event com os resultados da tabela de Address, feito esse join
    // a gente faz uma comparação se as informações são parecidas.
    @Query("SELECT e FROM Event e " +
            "LEFT JOIN e.address a " +
            "WHERE (:title = '' OR e.title LIKE %:title%) " +
            "AND (:city = '' OR a.city LIKE %:city%) " +
            "AND (:uf = '' OR a.uf LIKE %:uf%) " +
            "AND (e.date >= :startDate AND e.date <= :endDate)")
    Page<Event> findFilteredEvents(@Param("title") String title,
                                   @Param("city") String city,
                                   @Param("uf") String uf,
                                   @Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate,
                                   Pageable pageable);
}
