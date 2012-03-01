package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;

public interface SynapsePersistable {

	public void addCancelHandler(CancelHandler handler); 
	
	public void addPersistSuccessHandler(EntityUpdatedHandler handler);

}
