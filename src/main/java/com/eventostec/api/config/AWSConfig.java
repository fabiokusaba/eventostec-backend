package com.eventostec.api.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Por ser uma classe de configuração preciso anotá-la com @Configuration e basicamente o que ela vai fazer é criar a
// instância do nosso objeto amazon s3 com as nossas credenciais, com tudo necessário para que eu consiga só usar esse
// objeto pra fazer o upload lá no s3.
@Configuration
public class AWSConfig {

    // Esse meu atributo da classe eu vou pegar do meu application.properties, esse meu region é onde vai estar o nosso
    // bucket lá na AWS.
    @Value("${aws.region}")
    private String awsRegion;

    // Agora vamos criar um metodo público que vai nos retornar um objeto do tipo AmazonS3 e aqui temos que colocar a
    // anotação @Bean nele para que o Spring consiga identificar que esse metodo aqui cria a instância correta do objeto
    // AmazonS3. Com esses comandos que passamos aqui a gente basicamente especificou qual a região que queremos usar
    // então ele cria a instância baseada nessa região e colocamos aqui para usar o standard isso significa que ele vai
    // usar as credenciais standard que tiverem configuradas aqui nessa máquina.
    @Bean
    public AmazonS3 createS3Instance() {
        return AmazonS3ClientBuilder.standard().withRegion(awsRegion).build();
    }
}
