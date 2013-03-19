package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButton;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HomeSearchBoxViewImpl extends LayoutContainer implements HomeSearchBoxView {
	
	private static final String ALL_TYPES = "All Types";
	private static final String PROJECTS = "Projects";
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;	
	private FlexTable horizontalTable;
	private Button typeDropdown;
	private TextBox searchField;
	private LayoutContainer searchButtonContainer;
	
	@Inject
	public HomeSearchBoxViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, 
			AccessMenuButton accessMenuButton,
			AccessControlListEditor accessControlListEditor) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.setLayout(new FitLayout());
				
		horizontalTable = new FlexTable();
		horizontalTable.setWidth("600px");
		
		this.add(horizontalTable);
	}
		
	@SuppressWarnings("serial")
	private void createSearchBox() {
		if(typeDropdown == null) {
			typeDropdown = new Button();
			typeDropdown.setText(PROJECTS);
			typeDropdown.setWidth(80);

			MenuItem menuItem = null;
			Menu menu = new Menu();  
			
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
		    
		    // add facet links
		}

		if(searchField == null) {
		    searchField = new TextBox();
		    searchField.setStyleName(DisplayUtils.HOMESEARCH_BOX_STYLE_NAME);
		    horizontalTable.setWidget(0, 1, searchField);
			searchField.addKeyDownHandler(new KeyDownHandler() {				
				@Override
				public void onKeyDown(KeyDownEvent event) {
					if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
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
		createSearchBox();
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
