package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.widget.user.BigUserBadge;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
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
	public void configure(List<UserProfile> profiles, List<MembershipInvtnSubmission> invitations) {
		clear();
		mainContainer.clear();
		for (int i = 0; i < profiles.size(); i++) {
			LayoutContainer rowPanel = DisplayUtils.createRowContainer();
			rowPanel.addStyleName("col-md-12");
			LayoutContainer left = new LayoutContainer();
			left.addStyleName("col-md-11");
			LayoutContainer right = new LayoutContainer();
			right.addStyleName("col-md-1");
			rowPanel.add(left);
			rowPanel.add(right);
			
			final UserProfile profile = profiles.get(i);
			BigUserBadge renderer = ginInjector.getBigUserBadgeWidget();
			MembershipInvtnSubmission invite = invitations.get(i);
			final String inviteId = invite.getId();
			String inviteMessage = invite.getMessage() != null ? invite.getMessage() : "";
			renderer.configure(profile, inviteMessage);
			Widget rendererWidget = renderer.asWidget();
			rendererWidget.addStyleName("margin-top-15 col-md-9");
			left.add(rendererWidget);
			
			//Remove invitation button
			Button leaveButton = DisplayUtils.createButton("Remove", ButtonType.DANGER);
			leaveButton.addStyleName("pull-right margin-left-5");
			leaveButton.addClickHandler(new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					presenter.removeInvitation(inviteId);
				}
			});
			right.add(leaveButton);
			
			mainContainer.add(rowPanel);
		}
		if (profiles.size() > 0)
			add(mainContainer);
	}
	
}
