package org.sagebionetworks.web.client.widget.search;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.search.EntitySearchSuggestOracle.EntitySuggestion;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SearchBoxViewImpl implements SearchBoxView {
	public interface Binder extends UiBinder<Widget, SearchBoxViewImpl> {}
	private Presenter presenter;
	Widget widget;
	@UiField
	Icon searchButton;
	@UiField
	Span searchSuggestBoxContainer;
	SuggestBox searchSuggestBox;
	GWTWrapper gwt;
	PlaceChanger placeChanger;
	public static final String INACTIVE_STYLE = "inactive";
	public static final String ACTIVE_STYLE = "active";
	EntitySearchSuggestOracle entitySearchOracle;
	EntitySuggestion currentlySelectedSuggestion;
	@Inject
	public SearchBoxViewImpl(Binder binder, 
			EntitySearchSuggestOracle entitySearchOracle, 
			GWTWrapper gwt, 
			GlobalApplicationState globalAppState) {
		widget = binder.createAndBindUi(this);
		this.gwt = gwt;
		this.entitySearchOracle = entitySearchOracle;
		searchSuggestBox = new SuggestBox(entitySearchOracle);
		searchSuggestBox.setAutoSelectEnabled(false);
		searchSuggestBox.setHeight("32px");
		searchSuggestBox.addStyleName("entity-search-menu");
		searchSuggestBox.getTextBox().addStyleName("form-control");
		searchSuggestBox.getTextBox().getElement().setAttribute("placeholder", " Search all of Synapse");
		searchSuggestBoxContainer.add(searchSuggestBox);
		placeChanger = globalAppState.getPlaceChanger();
		initClickHandlers();
	}

	private void initClickHandlers() {
		searchSuggestBox.addSelectionHandler(event -> {
			currentlySelectedSuggestion = (EntitySuggestion) event.getSelectedItem();
			placeChanger.goTo(new Synapse(currentlySelectedSuggestion.getEntityId()));
			searchFieldInactive();
		});

		searchSuggestBox.getTextBox().addKeyDownHandler(event -> {
			if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER && currentlySelectedSuggestion == null) {
				executeSearch();
			}
		});
		searchSuggestBox.getTextBox().addBlurHandler(event -> {
			// delay auto-hiding the search box (on focus loss) for .4s so that the search can execute on search button click (when active).
			gwt.scheduleExecution(() -> {
				searchFieldInactive();	
			}, 400);
		});
		searchButton.addClickHandler(event -> {
			if (isSearchFieldActive()) {
				executeSearch();
			} else {
				searchFieldActive();	
			}
		});
	}
	
	public void clearSearchBox() {
		searchSuggestBox.getValueBox().setText(null);
	}
	
	private void executeSearch() {
		if (!"".equals(searchSuggestBox.getValueBox().getText())) {
			presenter.search(searchSuggestBox.getValueBox().getText());
			clearSearchBox();
			searchFieldInactive();
		}
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
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}
	
	@Override
	public void setVisible(boolean isVisible) {
		widget.setVisible(isVisible);
	}
	
	@Override
	public void clear() {
		//searchField.setText("");
	}
	
	private void searchFieldInactive() {
		Element searchBoxContainer = _getSearchBoxContainer();
		searchBoxContainer.removeClassName(ACTIVE_STYLE);
		searchBoxContainer.addClassName(INACTIVE_STYLE);
		entitySearchOracle.cancelRequest();
		searchSuggestBox.hideSuggestionList();
		searchSuggestBox.setFocus(false);
	}
	private void searchFieldActive() {
		Element searchBoxContainer = _getSearchBoxContainer();
		searchBoxContainer.addClassName(ACTIVE_STYLE);
		searchBoxContainer.removeClassName(INACTIVE_STYLE);
		searchSuggestBox.setFocus(true);
		currentlySelectedSuggestion = null;
		clearSearchBox();
	}
	
	private boolean isSearchFieldActive() {
		Element searchBoxContainer = _getSearchBoxContainer();
		return searchBoxContainer.hasClassName(ACTIVE_STYLE);
	}
	
	public static native Element _getSearchBoxContainer() /*-{
		try {
			return $wnd.jQuery('#headerPanel .searchBoxContainer')[0];
		} catch (err) {
			console.error(err);
		}
	}-*/;
}
