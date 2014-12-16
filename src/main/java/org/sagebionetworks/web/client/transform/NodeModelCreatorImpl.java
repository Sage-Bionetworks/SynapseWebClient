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
import org.sagebionetworks.web.client.model.EntityBundle;
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
	public EntityBundle createEntityBundle(EntityBundleTransport transport) throws JSONObjectAdapterException {
		Entity entity = null;
		Annotations annotations = null;
		UserEntityPermissions permissions = null;
		EntityPath path = null;
		List<AccessRequirement> accessRequirements = null;
		List<AccessRequirement> unmetDownloadAccessRequirements = null;
		List<FileHandle> fileHandles = null;
		TableBundle tableBundle = null;
		Long version = null;
		// entity?
		if(transport.getEntityJson() != null){
			entity = factory.createEntity(transport.getEntityJson());
		}
		// annotations?
		if(transport.getAnnotationsJson() != null){
			annotations = factory.initializeEntity(transport.getAnnotationsJson(), new Annotations());
		}
		// permissions?
		if(transport.getPermissions() != null){
			permissions = transport.getPermissions();
		}
		// path?
		if(transport.getEntityPath() != null){
			path =  transport.getEntityPath();
		}
		// accessRequirements?
		if(transport.getAccessRequirementsJson() != null){
			accessRequirements =  new ArrayList<AccessRequirement>();
			JSONArrayAdapter aa = jsonObjectAdapter.createNewArray(transport.getAccessRequirementsJson());
			for (int i=0; i<aa.length(); i++) {
				JSONObjectAdapter joa = aa.getJSONObject(i);
				accessRequirements.add((AccessRequirement)EntityClassHelper.deserialize(joa));
			}
		}			
		// unmetDownloadAccessRequirements?
		if(transport.getUnmetDownloadAccessRequirementsJson() != null){
			unmetDownloadAccessRequirements =  new ArrayList<AccessRequirement>();
			JSONArrayAdapter aa = jsonObjectAdapter.createNewArray(transport.getUnmetDownloadAccessRequirementsJson());
			for (int i=0; i<aa.length(); i++) {
				JSONObjectAdapter joa = aa.getJSONObject(i);
				unmetDownloadAccessRequirements.add((AccessRequirement)EntityClassHelper.deserialize(joa));
			}
		}
		// file handles?
		if(transport.getFileHandlesJson() != null){
			fileHandles =  new ArrayList<FileHandle>();
			JSONArrayAdapter aa = jsonObjectAdapter.createNewArray(transport.getFileHandlesJson());
			for (int i=0; i<aa.length(); i++) {
				JSONObjectAdapter joa = aa.getJSONObject(i);
				String concreteClassName = (String)joa.get(FILE_HANDLE_TYPE_FIELD_NAME);
				fileHandles.add((FileHandle)factory.createEntity(joa.toJSONString(), concreteClassName));
			}
		}
		// Table data
		if (transport.getTableData() != null) {
			tableBundle = transport.getTableData();
		}
		
		// put it all together.
		EntityBundle eb = new EntityBundle(entity, annotations, permissions,
				path, accessRequirements, unmetDownloadAccessRequirements, fileHandles, tableBundle);
		// Set the child count when there.
		if(transport.getHasChildren() != null){
			eb.setChildCount(transport.getHasChildren());
		}

		return eb;
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

