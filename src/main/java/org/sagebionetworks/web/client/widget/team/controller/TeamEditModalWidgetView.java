package org.sagebionetworks.web.client.widget.team.controller;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.team.controller.TeamEditModalWidgetView.Presenter;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TeamEditModalWidgetView {

	public interface Presenter {
		public Widget asWidget();

		void setRefreshCallback(Callback refreshCallback);

		void setTeam(Team team);

		void setVisible(boolean isVisible);

		void onConfirm(String newName, String newDescription,
				boolean canPublicJoin);

		void clear();
	}

	public Widget asWidget();

	public void setAlertWidget(Widget asWidget);

	void setPresenter(Presenter presenter);

	String getName();

	String getDescription();

	boolean getPublicJoin();

	void setTeam(Team team);

	void setUploadWidget(Widget uploader);

	void setUploadedFileName(String fileName);

	void setLoading(boolean isLoading);

	void setVisible(boolean isVisible);

	void setImageURL(String fileHandleId);

}
