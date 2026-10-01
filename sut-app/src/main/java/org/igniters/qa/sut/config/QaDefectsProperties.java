package org.igniters.qa.sut.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Backs the {@code QA_DEFECTS_ENABLED} environment variable. When true, the
 * five deliberate bugs documented in the README are switched on so the test
 * suites have something real to catch. Defaults to false — "correct" behavior.
 */
@Component
@ConfigurationProperties(prefix = "igniters")
public class QaDefectsProperties {

    private boolean qaDefectsEnabled = false;

    public boolean isEnabled() {
        return qaDefectsEnabled;
    }

    public void setQaDefectsEnabled(boolean qaDefectsEnabled) {
        this.qaDefectsEnabled = qaDefectsEnabled;
    }
}
