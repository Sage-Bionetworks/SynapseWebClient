package org.sagebionetworks.web.client.view;

import java.util.List;
import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ACTView extends IsWidget, SynapseView {
	void setPresenter(Presenter presenter);

	void setUserPickerWidget(Widget w);

	void setLoadMoreContainer(Widget w);

	void setStates(List<String> states);

	void setSynAlert(Widget w);

	void setSelectedStateText(String state);

	void setSelectedUserBadge(Widget w);

	void setSelectedUserBadgeVisible(boolean visible);

	public interface Presenter {
		void onClearUserFilter();

		void onClearStateFilter();

		void onStateSelected(String state);
	}
}
