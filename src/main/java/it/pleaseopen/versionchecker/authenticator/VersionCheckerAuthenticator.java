package it.pleaseopen.versionchecker.authenticator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.info.SystemInfoRepresentation;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class VersionCheckerAuthenticator implements Authenticator {
    private final KeycloakSession session;
    private static final Logger logger = Logger.getLogger(VersionCheckerAuthenticator.class);

    public VersionCheckerAuthenticator(KeycloakSession session){
        this.session = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
        String sourceVersion = SystemInfoRepresentation.create(session.getKeycloakSessionFactory().getServerStartupTimestamp()).getVersion();
        String githubReleaseVersion = getLastReleaseFromGithub();

        int versionAsInt = Integer.parseInt(sourceVersion.replace(".", ""));
        int githubReleaseVersionAsInt = Integer.parseInt(githubReleaseVersion.replace(".", ""));

        if(versionAsInt == githubReleaseVersionAsInt){
            authenticationFlowContext.success();
            return;
        }
        
        LoginFormsProvider form = authenticationFlowContext.form().setExecution(authenticationFlowContext.getExecution().getId());
        form.setAttribute("current", sourceVersion);
        form.setAttribute("available", githubReleaseVersion);
        Response response = form.createForm("version.ftl");
        authenticationFlowContext.challenge(response);
    }

    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {
        authenticationFlowContext.success();
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    @Override
    public void close() {

    }

    private String getLastReleaseFromGithub(){
        HttpClient client = HttpClient.newBuilder().build();


        ObjectMapper objectMapper = new ObjectMapper();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/keycloak/keycloak/releases/latest"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = null;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                logger.log(Logger.Level.ERROR, "Unable to get last version from Github", e);
                return "0";
            } catch (InterruptedException e) {
                logger.log(Logger.Level.ERROR, "Unable to get last version from Github", e);
                return "0";
            }
            if (response.statusCode() == 404) {
                logger.log(Logger.Level.ERROR, "Unable to get last version from Github");
                return "0";
            }
            final Gson gson = new Gson();
            final JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);
            return jsonObject.get("name").getAsString();
        } catch (Exception e) {
            logger.log(Logger.Level.ERROR, "Unable to get last version from Github", e);
            return "0";
        }
    }
}
