package org.sagebionetworks.web.client.transform;


import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.shared.DownloadLocation;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityTypeResponse;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.LayerPreview;
import org.sagebionetworks.web.shared.PagedResults;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;

public interface NodeModelCreator {

	Entity createEntity(EntityWrapper entityWrapper) throws RestServiceException;
	
	<T extends JSONEntity> T createEntity(String jsonString, Class<? extends T> clazz) throws RestServiceException;
	
	<T extends JSONEntity> T createEntity(EntityWrapper entityWrapper, Class<? extends T> clazz) throws RestServiceException;
	
	<T extends JSONEntity> T initializeEntity(String json, T newEntity) throws RestServiceException;
	
	/**
	 * Convert the transport object to the bundle
	 * @param transport
	 * @return
	 * @throws RestServiceException 
	 */
	EntityBundle createEntityBundle(EntityBundleTransport transport) throws RestServiceException;
	
	PagedResults createPagedResults(String json) throws RestServiceException;

	LayerPreview createLayerPreview(String json) throws RestServiceException;
	
	DownloadLocation createDownloadLocation(String json) throws RestServiceException;
	
	EntityTypeResponse createEntityTypeResponse(String json) throws RestServiceException;
	
	/**
	 * Validates that the json parses and does not throw any RestService exceptions
	 * this is useful for json that doesn't have a model object (like schemas)
	 * @param json
	 * @throws UnauthorizedException
	 * @throws ForbiddenException
	 */
	void validate(String json) throws RestServiceException;

	<T extends JSONEntity> PaginatedResults<T> createPaginatedResults(String jsonString, Class<? extends JSONEntity> clazz);

	<T extends JSONEntity> BatchResults<T> createBatchResults(String jsonString, Class<? extends JSONEntity> clazz);

}
