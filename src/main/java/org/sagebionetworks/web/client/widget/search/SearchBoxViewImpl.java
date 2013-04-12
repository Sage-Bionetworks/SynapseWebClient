package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.utils.AlertUtils;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButton;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SearchBoxViewImpl extends LayoutContainer implements SearchBoxView {

	private static final int BUTTON_HEIGHT = 28;
	private static final String ALL_TYPES = "All Types";
	private static final String PROJECTS = "Projects";
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;	
	private HorizontalPanel hp;
	private FlexTable horizontalTable;
	private Button typeDropdown;
	private TextBox searchField;
	private Button searchBtn;
	private Boolean currentIsLarge;
		
	private static final String SEARCH_BOX_STYLE_NAME = "smallsearchbox";
	
	@Inject
	public SearchBoxViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, 
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
			typeDropdown.setText(PROJECTS);
			typeDropdown.setWidth(85);
			typeDropdown.setHeight(26);
			
		    Menu menu = new Menu();  
		    MenuItem menuItem = null;
		    
		    menuItem = new MenuItem(PROJECTS);
		    menuItem.addSelectionListener(new SelectionListener<MenuEvent>() {
				@Override
				public void componentSelected(MenuEvent ce) {
					typeDropdown.setText(PROJECTS);
					presenter.setSearchAll(false);
				}		    
			});		    
		    menu.add(menuItem);
		    
		    menuItem = new MenuItem(ALL_TYPES);
		    menuItem.addSelectionListener(new SelectionListener<MenuEvent>() {
				@Override
				public void componentSelected(MenuEvent ce) {
					typeDropdown.setText(ALL_TYPES);
					presenter.setSearchAll(true);
				}		    
			});
		    menu.add(menuItem);
		    		    
		    typeDropdown.setMenu(menu);		    
		    horizontalTable.setWidget(0, 0, typeDropdown);	
		    horizontalTable.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		}

		if(searchField == null) {
		    searchField = new TextBox();
		    searchField.setStyleName(SEARCH_BOX_STYLE_NAME);    	
		    horizontalTable.setWidget(0, 1, searchField);
		    horizontalTable.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
			searchField.addKeyDownHandler(new KeyDownHandler() {				
				@Override
				public void onKeyDown(KeyDownEvent event) {
					if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
						presenter.search(searchField.getValue());
		            }					
				}
			});				
		}
	    
		if(searchBtn != null) {
			horizontalTable.clearCell(0, 2);
		} else {
			searchBtn = new Button("Search");
			searchBtn.setHeight(BUTTON_HEIGHT);
			searchBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					presenter.search(searchField.getValue());
				}
			});
					
			
		}
	    horizontalTable.setWidget(0, 2, searchBtn);
	    horizontalTable.getFlexCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);
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
		AlertUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		AlertUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
	}

	/*
	 * Private Methods
	 */


}
