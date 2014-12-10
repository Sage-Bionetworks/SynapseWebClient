package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.InlineRadio;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBox;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowser.SelectedHandler;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityFinderViewImpl implements EntityFinderView {
	
	public interface Binder extends UiBinder<Widget, EntityFinderViewImpl> {}
	
	private Presenter presenter;
	private MyEntitiesBrowser myEntitiesBrowser;	
	private EntitySearchBox entitySearchBox;
	
	//the modal dialog
	private Modal modal;
	
	@UiField
	Button okButton;
	
	@UiField
	SimplePanel browseMyEntitiesContainer;
	@UiField
	SimplePanel searchContainer;
	@UiField
	SimplePanel enterSynapseIdContainer;
	@UiField
	SimplePanel myEntitiesBrowserContainer;
	@UiField
	SimplePanel entitySearchWidgetContainer;
	@UiField
	SimplePanel enterIdWidgetContainer;
	@UiField
	FlowPanel versionUI;
	@UiField
	HTML selectedText;
	@UiField
	Button versionDropDownButton;
	@UiField
	DropDownMenu versionDropDownMenu;
	
	@UiField
	Radio currentVersionRadio;
	@UiField
	Radio currentVersionRadioShowingVersions;
	@UiField
	InlineRadio specificVersionRadio;
	
	private Reference selectedRef; // DO NOT SET THIS DIRECTLY, use setSelected... methods
	private Long maxVersion = 0L;
	@Inject
	public EntityFinderViewImpl(Binder binder,
			SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, 
			MyEntitiesBrowser myEntitiesBrowser, 
			EntitySearchBox entitySearchBox) {
		this.modal = (Modal)binder.createAndBindUi(this);
		this.myEntitiesBrowser = myEntitiesBrowser;
		this.entitySearchBox = entitySearchBox;
		
		selectedRef = new Reference();
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.okClicked();
			}
		});
		
		createMyEntityBrowserWidget();		
		createSearchBoxWidget();			
		createEnterIdWidget();
		showTopRightContainer(myEntitiesBrowserContainer);
		
		ClickHandler currentVersionClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				versionDropDownButton.setEnabled(false);
    			setSelectedVersion(null);
    			updateSelectedView();		    			
	    	}
		};
		currentVersionRadio.addClickHandler(currentVersionClickHandler);
		currentVersionRadioShowingVersions.addClickHandler(currentVersionClickHandler);
		specificVersionRadio.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				versionDropDownButton.setEnabled(true);
				setSelectedVersion(maxVersion);
				updateSelectedView();
			}
		});
	}
	
	private void hideAllRightTopWidgets() {
		myEntitiesBrowserContainer.setVisible(false);
		entitySearchWidgetContainer.setVisible(false);
		enterIdWidgetContainer.setVisible(false);
	}
	
	private void showTopRightContainer(SimplePanel container) {
		versionUI.setVisible(false);
		versionDropDownMenu.clear();
		hideAllRightTopWidgets();
		container.setVisible(true);
	}
	
	@Override 
	public void setPresenter(Presenter presenter) {
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
		selectedRef = new Reference();
		presenter.setSelectedEntity(selectedRef);
		showTopRightContainer(myEntitiesBrowserContainer);
	}

	/*
	 * Private Methods
	 */
	private void createMyEntityBrowserWidget() {
		// configure tree browsers
		EntityTreeBrowser tree;
		tree = myEntitiesBrowser.getEntityTreeBrowser();
		tree.makeSelectable();
		
		tree = myEntitiesBrowser.getFavoritesTreeBrowser();
		tree.makeSelectable();
		
		myEntitiesBrowser.setEntitySelectedHandler(new SelectedHandler() {					
			@Override
			public void onSelection(String selectedEntityId) {
				setSelectedId(selectedEntityId);
				updateSelectedView();
				createVersionChooser(selectedEntityId);
			}
		});

		myEntitiesBrowserContainer.setWidget(myEntitiesBrowser.asWidget());

		// list entry
		Widget entry = createNewLeftEntry(DisplayConstants.BROWSE_MY_ENTITIES, new ClickHandler(){
	        @Override
	        public void onClick(ClickEvent event) {
	        	showTopRightContainer(myEntitiesBrowserContainer);
	        }
	    });
		browseMyEntitiesContainer.setWidget(entry);
	}

	private void createSearchBoxWidget() {
		// Search Widget
		entitySearchBox.setEntitySelectedHandler(new EntitySearchBox.EntitySelectedHandler() {			
			@Override
			public void onSelected(String entityId, String name, List<VersionInfo> versions) {
				setSelectedId(entityId);
				updateSelectedView();
				createVersionChooser(entityId);
			}
		}, false);
		
		LayoutContainer entitySearchWidget = new LayoutContainer();
		HTML search = new HTML("<h4>" + DisplayConstants.LABEL_SEARCH + "</h4>");
		search.addStyleName("span-2 notopmargin");		
		entitySearchWidget.add(search, new MarginData(0, 0, 10, 0));
		Widget box = entitySearchBox.asWidget(490);
		box.addStyleName("span-13 notopmargin last");
		entitySearchWidget.add(box, new MarginData(0, 0, 10, 0));
		
		entitySearchWidgetContainer.setWidget(entitySearchWidget);
		
		// list entry
		Widget entry = createNewLeftEntry(DisplayConstants.LABEL_SEARCH, new ClickHandler(){
	        @Override
	        public void onClick(ClickEvent event) {
	        	showTopRightContainer(entitySearchWidgetContainer);
	        }
	    });
		searchContainer.setWidget(entry);
	}	

	private void createEnterIdWidget() {
		MarginData margin = new MarginData(0, 10, 0, 0);
		
		LayoutContainer widget = new LayoutContainer();
		HTML html = new HTML("<h4>" + DisplayConstants.SYNAPSE_ID + "</h4>");
		html.addStyleName("floatleft");
		widget.add(html, margin);		
		
		final TextField<String> input = new TextField<String>();
		input.setEmptyText(DisplayConstants.ENTER_SYNAPSE_ID + " (i.e. syn123)");
		input.addStyleName("floatleft");
		input.setWidth(250);
		input.setHeight(25);
		widget.add(input, margin);
		
		final Button btn = new Button(DisplayConstants.LOOKUP);
		btn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// lookup id
				presenter.lookupEntity(input.getValue(), new AsyncCallback<Entity>() {
					@Override
					public void onSuccess(Entity entity) {
						setSelectedId(entity.getId());
						updateSelectedView();											
						// if versionable, create and show versions
						createVersionChooser(entity.getId());
					}

					@Override
					public void onFailure(Throwable caught) {						
					}
				});
			}
		});
		btn.addStyleName("floatleft");
		btn.setSize(ButtonSize.LARGE);
		widget.add(btn);
		enterIdWidgetContainer.setWidget(widget);

		// list entry		
		final Widget entry = createNewLeftEntry(DisplayConstants.ENTER_SYNAPSE_ID, new ClickHandler(){
	        @Override
	        public void onClick(ClickEvent event) {
	        	showTopRightContainer(enterIdWidgetContainer);
	        }
	    });
		enterSynapseIdContainer.setWidget(entry);
	}
				
	private Widget createNewLeftEntry(String name, ClickHandler handler) {
		SimplePanel p = new SimplePanel();
	    p.sinkEvents(Event.ONCLICK);	    
	    p.addHandler(handler, ClickEvent.getType());
		
		p.addStyleName("last sidebarMenu");		
		p.setWidget(new HTML(name));
		return p;
	}
	
	private void updateSelectedView() {		
		selectedText.setHTML("<h4>" + DisplayConstants.CURRENTLY_SELCTED + ": " + DisplayUtils.createEntityVersionString(selectedRef) + "</h4>");
	}

	private void createVersionChooser(String entityId) {
		versionUI.setVisible(false);
		boolean showVersions = presenter.showVersions();
		specificVersionRadio.setEnabled(showVersions);
		if (showVersions) {
			currentVersionRadio.setVisible(false);
			currentVersionRadioShowingVersions.setVisible(true);
			currentVersionRadioShowingVersions.setValue(true);
		} else {
			currentVersionRadioShowingVersions.setVisible(false);
			currentVersionRadio.setVisible(true);
			currentVersionRadio.setValue(true);
		}
		
		presenter.loadVersions(entityId);
		versionUI.setVisible(true);
	}

	private String getVersionLabel(VersionInfo info, boolean isCurrent) {
		String current = isCurrent ? " [" + DisplayConstants.CURRENT + "]" : "";
		if(info.getVersionLabel().equals(info.getVersionNumber().toString())) {
			return info.getVersionNumber().toString() + current;
		} else {
			return info.getVersionLabel() + " (" + info.getVersionNumber() + ")" + current;
		}  
	}
	
	@Override
	public void setVersions(List<VersionInfo> versions) {
		if(versions == null) return;
		versionDropDownMenu.clear();
		versionDropDownButton.setEnabled(false);
		maxVersion = 0L;
		//find maxVersion
		for (VersionInfo info : versions) {
			if(info.getVersionNumber() > maxVersion) {
				maxVersion = info.getVersionNumber();
			}
		}
		//now add items to the drop down menu
		for (final VersionInfo info : versions) {
			boolean isCurrent = info.getVersionNumber() == maxVersion;
			final String text = getVersionLabel(info, isCurrent);
			AnchorListItem item = new AnchorListItem(text);
			item.setMarginLeft(10);
			ClickHandler itemClicked = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					versionDropDownButton.setText(text);
					setSelectedVersion(info.getVersionNumber());
					updateSelectedView();
				}
			};
			item.addClickHandler(itemClicked);
			versionDropDownMenu.add(item);
			if (isCurrent) {
				//set text to the current version by default
				versionDropDownButton.setText(text);
			}
		}
	}

	private void setSelectedId(String entityId) {
		// clear out selection and set new id
		selectedRef.setTargetId(entityId);
		selectedRef.setTargetVersionNumber(null);
		presenter.setSelectedEntity(selectedRef);
	}
	
	private void setSelectedVersion(Long versionNumber) {
		selectedRef.setTargetVersionNumber(versionNumber);
		presenter.setSelectedEntity(selectedRef);
	}
	
	@Override
	public void show() {
		//show modal
		modal.show();
	}
	
	@Override
	public void hide() {
		modal.hide();
	}
}







