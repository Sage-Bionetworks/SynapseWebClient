package org.sagebionetworks.web.client.widget.entity.act;

import org.sagebionetworks.web.client.widget.CheckBoxState;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface UserBadgeListView extends IsWidget {

	public interface Presenter {
		void deleteSelected();

		void selectNone();

		void selectAll();
	}

	void setPresenter(Presenter presenter);

	void setToolbarVisible(boolean visible);

	void addUserBadge(Widget widget);

	void clearUserBadges();

	void setCanDelete(boolean canDelete);

	void setSelectionState(CheckBoxState selectionState);
}
