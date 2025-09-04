package br.com.techlead.selo_nivel_gov_br_spi;

import br.com.techlead.selo_nivel_gov_br_spi.service.GovBrService;
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
        System.out.println("=== AUTHENTICATOR EXECUTANDO ===");

        UserModel user = context.getUser();
        System.out.println("Usuario encontrado: " + (user != null ? user.getUsername() : "NULL"));

        if (user == null) {
            System.out.println("ERRO: Usuario eh null - falhando com UNKNOWN_USER");
            context.failure(AuthenticationFlowError.UNKNOWN_USER);
            return;
        }

        try {
            System.out.println("Criando GovBrService para usuario: " + user.getUsername());
            GovBrService govBrService = new GovBrService(context.getSession());

            System.out.println("Executando adicionarSeloENivel...");
            govBrService.adicionarSeloENivel(user);

            System.out.println("SUCCESS - Dados processados com sucesso para: " + user.getUsername());
            context.success();

        } catch (Exception e) {
            System.out.println("ERRO no authenticator: " + e.getMessage());
            e.printStackTrace();
            logger.error("ERRO ao consultar dados GovBr para usuario: " + user.getUsername(), e);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR);
        }

        System.out.println("=== FINALIZANDO UserAttributeAuthenticator ===");
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        System.out.println("Metodo action() chamado - executando success()");
        context.success();
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // Não necessário
    }

    @Override
    public void close() {
        // Não necessário
    }
}