package io.jenkins.plugins.credentials.secretsmanager.config.endpointConfiguration;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import io.jenkins.plugins.credentials.secretsmanager.util.FormValidationResult;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import io.jenkins.plugins.credentials.secretsmanager.util.PluginConfigurationForm;
import io.jenkins.plugins.credentials.secretsmanager.util.Rules;
import org.junit.Rule;
import org.junit.rules.RuleChain;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class CheckEndpointConfigurationWebIT extends AbstractCheckEndpointConfigurationIT {

    public final JenkinsConfiguredWithWebRule jenkins = new JenkinsConfiguredWithWebRule();

    @Rule
    public final RuleChain chain = RuleChain
            .outerRule(Rules.awsAccessKey("fake", "fake"))
            .around(jenkins);

    @Override
    protected FormValidationResult validate(String serviceEndpoint, String signingRegion) {
        final AtomicReference<FormValidationResult> result = new AtomicReference<>();

        jenkins.configure(f -> {
            final PluginConfigurationForm form = new PluginConfigurationForm(f);

            form.setEndpointConfiguration(serviceEndpoint, signingRegion);

            final HtmlButton validateButton = form.getValidateButtons("Test Endpoint Configuration").get(1);
            try {
                validateButton.click();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            final FormValidationResult r;
            final Optional<String> successMessage = form.getValidateSuccessMessage();
            if (successMessage.isPresent()) {
                r = FormValidationResult.success(successMessage.get());
            } else {
                final String failureMessage = form.getValidateErrorMessage();
                r = FormValidationResult.error(failureMessage);
            }

            result.set(r);
        });

        return result.get();
    }
}
