package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.List;

public interface LayoutResult {

	public List<XYPoint> getPointsForId(String provGraphNodeId);

}
