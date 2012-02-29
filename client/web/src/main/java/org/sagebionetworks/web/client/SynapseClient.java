package org.sagebionetworks.web.client;

import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.SerializableWhitelist;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("synapse")	
public interface SynapseClient extends RemoteService {

	public EntityWrapper getEntity(String entityId);

	//public EntityWrapper createEntity(EntityType type, JSONObjectAdaptor properties);
	
	public String getEntityTypeRegistryJSON();
	
	public EntityWrapper getEntityPath(String entityId);
	
	public EntityWrapper search(String searchQueryJson); 
	
	public SerializableWhitelist junk(SerializableWhitelist l);

	/**
	 * Get a bundle of information about an entity in a single call
	 * @param entityId
	 * @return
	 * @throws RestServiceException 
	 * @throws SynapseException 
	 */
	public EntityBundleTransport getEntityBundle(String entityId, int partsMask) throws RestServiceException;
	
}
