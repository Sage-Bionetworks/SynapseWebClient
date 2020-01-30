package org.sagebionetworks.web.client.widget.table.v2.schema;

import java.util.List;

/**
 * A view of a single ColumnModel.
 * 
 * @author John
 *
 */
public interface ColumnModelView {

	/**
	 * ColumnModel.id
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * ColumnModel.id
	 */
	public void setId(String id);

	/**
	 * ColumnModel.name
	 * 
	 * @return
	 */
	public String getColumnName();

	/**
	 * ColumnModel.name
	 */
	public void setColumnName(String name);

	/**
	 * ColumnModel.columnType
	 * 
	 * @return
	 */
	public ColumnTypeViewEnum getColumnType();

	/**
	 * ColumnModel.columnType
	 */
	public void setColumnType(ColumnTypeViewEnum type);

	/**
	 * ColumnModel.facetType
	 * 
	 * @return
	 */
	public ColumnFacetTypeViewEnum getFacetType();

	/**
	 * ColumnModel.facetType
	 */
	public void setFacetType(ColumnFacetTypeViewEnum type);

	/**
	 * ColumnModel.maximumSize
	 * 
	 * @return
	 */
	public String getMaxSize();

	/**
	 * ColumnModel.maximumSize
	 */
	public void setMaxSize(String maxSize);

	/**
	 * ColumnModel.defaultValue
	 * 
	 * @return
	 */
	public String getDefaultValue();

	/**
	 * ColumnModel.defaultValue
	 */
	public void setDefaultValue(String defaultValue);

	/**
	 * Restrict values to an enumeration.
	 * 
	 * @param enums
	 */
	public void setEnumValues(List<String> enums);

	/**
	 * Restrict values to an enumeration.
	 * 
	 * @return
	 */
	public List<String> getEnumValues();
}
