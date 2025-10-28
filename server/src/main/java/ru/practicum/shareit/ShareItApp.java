package ru.practicum.shareit;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title = "ShareIt API",
                version = "1.0",
                description = """
                REST API сервиса **ShareIt** — платформы для обмена вещами между пользователями.
                Сервис позволяет:
                - публиковать вещи для аренды,
                - бронировать вещи других пользователей,
                - оставлять комментарии,
                - создавать запросы на недостающие предметы.
                """,
                contact = @Contact(
                        name = "ShareIt Dev Team",
                        email = "semenenko08@gmail.com",
                        url = "https://github.com/SemenenkoEugene/java-shareit"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(url = "http://localhost:9090", description = "Local Server (shareIt-server)"),
                @Server(url = "http://localhost:8080", description = "Gateway Server (shareIt-gateway)")
        }
)
@SpringBootApplication
public class ShareItApp {

    public static void main(final String[] args) {
        SpringApplication.run(ShareItApp.class, args);
    }
}
