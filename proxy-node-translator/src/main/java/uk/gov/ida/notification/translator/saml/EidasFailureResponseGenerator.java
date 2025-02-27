package uk.gov.ida.notification.translator.saml;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.notification.saml.EidasResponseBuilder;

import javax.ws.rs.core.Response.Status;
import java.util.function.Supplier;

public class EidasFailureResponseGenerator {

    private String connectorNodeIssuerId;
    private String proxyNodeMetadataForConnectorNodeUrl;
    private Supplier<EidasResponseBuilder> eidasResponseBuilderSupplier;

    public EidasFailureResponseGenerator(
            Supplier<EidasResponseBuilder> eidasResponseBuilderSupplier,
            String connectorNodeIssuerId,
            String proxyNodeMetadataForConnectorNodeUrl) {
        this.connectorNodeIssuerId = connectorNodeIssuerId;
        this.proxyNodeMetadataForConnectorNodeUrl = proxyNodeMetadataForConnectorNodeUrl;
        this.eidasResponseBuilderSupplier = eidasResponseBuilderSupplier;
    }

    Response generateFailureSamlResponse(Status responseStatus, String eidasRequestId, String destinationUrl) {
        return eidasResponseBuilderSupplier.get()
                .withIssuer(proxyNodeMetadataForConnectorNodeUrl)
                .withStatus(getMappedStatusCode(responseStatus))
                .withInResponseTo(eidasRequestId)
                .withIssueInstant(DateTime.now())
                .withDestination(destinationUrl)
                .withAssertionConditions(connectorNodeIssuerId)
                .build();
    }

    private static String getMappedStatusCode(Status responseStatus) {
        switch (responseStatus) {
            case BAD_REQUEST:
                return StatusCode.REQUESTER;
            case INTERNAL_SERVER_ERROR:
                return StatusCode.RESPONDER;
            default:
                return StatusCode.RESPONDER;
        }
    }
}
