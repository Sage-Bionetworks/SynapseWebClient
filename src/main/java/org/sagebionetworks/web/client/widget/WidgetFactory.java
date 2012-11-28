package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;

public interface WidgetFactory {

	public ProvenanceWidget createProvenanceWidget();
	
}
