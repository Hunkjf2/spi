package br.com.teste.selo_nivel_gov_br_spi;

import br.com.teste.selo_nivel_gov_br_spi.service.GovBrService;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.RealmModel;
import org.jboss.logging.Logger;

public class GovBrEventListener implements EventListenerProvider {
    private static final Logger logger = Logger.getLogger(GovBrEventListener.class);

    private final KeycloakSession session;

    public GovBrEventListener(KeycloakSession session) {
        this.session = session;
        logger.info("=== GovBrEventListener CRIADO ===");
        System.out.println("=== GovBrEventListener CRIADO ===");
    }

    @Override
    public void onEvent(Event event) {
        logger.info("Evento recebido: " + event.getType() + " para realm: " + event.getRealmId());
        System.out.println("Evento recebido: " + event.getType() + " para realm: " + event.getRealmId());

        // Log detalhado do evento para debug
        if (event.getDetails() != null) {
            event.getDetails().forEach((key, value) -> {
                logger.info("  Detalhe: " + key + " = " + value);
                System.out.println("  Detalhe: " + key + " = " + value);
            });
        }

        // Processar apenas eventos de login via identity provider
        if (isEventoLoginGovBr(event)) {
            logger.info("EVENTO GOVBR DETECTADO - processando selo e nivel");
            System.out.println("EVENTO GOVBR DETECTADO - processando selo e nivel");

            processarLoginGovBr(event);
        }
    }

    private boolean isEventoLoginGovBr(Event event) {
        // Verifica se é um evento de login via identity provider
        boolean isLoginEvent = event.getType() == EventType.IDENTITY_PROVIDER_POST_LOGIN ||
                event.getType() == EventType.IDENTITY_PROVIDER_FIRST_LOGIN ||
                event.getType() == EventType.LOGIN;

        if (!isLoginEvent || event.getDetails() == null) {
            return false;
        }

        // Verifica se é login via GovBr
        String identityProvider = event.getDetails().get("identity_provider");
        return identityProvider != null &&
                (identityProvider.equals("gov-br") ||
                        identityProvider.contains("govbr") ||
                        identityProvider.toLowerCase().contains("gov"));
    }

    private void processarLoginGovBr(Event event) {
        try {
            logger.info("=== INICIANDO PROCESSAMENTO LOGIN GOVBR ===");
            System.out.println("=== INICIANDO PROCESSAMENTO LOGIN GOVBR ===");

            // Obter usuário
            UserModel user = obterUsuario(event);
            if (user == null) {
                logger.warn("Usuario não encontrado - abortando processamento");
                System.out.println("Usuario não encontrado - abortando processamento");
                return;
            }

            logger.info("Usuario encontrado: " + user.getUsername() + " (ID: " + user.getId() + ")");
            System.out.println("Usuario encontrado: " + user.getUsername() + " (ID: " + user.getId() + ")");

            // Aguardar para garantir que o token foi armazenado
            logger.info("Aguardando armazenamento do token...");
            System.out.println("Aguardando armazenamento do token...");
            Thread.sleep(3000);

            // Processar selo e nível
            logger.info("Iniciando processamento de selo e nivel");
            System.out.println("Iniciando processamento de selo e nivel");

            GovBrService govBrService = new GovBrService(session);
            govBrService.adicionarSeloENivel(user);

            logger.info("SUCCESS - Selo e nivel processados com sucesso para: " + user.getUsername());
            System.out.println("SUCCESS - Selo e nivel processados com sucesso para: " + user.getUsername());

        } catch (Exception e) {
            logger.error("ERRO ao processar login GovBr: " + e.getMessage(), e);
            System.out.println("ERRO ao processar login GovBr: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private UserModel obterUsuario(Event event) {
        try {
            String userId = event.getUserId();

            if (userId != null) {
                logger.info("Obtendo usuario por ID: " + userId);
                System.out.println("Obtendo usuario por ID: " + userId);

                RealmModel realm = session.realms().getRealm(event.getRealmId());
                UserModel user = session.users().getUserById(realm, userId);

                if (user != null) {
                    return user;
                }
            }

            // Fallback: tentar obter por identity provider identity
            String identityProviderIdentity = event.getDetails() != null ?
                    event.getDetails().get("identity_provider_identity") : null;

            if (identityProviderIdentity != null) {
                logger.info("Tentando encontrar usuario por CPF/identity: " + identityProviderIdentity);
                System.out.println("Tentando encontrar usuario por CPF/identity: " + identityProviderIdentity);

                RealmModel realm = session.realms().getRealm(event.getRealmId());

                // Buscar por username
                UserModel user = session.users().getUserByUsername(realm, identityProviderIdentity);
                if (user != null) {
                    return user;
                }

                // Buscar por atributo CPF
                return session.users().searchForUserByUserAttributeStream(realm, "cpf", identityProviderIdentity)
                        .findFirst()
                        .orElse(null);
            }

            logger.warn("Não foi possível obter o usuário do evento");
            System.out.println("Não foi possível obter o usuário do evento");
            return null;

        } catch (Exception e) {
            logger.error("Erro ao obter usuario: " + e.getMessage(), e);
            System.out.println("Erro ao obter usuario: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        // Não processar eventos admin
    }

    @Override
    public void close() {
        logger.info("GovBrEventListener fechado");
        System.out.println("GovBrEventListener fechado");
    }
}