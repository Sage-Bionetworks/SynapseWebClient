package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BasicTitleBar implements BasicTitleBarView.Presenter, SynapseWidgetPresenter {
	
	private BasicTitleBarView view;
	private AuthenticationController authenticationController;
	private FavoriteWidget favWidget;
	@Inject
	public BasicTitleBar(BasicTitleBarView view, AuthenticationController authenticationController, FavoriteWidget favWidget) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.favWidget = favWidget;
		
		view.setPresenter(this);
		view.setFavoritesWidget(favWidget.asWidget());
	}	
	
	public void configure(EntityBundle bundle) {		
		favWidget.configure(bundle.getEntity().getId());
		view.setFavoritesWidgetVisible(authenticationController.isLoggedIn());
		view.setTitle(bundle.getEntity().getName());
		view.setIconType(DisplayUtils.getIconTypeForEntity(bundle.getEntity()));
	}
	
	public void clearState() {
		view.clear();
		
	}

	/**
	 * Does nothing. Use asWidget(Entity)
	 */
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
    
	/*
	 * Private Methods
	 */
}
