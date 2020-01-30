package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BasicTitleBar implements SynapseWidgetPresenter {

	private BasicTitleBarView view;
	private AuthenticationController authenticationController;
	private FavoriteWidget favWidget;

	@Inject
	public BasicTitleBar(BasicTitleBarView view, AuthenticationController authenticationController, FavoriteWidget favWidget) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.favWidget = favWidget;
		view.setFavoritesWidget(favWidget.asWidget());
	}

	public void configure(EntityBundle bundle) {
		favWidget.configure(bundle.getEntity().getId());
		view.setFavoritesWidgetVisible(authenticationController.isLoggedIn());
		view.setTitle(bundle.getEntity().getName());
		view.setIconType(EntityTypeUtils.getIconTypeForEntity(bundle.getEntity()));
	}

	public void configure(EntityHeader entityHeader) {
		favWidget.configure(entityHeader.getId());
		view.setFavoritesWidgetVisible(authenticationController.isLoggedIn());
		view.setTitle(entityHeader.getName());
		view.setIconType(EntityTypeUtils.getIconTypeForEntityClassName(entityHeader.getType()));
	}

	public void clearState() {
		view.clear();
	}
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void setActionMenu(IsWidget w) {
		view.setActionMenu(w);
	}
}
