package org.sagebionetworks.web.client.widget.entity.row;

import java.util.List;

/**
 * For each field that will be shown in edit entity form we will have EntityRow.
 * @author John
 *
 */
public class EntityFormModel {
	
	// The name of this entity.
	EntityRowScalar<String> name;
	// The description of this entity.
	EntityRowScalar<String> description;
	// The first class properties of an entity.
	List<EntityRow<?>> properties;
	// the annotations of an entity
	List<EntityRow<?>> annotations;
	
	
	/**
	 * Create a new form model from all of the data.
	 * @param name
	 * @param description
	 * @param properties
	 * @param annotations
	 */
	public EntityFormModel(EntityRowScalar<String> name, EntityRowScalar<String> description,
			List<EntityRow<?>> properties, List<EntityRow<?>> annotations) {
		super();
		this.name = name;
		this.description = description;
		this.properties = properties;
		this.annotations = annotations;
	}
	public EntityRowScalar<String> getName() {
		return name;
	}
	public void setName(EntityRowScalar<String> name) {
		this.name = name;
	}
	public EntityRowScalar<String> getDescription() {
		return description;
	}
	public void setDescription(EntityRowScalar<String> description) {
		this.description = description;
	}
	public List<EntityRow<?>> getProperties() {
		return properties;
	}
	public void setProperties(List<EntityRow<?>> properties) {
		this.properties = properties;
	}
	public List<EntityRow<?>> getAnnotations() {
		return annotations;
	}
	public void setAnnotations(List<EntityRow<?>> annotations) {
		this.annotations = annotations;
	}

}
