package org.sagebionetworks.web.client.widget.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.data.ModelData;

/**
 * Nothing like using a map to store a single cell but we work with what we have.
 * @author John
 *
 * @param <T>
 */
public class ListItem<T> implements ModelData {
	public static final String VALUE = "Value";
	public static final String REMOVE_COLUMN_ID = "Remove";
	Map<String, Object> map;
	
	public ListItem(T item){
		this.map = new HashMap<String, Object>(1);
		this.map.put(VALUE, item);
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
		throw new IllegalArgumentException("Not supported");
	}

	@Override
	public <X> X set(String property, X value) {
		return (X) this.map.put(VALUE, value);
	}
	
	public T getItem(){
		return (T) this.map.get(VALUE);
	}

}
