package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.editpanels.NodeEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButton;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HomeSearchBoxViewImpl extends LayoutContainer implements HomeSearchBoxView {

	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;	
	private FlexTable horizontalTable;
	private Button typeDropdown;
	private TextBox searchField;
	private LayoutContainer searchButtonContainer;
	private Boolean currentIsLarge;
		
	private static final String HOMESEARCH_BOX_STYLE_NAME = "homesearchbox";
	
	@Inject
	public HomeSearchBoxViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, NodeEditor nodeEditor,
			AccessMenuButton accessMenuButton,
			AccessControlListEditor accessControlListEditor) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.setLayout(new FitLayout());
				
		horizontalTable = new FlexTable();
		createSearchBox();
		this.add(horizontalTable);
	}
		
	private void createSearchBox() {
		if(typeDropdown == null) {
			typeDropdown = new Button();
			typeDropdown.setText("Everything");
			typeDropdown.setWidth(80);
			
		    Menu menu = new Menu();  
		    MenuItem menuItem = null;
		    
		    menuItem = new MenuItem("Everything");
		    menuItem.addSelectionListener(new SelectionListener<MenuEvent>() {
				@Override
				public void componentSelected(MenuEvent ce) {
					typeDropdown.setText("Everything");					
				}		    
			});
		    menu.add(menuItem);
		    
		    menuItem = new MenuItem("Projects");
		    menuItem.addSelectionListener(new SelectionListener<MenuEvent>() {
				@Override
				public void componentSelected(MenuEvent ce) {
					typeDropdown.setText("Projects");					
				}		    
			});
		    menuItem.disable();
		    menu.add(menuItem);
		    
		    menuItem = new MenuItem("Datasets");
		    menuItem.addSelectionListener(new SelectionListener<MenuEvent>() {
				@Override
				public void componentSelected(MenuEvent ce) {
					typeDropdown.setText("Datasets");					
				}		    
			});
		    menuItem.disable();
		    menu.add(menuItem);
		    
		    typeDropdown.setMenu(menu);		    
		    horizontalTable.setWidget(0, 0, typeDropdown);
		}

		if(searchField == null) {
		    searchField = new TextBox();
		    searchField.setStyleName(HOMESEARCH_BOX_STYLE_NAME);
		    horizontalTable.setWidget(0, 1, searchField);
			searchField.addKeyPressHandler(new KeyPressHandler() {				
				@Override
				public void onKeyPress(KeyPressEvent event) {
					char charCode = event.getCharCode();
					if (charCode == 13 || charCode == '\n' || charCode == '\r') {
						presenter.search(searchField.getValue());
		            }					
				}
			});						    		    
		}
	    
		if(searchButtonContainer != null) {
			horizontalTable.clearCell(0, 2);
		} else {
			searchButtonContainer = new LayoutContainer();
		}
		searchButtonContainer.removeAll();
		Anchor anchor = new Anchor("Search");
		anchor.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.search(searchField.getValue());
			}
		});
    	searchButtonContainer.setStyleName("mega-button");
    	typeDropdown.setHeight(37);
	    searchButtonContainer.add(anchor);
	    horizontalTable.setWidget(0, 2, searchButtonContainer);
	}
	
	@Override
	public Widget asWidget() {		
		return this;
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
	}

	/*
	 * Private Methods
	 */


}
