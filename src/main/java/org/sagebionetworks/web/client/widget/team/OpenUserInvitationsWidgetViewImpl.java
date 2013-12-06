package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.user.BigUserBadge;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenUserInvitationsWidgetViewImpl extends FlowPanel implements
	OpenUserInvitationsWidgetView {
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private PortalGinInjector ginInjector;
	private FlowPanel mainContainer;
	
	@Inject
	public OpenUserInvitationsWidgetViewImpl(SageImageBundle sageImageBundle, PortalGinInjector ginInjector) {
		this.sageImageBundle = sageImageBundle;
		this.ginInjector = ginInjector;
		mainContainer = new FlowPanel();
		mainContainer.addStyleName("highlight-box");
		mainContainer.setTitle(DisplayConstants.PENDING_INVITATIONS);
	}
	
	
	@Override
	public void showLoading() {
		clear();
		add(DisplayUtils.getLoadingWidget(sageImageBundle));
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void configure(List<UserProfile> profiles, List<String> requestMessages) {
		clear();
		mainContainer.clear();
		for (int i = 0; i < profiles.size(); i++) {
			LayoutContainer lc = DisplayUtils.createRowContainer();
			final UserProfile profile = profiles.get(i);
			BigUserBadge renderer = ginInjector.getBigUserBadgeWidget();
			renderer.configure(profile, requestMessages.get(i));
			Widget rendererWidget = renderer.asWidget();
			rendererWidget.addStyleName("margin-top-15 col-md-9");
			
			lc.add(rendererWidget);
			
			mainContainer.add(lc);
		}
		if (profiles.size() > 0)
			add(mainContainer);
	}
}
