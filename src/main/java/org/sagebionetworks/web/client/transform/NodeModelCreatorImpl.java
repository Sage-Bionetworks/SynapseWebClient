package org.sagebionetworks.web.client.transform;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityClassHelper;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.shared.DownloadLocation;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityTypeResponse;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.LayerPreview;
import org.sagebionetworks.web.shared.PagedResults;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.inject.Inject;

/**
 * This class exists to isolate JSONObject creation from any classes that need JVM based tests
 * This class doesn't need to be tested as the business logic is located elsewhere.
 * (JSONObect creation and JSONParser should not be used in classes that need testing)
 * @author dburdick
 *
 */
public class NodeModelCreatorImpl implements NodeModelCreator {		
	
	JSONEntityFactory factory;
	JSONObjectAdapter jsonObjectAdapter;
	
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
		PaginatedResults<EntityHeader> referencedBy = null;
		List<AccessRequirement> accessRequirements = null;
		List<AccessRequirement> unmetAccessRequirements = null;
		// entity?
		if(transport.getEntityJson() != null){
			entity = factory.createEntity(transport.getEntityJson());
		}
		// annotaions?
		if(transport.getAnnotationsJson() != null){
			annotations = factory.initializeEntity(transport.getAnnotationsJson(), new Annotations());
		}
		// permissions?
		if(transport.getPermissionsJson() != null){
			permissions = factory.createEntity(transport.getPermissionsJson(), UserEntityPermissions.class);
		}
		// path?
		if(transport.getEntityPathJson() != null){
			path =  factory.createEntity(transport.getEntityPathJson() , EntityPath.class);
		}
		// referencedBy?
		if(transport.getEntityReferencedByJson() != null){
			referencedBy =  new PaginatedResults<EntityHeader>(EntityHeader.class, factory);
			referencedBy.initializeFromJSONObject(jsonObjectAdapter.createNew(transport.getEntityReferencedByJson()));
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
		// unmetAccessRequirements?
		if(transport.getUnmetAccessRequirementsJson() != null){
			unmetAccessRequirements =  new ArrayList<AccessRequirement>();
			JSONArrayAdapter aa = jsonObjectAdapter.createNewArray(transport.getUnmetAccessRequirementsJson());
			for (int i=0; i<aa.length(); i++) {
				JSONObjectAdapter joa = aa.getJSONObject(i);
				unmetAccessRequirements.add((AccessRequirement)EntityClassHelper.deserialize(joa));
			}
		}
		// put it all together.
		EntityBundle eb =  new EntityBundle(entity, annotations, 
				permissions, path, referencedBy,
				accessRequirements, unmetAccessRequirements);
		// Set the child count when there.
		if(transport.getHasChildren() != null){
			eb.setChildCount(transport.getHasChildren());
		}

		return eb;
	}

	@Deprecated
	@Override
	public EntityTypeResponse createEntityTypeResponse(String json) throws RestServiceException {
		JSONObject obj = JSONParser.parseStrict(json).isObject();
		DisplayUtils.checkForErrors(obj);
		return new EntityTypeResponse(obj);
	}
		
}

