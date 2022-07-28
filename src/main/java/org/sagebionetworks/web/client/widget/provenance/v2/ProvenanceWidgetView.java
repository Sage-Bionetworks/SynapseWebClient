package org.sagebionetworks.web.client.widget.provenance.v2;

import java.util.List;

import org.sagebionetworks.repo.model.Reference;

import com.google.gwt.user.client.ui.IsWidget;

public interface ProvenanceWidgetView extends IsWidget {
	void configure(List<Reference> refs, String containerHeight);
}
