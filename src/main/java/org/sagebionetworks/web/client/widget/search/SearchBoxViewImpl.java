package org.sagebionetworks.web.client.widget.search;

import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SearchBoxViewImpl implements SearchBoxView {
	public interface Binder extends UiBinder<Widget, SearchBoxViewImpl> {}
	private Presenter presenter;
	Widget widget;
	@UiField
	Button searchButton;
	@UiField
	TextBox searchField;
	
	@Inject
	public SearchBoxViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		initClickHandlers();
	}
		
	private void initClickHandlers() {
		searchButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.search(searchField.getValue());
				searchField.setValue("");
			}
		});
	    searchField.addKeyDownHandler(new KeyDownHandler() {				
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					searchButton.click();
	            }					
			}
		});				
	}
	
	@Override
	public Widget asWidget() {		
		return widget;
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
	public void setVisible(boolean isVisible) {
		widget.setVisible(isVisible);
	}
	
	@Override
	public void clear() {
		//searchField.setText("");		
	}

	/*
	 * Private Methods
	 */


}
