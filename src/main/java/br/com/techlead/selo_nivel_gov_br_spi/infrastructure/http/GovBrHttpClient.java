package br.com.techlead.selo_nivel_gov_br_spi.infrastructure.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;
import org.jboss.logging.Logger;

import java.io.IOException;

public class GovBrHttpClient {
    private static final Logger logger = Logger.getLogger(GovBrHttpClient.class);

    private final KeycloakSession session;
    private final ObjectMapper mapper;

    public GovBrHttpClient(KeycloakSession session) {
        this.session = session;
        this.mapper = new ObjectMapper();
    }

    public JsonNode executeGetRequest(String url, String token) {
        System.out.println("=== EXECUTANDO REQUISICAO HTTP ===");
        System.out.println("URL: " + url);
        System.out.println("Token presente: " + (token != null && !token.isEmpty()));

        try {
            SimpleHttp.Response response = SimpleHttp.doGet(url, session)
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .asResponse();

            System.out.println("Status da resposta: " + response.getStatus());

            if (response.getStatus() == 200) {
                String responseBody = response.asString();
                System.out.println("Resposta recebida: " + responseBody);
                return mapper.readTree(responseBody);
            } else {
                System.out.println("ERRO: Requisicao falhou com status: " + response.getStatus());
                logger.warn("Requisição falhou com status: " + response.getStatus() + " para URL: " + url);
            }
        } catch (IOException e) {
            System.out.println("ERRO na requisicao HTTP: " + e.getMessage());
            logger.error("Erro na requisição HTTP para URL: " + url, e);
        }
        return null;
    }
}