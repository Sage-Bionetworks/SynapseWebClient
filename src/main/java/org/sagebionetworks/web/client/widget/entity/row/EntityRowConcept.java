package org.sagebionetworks.web.client.widget.entity.row;

import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;

/**
 * An ontology concept entity row.
 * 
 * @author jmhill
 *
 */
public class EntityRowConcept extends EntityRowScalar<String> {
	
	/**
	 * All concpets must start with this prefix
	 */
	public static String CONCEPT_URL_PREFIX = "http://synapse.sagebase.org/ontology";
	public static String DELIMITER = "#";
	
	String conceptId;

	public EntityRowConcept(JSONObjectAdapter adapter, String key,
			ObjectSchema propertySchema) {
		super(adapter, key, propertySchema, String.class);
		// Extract the concept ID for this row
		conceptId = extractConceptId(propertySchema);
	}

	/**
	 * Extract the concept ID from the schema.
	 * @param propertySchema
	 * @return
	 */
	public static String extractConceptId(ObjectSchema propertySchema) {
		if(propertySchema.getLinks() == null) throw new IllegalArgumentException("A concept must have links that describe it.");
		if(propertySchema.getLinks().length != 1) throw new IllegalArgumentException("A concept must have a single link description but length was: "+propertySchema.getLinks().length);
		if(propertySchema.getLinks()[0] == null) throw new IllegalArgumentException("A concept must have a single link description but was null");
		if(propertySchema.getLinks()[0].getHref() == null) throw new IllegalArgumentException("A concept must have a single link description but getHref() returned null");
		String concpetUrl = propertySchema.getLinks()[0].getHref();
		String[] split = concpetUrl.split(DELIMITER);
		if(split.length != 2) throw new IllegalArgumentException("Cannot parse concept url: "+concpetUrl);
		if(!CONCEPT_URL_PREFIX.equals(split[0].toLowerCase())) throw new IllegalArgumentException("Cannot parse concept url: "+concpetUrl);
		return split[1];
	}
	
	
	/**
	 * Is the passed schema a concept property?
	 * @param propertySchema
	 * @return
	 */
	public static boolean isConceptSchema(ObjectSchema propertySchema){
		try{
			// This is a concept if we can extract the concept Id
			return extractConceptId(propertySchema) != null;
		}catch(IllegalArgumentException e){
			// This is not a concept
			return false;
		}
	}
	/**
	 * Get the ID of this concept.
	 * @return
	 */
	public String getConceptId(){
		return conceptId;
	}

}
