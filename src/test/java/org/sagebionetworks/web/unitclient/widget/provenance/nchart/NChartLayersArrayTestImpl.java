package org.sagebionetworks.web.unitclient.widget.provenance.nchart;

import java.util.List;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayer;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;

public class NChartLayersArrayTestImpl implements NChartLayersArray {
	List<NChartLayer> layers;

	@Override
	public void setLayers(List<NChartLayer> layers) {
		this.layers = layers;
	}

	public List<NChartLayer> getLayers() {
		return layers;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((layers == null) ? 0 : layers.hashCode());
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
		NChartLayersArrayTestImpl other = (NChartLayersArrayTestImpl) obj;
		if (layers == null) {
			if (other.layers != null)
				return false;
		} else if (!layers.equals(other.layers))
			return false;
		return true;
	}

}
