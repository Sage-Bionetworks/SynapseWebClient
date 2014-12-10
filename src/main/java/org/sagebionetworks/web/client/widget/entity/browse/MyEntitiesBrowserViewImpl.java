package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MyEntitiesBrowserViewImpl implements MyEntitiesBrowserView {

	public interface MyEntitiesBrowserViewImplUiBinder extends UiBinder<Widget, MyEntitiesBrowserViewImpl> {}
	
	private Presenter presenter;
	private EntityTreeBrowser myTreeBrowser;
	private EntityTreeBrowser favoritesTreeBrowser;
	private EntitySelectedHandler mySelectedHandler;
	private EntitySelectedHandler favoritesSelectedHandler;
	@UiField
	SimplePanel myProjectsContainer;
	@UiField
	SimplePanel myFavoritesContainer;
	
	private Widget widget;
	@Inject
	public MyEntitiesBrowserViewImpl(MyEntitiesBrowserViewImplUiBinder binder, 
			PortalGinInjector ginInjector) {
		widget = binder.createAndBindUi(this);
		this.myTreeBrowser = ginInjector.getEntityTreeBrowser();
		this.favoritesTreeBrowser = ginInjector.getEntityTreeBrowser();
		myProjectsContainer.setWidget(myTreeBrowser.asWidget());
		myFavoritesContainer.setWidget(favoritesTreeBrowser.asWidget());
	}
	
	@Override
	public void setUpdatableEntities(List<EntityHeader> rootEntities) {
		myTreeBrowser.configure(rootEntities, true);		
	}
	
	@Override
	public void setFavoriteEntities(List<EntityHeader> favoriteEntities) {
		favoritesTreeBrowser.configure(favoriteEntities, true);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {		
		// create a new handler for this presenter
		if(mySelectedHandler != null) {
			myTreeBrowser.removeEntitySelectedHandler(mySelectedHandler);
		}
		if(favoritesSelectedHandler != null) {
			favoritesTreeBrowser.removeEntitySelectedHandler(favoritesSelectedHandler);
		}
		createSelectedHandlers();
		this.presenter = presenter;
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
	}

	@Override
	public EntityTreeBrowser getEntityTreeBrowser() {
		return myTreeBrowser;
	}

	@Override
	public EntityTreeBrowser getFavoritesTreeBrowser() {
		return favoritesTreeBrowser;
	}

	/*
	 * Private Methods
	 */
	private void createSelectedHandlers() {
		mySelectedHandler = new EntitySelectedHandler() {			
			@Override
			public void onSelection(EntitySelectedEvent event) {
				presenter.entitySelected(myTreeBrowser.getSelected());
			}
		};
		myTreeBrowser.addEntitySelectedHandler(mySelectedHandler);

		favoritesSelectedHandler = new EntitySelectedHandler() {			
			@Override
			public void onSelection(EntitySelectedEvent event) {
				presenter.entitySelected(favoritesTreeBrowser.getSelected());
			}
		};
		favoritesTreeBrowser.addEntitySelectedHandler(favoritesSelectedHandler);
	}

}
