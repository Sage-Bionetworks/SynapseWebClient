package org.sagebionetworks.web.client.widget.handlers;

import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;

public interface AreaChangeHandler {

	void areaChanged(Synapse.EntityArea area, String areaToken);

	void replaceArea(EntityArea area, String areaToken);
}
