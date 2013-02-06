package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.List;

public abstract class LayerNode {	
	
	private List<String> subnodes;
	private String event;

	public List<String> getSubnodes() {
		return subnodes;
	}
	public void setSubnodes(List<String> subnodes) {
		this.subnodes = subnodes;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}		
		
}
