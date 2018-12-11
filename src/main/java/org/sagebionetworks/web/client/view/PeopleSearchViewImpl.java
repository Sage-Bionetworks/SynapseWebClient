package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PeopleSearchViewImpl extends Composite implements PeopleSearchView {
	public interface PeopleSearchViewImplUiBinder extends UiBinder<Widget, PeopleSearchViewImpl> {}
	@UiField
	SimplePanel searchBoxPanel;
	@UiField
	SimplePanel peopleListPanel;
	@UiField
	SimplePanel synAlertPanel;
	@UiField
	Button searchButton;
	@UiField
	TextBox searchField;
	private Header headerWidget;
	private Presenter presenter;
	
	@Inject
	public PeopleSearchViewImpl(PeopleSearchViewImplUiBinder binder,
			Header headerWidget) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		headerWidget.configure();
		configureSearchBox();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure();
		headerWidget.refresh();	
		Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void setLoadMoreContainer(Widget w) {
		peopleListPanel.setWidget(w);
	}
	
	@Override
	public void setSearchTerm(String searchTerm) {
		searchField.setValue(searchTerm);
	}
	
	private void configureSearchBox() {
		searchButton.addClickHandler(new ClickHandler() {				
			@Override
			public void onClick(ClickEvent event) {					
				presenter.goTo(new PeopleSearch(searchField.getValue()));
			}
		});
		
		searchField.addKeyDownHandler(new KeyDownHandler() {				
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
	                searchButton.fireEvent(new ClickEvent() {});
	            }					
			}
		});				
	}

	@Override
	public void setSynAlertWidget(Widget synAlert) {
		synAlertPanel.setVisible(false);
		this.synAlertPanel.setWidget(synAlert);
	}

	@Override
	public void setSynAlertWidgetVisible(boolean isVisible) {
		synAlertPanel.setVisible(isVisible);
	}

}
