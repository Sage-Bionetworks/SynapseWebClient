package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SearchBoxViewImpl extends Composite implements SearchBoxView {

	private static final int BUTTON_HEIGHT = 28;
	private static final String ALL_TYPES = "All Types";
	private static final String PROJECTS = "Projects";
	private Presenter presenter;
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
				
		createSearchBox();
	}
		
	private void createSearchBox() {
		if(searchField == null) {
		    searchField = new TextBox();
		    searchField.setWidth("150px");
		    searchField.setStyleName(SEARCH_BOX_STYLE_NAME);
		    searchField.addStyleName("display-inline");
		    initWidget(searchField);
		    searchField.getElement().setAttribute("placeholder", "Search");
		    searchField.addKeyDownHandler(new KeyDownHandler() {				
				@Override
				public void onKeyDown(KeyDownEvent event) {
					if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
						searchField.setValue("");
						presenter.search(searchField.getValue());
		            }					
				}
			});				
		}	    
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
		//searchField.setText("");		
	}

	/*
	 * Private Methods
	 */


}
