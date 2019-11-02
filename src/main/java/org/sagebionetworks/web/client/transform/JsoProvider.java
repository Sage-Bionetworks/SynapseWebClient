package org.sagebionetworks.web.client.transform;

import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayer;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayerNode;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;
import org.sagebionetworks.web.client.widget.provenance.nchart.XYPoint;

/**
 * Provider interface for objects that have implementations that subclass JavaScriptObject.
 * Generally used for JSNI Overlay types.
 * 
 * Class needed as Gin doesn't properly instantiate overlay types
 * 
 * @author dburdick
 *
 */
public interface JsoProvider {
	NChartCharacters newNChartCharacters();

	NChartLayer newNChartLayer();

	NChartLayerNode newNChartLayerNode();

	NChartLayersArray newNChartLayersArray();

	XYPoint newXYPoint();

}
