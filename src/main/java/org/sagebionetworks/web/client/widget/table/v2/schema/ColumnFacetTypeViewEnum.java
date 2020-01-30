package org.sagebionetworks.web.client.widget.table.v2.schema;

import org.sagebionetworks.repo.model.table.FacetType;

/**
 * Mapping of a column type to a view.
 * 
 * @author John
 *
 */
public enum ColumnFacetTypeViewEnum {
	None(null, ""), Values(FacetType.enumeration, "Values"), Range(FacetType.range, "Range");

	private FacetType type;
	private String friendlyDisplay;

	ColumnFacetTypeViewEnum(FacetType type, String friendlyDisplay) {
		this.type = type;
		this.friendlyDisplay = friendlyDisplay;
	}

	/**
	 * Get the type for this view.
	 * 
	 * @return
	 */
	public FacetType getType() {
		return this.type;
	}

	/**
	 * Lookup the view for a type.
	 * 
	 * @param type
	 * @return
	 */
	public static ColumnFacetTypeViewEnum getViewForType(FacetType type) {
		if (type == null) {
			return ColumnFacetTypeViewEnum.None;
		}
		for (ColumnFacetTypeViewEnum view : ColumnFacetTypeViewEnum.values()) {
			if (type.equals(view.type)) {
				return view;
			}
		}
		throw new IllegalArgumentException("Unknown type: " + type);
	}

	/**
	 * Lookup the view for a given friendly name.
	 * 
	 * @param type
	 * @return
	 */
	public static ColumnFacetTypeViewEnum getEnumForFriendlyName(String targetFriendlyName) {
		if (targetFriendlyName == null) {
			return ColumnFacetTypeViewEnum.None;
		}
		for (ColumnFacetTypeViewEnum view : ColumnFacetTypeViewEnum.values()) {
			if (targetFriendlyName.equals(view.toString())) {
				return view;
			}
		}
		throw new IllegalArgumentException("Unknown friendly name: " + targetFriendlyName);
	}

	@Override
	public String toString() {
		return friendlyDisplay;
	}
}
