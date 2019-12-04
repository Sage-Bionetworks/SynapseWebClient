package org.sagebionetworks.web.client.widget.table.v2.schema;

import org.sagebionetworks.repo.model.table.ColumnType;

/**
 * Mapping of a column type to a view.
 * 
 * @author John
 *
 */
public enum ColumnTypeViewEnum {

	String(ColumnType.STRING), 
	Boolean(ColumnType.BOOLEAN), 
	Integer(ColumnType.INTEGER), 
	Double(ColumnType.DOUBLE), 
	Date(ColumnType.DATE), 
	File(ColumnType.FILEHANDLEID), 
	Entity(ColumnType.ENTITYID), 
	Link(ColumnType.LINK), 
	LargeText(ColumnType.LARGETEXT), 
	User(ColumnType.USERID),
	StringList(ColumnType.STRING_LIST),
	IntegerList(ColumnType.INTEGER_LIST),
	DateList(ColumnType.DATE_LIST),
	BooleanList(ColumnType.BOOLEAN_LIST);

	private ColumnType type;

	ColumnTypeViewEnum(ColumnType type) {
		this.type = type;
	}

	/**
	 * Get the type for this view.
	 * 
	 * @return
	 */
	public ColumnType getType() {
		return this.type;
	}

	/**
	 * Lookup the view for a type.
	 * 
	 * @param type
	 * @return
	 */
	public static ColumnTypeViewEnum getViewForType(ColumnType type) {
		for (ColumnTypeViewEnum view : ColumnTypeViewEnum.values()) {
			if (view.type.equals(type)) {
				return view;
			}
		}
		throw new IllegalArgumentException("Unknown type: " + type);
	}
}
