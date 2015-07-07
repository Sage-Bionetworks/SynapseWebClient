package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityViewImpl implements EntityView {
	
	private SageImageBundle sageImageBundle;
	private Widget loadingPanel;

	public interface EntityViewImplUiBinder extends UiBinder<Widget, EntityViewImpl> {}

	@UiField
	SimplePanel headerPanel;
	@UiField
	SimplePanel footerPanel;
	@UiField
	SimplePanel entityPageTopPanel;
	@UiField
	SimplePanel openInvitesPanel;
	@UiField
	SimplePanel synAlertContainer;
	@UiField
	Image entityBackgroundImage;
	
	private Presenter presenter;
	private Widget widget;
	
	@Inject
	public EntityViewImpl(EntityViewImplUiBinder binder) {		
		widget = binder.createAndBindUi(this);
		// TODO : need to dynamically set the header widget
		//headerWidget.setMenuItemActive(MenuItems.PROJECTS);
	}
	
	@Override
	public void setHeaderWidget(IsWidget headerWidget) {
		headerPanel.setWidget(headerWidget);
	}
	
	@Override
	public void setFooterWidget(IsWidget footerWidget) {
		footerPanel.setWidget(footerWidget);
	}
	
	@Override
	public void setEntityPageTopWidget(IsWidget entityPageTopWidget) {
		entityPageTopPanel.setWidget(entityPageTopWidget);
	}
	
	@Override
	public void setOpenTeamInvitesWidget(IsWidget openTeamInvitesWidget) {
		openInvitesPanel.setWidget(openTeamInvitesWidget);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
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
	public void showLoading() {
		if (loadingPanel == null)
			loadingPanel = DisplayUtils.createFullWidthLoadingPanel(sageImageBundle);
		entityPageTopPanel.setWidget(loadingPanel);
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	@Override
	public void setSynAlertWidget(Widget synAlert) {
		synAlertContainer.setWidget(synAlert);
	}
	

	@Override
	public void clear() {
		
	}

	
	@Override
	public void setBackgroundImageVisible(boolean isVisible) {
		entityBackgroundImage.setVisible(isVisible);
	}

	@Override
	public void setBackgroundImageUrl(String url) {
		entityBackgroundImage.setUrl(url);
	}
}
