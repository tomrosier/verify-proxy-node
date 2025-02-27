package uk.gov.ida.notification.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import keystore.KeyStoreResource;
import org.glassfish.jersey.internal.util.Base64;
import uk.gov.ida.notification.apprule.rules.AbstractSamlAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.StubConnectorAppRule;
import uk.gov.ida.notification.saml.SamlFormMessageType;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.Map;

import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;

public class StubConnectorAppRuleTestBase extends AbstractSamlAppRuleTestBase {

    private static final KeyStoreResource METADATA_TRUSTSTORE = createMetadataTruststore();
    protected static final String METADATA_CERTS_PUBLISH_PATH = "/proxy-node-md-certs-publish-path";
    protected static final String METADATA_PUBLISH_PATH = "/stub-connector-md-publish-path";
    protected static final String ENTITY_ID = "http://stub-connector/Connector";

    private static final String METADATA_FILE_PATH =
            StubConnectorAppRuleTestBase.class.getClassLoader().getResource("metadata/test-stub-connector-metadata.xml").getPath();

    private static final String METADATA_CA_CERTS_FILE_PATH =
            StubConnectorAppRuleTestBase.class.getClassLoader().getResource("metadata/metadataCACerts").getPath();

    private Map<String, NewCookie> cookies;

    protected String getEidasRequest(StubConnectorAppRule stubConnectorAppRule) throws URISyntaxException {
        final Response response = stubConnectorAppRule.target("/RequestSubstantial").request().get();
        final String message = response.readEntity(String.class);
        cookies = response.getCookies();

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new RuntimeException("Received response with status " + response.getStatus() + " from Connector. Message:\n" + message);
        }

        return message;
    }

    protected String postEidasResponse(StubConnectorAppRule stubConnectorAppRule, String samlForm) throws URISyntaxException {
        final String encodedResponse = Base64.encodeAsString(samlForm);
        return postResponse(stubConnectorAppRule, encodedResponse);
    }

    protected String postMalformedEidasResponse(StubConnectorAppRule stubConnectorAppRule, String samlForm) throws URISyntaxException {
        final String encodedResponse = "not-a-base64-encoded-xml-start-tag" + Base64.encodeAsString(samlForm);
        return postResponse(stubConnectorAppRule, encodedResponse);
    }

    protected static StubConnectorAppRule createStubConnectorAppRule(DropwizardClientRule metadataClientRule) {
        final String proxyNodeMetadataUrl = metadataClientRule.baseUri() + "/proxy-node/Metadata";
        return new StubConnectorAppRule(
                ConfigOverride.config("connectorNodeBaseUrl", "http://stub-connector"),
                ConfigOverride.config("connectorNodeEntityId", ENTITY_ID),

                ConfigOverride.config("proxyNodeMetadataConfiguration.url", proxyNodeMetadataUrl),
                ConfigOverride.config("proxyNodeMetadataConfiguration.expectedEntityId", "http://proxy-node/Metadata"),
                ConfigOverride.config("proxyNodeMetadataConfiguration.trustStore.type", "file"),
                ConfigOverride.config("proxyNodeMetadataConfiguration.trustStore.store", METADATA_TRUSTSTORE.getAbsolutePath()),
                ConfigOverride.config("proxyNodeMetadataConfiguration.trustStore.password", METADATA_TRUSTSTORE.getPassword()),

                ConfigOverride.config("credentialConfiguration.type", "file"),
                ConfigOverride.config("credentialConfiguration.publicKey.type", "x509"),
                ConfigOverride.config("credentialConfiguration.publicKey.cert", TEST_PUBLIC_CERT),
                ConfigOverride.config("credentialConfiguration.privateKey.key", TEST_PRIVATE_KEY),

                ConfigOverride.config("metadataPublishingConfiguration.metadataFilePath", METADATA_FILE_PATH),
                ConfigOverride.config("metadataPublishingConfiguration.metadataPublishPath", METADATA_PUBLISH_PATH),
                ConfigOverride.config("metadataPublishingConfiguration.metadataCertsPublishPath", METADATA_CERTS_PUBLISH_PATH),
                ConfigOverride.config("metadataPublishingConfiguration.metadataCACertsFilePath", METADATA_CA_CERTS_FILE_PATH)
        ) {
            @Override
            protected void before() {
                waitForMetadata(proxyNodeMetadataUrl);
                super.before();
            }
        };
    }

    private static Form createForm(String encodedResponse) {
        return new Form()
                .param(SamlFormMessageType.SAML_RESPONSE, encodedResponse)
                .param("RelayState", "relay");
    }

    private String postResponse(StubConnectorAppRule stubConnectorAppRule, String encodedResponse) throws URISyntaxException {
        final Form postForm = createForm(encodedResponse);
        final Invocation.Builder request = stubConnectorAppRule.target("/SAML2/Response/POST").request();

        if (cookies != null) {
            request.cookie(cookies.get("stub-connector-session"));
        }

        return request.post(Entity.form(postForm)).readEntity(String.class);
    }
}
