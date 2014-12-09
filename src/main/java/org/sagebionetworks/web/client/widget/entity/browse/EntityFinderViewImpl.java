package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
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
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorViewImpl;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorViewImpl.Binder;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
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
	
	private static final int MARGIN_WIDTH_PX = 10;
	private static final MarginData MARGIN_10 = new MarginData(MARGIN_WIDTH_PX);
	
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
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
	FlowPanel versionContainer;
	@UiField
	HTML selectedText;
	
	private LayoutContainer versionComboContainer;
	private Reference selectedRef; // DO NOT SET THIS DIRECTLY, use setSelected... methods
	private SimpleComboBox<VersionInfoModelData> versionComboBox;
	private Radio currentVersionRadio;
	private Radio specifyVersionRadio;
			
	@Inject
	public EntityFinderViewImpl(Binder binder,
			SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, 
			MyEntitiesBrowser myEntitiesBrowser, 
			EntitySearchBox entitySearchBox) {
		this.modal = (Modal)binder.createAndBindUi(this);
		this.sageImageBundle = sageImageBundle;
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
	}
	
	private void hideAllRightTopWidgets() {
		myEntitiesBrowserContainer.setVisible(false);
		entitySearchWidgetContainer.setVisible(false);
		enterIdWidgetContainer.setVisible(false);
	}
	
	private void showTopRightContainer(SimplePanel container) {
		versionContainer.clear();
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
		boolean showVersions = presenter.showVersions();
		
		MarginData first = new MarginData(15, 10, 0, 10);
		MarginData others = new MarginData(15, 10, 0, 0);
		
		int boxHeight = 52;
		
		LayoutContainer currentVersion = new LayoutContainer();
		currentVersion.setBorders(true);		
		currentVersion.setHeight(boxHeight);
		currentVersion.addStyleName("clearleft");
		currentVersionRadio = new Radio();
		currentVersionRadio.setId("123");
		currentVersionRadio.setValue(true);		
		currentVersionRadio.addStyleName("floatleft");
		HTML label = showVersions ? new HTML(DisplayConstants.CURRENT_VERSION + " (" + DisplayConstants.ALWAYS_CURRENT_VERSION + ")")
				: new HTML(DisplayConstants.CURRENT_VERSION);
		label.addStyleName("floatleft");
		currentVersion.add(currentVersionRadio, first);
		currentVersion.add(label, others);
		versionContainer.add(currentVersion);
		
		LayoutContainer specificVersion = new LayoutContainer();
		if(!showVersions) specificVersion.disable();
		specificVersion.setBorders(true);
		specificVersion.setHeight(boxHeight);
		specificVersion.addStyleName("clearleft");
		specifyVersionRadio = new Radio();
		specifyVersionRadio.setId("456");
		specifyVersionRadio.addStyleName("floatleft");
		label = new HTML(DisplayConstants.REFER_TO_SPECIFIC_VERSION);
		label.addStyleName("floatleft");
		versionComboContainer = new LayoutContainer();
		versionComboContainer.add(new HTML(DisplayUtils.getLoadingHtml(sageImageBundle)));
		versionComboContainer.addStyleName("floatleft");
		presenter.loadVersions(entityId);
		specificVersion.add(specifyVersionRadio, first);
		specificVersion.add(label, others);
		specificVersion.add(versionComboContainer, others);
		versionContainer.add(specificVersion);
		
		final RadioGroup group = new RadioGroup();
		group.add(currentVersionRadio);
		group.add(specifyVersionRadio);
		group.setSelectionRequired(true);		
		
		group.addListener(Events.Change, new Listener<FieldEvent>() {
		    @Override
		    public void handleEvent(FieldEvent fe) {
		    	Radio selected = group.getValue();
		    	if(currentVersionRadio.equals(selected)) {
		    		if(versionComboBox != null) {
		    			versionComboBox.disable();
		    			versionComboBox.clearSelections();
		    		}
		    		if(currentVersionRadio.getValue()) {
		    			// current always selected. null out selected
		    			setSelectedVersion(null);
		    			updateSelectedView();		    			
		    		}
		    	} else if(specifyVersionRadio.equals(selected)) {
		    		if(versionComboBox != null) versionComboBox.enable();
		    	}
		    }
		});
	}

	@Override
	public void setVersions(List<VersionInfo> versions) {
		versionComboContainer.removeAll();
		if(versions == null) return;
		versionComboBox = new SimpleComboBox<VersionInfoModelData>();
		versionComboBox.setTypeAhead(false);
		versionComboBox.setEditable(false);
		versionComboBox.setForceSelection(true);
		versionComboBox.setTriggerAction(TriggerAction.ALL);
		versionComboBox.disable();
		Long maxVersion = 0L;
		VersionInfoModelData maxModel = null;
		for(int i=0; i<versions.size(); i++) {
			VersionInfo info = versions.get(i);
			VersionInfoModelData model = new VersionInfoModelData(info, false); 
			if(info.getVersionNumber() > maxVersion) {
				maxVersion = info.getVersionNumber();
				maxModel = model;
			}
			versionComboBox.add(model);
		}
		if(maxModel != null) maxModel.setIsCurrent(true);
		versionComboBox.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<VersionInfoModelData>>() {			
			@Override
			public void selectionChanged(SelectionChangedEvent<SimpleComboValue<VersionInfoModelData>> se) {
				SimpleComboValue<VersionInfoModelData> val = se.getSelectedItem();
				if(val != null) {
					VersionInfoModelData data = val.getValue();
					if(data != null && data.getVersionInfo() != null) {
						VersionInfo info = data.getVersionInfo();
						setSelectedVersion(info.getVersionNumber());
						updateSelectedView();
						return;
					}
				}
				showErrorMessage(DisplayConstants.ERROR_GENERIC);
			}
		});
		
		versionComboContainer.add(versionComboBox);
		versionComboContainer.layout(true);
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
	
	class VersionInfoModelData {		
		private VersionInfo info;
		private boolean isCurrent;
		
		public VersionInfoModelData(VersionInfo info, boolean isCurrent) {
			this.info = info;		
			this.isCurrent = isCurrent;
		}
		
		public VersionInfo getVersionInfo() {
			return info;
		}
		
		public void setIsCurrent(boolean isCurrent) {
			this.isCurrent = isCurrent;
		}
		
		public String toString() {
			String current = isCurrent ? " [" + DisplayConstants.CURRENT + "]" : "";
			if(info.getVersionLabel().equals(info.getVersionNumber().toString())) {
				return info.getVersionNumber().toString() + current;
			} else {
				return info.getVersionLabel() + " (" + info.getVersionNumber() + ")" + current;
			}			
		}
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







