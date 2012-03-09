package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

/**
 * Abstraction for the EntitySchemaCache.
 * @author John
 *
 */
public interface EntitySchemaCache {
	/**
	 * Get the schema for an entity.
	 * @param entity
	 * @return
	 * @throws JSONObjectAdapterException 
	 */
	public ObjectSchema getSchemaEntity(Entity entity);
	
	/**
	 * Get the schema for a class
	 * @param json
	 * @param clazz
	 * @return
	 */
	public ObjectSchema getEntitySchema(String json, Class<? extends Entity> clazz);
}
