package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;

import com.google.gwt.dom.client.LIElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MyEntitiesBrowserViewImpl implements MyEntitiesBrowserView {

	public interface MyEntitiesBrowserViewImplUiBinder extends UiBinder<Widget, MyEntitiesBrowserViewImpl> {}
	
	private Presenter presenter;
	private EntityTreeBrowser currentContextTreeBrowser;
	private EntityTreeBrowser myTreeBrowser;
	private EntityTreeBrowser favoritesTreeBrowser;
	@UiField
	SimplePanel myProjectsContainer;
	@UiField
	SimplePanel myFavoritesContainer;
	@UiField
	SimplePanel currentContextContainer;
	
	@UiField
	LIElement myProjectsListItem;
	@UiField
	LIElement myFavoritesListItem;
	@UiField
	LIElement currentContextListItem;
	
	@UiField
	Anchor myProjectsLink;
	@UiField
	Anchor myFavoritesLink;
	@UiField
	Anchor currentContextLink;
	
	@UiField
	Div myProjectsTabContents;
	@UiField
	Div myFavoritesTabContents;
	@UiField
	Div currentContextTabContents;
	
	private Widget widget;
	@Inject
	public MyEntitiesBrowserViewImpl(MyEntitiesBrowserViewImplUiBinder binder, 
			PortalGinInjector ginInjector) {
		widget = binder.createAndBindUi(this);
		this.myTreeBrowser = ginInjector.getEntityTreeBrowser();
		this.favoritesTreeBrowser = ginInjector.getEntityTreeBrowser();
		this.currentContextTreeBrowser = ginInjector.getEntityTreeBrowser();
		myProjectsContainer.setWidget(myTreeBrowser.asWidget());
		myFavoritesContainer.setWidget(favoritesTreeBrowser.asWidget());
		currentContextContainer.setWidget(currentContextTreeBrowser.asWidget());
		
		myProjectsLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setTabSelected(myProjectsLink, myProjectsListItem, myProjectsTabContents);
			}
		});
		
		myFavoritesLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setTabSelected(myFavoritesLink, myFavoritesListItem, myFavoritesTabContents);
			}
		});
		
		currentContextLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setTabSelected(currentContextLink, currentContextListItem, currentContextTabContents);
			}
		});
		
		setTabSelected(myProjectsLink, myProjectsListItem, myProjectsTabContents);
	}
	
	@Override
	public void setUpdatableEntities(List<EntityHeader> rootEntities) {
		myTreeBrowser.configure(rootEntities);		
	}
	
	@Override
	public void setFavoriteEntities(List<EntityHeader> favoriteEntities) {
		favoritesTreeBrowser.configure(favoriteEntities);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {		
		// create a new handler for this presenter
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
	public void clearSelection() {
		myTreeBrowser.clearSelection();
		favoritesTreeBrowser.clearSelection();
		currentContextTreeBrowser.clearSelection();
	}
	
	@Override
	public void clear() {
		myTreeBrowser.clearState();
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
		EntitySelectedHandler mySelectedHandler = new EntitySelectedHandler() {			
			@Override
			public void onSelection(EntitySelectedEvent event) {
				presenter.entitySelected(myTreeBrowser.getSelected());
			}
		};
		myTreeBrowser.setEntitySelectedHandler(mySelectedHandler);

		EntitySelectedHandler favoritesSelectedHandler = new EntitySelectedHandler() {			
			@Override
			public void onSelection(EntitySelectedEvent event) {
				presenter.entitySelected(favoritesTreeBrowser.getSelected());
			}
		};
		favoritesTreeBrowser.setEntitySelectedHandler(favoritesSelectedHandler);
		
		EntitySelectedHandler currentContextSelectedHandler = new EntitySelectedHandler() {			
			@Override
			public void onSelection(EntitySelectedEvent event) {
				presenter.entitySelected(currentContextTreeBrowser.getSelected());
			}
		};
		currentContextTreeBrowser.setEntitySelectedHandler(currentContextSelectedHandler);
	}
	
	

	/**
	 * Used only for setting the view's tab display
	 */
	private void setTabSelected(Anchor tabLink, LIElement tabListItem, Div tabContents) {
		setTabInActive(currentContextLink, currentContextListItem, currentContextTabContents);
		setTabInActive(myFavoritesLink, myFavoritesListItem, myFavoritesTabContents);
		setTabInActive(myProjectsLink, myProjectsListItem, myProjectsTabContents);
		setTabActive(tabLink, tabListItem, tabContents);
	}
	
	private void setTabActive(Anchor tabLink, LIElement tabListItem, Div tabContents) {
		tabContents.setVisible(true);
		tabListItem.addClassName("active");
		tabLink.removeStyleName("link");
	}
	
	private void setTabInActive(Anchor tabLink, LIElement tabListItem, Div tabContents) {
		tabContents.setVisible(false);
		tabListItem.removeClassName("active");
		tabLink.addStyleName("link");
	}

	@Override
	public EntityTreeBrowser getCurrentContextTreeBrowser() {
		return currentContextTreeBrowser;
	}
	
	@Override
	public void setCurrentContextTabVisible(boolean visible) {
		UIObject.setVisible(currentContextListItem, visible);	
	}
}
