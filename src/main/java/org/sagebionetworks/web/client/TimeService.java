package org.sagebionetworks.web.client;

import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import org.sagebionetworks.web.shared.DateTime;

/**
 *
 * This interface allows the client to retrieve localized date-times
 * for arbitrary timezones for use in the Wiki clock widget for
 * SWC-1496
 *
 * @author Geoff Shannon
 */
@RemoteServiceRelativePath("time")
public interface TimeService extends RemoteService {

    Set<String> getAvailableZoneIds();

    DateTime getTimeForZoneId(String zoneId);

}
