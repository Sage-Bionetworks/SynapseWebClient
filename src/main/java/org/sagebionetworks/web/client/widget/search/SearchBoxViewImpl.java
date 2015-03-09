package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SearchBoxViewImpl extends Composite implements SearchBoxView {

	private Presenter presenter;
	private TextBox searchField;
		
	private static final String SEARCH_BOX_STYLE_NAME = "smallsearchbox";
	
	@Inject
	public SearchBoxViewImpl() {
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
						presenter.search(searchField.getValue());
						searchField.setValue("");
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
