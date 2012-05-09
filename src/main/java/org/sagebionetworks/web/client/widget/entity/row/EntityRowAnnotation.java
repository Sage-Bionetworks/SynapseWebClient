package org.sagebionetworks.web.client.widget.entity.row;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.widget.entity.StringValueUtils;

/**
 * An entity row for an annotation.
 * @author jmhill
 *
 * @param <T>
 */
public class EntityRowAnnotation<T> implements EntityRowList<T> {
	
	String key;
	Map<String, List<T>> map;
	String toolTipsBody;
	String dislplayValue;
	Class<? extends T> clazz;
	
	public EntityRowAnnotation(Map<String, List<T>> map, String key, Class<? extends T> clazz){
		this.map = map;
		this.key = key;
		this.clazz = clazz;
		updateDisplayValue(map.get(key));
	}
	@Override
	public String getLabel() {
		return key;
	}
	
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getToolTipsBody() {
		return this.toolTipsBody;
	}

	@Override
	public String getDislplayValue() {
		return this.dislplayValue;
	}
	@Override
	public void setValue(List<T> newValue) {
		map.put(key, newValue);
		updateDisplayValue(newValue);
	}
	
	/**
	 * Update the display value
	 * @param newValue
	 */
	public final void updateDisplayValue(Object newValue){
		this.toolTipsBody = StringValueUtils.valueToToolTips(newValue);
		this.dislplayValue = StringValueUtils.valueToString(newValue);
	}
	@Override
	public List<T> getValue() {
		return map.get(key);
	}
	
	/**
	 * The type of this annotation.
	 * @return
	 */
	public Class<? extends T> getListClass(){
		return clazz;
	}

}
