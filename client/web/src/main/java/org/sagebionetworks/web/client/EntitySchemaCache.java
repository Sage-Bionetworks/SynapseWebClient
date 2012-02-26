package org.sagebionetworks.web.client;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;

import com.google.inject.Inject;

/**
 * A singleton cache for Entity schemas.
 * @author John
 *
 */
public class EntitySchemaCache {
	
	/**
	 * The actual cache.
	 */
	private Map<Class<? extends Entity>, ObjectSchema> cache = new HashMap<Class<? extends Entity>, ObjectSchema>();
	JSONEntityFactory factory = null;
	
	@Inject
	public EntitySchemaCache(JSONEntityFactory factory){
		if(factory == null) throw new IllegalArgumentException("The JSONEntityFactory cannot be null");
		this.factory = factory;
	}
	
	/**
	 * Get the schema for an entity.
	 * @param entity
	 * @return
	 * @throws JSONObjectAdapterException 
	 */
	public ObjectSchema getSchemaEntity(Entity entity) throws JSONObjectAdapterException{
		if(entity == null) throw new IllegalArgumentException("Entity cannot be null");
		ObjectSchema schema = cache.get(entity.getClass());
		if(schema == null){
			ObjectSchema newSchema = new ObjectSchema();
			schema = factory.initializeEntity(entity.getJSONSchema(), newSchema);
			cache.put(entity.getClass(), schema);
		}
		return schema;		
	}

}
