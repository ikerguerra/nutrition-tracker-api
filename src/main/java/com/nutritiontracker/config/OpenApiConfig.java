package com.nutritiontracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Value("${server.port:8080}")
        private String serverPort;

        @Value("${swagger.server.url:}")
        private String productionServerUrl;

        @Bean
        public OpenAPI nutritionTrackerOpenAPI() {
                Server localServer = new Server();
                localServer.setUrl("http://localhost:" + serverPort);
                localServer.setDescription("Local Development Server");

                // Add production server if URL is provided
                Server productionServer = null;
                if (productionServerUrl != null && !productionServerUrl.isEmpty()) {
                        productionServer = new Server();
                        productionServer.setUrl(productionServerUrl);
                        productionServer.setDescription("Production Server");
                }

                Contact contact = new Contact();
                contact.setName("Nutrition Tracker Team");
                contact.setEmail("support@nutritiontracker.com");

                License license = new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT");

                Info info = new Info()
                                .title("Nutrition Tracker API")
                                .version("1.0.0")
                                .description("REST API for managing food items and tracking nutritional information. " +
                                                "This API provides endpoints for CRUD operations on foods, barcode scanning, "
                                                +
                                                "and nutritional data management.")
                                .contact(contact)
                                .license(license);

                // Define JWT Security Scheme
                String securitySchemeName = "bearerAuth";
                SecurityRequirement securityRequirement = new SecurityRequirement().addList(securitySchemeName);
                Components components = new Components().addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT"));

                return new OpenAPI()
                                .info(info)
                                .servers(productionServer != null ? List.of(productionServer, localServer)
                                                : List.of(localServer))
                                .addSecurityItem(securityRequirement)
                                .components(components);
        }
}
