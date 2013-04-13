package org.sagebionetworks.web.client.widget.provenance;

import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;

public interface ProvenanceJSNIUtils {

	public LayoutResult nChartlayout(NChartLayersArray layers, NChartCharacters characters);
}
