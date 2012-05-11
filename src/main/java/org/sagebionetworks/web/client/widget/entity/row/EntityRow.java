package org.sagebionetworks.web.client.widget.entity.row;


/**
 * This is an abstraction from a single entity property or annotations.
 * 
 * @author John
 *
 * @param <T>
 */
public interface EntityRow<T> {
	
	/**
	 * This row's label.
	 * @return
	 */
	public String getLabel();
	
	/**
	 * The actual value of this row.
	 * @return
	 */
	public T getValue();
	
	/**
	 * The actual value of this row.
	 * @param newValue
	 */
	public void setValue(T newValue);
	
	/**
	 * The description of this row.
	 * @return
	 */
	public String getDescription();
	
	/**
	 * The body of the tool tips.
	 * @return
	 */
	public String getToolTipsBody();
	
	/**
	 * The short display string for this row's value.
	 * 
	 * @return
	 */
	public String getDislplayValue();
	

}
