package br.com.techlead.selo_nivel_gov_br_spi;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import java.util.Collections;
import java.util.List;

public class UserAttributeAuthenticatorFactory implements AuthenticatorFactory {
    public static final String PROVIDER_ID = "govbr-selo-nivel-authenticator";

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
                AuthenticationExecutionModel.Requirement.DISABLED
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
        return new UserAttributeAuthenticator();
    }

    @Override
    public void init(Config.Scope scope) {
        // Não necessário
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        // Não necessário
    }

    @Override
    public void close() {
        // Não necessário
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}