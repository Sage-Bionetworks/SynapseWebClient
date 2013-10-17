package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MyEntitiesBrowserViewImpl extends LayoutContainer implements MyEntitiesBrowserView {

	private static final int HEIGHT_PX = 250;
	private static final int WIDTH_PX = 459;
	
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private EntityTreeBrowser myTreeBrowser;
	private EntityTreeBrowser favoritesTreeBrowser;
	private EntitySelectedHandler mySelectedHandler;
	private EntitySelectedHandler favoritesSelectedHandler;
	private PortalGinInjector ginInjector;
	
	private TabPanel panel;
			
	@Inject
	public MyEntitiesBrowserViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, PortalGinInjector ginInjector) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.myTreeBrowser = ginInjector.getEntityTreeBrowser();		
		this.favoritesTreeBrowser = ginInjector.getEntityTreeBrowser();
		this.ginInjector = ginInjector;
	}
	
	@Override
	protected void onRender(com.google.gwt.user.client.Element parent, int index) {
		super.onRender(parent, index);		
		panel = new TabPanel();
		panel.setPlain(true);
		panel.setHeight(HEIGHT_PX);
		panel.setAutoWidth(true);
		
		final String MY_TAB_ID = "myTab";
		final String FAVORITE_TAB_ID = "favoriteTab";
		
		TabItem myTab = new TabItem(DisplayConstants.MY_PROJECTS);
		myTab.add(myTreeBrowser.asWidget());
		myTab.setScrollMode(Scroll.AUTO);	
		panel.add(myTab);
			
		TabItem favoritesTab = new TabItem(DisplayConstants.MY_FAVORITES);
		favoritesTab.add(favoritesTreeBrowser.asWidget());
		favoritesTab.setScrollMode(Scroll.AUTO);
		favoritesTab.setIcon(AbstractImagePrototype.create(iconsImageBundle.star16()));
		panel.add(favoritesTab);
		
		panel.addListener(Events.Select, new Listener<TabPanelEvent>() {
			public void handleEvent(TabPanelEvent be) {
				String tabId = be.getItem().getId();
				if(MY_TAB_ID.equals(tabId)) {
					
				} else if(FAVORITE_TAB_ID.equals(tabId)) {
					presenter.loadFavorites();
				}
			}
		});
		
		add(panel);
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
		return this;
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
