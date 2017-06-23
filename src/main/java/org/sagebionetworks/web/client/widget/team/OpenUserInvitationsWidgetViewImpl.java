package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenUserInvitationsWidgetViewImpl implements OpenUserInvitationsWidgetView {
	public interface Binder extends UiBinder<Widget, OpenUserInvitationsWidgetViewImpl> {}

	@UiField
	Div mainContainer;
	@UiField
	Div synAlertContainer;
	Widget widget;
	
	private Presenter presenter;
	private PortalGinInjector ginInjector;
	private UnorderedListPanel ulPanel;
	
	@Inject
	public OpenUserInvitationsWidgetViewImpl(Binder binder, PortalGinInjector ginInjector) {
		widget = binder.createAndBindUi(this);
		this.ginInjector = ginInjector;
		mainContainer.addStyleName("highlight-box");
		mainContainer.getElement().setAttribute("highlight-box-title", DisplayConstants.PENDING_INVITATIONS);
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void configure(List<UserProfile> profiles, List<MembershipInvtnSubmission> invitations) {
		clear();
		FlowPanel singleRow = DisplayUtils.createRowContainerFlowPanel();
		mainContainer.setVisible(false);
		for (int i = 0; i < profiles.size(); i++) {
			FlowPanel rowPanel = new FlowPanel();
			rowPanel.addStyleName("col-md-12");
			FlowPanel left = new FlowPanel();
			left.addStyleName("col-xs-9 col-sm-10 col-md-11");
			FlowPanel right = new FlowPanel();
			right.addStyleName("col-xs-3 col-sm-2 col-md-1");
			rowPanel.add(left);
			rowPanel.add(right);
			
			final UserProfile profile = profiles.get(i);
			UserBadge renderer = ginInjector.getUserBadgeWidget();
			MembershipInvtnSubmission invite = invitations.get(i);
			final String inviteId = invite.getId();
			String inviteMessage = invite.getMessage() != null ? invite.getMessage() : "";
			renderer.configure(profile, inviteMessage);
			renderer.setSize(BadgeSize.LARGE);
			Widget rendererWidget = renderer.asWidget();
			rendererWidget.addStyleName("margin-top-15");
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
			
			singleRow.add(rowPanel);
			mainContainer.setVisible(true);
		}
		mainContainer.add(singleRow);
		ulPanel = new UnorderedListPanel();
		if (profiles.size() > 0) {
			ulPanel.addStyleName("pager");
			Anchor moreLink = new Anchor("More");
			moreLink.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.getNextBatch();
				}
			});
			ulPanel.add(moreLink);
			ulPanel.setVisible(false);
			mainContainer.add(ulPanel);
		}
	}
	
	@Override
	public void setMoreResultsVisible(boolean isVisible) {
		if (ulPanel != null)
			ulPanel.setVisible(isVisible);
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	@Override
	public void clear() {
		mainContainer.clear();
	}
}
