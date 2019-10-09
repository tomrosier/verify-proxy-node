package uk.gov.ida.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import engineering.reliability.gds.metrics.config.PrometheusConfiguration;
import io.dropwizard.Configuration;
import uk.gov.ida.notification.configuration.EidasSamlParserServiceConfiguration;
import uk.gov.ida.notification.configuration.RedisServiceConfiguration;
import uk.gov.ida.notification.configuration.TranslatorServiceConfiguration;
import uk.gov.ida.notification.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.notification.shared.metadata.MetadataPublishingConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class GatewayConfiguration extends Configuration implements PrometheusConfiguration {

    @Valid
    @NotNull
    @JsonProperty
    private TranslatorServiceConfiguration translatorService;

    @Valid
    @NotNull
    @JsonProperty
    private EidasSamlParserServiceConfiguration eidasSamlParserService;

    @Valid
    @NotNull
    @JsonProperty
    private VerifyServiceProviderConfiguration verifyServiceProviderService;

    @Valid
    @NotNull
    @JsonProperty
    private RedisServiceConfiguration redisService;

    @Valid
    @NotNull
    @JsonProperty
    private URI errorPageRedirectUrl;

    @Valid
    @NotNull
    @JsonProperty
    private MetadataPublishingConfiguration metadataPublishingConfiguration;

    @JsonProperty
    private boolean prometheusEnabled;

    public TranslatorServiceConfiguration getTranslatorServiceConfiguration() { return translatorService; }

    public EidasSamlParserServiceConfiguration getEidasSamlParserServiceConfiguration() { return eidasSamlParserService; }

    public VerifyServiceProviderConfiguration getVerifyServiceProviderConfiguration() { return verifyServiceProviderService; }

    public RedisServiceConfiguration getRedisService() {
        return redisService;
    }

    public URI getErrorPageRedirectUrl() {
        return errorPageRedirectUrl;
    }

    public MetadataPublishingConfiguration getMetadataPublishingConfiguration() {
        return metadataPublishingConfiguration;
    }

    @Override
    public boolean isPrometheusEnabled() {
        return prometheusEnabled;
    }
}
