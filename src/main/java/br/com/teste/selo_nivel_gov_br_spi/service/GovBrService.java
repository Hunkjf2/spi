package br.com.teste.selo_nivel_gov_br_spi.service;

import br.com.teste.selo_nivel_gov_br_spi.infrastructure.http.GovBrHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.RealmModel;
import org.jboss.logging.Logger;

public class GovBrService {
    private static final Logger logger = Logger.getLogger(GovBrService.class);

    private final KeycloakSession session;
    private final GovBrHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String NIVEL_URL = "https://sso.govbr/v1/govbr/nivel";
    private static final String SELO_URL = "https://sso.govbr/v1/govbr/confiabilidade";

    public GovBrService(KeycloakSession session) {
        this.session = session;
        this.httpClient = new GovBrHttpClient(session);
        this.objectMapper = new ObjectMapper();
        logger.info("=== GovBrService CRIADO ===");
        System.out.println("=== GovBrService CRIADO ===");
    }

    public void adicionarSeloENivel(UserModel user) {
        logger.info("=== INICIANDO adicionarSeloENivel para: " + user.getUsername() + " ===");
        System.out.println("=== INICIANDO adicionarSeloENivel para: " + user.getUsername() + " ===");

        try {
            // Debug do usuário
            debugUserInfo(user);

            // Obter token GovBr (apenas o access_token)
            String govBrAccessToken = obterTokenGovBr(user);

            if (govBrAccessToken == null) {
                logger.warn("Access Token GovBr não encontrado - adicionando atributos de teste");
                System.out.println("Access Token GovBr não encontrado - adicionando atributos de teste");
                adicionarAtributosTeste(user);
                return;
            }

            logger.info("Access Token GovBr encontrado (tamanho: " + govBrAccessToken.length() + " chars)");
            System.out.println("Access Token GovBr encontrado (tamanho: " + govBrAccessToken.length() + " chars)");

            // Consultar nível
            String nivel = consultarNivel(govBrAccessToken);
            if (nivel != null) {
                user.setSingleAttribute("govbr_nivel", nivel);
                logger.info("Nível definido: " + nivel);
                System.out.println("Nível definido: " + nivel);
            }

            // Consultar selo
            String selo = consultarSelo(govBrAccessToken);
            if (selo != null) {
                user.setSingleAttribute("govbr_selo", selo);
                logger.info("Selo definido: " + selo);
                System.out.println("Selo definido: " + selo);
            }

            // Verificar atributos adicionados
            verificarAtributos(user);

        } catch (Exception e) {
            logger.error("ERRO em adicionarSeloENivel: " + e.getMessage(), e);
            System.out.println("ERRO em adicionarSeloENivel: " + e.getMessage());
            e.printStackTrace();
        }

        logger.info("=== FINALIZANDO adicionarSeloENivel ===");
        System.out.println("=== FINALIZANDO adicionarSeloENivel ===");
    }

    private void debugUserInfo(UserModel user) {
        logger.info("=== DEBUG USER INFO ===");
        System.out.println("=== DEBUG USER INFO ===");

        logger.info("User ID: " + user.getId());
        logger.info("Username: " + user.getUsername());
        logger.info("Email: " + user.getEmail());

        System.out.println("User ID: " + user.getId());
        System.out.println("Username: " + user.getUsername());
        System.out.println("Email: " + user.getEmail());

        // Listar identidades federadas
        RealmModel realm = session.getContext().getRealm();
        long identityCount = session.users().getFederatedIdentitiesStream(realm, user).count();

        logger.info("Identidades federadas: " + identityCount);
        System.out.println("Identidades federadas: " + identityCount);

        session.users().getFederatedIdentitiesStream(realm, user).forEach(identity -> {
            logger.info("Provider: " + identity.getIdentityProvider() + " | Token: " + (identity.getToken() != null ? "SIM" : "NÃO"));
            System.out.println("Provider: " + identity.getIdentityProvider() + " | Token: " + (identity.getToken() != null ? "SIM" : "NÃO"));
        });
    }

