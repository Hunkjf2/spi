package br.com.teste.selo_nivel_gov_br_spi;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.jboss.logging.Logger;

public class GovBrEventListenerProviderFactory implements EventListenerProviderFactory {
    private static final Logger logger = Logger.getLogger(GovBrEventListenerProviderFactory.class);
    public static final String PROVIDER_ID = "govbr-event-listener";

    public GovBrEventListenerProviderFactory() {
        logger.info("=== GovBrEventListenerProviderFactory CRIADO ===");
        System.out.println("=== GovBrEventListenerProviderFactory CRIADO ===");
    }

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        logger.info("=== CRIANDO GovBrEventListener ===");
        System.out.println("=== CRIANDO GovBrEventListener ===");
        return new GovBrEventListener(session);
    }

    @Override
    public void init(Config.Scope config) {
        logger.info("=== GovBrEventListenerProviderFactory INIT ===");
        System.out.println("=== GovBrEventListenerProviderFactory INIT ===");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        logger.info("=== GovBrEventListenerProviderFactory POST INIT ===");
        System.out.println("=== GovBrEventListenerProviderFactory POST INIT ===");
    }

    @Override
    public void close() {
        logger.info("=== GovBrEventListenerProviderFactory CLOSE ===");
        System.out.println("=== GovBrEventListenerProviderFactory CLOSE ===");
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}