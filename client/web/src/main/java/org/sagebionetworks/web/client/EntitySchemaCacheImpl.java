package org.sagebionetworks.web.client;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

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
	//JSONEntityFactory factory = null;
	AdapterFactory adapterFactory;
	AutoGenFactory entityFactory;
	
	@Inject
	public EntitySchemaCacheImpl(AdapterFactory adapterFactory){
		if(adapterFactory == null) throw new IllegalArgumentException("The AdapterFactory cannot be null");
		this.adapterFactory = adapterFactory;
		this.entityFactory = new AutoGenFactory();
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
				JSONObjectAdapter adapter = adapterFactory.createNew(json);
				newSchema.initializeFromJSONObject(adapter);
				schema = newSchema;
			} catch (JSONObjectAdapterException e) {
				throw new RuntimeException(e);
			}
			cache.put(clazz, schema);
		}
		return schema;		
	}

	@Override
	public ObjectSchema getSchemaEntity(String entityClassName) {
		// First get an instance of the class.
		Entity instance = (Entity) entityFactory.newInstance(entityClassName);
		Class clazz = instance.getClass();
		return getEntitySchema(instance.getJSONSchema(), clazz);
	}

}
