package com.ford.protech.config;

import io.swagger.v3.oas.models.OpenAPI;

import io.swagger.v3.oas.models.info.Contact;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import lombok.Data;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "swagger")
@EnableConfigurationProperties(SwaggerConfiguration.class)
public class SwaggerConfiguration {
    public static final String AZURE = "AZURE";
    private String contactUrl;
    private String contactName;
    private String contactEmail;
    private String version;
    private String title;
    private String description;
    private String licenseName;
    private String serverUrl;
    private String tokenUrl;
    private String scope;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title(title).description(description).version(version)
                .license(new License().name(licenseName))
                .contact(new Contact().email(contactEmail).name(contactName).url(contactUrl)))
                .servers(Collections.singletonList(new Server().url(serverUrl)))
                .schemaRequirement(AZURE, new SecurityScheme().type(SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows().clientCredentials(new OAuthFlow().tokenUrl(tokenUrl)
                                .scopes(new Scopes().addString("GBMS", scope)))))
                .addSecurityItem(new SecurityRequirement().addList(AZURE));
    }

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> openApi.getComponents().getSchemas().values().forEach(schema -> {
            schema.setAdditionalProperties(Boolean.FALSE);
            schema.setNullable(true);
            Map<String, Schema<?>> properties = schema.getProperties();
            if (!CollectionUtils.isEmpty(properties)) {
                properties.forEach((property, propertySchema) -> propertySchema.addExtension("x-nullable", true));
            }
        });
    }

    @Bean
    public OpenApiCustomizer openApiResponseCustomizer() {
        Content errorResponseContent = new Content().addMediaType(MediaType.APPLICATION_JSON_VALUE,
                new io.swagger.v3.oas.models.media.MediaType().schema(new Schema<>().$ref("#/components/schemas/StandardErrorResponse")));
        return openApi -> openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
            ApiResponses apiResponses = operation.getResponses();
            apiResponses.addApiResponse("204", new ApiResponse().description("No Content"));
            apiResponses.addApiResponse("400", new ApiResponse().description("Bad Request").content(errorResponseContent));
            apiResponses.addApiResponse("401", new ApiResponse().description("Unauthorized").content(errorResponseContent));
            apiResponses.addApiResponse("403", new ApiResponse().description("Forbidden").content(errorResponseContent));
            apiResponses.addApiResponse("404", new ApiResponse().description("Not Found").content(errorResponseContent));
            apiResponses.addApiResponse("405", new ApiResponse().description("Method Not Allowed").content(errorResponseContent));
            apiResponses.addApiResponse("406", new ApiResponse().description("Not Acceptable").content(errorResponseContent));
            apiResponses.addApiResponse("409", new ApiResponse().description("Conflict").content(errorResponseContent));
            apiResponses.addApiResponse("412", new ApiResponse().description("Precondition Failed").content(errorResponseContent));
            apiResponses.addApiResponse("415", new ApiResponse().description("Unsupported Media Type").content(errorResponseContent));
            apiResponses.addApiResponse("417", new ApiResponse().description("Expectation Failed").content(errorResponseContent));
            apiResponses.addApiResponse("429", new ApiResponse().description("Too many Requests").content(errorResponseContent));
            apiResponses.addApiResponse("500", new ApiResponse().description("Internal Server Error").content(errorResponseContent));
            apiResponses.addApiResponse("503", new ApiResponse().description("Service Unavailable").content(errorResponseContent));
            apiResponses.addApiResponse("default", new ApiResponse().description("Unexpected Error").content(errorResponseContent));
        }));
    }
}

 