package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.user.BigUserBadge;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenMembershipRequestsWidgetViewImpl extends FlowPanel implements
		OpenMembershipRequestsWidgetView {
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private PortalGinInjector ginInjector;
	private FlowPanel mainContainer;
	
	@Inject
	public OpenMembershipRequestsWidgetViewImpl(SageImageBundle sageImageBundle, PortalGinInjector ginInjector) {
		this.sageImageBundle = sageImageBundle;
		this.ginInjector = ginInjector;
		mainContainer = new FlowPanel();
		mainContainer.addStyleName("highlight-box");
		mainContainer.getElement().setAttribute("highlight-box-title", DisplayConstants.PENDING_JOIN_REQUESTS);
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
		FlowPanel singleRow = DisplayUtils.createRowContainerFlowPanel();
		for (int i = 0; i < profiles.size(); i++) {
			FlowPanel lc = new FlowPanel();
			final UserProfile profile = profiles.get(i);
			BigUserBadge renderer = ginInjector.getBigUserBadgeWidget();
			renderer.configure(profile, requestMessages.get(i));
			Widget rendererWidget = renderer.asWidget();
			rendererWidget.addStyleName("margin-top-15 col-xs-8 col-sm-9 col-md-10");
			
			Button joinButton = DisplayUtils.createButton(DisplayConstants.ACCEPT, ButtonType.PRIMARY);
			joinButton.addStyleName("right margin-top-15 margin-right-15 btn-lg");
			joinButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.acceptRequest(profile.getOwnerId());
				}
			});
			FlowPanel buttonContainer = new FlowPanel();
			buttonContainer.addStyleName("col-xs-4 col-sm-3 col-md-2");
			buttonContainer.add(joinButton);
			lc.add(rendererWidget);
			lc.add(buttonContainer);
			
			singleRow.add(lc);
		}
		mainContainer.add(singleRow);
		if (profiles.size() > 0)
			add(mainContainer);
	}
}
