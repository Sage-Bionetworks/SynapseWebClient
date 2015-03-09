package org.sagebionetworks.web.client.widget.search;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HomeSearchBoxViewImpl extends FlowPanel implements HomeSearchBoxView {
	
	private static final String ALL_TYPES = "All Types";
	private static final String PROJECTS = "Projects";
	private Presenter presenter;
	private Row simplePanel;
	Button searchBtn;
	
	private Button typeDropdown;
	private TextBox searchField;
	
	@Inject
	public HomeSearchBoxViewImpl() {			
		simplePanel = new Row();
		this.add(simplePanel);
	}
		
	private void createSearchBox() {
		if(typeDropdown == null) {
			typeDropdown = new Button();
			typeDropdown.setSize(ButtonSize.LARGE);
			typeDropdown.setWidth("150px");
			typeDropdown.setDataToggle(Toggle.DROPDOWN);
			typeDropdown.setText(PROJECTS);
			
			DropDownMenu menu = new DropDownMenu();
			AnchorListItem menuItem = null;
			
			menuItem = new AnchorListItem(PROJECTS);
		    menuItem.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					typeDropdown.setText(PROJECTS);
					presenter.setSearchAll(false);
				}
			});
		    menu.add(menuItem);
		    
		    menuItem = new AnchorListItem(ALL_TYPES);
		    menuItem.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					typeDropdown.setText(ALL_TYPES);
					presenter.setSearchAll(true);
				}
			});
		    menu.add(menuItem);		    
		    
		    ButtonGroup group = new ButtonGroup();
		    group.add(typeDropdown);
		    group.add(menu);
		    Column container = new Column(ColumnSize.MD_2);
			container.add(group);
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
			Column container = new Column(ColumnSize.MD_8);
			container.add(searchField);
		    simplePanel.add(container);
		}
	    
		if(searchBtn == null) {
			searchBtn = new Button(DisplayConstants.LABEL_SEARCH);
			searchBtn.setIcon(IconType.SEARCH);
			searchBtn.setSize(ButtonSize.LARGE);
			searchBtn.setBlock(true);
			searchBtn.addClickHandler(new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					presenter.search(searchField.getValue());
				}
			});
			Column container = new Column(ColumnSize.MD_2);
			container.add(searchBtn);
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
