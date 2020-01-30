package org.sagebionetworks.web.client.view;

import java.util.ArrayList;
import java.util.Date;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroup;
import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;

public interface ACTAccessApprovalsView extends IsWidget, SynapseView {
	void setPresenter(Presenter presenter);

	void setLoadMoreContainer(IsWidget w);

	void setSynAlert(IsWidget w);

	void setShowHideButton(IsWidget button);

	void setAccessRequirementWidget(IsWidget w);

	void setAccessRequirementUIVisible(boolean visible);

	void setUserPickerWidget(IsWidget w);

	void setSelectedUserBadge(IsWidget w);

	void setSelectedUserBadgeVisible(boolean visible);

	void setExpiresBeforeDate(Date date);

	void setClearAccessRequirementFilterButtonVisible(boolean visible);

	void resetExportButton();

	public interface Presenter {
		void onClearUserFilter();

		void onClearExpireBeforeFilter();

		void onClearAccessRequirementFilter();

		void onExpiresBeforeDateSelected(Date selectedDate);

		ArrayList<AccessorGroup> getExportData();
	}
}
