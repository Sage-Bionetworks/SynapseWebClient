package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.InlineRadio;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Text;
import org.gwtbootstrap3.client.ui.html.UnorderedList;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBox;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowser.SelectedHandler;

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
	Button cancelButton;
	
	@UiField
	SimplePanel browseMyEntitiesContainer;
	@UiField
	SimplePanel searchContainer;
	@UiField
	SimplePanel enterSynapseIdContainer;
	@UiField
	SimplePanel myEntitiesBrowserContainer;
	@UiField
	FlowPanel entitySearchWidgetContainer;
	@UiField
	SimplePanel entitySearchBoxContainer;
	
	@UiField
	FlowPanel enterIdWidgetContainer;
	@UiField
	FlowPanel versionUI;
	@UiField
	Text selectedText;
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
	
	@UiField
	TextBox synapseIdTextBox;
	@UiField
	Button lookupSynapseIdButton;
	
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
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				modal.hide();
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
	
	private void showTopRightContainer(Widget container) {
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
		updateSelectedView();
		myEntitiesBrowser.clearState();
		myEntitiesBrowser.refresh();
		synapseIdTextBox.clear();
		entitySearchBox.clearSelection();
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
		entitySearchBoxContainer.add(entitySearchBox.asWidget());
		
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
		lookupSynapseIdButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.lookupEntity(synapseIdTextBox.getValue(), new AsyncCallback<Entity>() {
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
		selectedText.setText(DisplayUtils.createEntityVersionString(selectedRef));
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







