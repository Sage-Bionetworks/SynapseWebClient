package org.sagebionetworks.web.client.widget.entity.browse;

import java.io.Serializable;
import com.extjs.gxt.ui.client.data.BaseTreeModel;

public class EntityTreeModel extends BaseTreeModel implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final String KEY_ID = "entityid";
	private static final String KEY_NAME = "name";
	private static final String KEY_TYPE = "type";
	private static final String RAND_ID = "id";

	public EntityTreeModel(String id) {
		this(id,null,null);
		
	}

	public EntityTreeModel(String id, String name) {
		this(id,name,null);
	}

	public EntityTreeModel(String id, String name, String type) {
		set(KEY_ID, id);
		set(KEY_NAME, name);
		set(KEY_TYPE, type);
		
		// make each unique
		set(RAND_ID, "node_" + (int)(Math.random()*1000000));
	}

	public void setChildren(BaseTreeModel[] children) {
		this.removeAll();
		for(BaseTreeModel child : children) {
			add(child);
		}
	}
	
	public String getId() {
		return (String) get(KEY_ID);
	}

	public String getName() {
		return (String) get(KEY_NAME);
	}
	
	public String getType() {
		return (String) get(KEY_TYPE);
	}

	public String toString() {
		return getName();
	}
	
	public String getKey() {
		return (String) get(RAND_ID);
	}

}
