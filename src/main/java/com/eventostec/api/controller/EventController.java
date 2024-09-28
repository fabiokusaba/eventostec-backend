package com.eventostec.api.controller;

import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/event")
public class EventController {

    @Autowired
    private EventService eventService;

    // Basicamente o que a gente precisa fazer é ao invés de receber um request body sendo o nosso DTO a gente vai ter
    // que mapear cada uma das entradas todas como RequestParam e depois a gente vai ter que montar o nosso DTO na mão
    // porque o Spring não consegue fazer essa conversão quando a gente recebe em formato de multipart/form-data porque
    // isso daqui vem tudo como RequestParam, então mapeamos todos esses params e montamos o nosso DTO. E precisamos
    // também falar que o nosso PostMapping consome do tipo multipart/form-data para ele conseguir aceitar esse tipo de
    // requisição.
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Event> create(@RequestParam("title") String title,
                                        @RequestParam(value = "description", required = false) String description,
                                        @RequestParam("date") Long date,
                                        @RequestParam("city") String city,
                                        @RequestParam("state") String state,
                                        @RequestParam("remote") Boolean remote,
                                        @RequestParam("eventUrl") String eventUrl,
                                        @RequestParam(value = "image", required = false) MultipartFile image) {
        EventRequestDTO eventRequestDTO = new EventRequestDTO(title, description, date, city, state, remote, eventUrl, image);
        Event newEvent = this.eventService.createEvent(eventRequestDTO);
        return ResponseEntity.ok(newEvent);
    }
}