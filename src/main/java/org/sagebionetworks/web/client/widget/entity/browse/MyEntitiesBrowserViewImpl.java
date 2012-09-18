package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MyEntitiesBrowserViewImpl extends LayoutContainer implements MyEntitiesBrowserView {

	private static final int HEIGHT_PX = 250;
	private static final int WIDTH_PX = 459;
	
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private EntityTreeBrowser entityTreeBrowser;
	private EntitySelectedHandler entitySelectedHandler;
	
	private ContentPanel cp;
			
	@Inject
	public MyEntitiesBrowserViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, EntityTreeBrowser entityTreeBrowser) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.entityTreeBrowser = entityTreeBrowser;			
	}
	
	@Override
	protected void onRender(com.google.gwt.user.client.Element parent, int index) {
		super.onRender(parent, index);		

		cp = new ContentPanel();
		cp.setHeaderVisible(false);
		cp.setHeight(HEIGHT_PX);
		cp.setWidth(WIDTH_PX);
		cp.setScrollMode(Scroll.AUTO);				
		
		cp.add(entityTreeBrowser.asWidget());
		
		add(cp);
	}

	@Override
	public void setUpdatableEntities(List<EntityHeader> rootEntities) {
		entityTreeBrowser.setRootEntities(rootEntities);		
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		// create a new handler for this presenter
		if(entitySelectedHandler != null) {
			entityTreeBrowser.removeEntitySelectedHandler(entitySelectedHandler);
		}
		createSelectedHandler();
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
		return entityTreeBrowser;
	}
	
	/*
	 * Private Methods
	 */
	private void createSelectedHandler() {
		entitySelectedHandler = new EntitySelectedHandler() {			
			@Override
			public void onSelection(EntitySelectedEvent event) {
				presenter.entitySelected(entityTreeBrowser.getSelected());
			}
		};
		entityTreeBrowser.addEntitySelectedHandler(entitySelectedHandler);
	}
}
