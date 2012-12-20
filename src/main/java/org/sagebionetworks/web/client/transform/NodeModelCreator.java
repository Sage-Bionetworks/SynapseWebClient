package org.sagebionetworks.web.client.transform;


import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityTypeResponse;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

public interface NodeModelCreator {

	Entity createEntity(EntityWrapper entityWrapper) throws JSONObjectAdapterException;
	
		<T extends JSONEntity> T createJSONEntity(String jsonString, Class<? extends T> clazz) throws JSONObjectAdapterException;

	<T extends JSONEntity> T createJSONEntity(String jsonString, String entityClassName) throws JSONObjectAdapterException;
	
	<T extends JSONEntity> T initializeJSONEntity(String json, T newEntity) throws JSONObjectAdapterException;
	
	<T extends JSONEntity> PaginatedResults<T> createPaginatedResults(String jsonString, Class<? extends JSONEntity> clazz) throws JSONObjectAdapterException;

	<T extends JSONEntity> BatchResults<T> createBatchResults(String jsonString, Class<? extends JSONEntity> clazz) throws JSONObjectAdapterException;

	JSONEntity newInstance(String className);
	/**
	 * Convert the transport object to the bundle
	 * @param transport
	 * @return
	 * @throws RestServiceException 
	 */
	EntityBundle createEntityBundle(EntityBundleTransport transport) throws JSONObjectAdapterException;
	
	@Deprecated
	EntityTypeResponse createEntityTypeResponse(String json) throws RestServiceException;
	
}
