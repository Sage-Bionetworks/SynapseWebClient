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
public class EntitySchemaCacheImpl implements EntitySchemaCache {
	
	/**
	 * The actual cache.
	 */
	private Map<Class<? extends Entity>, ObjectSchema> cache = new HashMap<Class<? extends Entity>, ObjectSchema>();
	JSONEntityFactory factory = null;
	
	@Inject
	public EntitySchemaCacheImpl(JSONEntityFactory factory){
		if(factory == null) throw new IllegalArgumentException("The JSONEntityFactory cannot be null");
		this.factory = factory;
	}
	
	/**
	 * Get the schema for an entity.
	 * @param entity
	 * @return
	 * @throws JSONObjectAdapterException 
	 */
	@Override
	public ObjectSchema getSchemaEntity(Entity entity){
		if(entity == null) throw new IllegalArgumentException("Entity cannot be null");
		return getEntitySchema(entity.getJSONSchema(), entity.getClass());
	}

	/**
	 * Get schema entity for a class and json.
	 */
	@Override
	public ObjectSchema getEntitySchema(String json, Class<? extends Entity> clazz) {
		if(json == null) throw new IllegalArgumentException("JSON cannot be null");
		ObjectSchema schema = cache.get(clazz);
		if(schema == null){
			ObjectSchema newSchema = new ObjectSchema();
			try {
				schema = factory.initializeEntity(json, newSchema);
			} catch (JSONObjectAdapterException e) {
				throw new RuntimeException(e);
			}
			cache.put(clazz, schema);
		}
		return schema;		
	}

}
