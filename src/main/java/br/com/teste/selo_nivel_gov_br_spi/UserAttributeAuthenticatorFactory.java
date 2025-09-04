package br.com.teste.selo_nivel_gov_br_spi;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.jboss.logging.Logger;
import java.util.Collections;
import java.util.List;

public class UserAttributeAuthenticatorFactory implements AuthenticatorFactory {
    private static final Logger logger = Logger.getLogger(UserAttributeAuthenticatorFactory.class);
    public static final String PROVIDER_ID = "govbr-selo-nivel-authenticator";

    public UserAttributeAuthenticatorFactory() {
        logger.info("=== UserAttributeAuthenticatorFactory CRIADO ===");
        System.out.println("=== UserAttributeAuthenticatorFactory CRIADO ===");
    }

    @Override
    public String getDisplayType() {
        return "GovBr Selo e Nível";
    }

    @Override
    public String getReferenceCategory() {
        return "GovBr Integration";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[]{
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.ALTERNATIVE,
                AuthenticationExecutionModel.Requirement.DISABLED,
                AuthenticationExecutionModel.Requirement.CONDITIONAL
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Consulta selo e nível da conta GovBr e adiciona como atributos do usuário";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    public Authenticator create(KeycloakSession keycloakSession) {
        logger.info("=== CRIANDO NOVA INSTANCIA DO UserAttributeAuthenticator ===");
        System.out.println("=== CRIANDO NOVA INSTANCIA DO UserAttributeAuthenticator ===");
        return new UserAttributeAuthenticator();
    }

    @Override
    public void init(Config.Scope scope) {
        logger.info("=== INIT CHAMADO ===");
        System.out.println("=== INIT CHAMADO ===");
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        logger.info("=== POST INIT CHAMADO ===");
        System.out.println("=== POST INIT CHAMADO ===");
    }

    @Override
    public void close() {
        logger.info("=== CLOSE CHAMADO ===");
        System.out.println("=== CLOSE CHAMADO ===");
    }

    @Override
    public String getId() {
        logger.info("=== getId() chamado, retornando: " + PROVIDER_ID + " ===");
        return PROVIDER_ID;
    }
}