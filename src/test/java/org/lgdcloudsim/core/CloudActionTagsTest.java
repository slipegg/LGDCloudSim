package org.lgdcloudsim.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CloudActionTagsTest {
    /**
     * Makes sure that all the tags have a string representation.
     */
    @Test
    public void testTagWithString() throws IllegalAccessException {
        for(final CloudActionTags tag : CloudActionTags.values()) {
                final String tagStr = CloudActionTags.tagToString(tag);
                assertNotEquals("UNKNOWN", tagStr);
        }
    }
}
