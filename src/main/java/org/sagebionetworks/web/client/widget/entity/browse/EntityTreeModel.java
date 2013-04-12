package org.sagebionetworks.web.client.widget.entity.browse;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

public class EntityTreeModel extends BaseTreeModel implements Serializable {
	private static final long serialVersionUID = 1L;

	private static int counter = 0;
	
	public static final String KEY_ID = "entityid";
	public static final String KEY_NAME = "name";
	public static final String KEY_TYPE = "type";
	public static final String KEY_LINK = "link";
	public static final String RAND_ID = "id";

	public EntityTreeModel(String id, String name, String link, String type) {
		set(KEY_ID, id);
		set(KEY_NAME, name);
		set(KEY_LINK, link);
		set(KEY_TYPE, type);
		
		// make each unique
		set(RAND_ID, "node_" + counter++);
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
	
	public String getLink() {
		return (String) get(KEY_LINK);
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
