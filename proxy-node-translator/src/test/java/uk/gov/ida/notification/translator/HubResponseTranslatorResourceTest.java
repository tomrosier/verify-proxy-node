package uk.gov.ida.notification.translator;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.common.ExceptionType;
import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseBuilder;
import uk.gov.ida.notification.shared.Urls;
import uk.gov.ida.notification.shared.proxy.VerifyServiceProviderProxy;
import uk.gov.ida.notification.translator.resources.HubResponseTranslatorResource;
import uk.gov.ida.notification.translator.saml.EidasResponseGenerator;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;

@RunWith(MockitoJUnitRunner.class)
public class HubResponseTranslatorResourceTest {

    private final static EidasResponseGenerator eidasResponseGenerator = mock(EidasResponseGenerator.class);

    private final static VerifyServiceProviderProxy verifyServiceProviderProxy = mock(VerifyServiceProviderProxy.class);

    @ClassRule
    public static final ResourceTestRule resources =
            ResourceTestRule.builder().addResource(new HubResponseTranslatorResource(eidasResponseGenerator, verifyServiceProviderProxy)).build();

    @Test
    public void shouldReceive500FromTranslatorWhenEidasResponseNull() {

        when(verifyServiceProviderProxy.getTranslatedHubResponse(any()))
                .thenReturn(TranslatedHubResponseBuilder.getTranslatedHubResponseIdentityVerified());

        Response translatorResponse = doPostToTranslator(buildHubResponseTranslatorRequest());

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), translatorResponse.getStatus());
    }

    @Test
    public void shouldReceive500FromTranslatorWhenVSPProxyThrowsApplicationException() {

        when(verifyServiceProviderProxy.getTranslatedHubResponse(any()))
                .thenThrow(ApplicationException.createAuditedException(ExceptionType.NETWORK_ERROR, UUID.randomUUID()));

        Response translatorResponse = doPostToTranslator(buildHubResponseTranslatorRequest());

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), translatorResponse.getStatus());
    }

    private Response doPostToTranslator(HubResponseTranslatorRequest hubResponseTranslatorRequest) {
        return resources.target(Urls.TranslatorUrls.TRANSLATOR_ROOT + Urls.TranslatorUrls.TRANSLATE_HUB_RESPONSE_PATH)
                .request()
                .post(Entity.entity(hubResponseTranslatorRequest, MediaType.APPLICATION_JSON_TYPE), Response.class);
    }

    private HubResponseTranslatorRequest buildHubResponseTranslatorRequest() {
        return new HubResponseTranslatorRequest(
                "",
                "_1234",
                ResponseBuilder.DEFAULT_REQUEST_ID,
                "LEVEL_2",
                URI.create("http://localhost:8081/bob"),
                STUB_COUNTRY_PUBLIC_PRIMARY_CERT
        );
    }
}
