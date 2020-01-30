package org.sagebionetworks.web.unitclient.widget.provenance.nchart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.XYPoint;

public class LayoutResultTestImpl implements LayoutResult {
	Map<String, List<XYPoint>> nodeToPoints;

	public LayoutResultTestImpl() {
		nodeToPoints = new HashMap<String, List<XYPoint>>();
	}

	public LayoutResultTestImpl(Map<String, List<XYPoint>> nodeToPoints) {
		super();
		this.nodeToPoints = nodeToPoints;
	}

	@Override
	public List<XYPoint> getPointsForId(String provGraphNodeId) {
		return nodeToPoints.get(provGraphNodeId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeToPoints == null) ? 0 : nodeToPoints.hashCode());
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
		LayoutResultTestImpl other = (LayoutResultTestImpl) obj;
		if (nodeToPoints == null) {
			if (other.nodeToPoints != null)
				return false;
		} else if (!nodeToPoints.equals(other.nodeToPoints))
			return false;
		return true;
	}

}
