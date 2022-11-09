package org.sagebionetworks.web.unitclient.widget.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.widget.evaluation.DurationHelper;

@RunWith(MockitoJUnitRunner.class)
public class DurationHelperTest {

  @Test
  public void testNullMs() {
    DurationHelper durationHelper = new DurationHelper(null);
    assertNull(durationHelper.getDurationMs());
  }

  @Test
  public void testRoundTrip() {
    // used external service (https://www.epochconverter.com/) to figure out ms between current time and a known amount in the future...
    Long knownDuration = 93784000L;
    DurationHelper durationHelper = new DurationHelper(knownDuration);
    assertEquals(1, durationHelper.getDays().intValue());
    assertEquals(2, durationHelper.getHours().intValue());
    assertEquals(3, durationHelper.getMinutes().intValue());
    assertEquals(4, durationHelper.getSeconds().intValue());

    // seconds, minutes, hours, days
    durationHelper = new DurationHelper(4.0, 3.0, 2.0, 1.0);
    assertEquals(knownDuration, durationHelper.getDurationMs());

    // try setting some values to null
    durationHelper = new DurationHelper(4.0, null, 2.0, null);
    assertEquals(7204000L, durationHelper.getDurationMs().longValue());
  }
}
