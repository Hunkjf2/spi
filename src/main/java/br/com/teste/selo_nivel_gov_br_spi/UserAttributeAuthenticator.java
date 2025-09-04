package br.com.teste.selo_nivel_gov_br_spi;

import br.com.teste.selo_nivel_gov_br_spi.service.GovBrService;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.jboss.logging.Logger;

public class UserAttributeAuthenticator implements Authenticator {
    private static final Logger logger = Logger.getLogger(UserAttributeAuthenticator.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        logger.info("=== INICIANDO UserAttributeAuthenticator ===");
        System.out.println("=== AUTHENTICATOR EXECUTANDO ===");

        UserModel user = context.getUser();
        logger.info("Usuario encontrado: " + (user != null ? user.getUsername() : "NULL"));
        System.out.println("Usuario encontrado: " + (user != null ? user.getUsername() : "NULL"));

        if (user == null) {
            logger.error("ERRO: Usuario eh null - falhando com UNKNOWN_USER");
            System.out.println("ERRO: Usuario eh null - falhando com UNKNOWN_USER");
            context.failure(AuthenticationFlowError.UNKNOWN_USER);
            return;
        }

        try {
            logger.info("Criando GovBrService para usuario: " + user.getUsername());
            System.out.println("Criando GovBrService para usuario: " + user.getUsername());

            GovBrService govBrService = new GovBrService(context.getSession());

            logger.info("Executando adicionarSeloENivel...");
            System.out.println("Executando adicionarSeloENivel...");

            govBrService.adicionarSeloENivel(user);

            logger.info("SUCCESS - Dados processados com sucesso para: " + user.getUsername());
            System.out.println("SUCCESS - Dados processados com sucesso para: " + user.getUsername());

            context.success();

        } catch (Exception e) {
            logger.error("ERRO no authenticator para usuario " + user.getUsername() + ": " + e.getMessage(), e);
            System.out.println("ERRO no authenticator: " + e.getMessage());
            e.printStackTrace();
            context.failure(AuthenticationFlowError.INTERNAL_ERROR);
        }

        logger.info("=== FINALIZANDO UserAttributeAuthenticator ===");
        System.out.println("=== FINALIZANDO UserAttributeAuthenticator ===");
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        logger.info("Metodo action() chamado - executando success()");
        System.out.println("Metodo action() chamado - executando success()");
        context.success();
    }

    @Override
    public boolean requiresUser() {
        logger.info("requiresUser() chamado - retornando true");
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.info("configuredFor() chamado para usuario: " + (user != null ? user.getUsername() : "NULL"));
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.info("setRequiredActions() chamado");
        // Não necessário
    }

    @Override
    public void close() {
        logger.info("close() chamado");
        // Não necessário
    }
}