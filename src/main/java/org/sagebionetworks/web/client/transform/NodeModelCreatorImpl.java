package org.sagebionetworks.web.client.transform;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityClassHelper;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityTypeResponse;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.inject.Inject;

/**
 * This class exists to isolate JSONObject creation from any classes that need JVM based tests
 * 
 * CODE SPLITTING NOTE: JSONEntityFactory brings in AutoGenFactory and thus all of the repo.model package
 * 
 * @author dburdick
 *
 */
public class NodeModelCreatorImpl implements NodeModelCreator {		
	
	JSONEntityFactory factory;
	JSONObjectAdapter jsonObjectAdapter;

	private static final String FILE_HANDLE_TYPE_FIELD_NAME = "concreteType";
	
	@Inject
	public NodeModelCreatorImpl(JSONEntityFactory factory, JSONObjectAdapter jsonObjectAdapter) {
		this.factory = factory;
		this.jsonObjectAdapter = jsonObjectAdapter;
	}
	
	@Override
	public Entity createEntity(EntityWrapper entityWrapper) throws JSONObjectAdapterException {
		return (Entity) factory.createEntity(entityWrapper.getEntityJson(), entityWrapper.getEntityClassName());
	}
	
	@Override
	public <T extends JSONEntity> T createJSONEntity(String jsonString, Class<? extends T> clazz) throws JSONObjectAdapterException {
		return factory.createEntity(jsonString, clazz);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends JSONEntity> T createJSONEntity(String jsonString, String entityClassName) throws JSONObjectAdapterException {
		return (T)factory.createEntity(jsonString, entityClassName);
	}
		
	@Override
	public <T extends JSONEntity> T initializeJSONEntity(String json, T newEntity) throws JSONObjectAdapterException {
		return factory.initializeEntity(json, newEntity);
	}
	
	@Override
	public <T extends JSONEntity> PaginatedResults<T> createPaginatedResults(String jsonString, Class<? extends JSONEntity> clazz) throws JSONObjectAdapterException {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		PaginatedResults<T> paginatedResults = new PaginatedResults(clazz, factory);
		paginatedResults.initializeFromJSONObject(jsonObjectAdapter.createNew(jsonString));
		return paginatedResults;
	}

	@Override
	public <T extends JSONEntity> BatchResults<T> createBatchResults(String jsonString, Class<? extends JSONEntity> clazz) throws JSONObjectAdapterException {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		BatchResults<T> batchResults = new BatchResults(clazz);
		batchResults.initializeFromJSONObject(jsonObjectAdapter.createNew(jsonString));
		return batchResults;
	}

	@Override
	public JSONEntity newInstance(String className) {
		return factory.newInstance(className);
	}
	
	@Deprecated
	@Override
	public EntityTypeResponse createEntityTypeResponse(String json) throws RestServiceException {
		JSONObject obj = JSONParser.parseStrict(json).isObject();
		DisplayUtils.checkForErrors(obj);
		return new EntityTypeResponse(obj);
	}
		
}

