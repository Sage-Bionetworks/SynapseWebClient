package org.sagebionetworks.web.client.widget.entity.row;

import java.util.List;

/**
 * For each field that will be shown in edit entity form we will have EntityRow.
 * @author John
 *
 */
public class EntityFormModel {
	
	// The name of this entity.
	EntityRowString name;
	// The description of this entity.
	EntityRowString description;
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
	public EntityFormModel(EntityRowString name, EntityRowString description,
			List<EntityRow<?>> properties, List<EntityRow<?>> annotations) {
		super();
		this.name = name;
		this.description = description;
		this.properties = properties;
		this.annotations = annotations;
	}
	public EntityRowString getName() {
		return name;
	}
	public void setName(EntityRowString name) {
		this.name = name;
	}
	public EntityRowString getDescription() {
		return description;
	}
	public void setDescription(EntityRowString description) {
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
