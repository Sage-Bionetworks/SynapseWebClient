package org.sagebionetworks.web.client.widget.table.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.shared.EntityBundleTransport;

import com.google.inject.Inject;

/**
 * Utilities for marshaling TableEntity model objects both to and from JSON and other useful utilities.
 * 
 * @author John
 *
 */
public class TableModelUtils {

	private AdapterFactory adapterFactory;

	@Inject
	public TableModelUtils(AdapterFactory adapterFactory) {
		super();
		this.adapterFactory = adapterFactory;
	}
	
	/**
	 * Write the JSON of the passed object
	 * @param model
	 * @return
	 * @throws JSONObjectAdapterException
	 */
	public String toJSON(JSONEntity model) throws JSONObjectAdapterException{
		return model.writeToJSONObject(adapterFactory.createNew()).toJSONString();
	}
	
	/**
	 * Create a list of JSON strings from a list of model objects.
	 * @param models
	 * @return
	 * @throws JSONObjectAdapterException
	 */
	public List<String> toJSONList(List<ColumnModel> models) throws JSONObjectAdapterException{
		List<String> resutls = new ArrayList<String>(models.size());
		for(JSONEntity model: models){
			String json = toJSON(model);
			resutls.add(json);
		}
		return resutls;
	}
	
	/**
	 * Create a ColumnModed form JSON.
	 * @param json
	 * @return
	 * @throws JSONObjectAdapterException
	 */
	public ColumnModel columnModelFromJSON(String json) throws JSONObjectAdapterException{
		return new ColumnModel(adapterFactory.createNew(json));
	}
	
	/**
	 * Create a list of ColumnModels from a list of JOSN Strings.
	 * 
	 * @param jsonList
	 * @return
	 * @throws JSONObjectAdapterException 
	 */
	public List<ColumnModel> columnModelFromJSON(List<String> jsonList) throws JSONObjectAdapterException{
		List<ColumnModel> results = new ArrayList<ColumnModel>(jsonList.size());
		for(String json: jsonList){
			ColumnModel cm = columnModelFromJSON(json);
			results.add(cm);
		}
		return results;
	}
	
	/**
	 * Create table bundle from JSON.
	 * 
	 * @param json
	 * @return
	 * @throws JSONObjectAdapterException 
	 */
	public TableBundle tableBundleFromJSON(String json) throws JSONObjectAdapterException{
		return new TableBundle(adapterFactory.createNew(json));
	}
	
	/**
	 * Create a TableEntity form the JSON
	 * @param json
	 * @return
	 * @throws JSONObjectAdapterException
	 */
	public TableEntity tableEntityFromJSON(String json) throws JSONObjectAdapterException{
		return new TableEntity(adapterFactory.createNew(json));
	}
	
	/**
	 * Convert a transport to a bundle.
	 * @param transport
	 * @return
	 * @throws JSONObjectAdapterException
	 */
	public EntityBundle bundleFromTransport(EntityBundleTransport transport) throws JSONObjectAdapterException{
		TableEntity entity = tableEntityFromJSON(transport.getEntityJson());
		TableBundle tableBundle = tableBundleFromJSON(transport.getEntityJson());
		return new EntityBundle(entity, null, null, null, null, null, null, tableBundle);
	}
	
	
	
	
}
