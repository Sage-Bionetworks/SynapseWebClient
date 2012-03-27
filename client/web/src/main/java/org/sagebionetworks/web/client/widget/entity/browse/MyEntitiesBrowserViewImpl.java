package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MyEntitiesBrowserViewImpl extends LayoutContainer implements MyEntitiesBrowserView {

	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private EntityTreeBrowser entityTreeBrowser;
	private EntitySelectedHandler entitySelectedHandler;
	private Button onlyCreatedFilter;
	private boolean onlyCreatedPressed;
	
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
		cp.setHeight(250);
		cp.setWidth(459);
		cp.setScrollMode(Scroll.AUTO);				
		
		final Status status = new Status();
		status.setAutoWidth(true);
		status.setBox(true);
		status.setText(DisplayConstants.STATUS_CAN_EDIT);
		
		ToolBar toolbar = new ToolBar();
		onlyCreatedPressed = false;
		final Button onlyCreatedButton = new Button(DisplayConstants.BUTTON_FILTER_ONLY_MY_CREATION, AbstractImagePrototype.create(iconsImageBundle.filter16()));
		onlyCreatedButton.addSelectionListener(new SelectionListener<ButtonEvent>() {			
			@Override
			public void componentSelected(ButtonEvent ce) {
				if(onlyCreatedPressed) {
					presenter.loadUserUpdateable();					
					onlyCreatedPressed = false;
					onlyCreatedButton.setText(DisplayConstants.BUTTON_FILTER_ONLY_MY_CREATION);
					status.setText(DisplayConstants.STATUS_CAN_EDIT);					
				} else {
					presenter.createdOnlyFilter();
					onlyCreatedPressed = true;
					onlyCreatedButton.setText(DisplayConstants.BUTTON_FILTER_USER_UPDATABLE);
					status.setText(DisplayConstants.STATUS_CREATED_BY);
				}
			}
		});
		toolbar.add(onlyCreatedButton);

		toolbar.add(new FillToolItem());
		
		
		toolbar.add(status);
		
		cp.setBottomComponent(toolbar);
		
		cp.add(entityTreeBrowser.asWidget());
		
		add(cp);
	}

	@Override
	public void setMyEntities(List<EntityHeader> rootEntities) {
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
