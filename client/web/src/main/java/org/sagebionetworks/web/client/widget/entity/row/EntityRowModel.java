package org.sagebionetworks.web.client.widget.entity.row;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.data.ModelData;

/**
 * EntityRow as 
 * @author John
 *
 */
public class EntityRowModel implements ModelData {
	
	public static final String LABEL = "Label";
	public static final String VALUE = "Value";
	
	Map<String, Object> map;
	String toolTipTitle;
	String toolTipBody;
	
	public EntityRowModel(String label, String value, String toolTipTitle, String toolTipBody){
		// Map this row
		map = new HashMap<String, Object>();
		map.put(LABEL, label);
		map.put(VALUE, value);
		this.toolTipTitle = toolTipTitle;
		this.toolTipBody = toolTipBody;
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
		return toolTipTitle;
	}

	public String getToolTipBody() {
		return toolTipBody;
	}

	@Override
	public String toString() {
		return "EntityRowModel [map=" + map + ", toolTipTitle=" + toolTipTitle
				+ ", toolTipBody=" + toolTipBody + "]";
	}

}
