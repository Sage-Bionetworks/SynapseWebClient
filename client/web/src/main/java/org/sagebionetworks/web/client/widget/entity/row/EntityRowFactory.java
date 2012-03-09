package org.sagebionetworks.web.client.widget.entity.row;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.schema.FORMAT;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.TYPE;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;

/**
 * Factory for creating EntityRow objects using the schema of a property.
 * 
 * @author John
 *
 */
public class EntityRowFactory {
	

	/**
	 * Create a new entity row for a given property schema.
	 * 
	 * @param adapter - The Entity adapter
	 * @param schema - The schema for the property.
	 * @param key - The property key.
	 * @return
	 */
	public static EntityRow<?> createRow(JSONObjectAdapter adapter, ObjectSchema schema, String key){
		if(adapter == null) throw new IllegalArgumentException("Adapter cannot be null");
		if(schema == null) throw new IllegalArgumentException("Schema cannot be null");
		if(key == null) throw new IllegalArgumentException("Key cannot be null");
		// The schema determines which row type we use.
		if(TYPE.STRING == schema.getType()){
			if(schema.getFormat() == null){
				return new EntityRowString(adapter, key, schema);
			}else if(FORMAT.DATE_TIME == schema.getFormat()){
				return new EntityRowDateAsString(adapter, key, schema);
			}else{
				throw new IllegalArgumentException("Unknown FORMAT: "+schema.getFormat()+" for TYPE: "+schema.getType());
			}
		}else if(TYPE.INTEGER == schema.getType()){
			if(schema.getFormat() == null){
				return new EntityRowLong(adapter, key, schema);
			}else if(FORMAT.UTC_MILLISEC == schema.getFormat()){
				return new EntityRowDateAsLong(adapter, key, schema);
			}else{
				throw new IllegalArgumentException("Unknown FORMAT: "+schema.getFormat()+" for TYPE: "+schema.getType());
			}
		}else if(TYPE.NUMBER == schema.getType()){
			return new EntityRowDouble(adapter, key, schema);
		}
		return null;
//		throw new IllegalArgumentException("Unknown type: "+schema.getType());
	}
	
	/**
	 * Create a new list of property data from an entity.
	 * 
	 * @param entity
	 * @param schema
	 * @param annos
	 * @param filter - Properties that should be filter out.
	 * @return
	 */
	public static List<EntityRow<?>> createEntityRowList(JSONObjectAdapter adapter, ObjectSchema schema, Annotations annos, Set<String> filter){
		List<EntityRow<?>> results = new ArrayList<EntityRow<?>>();
		// Fill in the list from the entity
		for(String key: schema.getProperties().keySet()){
			// filter any names we are asked to
			if(filter.contains(key)) continue;
			ObjectSchema propertySchema = schema.getProperties().get(key);
			// Only include properties with titles
			if(propertySchema.getTitle() == null) continue;
			// Use the factory to create the entity row
			EntityRow<?> row = EntityRowFactory.createRow(adapter, propertySchema, key);
			if(row != null){
				results.add(row);
			}
		}
		return results;
	}
	
	/**
	 * Create a filter for all transient properties of an entity..
	 * @param schema
	 * @return
	 */
	public static Set<String> createTransientFilter(ObjectSchema schema){
		if(schema == null) throw new IllegalArgumentException("Schema cannot be null");
		HashSet<String> filter = new HashSet<String>();
		for(String key: schema.getProperties().keySet()){
			ObjectSchema propSchema = schema.getProperties().get(key);
			if(propSchema.isTransient()){
				filter.add(key);
			}
		}
		return filter;
	}

}
