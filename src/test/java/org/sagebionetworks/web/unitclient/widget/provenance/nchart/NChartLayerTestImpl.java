package org.sagebionetworks.web.unitclient.widget.provenance.nchart;

import java.util.List;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayer;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayerNode;

public class NChartLayerTestImpl implements NChartLayer {
	List<NChartLayerNode> nodes;
	int duration;

	@Override
	public void setNodes(List<NChartLayerNode> nodes) {
		this.nodes = nodes;
	}

	@Override
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public List<NChartLayerNode> getNodes() {
		return nodes;
	}

	public int getDuration() {
		return duration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + duration;
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
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
		NChartLayerTestImpl other = (NChartLayerTestImpl) obj;
		if (duration != other.duration)
			return false;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}


}
