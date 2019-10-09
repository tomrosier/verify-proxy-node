package uk.gov.ida.notification.resources;

import io.dropwizard.jersey.sessions.Session;
import io.dropwizard.views.View;
import io.prometheus.client.Counter;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.proxy.TranslatorProxy;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.session.GatewaySessionData;
import uk.gov.ida.notification.session.storage.SessionStore;
import uk.gov.ida.notification.shared.logging.IngressEgressLogging;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.shared.Urls;
import uk.gov.ida.notification.validations.ValidBase64Xml;
import uk.gov.ida.notification.views.SamlFormView;

import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

@IngressEgressLogging
@Path(Urls.GatewayUrls.GATEWAY_ROOT)
public class HubResponseResource {

    static final String LEVEL_OF_ASSURANCE = "LEVEL_2";
    static final String SUBMIT_TEXT = "Post eIDAS Response SAML to Connector Node";

    private static final Counter SUCCESSFUL_RESPONSES = Counter.build(
            "verify_proxy_node_successful_responses",
            "Total number of successful Verify Proxy Node Responses")
            .labelNames("destination")
            .register();

    private final SamlFormViewBuilder samlFormViewBuilder;
    private final TranslatorProxy translatorProxy;
    private final SessionStore sessionStorage;

    public HubResponseResource(
            SamlFormViewBuilder samlFormViewBuilder,
            TranslatorProxy translatorProxy,
            SessionStore sessionStorage) {
        this.samlFormViewBuilder = samlFormViewBuilder;
        this.translatorProxy = translatorProxy;
        this.sessionStorage = sessionStorage;
    }

    @POST
    @Path(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_PATH)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View hubResponse(
        @FormParam(SamlFormMessageType.SAML_RESPONSE) @ValidBase64Xml String hubResponse,
        @FormParam("RelayState") String relayState,
        @Session HttpSession session) {

        GatewaySessionData sessionData = sessionStorage.getSession(session.getId());

        ProxyNodeLogger.info("Retrieved GatewaySessionData");

        HubResponseTranslatorRequest translatorRequest = new HubResponseTranslatorRequest(
            hubResponse,
            sessionData.getHubRequestId(),
            sessionData.getEidasRequestId(),
            LEVEL_OF_ASSURANCE,
            UriBuilder.fromUri(sessionData.getEidasDestination()).build(),
            sessionData.getEidasConnectorPublicKey()
        );

        String eidasResponse = translatorProxy.getTranslatedHubResponse(translatorRequest, session.getId());
        ProxyNodeLogger.info("Received eIDAS response from Translator");

        SamlFormView samlFormView = samlFormViewBuilder.buildResponse(
                sessionData.getEidasDestination(),
                eidasResponse,
                SUBMIT_TEXT,
                sessionData.getEidasRelayState()
        );
        SUCCESSFUL_RESPONSES.labels(sessionData.getEidasDestination()).inc();
        return samlFormView;
    }
}
