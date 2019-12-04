package org.sagebionetworks.web.client.widget.provenance;

import java.util.List;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.provenance.ExpandGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import com.google.gwt.user.client.ui.IsWidget;

public interface ProvenanceWidgetView extends IsWidget, SynapseView {

	void setPresenter(Presenter presenter);

	void clear();

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void expand(ExpandGraphNode node);

		void findOldVersions();
	}

	void setGraph(ProvGraph graph);

	void setHeight(int height);

	void markOldVersions(List<String> notCurrentNodeIds);

	void setSynAlert(IsWidget w);
}
