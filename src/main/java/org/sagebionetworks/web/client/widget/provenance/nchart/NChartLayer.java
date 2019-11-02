package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.List;

public interface NChartLayer {

	void setNodes(List<NChartLayerNode> nodes);

	void setDuration(int duration);

}
