package org.sagebionetworks.web.client.widget.footer;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Footer implements FooterView.Presenter, IsWidget {

	public static final String UNKNOWN = "unknown";
	private FooterView view;
	GlobalApplicationState globalAppState;
	AuthenticationController authController;
	
	@Inject
	public Footer(FooterView view, 
			GlobalApplicationState globalAppState,
			AuthenticationController authController
			) {
		this.view = view;
		this.globalAppState = globalAppState;
		this.authController = authController;
		view.setPresenter(this);
		init();
	}

	public void init() {
		globalAppState.checkVersionCompatibility(new AsyncCallback<VersionState>() {
			@Override
			public void onSuccess(VersionState state) {
				if (state == null || state.getVersion() == null) {
					onFailure(null);
					return;
				}
				String versions = state.getVersion();
				String[] vals = versions.split(",");
				if(vals.length == 2) {
					view.setVersion(vals[0],vals[1]);
				} else {
					onFailure(null);
					return;
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.setVersion(UNKNOWN, UNKNOWN);
			}
		});
		view.refresh();
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void onReportAbuseClicked() {
		// request access via Jira
		UserProfile userProfile = authController.getCurrentUserProfile();
		if (userProfile==null) throw new IllegalStateException("UserProfile is null");
		String primaryEmail = DisplayUtils.getPrimaryEmail(userProfile);
		view.showJiraIssueCollector(userProfile.getOwnerId(), 
				DisplayUtils.getDisplayName(userProfile), 
				primaryEmail);
	}
}
