package org.sagebionetworks.web.client.widget.table.v2.schema;

import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.FacetType;

/**
 * Mapping of a column type to a view.
 * 
 * @author John
 *
 */
public enum ColumnFacetTypeViewEnum {
	None(null),
	Values(FacetType.enumeration),
	Range(FacetType.range);
	
	private FacetType type;
	ColumnFacetTypeViewEnum(FacetType type){
		this.type = type;
	}
	/**
	 * Get the type for this view.
	 * @return
	 */
	public FacetType getType(){
		return this.type;
	}
	
	/**
	 * Lookup the view for a type.
	 * @param type
	 * @return
	 */
	public static ColumnFacetTypeViewEnum getViewForType(FacetType type){
		if (type == null) {
			return ColumnFacetTypeViewEnum.None;
		}
		for(ColumnFacetTypeViewEnum view: ColumnFacetTypeViewEnum.values()){
			if(view.type.equals(type)){
				return view;
			}
		}
		throw new IllegalArgumentException("Unknown type: "+type);
	}
}
