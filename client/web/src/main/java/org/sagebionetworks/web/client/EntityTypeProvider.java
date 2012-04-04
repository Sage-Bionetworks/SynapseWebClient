package org.sagebionetworks.web.client;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.RegisterConstants;
import org.sagebionetworks.repo.model.registry.EntityRegistry;
import org.sagebionetworks.repo.model.registry.EntityTypeMetadata;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.shared.EntityType;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class EntityTypeProvider {

	private List<EntityTypeMetadata> typeMetadatas;
	private List<EntityType> values;
	private EntitySchemaCache cache;
	
	@Inject
	public EntityTypeProvider(RegisterConstants constants, final AdapterFactory factory, EntitySchemaCache cache) throws UnsupportedEncodingException, JSONObjectAdapterException {
		// Read this from the constants.
		String base64String = constants.getRegisterJson();
		String decoded =  new String(Base64.decodeBase64(base64String.getBytes("UTF-8")), "UTF-8");		// Decode it
		JSONObjectAdapter adapter = factory.createNew(decoded);
		EntityRegistry registry = new EntityRegistry();
		registry.initializeFromJSONObject(adapter);
		typeMetadatas = registry.getEntityTypes();
		this.cache = cache;
		createEntityTypes();
	}
	
		
	public List<EntityType> getEntityTypes() {
		return values;
	}

	public EntityType getEntityTypeForEntity(Entity entity) {
		return getEntityTypeForUri(entity.getUri());
	}
	
	/**
	 * 
	 * @param entityType The short name for an entity, such as "project"
	 * @return
	 */
	public EntityType getEntityTypeForString(String entityType) {		
		if(entityType != null) { 
			for(EntityType type : values) {
				if(type.getName().equals(entityType) || (type.getAliases() != null && type.getAliases().contains(entityType))) {
					return type;
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param className The full class path to an entity. This is called available via getEntityType() in an Entity
	 * @return
	 */
	public EntityType getEntityTypeForClassName(String className) {
		for(EntityType type : values) {
			if(type.getClassName().equals(className)) {
				return type;
			}
		}
		return null;
	}
	
	public EntityType getEntityTypeForUri(String uri) {
		if(uri == null) throw new IllegalArgumentException("URI cannot be null");
		int maxIndex = -1;
		EntityType maxType = null;
		for(EntityType type: values) {
			int index = uri.lastIndexOf(type.getUrlPrefix());
			if(index > maxIndex){
				maxIndex = index;
				maxType = type;
			}
		}
		if(maxType != null) return maxType;
		throw new IllegalArgumentException("Unknown Entity type for URI: "+uri);
	}	
	
	/*
	 * Private Methods
	 */
	private void createEntityTypes() {
		values = new ArrayList<EntityType>();
		if(typeMetadatas != null) {
			Map<String, EntityType> pathToType = new HashMap<String, EntityType>();
			
			// create each type
			for(EntityTypeMetadata meta : typeMetadatas) {				
				EntityType type = new EntityType(meta.getName(),
						meta.getUrlPrefix(), meta.getClassName(),
						meta.getDefaultParentPath(), meta);
				pathToType.put(type.getUrlPrefix(), type);
				values.add(type);				
			}
			
			// fill in parents
			for(EntityType type : values) {
				List<EntityType> parents = new ArrayList<EntityType>();
				for(String parentUrlString : type.getMetadata().getValidParentTypes()) {
					if(pathToType.containsKey(parentUrlString)) {
						EntityType parent = pathToType.get(parentUrlString);
						if(!parents.contains(parent)) {
							parents.add(parent);
						}
					}
				}
				type.setValidParentTypes(parents);
			}
			
			// calculate and fill children			
			Map<EntityType, Set<EntityType>> typeToChildTypes = new HashMap<EntityType, Set<EntityType>>();
			for(EntityType type : values) {
				for(EntityType parent : type.getValidParentTypes()) {					
					if(!typeToChildTypes.containsKey(parent)) {
						typeToChildTypes.put(parent, new HashSet<EntityType>());
					}
					// add this type to its parent
					typeToChildTypes.get(parent).add(type);
				}
			}
			for(EntityType type : values) {
				if(typeToChildTypes.containsKey(type)) {
					Set<EntityType> children = typeToChildTypes.get(type);					
					type.setValidChildTypes(new ArrayList<EntityType>(children));
				}
			}

		}
	}
	
	/**
	 * The display name of an entity comes from the schema.
	 * @param type
	 * @return
	 */
	public String getEntityDispalyName(EntityType type){
		ObjectSchema schema = cache.getSchemaEntity(type.getClassName());
		String display = schema.getTitle();
		if(display == null) display = "";
		return display;
	}
	
	/**
	 * The display name of an entity comes from the schema.
	 * @param type
	 * @return
	 */
	public String getEntityDispalyName(Entity entity){
		EntityType type = getEntityTypeForEntity(entity);
		return getEntityDispalyName(type);
	}

}
