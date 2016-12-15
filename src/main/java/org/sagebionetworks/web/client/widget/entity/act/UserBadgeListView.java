package org.sagebionetworks.web.client.widget.entity.act;

import java.util.List;

import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface UserBadgeListView extends IsWidget {
	
	public interface Presenter{
		List<String> getUserIds();
		void deleteSelected();
		void selectNone();
		void selectAll();
	}
	
	void setPresenter(Presenter presenter);
	void setToolbarVisible(boolean visible);
	void addUserBadge(Widget widget);
	void clearUserBadges();
	void setCanDelete(boolean canDelete);
}