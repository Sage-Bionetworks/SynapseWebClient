package org.sagebionetworks.web.client.view;

import java.util.List;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface WikiDiffView extends IsWidget, SynapseView {
	void setPresenter(Presenter presenter);
	void setSynAlert(Widget w);
	void showDiff(String markdown1, String markdown2);
	void setVersionHistory(List<V2WikiHistorySnapshot> wikiVersionHistory);

	public interface Presenter {
		void onVersion1Selected(String version);
		void onVersion2Selected(String version);
	}
}
