package org.sagebionetworks.web.client.view;

import java.util.List;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import com.google.gwt.user.client.ui.IsWidget;

public interface WikiDiffView extends IsWidget {
	void setPresenter(Presenter presenter);

	void setSynAlert(IsWidget w);

	void showDiff(String markdown1, String markdown2);

	void setVersionHistory(List<V2WikiHistorySnapshot> wikiVersionHistory);

	void clear();

	void setVersion1(String version);

	void setVersion2(String version);

	public interface Presenter {
		void onVersion1Selected(String version);

		void onVersion2Selected(String version);
	}
}
