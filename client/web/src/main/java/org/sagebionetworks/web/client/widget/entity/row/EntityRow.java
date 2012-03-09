package org.sagebionetworks.web.client.widget.entity.row;


/**
 * This is an abstraction from a single entity property or annotations.
 * 
 * @author John
 *
 * @param <T>
 */
public interface EntityRow<T> {
	
	public String getLabel();
	
	public T getValue();
	
	public void setValue(T newValue);
	
	public String getDescription();
	

}
