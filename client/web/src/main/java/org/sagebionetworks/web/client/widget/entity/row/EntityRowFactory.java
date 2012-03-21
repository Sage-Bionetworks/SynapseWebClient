package org.sagebionetworks.web.client.widget.entity.row;

import java.util.ArrayList;
import java.util.Date;
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
				// Is this an enumeration?
				if(schema.getEnum() != null){
					return new EntityRowEnum(adapter, key, schema);
				}else if (EntityRowConcept.isConceptSchema(schema)){
					return new EntityRowConcept(adapter, key, schema);
				}else{
					return new EntityRowScalar<String>(adapter, key, schema, String.class);
				}
			}else if(FORMAT.DATE_TIME == schema.getFormat()){
				return new EntityRowScalar<Date>(adapter, key, schema, Date.class);
			}else{
				throw new IllegalArgumentException("Unknown FORMAT: "+schema.getFormat()+" for TYPE: "+schema.getType());
			}
		}else if(TYPE.INTEGER == schema.getType()){
			if(schema.getFormat() == null){
				return new EntityRowScalar<Long>(adapter, key, schema, Long.class);
			}else if(FORMAT.UTC_MILLISEC == schema.getFormat()){
				return new EntityRowScalar<Date>(adapter, key, schema, Date.class);
			}else{
				throw new IllegalArgumentException("Unknown FORMAT: "+schema.getFormat()+" for TYPE: "+schema.getType());
			}
		}else if(TYPE.NUMBER == schema.getType()){
			return new EntityRowScalar<Double>(adapter, key, schema, Double.class);
		}else if(TYPE.ARRAY == schema.getType()){
			// We must have an items
			if(schema.getItems() == null) throw new IllegalArgumentException("Items cannot be null for type: ARRAY");
			if(TYPE.STRING == schema.getItems().getType()){
				// Check the format
				if(schema.getFormat() == schema.getItems().getFormat()){
					// this is just a string
					return new EntityRowList<String>(adapter, key, schema, String.class);
				}else if(FORMAT.DATE_TIME == schema.getItems().getFormat()){
					// This is a date
					return new EntityRowList<Date>(adapter, key, schema, Date.class);
				}else{
					throw new IllegalArgumentException("Unknown FORMAT: "+schema.getFormat()+" for type "+schema.getType()+" for id: "+schema.getId());
				}

			} else if(TYPE.NUMBER == schema.getItems().getType()){
				return new EntityRowList<Double>(adapter, key, schema, Double.class);
			} else if(TYPE.INTEGER == schema.getItems().getType()){
				return new EntityRowList<Long>(adapter, key, schema, Long.class);
			}else{
				throw new IllegalArgumentException("Unknown ARRAY type: "+schema.getItems().getType()+" for "+schema.getId());
			}
		}else{
			// Unknown type.
			throw new IllegalArgumentException("Unknown type: "+schema.getType());
		}

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
	public static EntityFormModel createEntityRowList(JSONObjectAdapter adapter, ObjectSchema schema, Annotations annos, Set<String> filter){
		// Create the entity row list for the properties.
		List<EntityRow<?>> properties = createEntityRowListForProperties(adapter, schema, filter);
		// Get the name
		EntityRowScalar<String> name = (EntityRowScalar<String>) EntityRowFactory.createEntityRowByKey(adapter, schema, "name");
		// get the description.
		EntityRowScalar<String> description = (EntityRowScalar<String>) EntityRowFactory.createEntityRowByKey(adapter, schema, "description");
		return new EntityFormModel(name, description, properties, null);
	}
	
	/**
	 * Get a single row given a key.
	 * @param adapter
	 * @param schema
	 * @param key
	 * @return
	 */
	public static EntityRow<?> createEntityRowByKey(JSONObjectAdapter adapter, ObjectSchema schema, String key){
		ObjectSchema propertySchema = schema.getProperties().get(key);
		if(propertySchema == null) throw new IllegalArgumentException("Failed to find the schema for "+key+" property");
		return EntityRowFactory.createRow(adapter, propertySchema, key);
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
	public static List<EntityRow<?>> createEntityRowListForProperties(JSONObjectAdapter adapter, ObjectSchema schema, Set<String> filter){
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
	 * Add all transient properties of an entity to the passed filter.
	 * @param schema
	 * @return
	 */
	public static void addTransientToFilter(ObjectSchema schema, Set<String> filter){
		if(schema == null) throw new IllegalArgumentException("Schema cannot be null");
		if(filter == null) throw new IllegalArgumentException("Filter set cannot be null");
		for(String key: schema.getProperties().keySet()){
			ObjectSchema propSchema = schema.getProperties().get(key);
			if(propSchema.isTransient()){
				filter.add(key);
			}
		}
	}
	
	/**
	 * Add all TYPE.OBJECT to the passed filter.
	 * @param schema
	 * @return
	 */
	public static void addObjectTypeToFilter(ObjectSchema schema, Set<String> filter){
		if(schema == null) throw new IllegalArgumentException("Schema cannot be null");
		if(filter == null) throw new IllegalArgumentException("Filter set cannot be null");
		for(String key: schema.getProperties().keySet()){
			ObjectSchema propSchema = schema.getProperties().get(key);
			if(TYPE.OBJECT == propSchema.getType()){
				filter.add(key);
			}else if(TYPE.ARRAY == propSchema.getType()){
				if(propSchema.getItems() == null) throw new IllegalArgumentException("Items cannot be null for an array");
				if(TYPE.OBJECT == propSchema.getItems().getType()){
					filter.add(key);
				}
			}
		}
	}

}
