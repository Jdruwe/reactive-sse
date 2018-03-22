package be.jeroendruwe.reactivesse;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.file.dsl.Files;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.File;

@SpringBootApplication
@RestController
public class ReactiveSseApplication {

    // https://stackoverflow.com/questions/45076028/how-to-create-a-spring-reactor-flux-from-http-integration-flow

    @Value("${input-dir:file://${HOME}/Desktop/in}")
    private File in;

    @Bean
    public Publisher<Message<String>> integrationFlow() {

        return IntegrationFlows.from(Files.inboundAdapter(in).autoCreateDirectory(true),
                poller -> poller.poller(spec -> spec.fixedRate(1000L)))
                .transform(File.class, File::getAbsolutePath)
                .channel(MessageChannels.queue())
                .toReactivePublisher();
    }

    @GetMapping(path = "/files", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> files() {

        return Flux.from(integrationFlow())
                .map(Message::getPayload);
    }

    public static void main(String[] args) {
        SpringApplication.run(ReactiveSseApplication.class, args);
    }
}