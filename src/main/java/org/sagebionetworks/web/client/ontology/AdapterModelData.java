package org.sagebionetworks.web.client.ontology;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.schema.adapter.AdapterCollectionUtils;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

import com.extjs.gxt.ui.client.data.ModelData;


/**
 * This is a com.extjs.gxt.ui.client.data.ModelData object that can be initialized from an adapter
 * @author jmhill
 *
 */
public class AdapterModelData implements ModelData, JSONEntity {
	
	Map<String, Object> map;
	
	/**
	 * Create an empty model object
	 */
	public AdapterModelData(){
		map = new HashMap<String, Object>();
	}
	
	/**
	 * Create a new model object from an adapter.
	 * @param adapter
	 * @throws JSONObjectAdapterException
	 */
	public AdapterModelData(JSONObjectAdapter adapter) throws JSONObjectAdapterException{
		if(adapter == null) throw new IllegalArgumentException("Adapter cannot be null");
		// Convert the adapter to a map
		map = AdapterCollectionUtils.readMapFromObject(adapter);
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
		return (X) map.remove(property);
	}

	@Override
	public <X> X set(String property, X value) {
		return (X) map.put(property, value);
	}

	@Override
	public JSONObjectAdapter initializeFromJSONObject(JSONObjectAdapter toInitFrom) throws JSONObjectAdapterException {
		// Populate the map from the adapter;
		map = AdapterCollectionUtils.readMapFromObject(toInitFrom);
		return toInitFrom;
	}

	@Override
	public JSONObjectAdapter writeToJSONObject(JSONObjectAdapter writeTo) throws JSONObjectAdapterException {
		// Write the map to the adapter.
		AdapterCollectionUtils.writeToObject(writeTo, map);
		return writeTo;
	}

	@Override
	public String getJSONSchema() {
		throw new UnsupportedOperationException("Not supported for this class");
	}

}
