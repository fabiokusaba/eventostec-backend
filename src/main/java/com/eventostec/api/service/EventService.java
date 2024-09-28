package com.eventostec.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AddressService addressService;

    public Event createEvent(EventRequestDTO data) {
        String imgUrl = null;

        if (data.image() != null) {
            imgUrl = this.uploadImg(data.image());
        }

        Event newEvent = new Event();
        newEvent.setTitle(data.title());
        newEvent.setDescription(data.description());
        newEvent.setEventUrl(data.eventUrl());

        // Provavelmente do frontend a gente vai receber somente um timestamp gerado pelo JS, então na meu DTO ao invés
        // de um objeto Date quando eu receber isso daqui isso daqui vai ser um Long, um timestamp gerado pelo front aí
        // eu vou pegar esse timestamp gerado pelo front e vou transformar num objeto Date para então consolidar isso no
        // meu banco de dados e salvar no formato certo que a gente criou a nossa coluna.
        newEvent.setDate(new Date(data.date()));
        newEvent.setImgUrl(imgUrl);
        newEvent.setRemote(data.remote());

        // Salvando o nosso evento no banco de dados.
        eventRepository.save(newEvent);

        // Depois de registrar esse evento na tabela/banco de dados eu criei uma condicional que se o evento não for
        // remoto, então eu chamo o meu addressService e crio o address na tabela de endereços.
        if (!data.remote()) {
            this.addressService.createAddress(data, newEvent);
        }

        // Retornando o evento criado.
        return newEvent;
    }

    // Aqui vamos utilizar uma funcionalidade bem legal do Spring que é o utilitário de paginação, então o Spring já tem
    // isso implementado basta a gente utilizar.
    public List<EventResponseDTO> getUpcomingEvents(int page, int size) {
        // Então, aqui a gente vai importar o objeto Pageable e a gente vai fazer um PageRequest passando page e size.
        Pageable pageable = PageRequest.of(page, size);

        // E agora a gente vai buscar esses eventos no repository para buscar por todos os eventos, mas aí a gente vai
        // passar esse objeto de paginação ele vai entender que ele tem que buscar somente x eventos. Então, aqui vou
        // criar um objeto do tipo Page que vai conter instâncias de Event dentro, ou seja, a minha página vai ter
        // vários eventos dentro.
        // A nossa próxima funcionalidade é retornar os eventos que ainda não aconteceram, então precisamos colocar um
        // filtro para buscar somente eventos que a data ainda não ocorreu.
        Page<Event> eventsPage = eventRepository.findUpcomingEvents(new Date(), pageable);

        // E agora vou retornar o meu eventsPage mapeando que para cada evento eu vou declara um EventResponseDTO.
        return eventsPage.stream()
                .map(event -> new EventResponseDTO(
                        event.getId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDate(),
                        "",
                        "",
                        event.getRemote(),
                        event.getEventUrl(),
                        event.getImgUrl()))
                .collect(Collectors.toList());
    }

    // Para fazermos toda a lógica para salvar no bucket da S3 a gente vai precisar instalar a SDK da AWS pra gente
    // conseguir conectar com o S3 e fazer o upload da imagem por lá, então a primeira coisa que a gente precisa fazer
    // é adicionar a dependência no nosso pom.xml
    private String uploadImg(MultipartFile multipartFile) {
        // A primeira coisa que a gente tem que fazer é declarar o nome com o qual essa imagem vai ser salva lá. Isso
        // daqui vai gerar um nome único para essa nossa imagem aqui.
        String filename = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();

        // Vamos colocar um try-catch porque pode dar errado isso daqui.
        try {
            // Aqui dentro do nosso try a gente vai fazer a conversão desse nosso arquivo para um File de verdade
            File file = this.convertMultipartToFile(multipartFile);

            // Converti o meu arquivo para um arquivo agora o que eu vou fazer vou pegar o meu s3Client com o metodo
            // putObject passando o meu fileName e o meu file, mas eu preciso também passar como parâmetro o nome do
            // meu bucket e esse bucketName vou pegar lá do application.properties também.
            s3Client.putObject(bucketName, filename, file);

            // Então, eu vou deletar esse arquivo que eu criei aqui temporariamente para fazer o upload porque
            // basicamente o que a gente fez no nosso metodo convertMultipartToFile foi pegar o que a gente recebeu por
            // request criar um arquivo local na minha máquina para eu conseguir fazer o upload lá no S3, já posso
            // deletar ele depois de ter usado.
            file.delete();

            // Vou fazer um return aqui a url que a gente acabou de criar no momento de fazer o upload desse arquivo.
            return s3Client.getUrl(bucketName, filename).toString();
        } catch (Exception e) {
            // Caso tenha acontecido algum erro e eu não tenha subido o arquivo vou retornar um null.
            System.out.println("erro ao subir arquivo");
            return "";
        }
    }

    private File convertMultipartToFile(MultipartFile multipartFile) throws IOException {
        File convFile = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(multipartFile.getBytes());
        fos.close();
        return convFile;
    }
}
