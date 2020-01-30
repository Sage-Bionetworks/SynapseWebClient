package org.sagebionetworks.web.unitclient.widget.provenance.nchart;

import java.util.List;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayerNode;

public class NChartLayerNodeTestImpl implements NChartLayerNode {
	List<String> subnodes;
	String event;

	@Override
	public void setSubnodes(List<String> subnodes) {
		this.subnodes = subnodes;
	}

	@Override
	public void setEvent(String event) {
		this.event = event;
	}

	public List<String> getSubnodes() {
		return subnodes;
	}

	public String getEvent() {
		return event;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((event == null) ? 0 : event.hashCode());
		result = prime * result + ((subnodes == null) ? 0 : subnodes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NChartLayerNodeTestImpl other = (NChartLayerNodeTestImpl) obj;
		if (event == null) {
			if (other.event != null)
				return false;
		} else if (!event.equals(other.event))
			return false;
		if (subnodes == null) {
			if (other.subnodes != null)
				return false;
		} else if (!subnodes.equals(other.subnodes))
			return false;
		return true;
	}

}
