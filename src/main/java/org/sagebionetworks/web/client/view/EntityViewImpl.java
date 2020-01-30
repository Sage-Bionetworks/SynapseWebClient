package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Heading;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityViewImpl implements EntityView {

	public interface EntityViewImplUiBinder extends UiBinder<Widget, EntityViewImpl> {
	}

	@UiField
	SimplePanel entityPageTopPanel;
	@UiField
	SimplePanel openInvitesPanel;
	@UiField
	SimplePanel synAlertContainer;
	@UiField
	LoadingSpinner loadingUI;
	@UiField
	Heading accessDependentMessage;

	private Widget widget;

	@Inject
	public EntityViewImpl(EntityViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		Window.scrollTo(0, 0); // scroll user to top of page
		// TODO : need to dynamically set the header widget
		// headerWidget.setMenuItemActive(MenuItems.PROJECTS);
	}

	@Override
	public void setEntityPageTopWidget(IsWidget entityPageTopWidget) {
		entityPageTopPanel.clear();
		entityPageTopPanel.setWidget(entityPageTopWidget);
	}

	@Override
	public void setOpenTeamInvitesWidget(IsWidget openTeamInvitesWidget) {
		openInvitesPanel.clear();
		openInvitesPanel.setWidget(openTeamInvitesWidget);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void setLoadingVisible(boolean isVisible) {
		loadingUI.setVisible(isVisible);
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void setSynAlertWidget(IsWidget synAlert) {
		synAlert.asWidget().addStyleName("min-height-400 margin-top-60");
		synAlertContainer.setWidget(synAlert);
	}


	@Override
	public void clear() {
		openInvitesPanel.setVisible(false);
		accessDependentMessage.setVisible(false);
		loadingUI.setVisible(false);
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void setEntityPageTopVisible(boolean isVisible) {
		entityPageTopPanel.setVisible(isVisible);
	}

	@Override
	public void setAccessDependentMessageVisible(boolean isVisible) {
		accessDependentMessage.setVisible(isVisible);
	}

	@Override
	public void setOpenTeamInvitesVisible(boolean isVisible) {
		openInvitesPanel.setVisible(isVisible);
	}
}
