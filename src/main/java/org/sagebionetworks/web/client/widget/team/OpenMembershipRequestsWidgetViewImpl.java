package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenMembershipRequestsWidgetViewImpl implements OpenMembershipRequestsWidgetView {

	public interface Binder extends UiBinder<Widget, OpenMembershipRequestsWidgetViewImpl> {}

	@UiField
	Div mainContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	Image loadingUI;
	Widget widget;
	private Presenter presenter;
	private PortalGinInjector ginInjector;
	
	@Inject
	public OpenMembershipRequestsWidgetViewImpl(Binder binder, PortalGinInjector ginInjector) {
		widget = binder.createAndBindUi(this);
		this.ginInjector = ginInjector;
		mainContainer.addStyleName("highlight-box");
		mainContainer.getElement().setAttribute("highlight-box-title", DisplayConstants.PENDING_JOIN_REQUESTS);
	}
	
	@Override
	public void setLoadingVisible(boolean visible) {
		loadingUI.setVisible(visible);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void clear() {
		mainContainer.clear();
	}
	
	@Override
	public void configure(List<UserProfile> profiles, List<String> requestMessages) {
		clear();
		mainContainer.setVisible(false);
		FlowPanel singleRow = DisplayUtils.createRowContainerFlowPanel();
		for (int i = 0; i < profiles.size(); i++) {
			FlowPanel lc = new FlowPanel();
			final UserProfile profile = profiles.get(i);
			Column userBadgeColumn = new Column(ColumnSize.XS_8, ColumnSize.SM_9, ColumnSize.MD_10);
			userBadgeColumn.addStyleName("margin-top-15");
			UserBadge renderer = ginInjector.getUserBadgeWidget();
			renderer.configure(profile, requestMessages.get(i));
			renderer.setSize(BadgeSize.LARGE);
			userBadgeColumn.add(renderer.asWidget());
			
			Button joinButton = DisplayUtils.createButton(DisplayConstants.ACCEPT, ButtonType.PRIMARY);
			joinButton.addStyleName("right margin-top-15 margin-right-15 btn-lg");
			joinButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.acceptRequest(profile.getOwnerId());
				}
			});
			Column buttonContainer = new Column(ColumnSize.XS_4, ColumnSize.SM_3, ColumnSize.MD_2);
			buttonContainer.add(joinButton);
			lc.add(userBadgeColumn);
			lc.add(buttonContainer);
			
			singleRow.add(lc);
			mainContainer.setVisible(true);
		}
		mainContainer.add(singleRow);
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
}