    private void adicionarAtributosTeste(UserModel user) {
        logger.info("Adicionando atributos de teste");
        System.out.println("Adicionando atributos de teste");

        user.setSingleAttribute("govbr_nivel", "TESTE_NIVEL_3");
        user.setSingleAttribute("govbr_selo", "TESTE_SELO_OURO");
        user.setSingleAttribute("govbr_processado", "SIM_" + System.currentTimeMillis());
    }

    private void verificarAtributos(UserModel user) {
        logger.info("=== VERIFICANDO ATRIBUTOS ===");
        System.out.println("=== VERIFICANDO ATRIBUTOS ===");

        String nivel = user.getFirstAttribute("govbr_nivel");
        String selo = user.getFirstAttribute("govbr_selo");

        logger.info("govbr_nivel: " + nivel);
        logger.info("govbr_selo: " + selo);
        System.out.println("govbr_nivel: " + nivel);
        System.out.println("govbr_selo: " + selo);
    }

    private String consultarNivel(String accessToken) {
        logger.info("Consultando nível...");
        System.out.println("Consultando nível...");

        JsonNode response = httpClient.executeGetRequest(NIVEL_URL, accessToken);
        if (!response.isEmpty()) {
            return response.toString();
        }

        logger.warn("Nível não obtido");
        System.out.println("Nível não obtido");
        return null;
    }

    private String consultarSelo(String accessToken) {
        logger.info("Consultando selo...");
        System.out.println("Consultando selo...");

        JsonNode response = httpClient.executeGetRequest(SELO_URL, accessToken);
        if (!response.isEmpty()) {
            return response.toString();
        }

        logger.warn("Selo não obtido");
        System.out.println("Selo não obtido");
        return null;
    }

    private String obterTokenGovBr(UserModel user) {
        logger.info("=== Obtendo token GovBr ===");
        System.out.println("=== Obtendo token GovBr ===");

        try {
            RealmModel realm = session.getContext().getRealm();

            // Buscar a identidade federada do GovBr
            FederatedIdentityModel govBrIdentity = session.users()
                    .getFederatedIdentitiesStream(realm, user)
                    .filter(fi -> isGovBrProvider(fi.getIdentityProvider()))
                    .findFirst()
                    .orElse(null);

            if (govBrIdentity == null) {
                logger.warn("Identidade federada GovBr não encontrada");
                System.out.println("Identidade federada GovBr não encontrada");
                return null;
            }

            String tokenJson = govBrIdentity.getToken();
            if (tokenJson == null || tokenJson.trim().isEmpty()) {
                logger.warn("Token JSON está vazio ou null");
                System.out.println("Token JSON está vazio ou null");
                return null;
            }

            logger.info("Token JSON encontrado (tamanho: " + tokenJson.length() + " chars)");
            System.out.println("Token JSON encontrado (tamanho: " + tokenJson.length() + " chars)");

            // Extrair o access_token do JSON
            JsonNode tokenNode = objectMapper.readTree(tokenJson);

            if (!tokenNode.has("access_token")) {
                logger.warn("Campo 'access_token' não encontrado no token JSON");
                System.out.println("Campo 'access_token' não encontrado no token JSON");
                logger.info("Campos disponíveis: " + tokenNode.fieldNames());
                return null;
            }

            String accessToken = tokenNode.get("access_token").asText();

            if (accessToken == null || accessToken.trim().isEmpty()) {
                logger.warn("Access token está vazio");
                System.out.println("Access token está vazio");
                return null;
            }

            logger.info("Access token extraído com sucesso (tamanho: " + accessToken.length() + " chars)");
            System.out.println("Access token extraído com sucesso (tamanho: " + accessToken.length() + " chars)");

            // Log apenas dos primeiros e últimos caracteres por segurança
            String tokenPreview = accessToken.length() > 20 ?
                    accessToken.substring(0, 10) + "..." + accessToken.substring(accessToken.length() - 10) :
                    accessToken;
            logger.info("Access token preview: " + tokenPreview);
            System.out.println("Access token preview: " + tokenPreview);

            return accessToken;

        } catch (Exception e) {
            logger.error("Erro ao processar token JSON: " + e.getMessage(), e);
            System.out.println("Erro ao processar token JSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private boolean isGovBrProvider(String provider) {
        return provider != null &&
                (provider.equals("gov-br") ||
                        provider.contains("govbr") ||
                        provider.toLowerCase().contains("gov"));
    }
}