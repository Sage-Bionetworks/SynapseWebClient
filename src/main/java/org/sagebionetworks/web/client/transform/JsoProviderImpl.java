package org.sagebionetworks.web.client.transform;

import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayer;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayerNode;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;
import org.sagebionetworks.web.client.widget.provenance.nchart.XYPoint;
import com.google.gwt.core.client.JavaScriptObject;

public class JsoProviderImpl implements JsoProvider {

	@Override
	public NChartCharacters newNChartCharacters() {
		return (NChartCharacters) JavaScriptObject.createObject();
	}

	@Override
	public NChartLayer newNChartLayer() {
		return (NChartLayer) JavaScriptObject.createObject();
	}

	@Override
	public NChartLayerNode newNChartLayerNode() {
		return (NChartLayerNode) JavaScriptObject.createObject();
	}

	@Override
	public NChartLayersArray newNChartLayersArray() {
		return (NChartLayersArray) JavaScriptObject.createArray();
	}

	@Override
	public XYPoint newXYPoint() {
		return (XYPoint) JavaScriptObject.createObject();
	}

}
