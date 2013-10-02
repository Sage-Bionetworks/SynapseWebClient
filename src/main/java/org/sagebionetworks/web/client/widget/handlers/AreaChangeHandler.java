package org.sagebionetworks.web.client.widget.handlers;

import org.sagebionetworks.web.client.place.Synapse;

public interface AreaChangeHandler {

	void areaChanged(Synapse.EntityArea area, String areaToken);
}
