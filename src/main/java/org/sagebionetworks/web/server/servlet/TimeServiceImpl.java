package org.sagebionetworks.web.server.servlet;

import java.util.Set;

import org.sagebionetworks.web.shared.DateTime;
import org.sagebionetworks.web.client.TimeService;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.TemporalAccessor;

import static org.threeten.bp.temporal.ChronoField.*;

public class TimeServiceImpl implements TimeService {

    private static DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("eeee, MMMM dd, yyyy 'at' hh:mm:ss a");

    public Set<String> getAvailableZoneIds() {
        return ZoneId.getAvailableZoneIds();
    }

    public DateTime getTimeForZoneId(String zoneId) {
        TemporalAccessor now = Instant.now().atZone(ZoneId.of(zoneId));

        return new DateTime(formatter.format(now),
                            now.get(HOUR_OF_AMPM),
                            now.get(MINUTE_OF_HOUR),
                            now.get(SECOND_OF_MINUTE));
    }

}
