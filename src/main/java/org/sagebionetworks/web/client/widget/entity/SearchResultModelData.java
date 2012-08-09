package org.sagebionetworks.web.client.widget.entity;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class SearchResultModelData extends BaseModelData {
	
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String NODE_TYPE = "node_type";
	
	public String getId() {
		return this.get(ID);
	}
	public void setId(String id) {
		this.set(ID, id);
	}

	public String getName() {
		return this.get(NAME);
	}
	public void setName(String name) {
		this.set(NAME, name);
	}

	public String getNodeType() {
		return this.get(NODE_TYPE);
	}
	public void setNodeType(String nodeType) {
		this.set(NODE_TYPE, nodeType);
	}

}
