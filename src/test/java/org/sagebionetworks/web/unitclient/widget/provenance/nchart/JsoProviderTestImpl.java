package org.sagebionetworks.web.unitclient.widget.provenance.nchart;

import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayer;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayerNode;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;
import org.sagebionetworks.web.client.widget.provenance.nchart.XYPoint;

public class JsoProviderTestImpl implements JsoProvider {

	@Override
	public NChartCharacters newNChartCharacters() {
		return new NChartCharactersTestImpl();
	}

	@Override
	public NChartLayer newNChartLayer() {
		return new NChartLayerTestImpl();
	}

	@Override
	public NChartLayerNode newNChartLayerNode() {
		return new NChartLayerNodeTestImpl();
	}

	@Override
	public NChartLayersArray newNChartLayersArray() {
		return new NChartLayersArrayTestImpl();
	}

	@Override
	public XYPoint newXYPoint() {
		return new XYPointTestImpl(0, 0);
	}

}
