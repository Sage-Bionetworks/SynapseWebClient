package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.List;

public class Layer {

	private static final int DEFAULT_DURATION = 10;
	
	List<LayerNode> nodes;
	int duration = DEFAULT_DURATION;

	public Layer(List<LayerNode> nodes) {
		super();
		this.nodes = nodes;
	}
	
	public List<LayerNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<LayerNode> nodes) {
		this.nodes = nodes;
	}
	
	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}
	
}
