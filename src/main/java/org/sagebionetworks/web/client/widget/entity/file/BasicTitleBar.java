package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.clienthelp.ContainerClientsHelp;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BasicTitleBar implements BasicTitleBarView.Presenter, SynapseWidgetPresenter {
	
	private BasicTitleBarView view;
	private AuthenticationController authenticationController;
	private FavoriteWidget favWidget;
	private ContainerClientsHelp containerDownloadHelp;
	@Inject
	public BasicTitleBar(BasicTitleBarView view, AuthenticationController authenticationController, FavoriteWidget favWidget, ContainerClientsHelp containerDownloadHelp) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.containerDownloadHelp = containerDownloadHelp;
		this.favWidget = favWidget;
		
		view.setPresenter(this);
		view.setFavoritesWidget(favWidget.asWidget());
		view.setContainerHelpWidget(containerDownloadHelp.asWidget());
	}	
	
	public void configure(EntityBundle bundle) {
		containerDownloadHelp.configure(bundle.getEntity().getId());
		favWidget.configure(bundle.getEntity().getId());
		view.setFavoritesWidgetVisible(authenticationController.isLoggedIn());
		view.setTitle(bundle.getEntity().getName());
		view.setIconType(EntityTypeUtils.getIconTypeForEntity(bundle.getEntity()));
		view.setContainerHelpWidgetVisible(bundle.getEntity() instanceof Folder);
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
