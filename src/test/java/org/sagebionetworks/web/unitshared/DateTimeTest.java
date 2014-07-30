package org.sagebionetworks.web.unitshared;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import org.sagebionetworks.web.shared.DateTime;

public class DateTimeTest {

    @Test
    public void testTick() {
        DateTime dt = new DateTime("don't care", 1, 0, 0);

        for (int k = 1; k < 13; k++) {
            for (int j = 0; j < 60; j++) {
                for (int i = 0; i < 60; i++) {
                    assertThat(dt.getHour()).as("Loop hours")
                        .isEqualTo(k);
                    assertThat(dt.getMinute()).as("Loop minutes")
                        .isEqualTo(j);
                    assertThat(dt.getSecond()).as("Loop seconds")
                        .isEqualTo(i);
                    dt.tick();
                }
            }
        }

        // The time should have rolled over all the way back to
        // 1:00:00.
        assertThat(dt.getHour()).as("Hours").isEqualTo(1);
        assertThat(dt.getMinute()).as("Minutes").isEqualTo(0);
        assertThat(dt.getSecond()).as("Seconds").isEqualTo(0);
    }

}
