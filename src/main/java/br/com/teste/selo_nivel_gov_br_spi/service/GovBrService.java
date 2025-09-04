package br.com.teste.selo_nivel_gov_br_spi.service;

import br.com.teste.selo_nivel_gov_br_spi.infrastructure.http.GovBrHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.FederatedIdentityModel;
import org.jboss.logging.Logger;

public class GovBrService {
    private static final Logger logger = Logger.getLogger(GovBrService.class);

    private final KeycloakSession session;
    private final GovBrHttpClient httpClient;

    private static final String NIVEL_URL = "https://nivel-loginpa.openshift.homologar.prodepa.pa.gov.br/v1/govbr/nivel";
    private static final String SELO_URL = "https://confiabilidade-loginpa.openshift.homologar.prodepa.pa.gov.br/v1/govbr/confiabilidade";

    public GovBrService(KeycloakSession session) {
        this.session = session;
        this.httpClient = new GovBrHttpClient(session);
    }

    public void adicionarSeloENivel(UserModel user) {
        System.out.println("=== INICIANDO adicionarSeloENivel para: " + user.getUsername() + " ===");

        try {
            String govBrToken = obterTokenGovBr(user);
            System.out.println("Token GovBr obtido: " + (govBrToken != null ? "SIM" : "NAO"));

            if (govBrToken == null) {
                System.out.println("AVISO: Token GovBr nao encontrado para usuario: " + user.getUsername());
                logger.warn("Token GovBr não encontrado para usuário: " + user.getUsername());
                return;
            }

            String nivel = consultarNivel(govBrToken);
            if (nivel != null) {
                user.setSingleAttribute("govbr_nivel", nivel);
                System.out.println("Nivel definido: " + nivel);
            }

            String selo = consultarSelo(govBrToken);
            if (selo != null) {
                user.setSingleAttribute("govbr_selo", selo);
                System.out.println("Selo definido: " + selo);
            }

        } catch (Exception e) {
            System.out.println("ERRO em adicionarSeloENivel: " + e.getMessage());
            logger.error("Erro ao consultar dados GovBr para usuário: " + user.getUsername(), e);
        }

        System.out.println("=== FINALIZANDO adicionarSeloENivel ===");
    }

    private String consultarNivel(String token) {
        System.out.println("Consultando nivel...");
        JsonNode response = httpClient.executeGetRequest(NIVEL_URL, token);
        return response != null && response.has("nivel") ? response.get("nivel").asText() : null;
    }

    private String consultarSelo(String token) {
        System.out.println("Consultando selo...");
        JsonNode response = httpClient.executeGetRequest(SELO_URL, token);
        return response != null && response.has("confiabilidade") ? response.get("confiabilidade").asText() : null;
    }

    private String obterTokenGovBr(UserModel user) {
        System.out.println("Obtendo token GovBr para usuario: " + user.getUsername());

        String token = session.users()
                .getFederatedIdentitiesStream(session.getContext().getRealm(), user)
                .filter(fi -> "govbr".equals(fi.getIdentityProvider()) ||
                        fi.getIdentityProvider().contains("govbr"))
                .findFirst()
                .map(FederatedIdentityModel::getToken)
                .orElse(null);

        System.out.println("Token encontrado: " + (token != null));
        return token;
    }
}