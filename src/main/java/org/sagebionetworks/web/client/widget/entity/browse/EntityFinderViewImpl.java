package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBox;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowser.SelectedHandler;

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
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityFinderViewImpl extends LayoutContainer implements EntityFinderView {
	
	private static final int HEIGHT_PX = 500;
	private static final int HEIGHT_BOTTOM_RIGHT_PX = 48;
	private static final int HEIGHT_BOTTOM_RIGHT_PADDING_PX = 10;	
	private static final int LEFT_WIDTH_PX = 180;
	private static final int RIGHT_WIDTH_PX = 621;
	private static final int MARGIN_WIDTH_PX = 10;
	private static final MarginData MARGIN_10 = new MarginData(MARGIN_WIDTH_PX);
	private static final MarginData MARGIN_RIGHT_10 = new MarginData(0, MARGIN_WIDTH_PX, 0, 0);
	private static final String UNSELECTED_TEXT = "<h4>--</h4>";

	private static final int TOTAL_WIDTH_PX = LEFT_WIDTH_PX + RIGHT_WIDTH_PX + MARGIN_WIDTH_PX;
		
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private EntityTreeBrowser entityTreeBrowser;
	private EntitySelectedHandler entitySelectedHandler;
	private MyEntitiesBrowser myEntitiesBrowser;	
	private EntitySearchBox entitySearchBox;
	
	private Widget myEntitiesBrowserWidget;
	private LayoutContainer entitySearchWidget;
	private LayoutContainer enterIdWidget;
	private LayoutContainer enterIdEntityDetail;
	private LayoutContainer versionChooser;
	private LayoutContainer versionComboContainer;
	private HTML selectedText;
	private Button selectButton;
	private LayoutContainer container;
	private LayoutContainer left;
	private LayoutContainer rightTop;
	private LayoutContainer rightBottom;
	private Reference selectedEntity = null;
	private SimpleComboBox<VersionInfoModelData> versionComboBox;
	private Radio currentVersionRadio;
	private Radio specifyVersionRadio;
			
	@Inject
	public EntityFinderViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, EntityTreeBrowser entityTreeBrowser,
			MyEntitiesBrowser myEntitiesBrowser, EntitySearchBox entitySearchBox) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.entityTreeBrowser = entityTreeBrowser;
		this.myEntitiesBrowser = myEntitiesBrowser;
		this.entitySearchBox = entitySearchBox;
	}
	
	@Override
	protected void onRender(com.google.gwt.user.client.Element parent, int index) {
		super.onRender(parent, index);		
	}
		
	@Override
	public Widget asWidget() {
		if(container == null) {
			container = new LayoutContainer();
			container.setScrollMode(Scroll.NONE);
			add(container);
		} else {
			container.removeAll();
		}
		
		// left and right
		left = new LayoutContainer();
		left.setBorders(true);
		left.setHeight(HEIGHT_PX);
		left.addStyleName("floatleft notopmargin whiteBackground");
		left.setWidth(LEFT_WIDTH_PX);
		LayoutContainer right = new LayoutContainer();
		right.addStyleName("floatleft notopmargin last");
		right.setWidth(RIGHT_WIDTH_PX);
		
		rightTop = new LayoutContainer();
		rightTop.addStyleName("floatleft whiteBackground notopmargin last");
		rightTop.setWidth(RIGHT_WIDTH_PX);
		rightTop.setHeight(HEIGHT_PX - HEIGHT_BOTTOM_RIGHT_PX - HEIGHT_BOTTOM_RIGHT_PADDING_PX);
		rightTop.setBorders(true);
		
		rightBottom = new LayoutContainer();
		rightBottom.addStyleName("floatleft whiteBackground last");
		rightBottom.setWidth(RIGHT_WIDTH_PX);
		rightBottom.setHeight(HEIGHT_BOTTOM_RIGHT_PX);
		rightBottom.setBorders(true);
		createSelectedWidget();
			
		right.add(rightTop);
		right.add(rightBottom, new MarginData(HEIGHT_BOTTOM_RIGHT_PADDING_PX, 0, 0, 0));
		
		// pane widgets
		createMyEntityBrowserWidget();		
		createSearchBoxWidget();			
		createEnterIdWidget(); 
		
		// set top widget into rightTop view
		replaceRightWidget(myEntitiesBrowserWidget);
		
		container.add(left, MARGIN_RIGHT_10);
		container.add(right);
		
		container.layout(true);
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		// create a new handler for this presenter
		if(entitySelectedHandler != null) {
			entityTreeBrowser.removeEntitySelectedHandler(entitySelectedHandler);
		}
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
	private void createMyEntityBrowserWidget() {
		// My Entity Browser
		EntityTreeBrowser tree = myEntitiesBrowser.getEntityTreeBrowser();
		tree.setMakeLinks(false);
		tree.setShowContextMenu(false);
		myEntitiesBrowser.setEntitySelectedHandler(new SelectedHandler() {					
			@Override
			public void onSelection(String selectedEntityId) {
				selectedEntity = new Reference();
				selectedEntity.setTargetId(selectedEntityId);		
				updateSelected();
				createVersionChooser(selectedEntityId);
			}
		});
		myEntitiesBrowserWidget = myEntitiesBrowser.asWidget();

		// list entry
		Widget entry = createNewLeftEntry(DisplayConstants.BROWSE_MY_ENTITIES, new ClickHandler(){
	        @Override
	        public void onClick(ClickEvent event) {
				replaceRightWidget(myEntitiesBrowserWidget);				
	        }
	    });
		left.add(entry);

	}

	private void createSearchBoxWidget() {
		// Search Widget
		entitySearchBox.setEntitySelectedHandler(new EntitySearchBox.EntitySelectedHandler() {			
			@Override
			public void onSelected(String entityId, String name, List<VersionInfo> versions) {
				selectedEntity = new Reference();
				selectedEntity.setTargetId(entityId);
				updateSelected();
				createVersionChooser(entityId);
			}
		}, false);
		
		entitySearchWidget = new LayoutContainer();
		HTML search = new HTML("<h4>" + DisplayConstants.LABEL_SEARCH + "</h4>");
		search.addStyleName("span-2 notopmargin");		
		entitySearchWidget.add(search, new MarginData(0, 0, 10, 0));
		Widget box = entitySearchBox.asWidget(490);
		box.addStyleName("span-13 notopmargin last");
		entitySearchWidget.add(box, new MarginData(0, 0, 10, 0));

		// list entry
		Widget entry = createNewLeftEntry(DisplayConstants.LABEL_SEARCH, new ClickHandler(){
	        @Override
	        public void onClick(ClickEvent event) {
				replaceRightWidget(entitySearchWidget);
	        }
	    });
		left.add(entry);
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
		btn.addSelectionListener(new SelectionListener<ButtonEvent>() {			
			@Override
			public void componentSelected(ButtonEvent ce) {
				// lookup id
				selectedEntity = new Reference();
				String entityId = input.getValue();
				selectedEntity.setTargetId(entityId);
				presenter.lookupEntity(entityId, new AsyncCallback<Entity>() {
					@Override
					public void onSuccess(Entity entity) {
						updateSelected();											
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
		btn.setHeight(25);
		widget.add(btn);
		
		// Enter key submits lookup
		new KeyNav<ComponentEvent>(input) {
			@Override
			public void onEnter(ComponentEvent ce) {
				super.onEnter(ce);
				if(btn.isEnabled())
					btn.fireEvent(Events.Select);
			}
		};

		
		enterIdEntityDetail = new LayoutContainer();
		versionChooser = new LayoutContainer();
		widget.add(enterIdEntityDetail);
		widget.add(versionChooser);
		this.enterIdWidget = widget;		
		
		// list entry
		Widget entry = createNewLeftEntry(DisplayConstants.ENTER_SYNAPSE_ID, new ClickHandler(){
	        @Override
	        public void onClick(ClickEvent event) {
				replaceRightWidget(enterIdWidget);
	        }
	    });
		left.add(entry);
	}
				
	private Widget createNewLeftEntry(String name, ClickHandler handler) {
		SimplePanel p = new SimplePanel();
	    p.sinkEvents(Event.ONCLICK);	    
	    p.addHandler(handler, ClickEvent.getType());
		
		p.addStyleName("last sidebarMenu");		
		p.setWidget(new HTML(name));
		return p;
	}

	private void replaceRightWidget(Widget widget) {
		rightTop.removeAll();
		versionChooser = null;
		rightTop.add(widget, MARGIN_10);
		rightTop.layout(true);
	}
	
	private void createSelectedWidget() {
		selectedText = new HTML(UNSELECTED_TEXT);
		selectedText.addStyleName("floatleft");
		selectButton = new Button(DisplayConstants.SELECT);
		selectButton.addStyleName("floatleft");
		selectButton.setHeight(25);
		selectButton.disable();
		selectButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				presenter.entitySelected(selectedEntity);
			}
		});
		rightBottom.add(selectedText, MARGIN_10);
		rightBottom.add(selectButton, MARGIN_10);
	}

	private void updateSelected() {
		selectButton.enable();
		selectedText.setHTML("<h4>" + DisplayUtils.getVersionDisplay(selectedEntity) + "</h4>");
	}

	private void createVersionChooser(String entityId) {
		if(versionChooser != null) rightTop.remove(versionChooser);
		boolean showVersions = presenter.showVersions();
		versionChooser = new LayoutContainer();
		
		MarginData first = new MarginData(15, 10, 0, 10);
		MarginData others = new MarginData(15, 10, 0, 0);
		MarginData verticalSpace = new MarginData(5, 0, 0, 0);
		int boxHeight = 52;
		
		LayoutContainer currentVersion = new LayoutContainer();
		currentVersion.setBorders(true);		
		currentVersion.setHeight(boxHeight);
		currentVersion.addStyleName("clearleft");
		currentVersionRadio = new Radio();
		currentVersionRadio.setId("123");
		currentVersionRadio.setValue(true);		
		currentVersionRadio.addStyleName("floatleft");
		HTML label = new HTML(DisplayConstants.CURRENT_VERSION_ALWAYS);
		label.addStyleName("floatleft");
		currentVersion.add(currentVersionRadio, first);
		currentVersion.add(label, others);
		versionChooser.add(currentVersion);
		
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
		versionChooser.add(specificVersion, verticalSpace);
		
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
		    			selectedEntity.setTargetVersionNumber(null);
		    			updateSelected();		    			
		    		}
		    	} else if(specifyVersionRadio.equals(selected)) {
		    		if(versionComboBox != null) versionComboBox.enable();
		    	}
		    }
		});
		
		rightTop.add(versionChooser, MARGIN_10);
		rightTop.layout(true);
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
		boolean isCurrent = true;
		for(int i=versions.size()-1; i>=0; i--) {						
			versionComboBox.add(new VersionInfoModelData(versions.get(i), isCurrent));
			isCurrent = false;
		}
		versionComboBox.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<VersionInfoModelData>>() {			
			@Override
			public void selectionChanged(SelectionChangedEvent<SimpleComboValue<VersionInfoModelData>> se) {
				SimpleComboValue<VersionInfoModelData> val = se.getSelectedItem();
				if(val != null) {
					VersionInfoModelData data = val.getValue();
					if(data != null && data.getVersionInfo() != null) {
						VersionInfo info = data.getVersionInfo();
						selectedEntity.setTargetVersionNumber(info.getVersionNumber());
						updateSelected();
						return;
					}
				}
				showErrorMessage(DisplayConstants.ERROR_GENERIC);
			}
		});
		
		versionComboContainer.add(versionComboBox);
		versionComboContainer.layout(true);
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
	public int getViewWidth() {
		return TOTAL_WIDTH_PX;
	}

	@Override
	public int getViewHeight() {
		return HEIGHT_PX;
	}
	
}







