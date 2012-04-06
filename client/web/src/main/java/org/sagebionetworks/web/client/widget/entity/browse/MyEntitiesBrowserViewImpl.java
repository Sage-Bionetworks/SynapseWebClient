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

	private static final int HEIGHT_PX = 250;
	private static final int WIDTH_PX = 459;
	
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private EntityTreeBrowser entityTreeBrowser;
	private EntitySelectedHandler entitySelectedHandler;
	private Button onlyCreatedFilter;
	private boolean onlyCreatedPressed;
	private Status status;
	private Button onlyCreatedButton;
	
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
		
		status = new Status();
		status.setAutoWidth(true);
		status.setBox(true);
		status.setText(DisplayConstants.STATUS_CAN_EDIT);
		
		ToolBar toolbar = new ToolBar();
		onlyCreatedPressed = false;
		onlyCreatedButton = new Button(DisplayConstants.BUTTON_FILTER_ONLY_MY_CREATION, AbstractImagePrototype.create(iconsImageBundle.filter16()));
		onlyCreatedButton.addSelectionListener(new SelectionListener<ButtonEvent>() {			
			@Override
			public void componentSelected(ButtonEvent ce) {
				if(onlyCreatedPressed) {
					presenter.loadUserUpdateable();
					onlyCreatedPressed = false;
				} else {
					presenter.createdOnlyFilter();
					onlyCreatedPressed = true;
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
	public void setUpdatableEntities(List<EntityHeader> rootEntities) {
		setToolbarStateUserUpdateable();
		entityTreeBrowser.setRootEntities(rootEntities);		
	}
	
	@Override
	public void setCreatedEntities(List<EntityHeader> rootEntities) {
		setToolbarStateCreatedBy();
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

	private void setToolbarStateCreatedBy() {
		if(onlyCreatedButton != null && status != null) {
			onlyCreatedPressed = true;
			onlyCreatedButton.setText(DisplayConstants.BUTTON_FILTER_USER_UPDATABLE);
			status.setText(DisplayConstants.STATUS_CREATED_BY);
		}
	}

	private void setToolbarStateUserUpdateable() {
		if(onlyCreatedButton != null && status != null) {
			onlyCreatedPressed = false;
			onlyCreatedButton.setText(DisplayConstants.BUTTON_FILTER_ONLY_MY_CREATION);
			status.setText(DisplayConstants.STATUS_CAN_EDIT);
		}
	}

}
