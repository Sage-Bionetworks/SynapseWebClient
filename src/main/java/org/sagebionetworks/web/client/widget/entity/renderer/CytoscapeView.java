package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface CytoscapeView extends IsWidget {

	interface Presenter {
	}

	/**
	 * Set the Cytoscape JS json, and (optionally) style json
	 * 
	 * @param cyJs
	 * @param styleJson
	 */
	void configure(String cyJs, String styleJson, String height);

	void setPresenter(Presenter presenter);

	void setSynAlert(Widget w);

	void setLoading(boolean loading);

	void setGraphVisible(boolean isVisible);
}
