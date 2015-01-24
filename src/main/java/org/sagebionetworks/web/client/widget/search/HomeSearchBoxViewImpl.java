package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HomeSearchBoxViewImpl extends LayoutContainer implements HomeSearchBoxView {
	
	private static final String ALL_TYPES = "All Types";
	private static final String PROJECTS = "Projects";
	private Presenter presenter;
	private LayoutContainer simplePanel;
	com.google.gwt.user.client.ui.Button searchBtn;
	
	private Button typeDropdown;
	private TextBox searchField;
	
	@Inject
	public HomeSearchBoxViewImpl() {			
		simplePanel = new LayoutContainer();
		simplePanel.setId("simplePanelLC");
		simplePanel.addStyleName("row");
		
		this.add(simplePanel);
	}
		
	@SuppressWarnings("serial")
	private void createSearchBox() {
		if(typeDropdown == null) {
			typeDropdown = new Button();
			typeDropdown.setText(PROJECTS);
			typeDropdown.setWidth(150);

			MenuItem menuItem = null;
			Menu menu = new Menu();
			menu.setEnableScrolling(false);
			
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
		    typeDropdown.setHeight(37);
		    SimplePanel container = new SimplePanel(typeDropdown);
		    container.addStyleName("col-md-2");
		    simplePanel.add(container);
		}

		if(searchField == null) {
		    searchField = new TextBox();
		    searchField.setStyleName("form-control input-lg");
			searchField.addKeyDownHandler(new KeyDownHandler() {				
				@Override
				public void onKeyDown(KeyDownEvent event) {
					if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
						presenter.search(searchField.getValue());
		            }					
				}
			});				
		    SimplePanel container = new SimplePanel(searchField);
		    container.addStyleName("col-md-8");
			simplePanel.add(container);
		}
	    
		if(searchBtn == null) {
			searchBtn = new com.google.gwt.user.client.ui.Button(DisplayConstants.LABEL_SEARCH);
			searchBtn.removeStyleName("gwt-Button");
			searchBtn.addStyleName("btn btn-default btn-lg btn-block");
			searchBtn.addClickHandler(new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					presenter.search(searchField.getValue());
				}
			});
		    SimplePanel container = new SimplePanel(searchBtn);
		    container.addStyleName("col-md-2");
			simplePanel.add(container);
		}
		
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
