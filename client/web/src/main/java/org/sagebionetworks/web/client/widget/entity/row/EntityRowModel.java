package org.sagebionetworks.web.client.widget.entity.row;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.data.ModelData;

/**
 * Models a single row of the property table.
 * 
 * @author John
 *
 */
public class EntityRowModel implements ModelData {
	
	public static final String LABEL = "Label";
	public static final String VALUE = "Value";
	
	Map<String, Object> map;
	EntityRow<?> row;
	
	public EntityRowModel(EntityRow<?> row){
		// Map this row
		map = new HashMap<String, Object>(2);
		map.put(LABEL, row.getLabel());
		map.put(VALUE, row.getDislplayValue());
		this.row = row;
	}

	@Override
	public <X> X get(String property) {
		return (X) map.get(property);
	}

	@Override
	public Map<String, Object> getProperties() {
		return map;
	}

	@Override
	public Collection<String> getPropertyNames() {
		return map.keySet();
	}

	@Override
	public <X> X remove(String property) {
		throw new IllegalStateException("Not allowed");
	}

	@Override
	public <X> X set(String property, X value) {
		throw new IllegalStateException("Not allowed");
	}

	public String getToolTipTitle() {
		return row.getDescription();
	}

	public String getToolTipBody() {
		return row.getToolTipsBody();
	}

}
