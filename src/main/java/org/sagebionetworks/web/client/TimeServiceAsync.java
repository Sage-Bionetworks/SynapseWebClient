package org.sagebionetworks.web.client;

import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.sagebionetworks.web.shared.DateTime;

/**
 *
 * This interface allows the client to retrieve localized date-times
 * for arbitrary timezones for use in the Wiki clock widget for
 * SWC-1496
 *
 * @author Geoff Shannon
 */
public interface TimeServiceAsync {

    void getAvailableZoneIds(AsyncCallback<Set<String>> callback);

    void getTimeForZoneId(String zoneId,
                          AsyncCallback<DateTime> callback);

}
