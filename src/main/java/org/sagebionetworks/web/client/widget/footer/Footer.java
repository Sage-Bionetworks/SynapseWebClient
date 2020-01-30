package org.sagebionetworks.web.client.widget.footer;

import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_COLLECTOR_URL;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_DESCRIPTION_PART_1;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_PRIORITY;
import static org.sagebionetworks.web.shared.WebConstants.REVIEW_ABUSIVE_CONTENT_REQUEST_COMPONENT_ID;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Footer implements FooterView.Presenter, IsWidget {

	public static final String UNKNOWN = "unknown";
	private FooterView view;
	GlobalApplicationState globalAppState;
	AuthenticationController authController;
	GWTWrapper gwt;
	SynapseJSNIUtils jsniUtils;

	@Inject
	public Footer(FooterView view, GlobalApplicationState globalAppState, AuthenticationController authController, GWTWrapper gwt, SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.globalAppState = globalAppState;
		this.authController = authController;
		this.gwt = gwt;
		this.jsniUtils = jsniUtils;
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
				if (vals.length == 2) {
					view.setVersion(vals[0], vals[1]);
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
		// report abuse via Jira issue collector
		String userId = WebConstants.ANONYMOUS, email = WebConstants.ANONYMOUS, displayName = WebConstants.ANONYMOUS;
		UserProfile userProfile = authController.getCurrentUserProfile();
		if (userProfile != null) {
			userId = userProfile.getOwnerId();
			displayName = DisplayUtils.getDisplayName(userProfile);
			email = DisplayUtils.getPrimaryEmail(userProfile);
		}

		jsniUtils.showJiraIssueCollector("", // summary
				FLAG_ISSUE_DESCRIPTION_PART_1 + gwt.getCurrentURL() + WebConstants.FLAG_ISSUE_DESCRIPTION_PART_2, // description
				FLAG_ISSUE_COLLECTOR_URL, userId, displayName, email, null, // Synapse data object ID
				REVIEW_ABUSIVE_CONTENT_REQUEST_COMPONENT_ID, null, // Access requirement ID
				FLAG_ISSUE_PRIORITY);
	}
}
