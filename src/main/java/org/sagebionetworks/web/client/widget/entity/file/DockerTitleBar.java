package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerTitleBar implements BasicTitleBarView.Presenter, SynapseWidgetPresenter {

	private BasicTitleBarView view;
	private AuthenticationController authenticationController;
	private FavoriteWidget favWidget;

	@Inject
	public DockerTitleBar(BasicTitleBarView view, AuthenticationController authenticationController, FavoriteWidget favWidget) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.favWidget = favWidget;

		view.setPresenter(this);
		view.setFavoritesWidget(favWidget.asWidget());
	}

	public void configure(DockerRepository repo) {
		favWidget.configure(repo.getId());
		view.setFavoritesWidgetVisible(authenticationController.isLoggedIn());
		view.setTitle(repo.getRepositoryName());
		view.setIconType(EntityTypeUtils.getIconTypeForEntity(repo));
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
}
