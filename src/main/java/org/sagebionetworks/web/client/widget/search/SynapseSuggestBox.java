package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseSuggestBox implements SynapseSuggestBoxView.Presenter, SynapseWidgetPresenter, IsWidget, Focusable, HasKeyDownHandlers {

	public static final int DELAY = 250; // milliseconds
	public static final int PAGE_SIZE = 10;
	private SynapseSuggestBoxView view;
	private SynapseSuggestOracle oracle;
	private UserGroupSuggestion selectedSuggestion;
	private int offset; // suggestion offset for paging
	private CallbackP<UserGroupSuggestion> callback;

	@Inject
	public SynapseSuggestBox(SynapseSuggestBoxView view, SynapseSuggestOracle oracle) {
		this.oracle = oracle;
		this.view = view;
		this.view.configure(oracle);
		view.setPresenter(this);
	}

	public void setSuggestionProvider(UserGroupSuggestionProvider provider) {
		oracle.configure(this, PAGE_SIZE, provider);
	}

	public void setTypeFilter(TypeFilter type) {
		oracle.setTypeFilter(type);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void setPlaceholderText(String text) {
		view.setPlaceholderText(text);
	}

	public void setWidth(String width) {
		view.setDisplayWidth(width);
	}

	public int getWidth() {
		return view.getWidth();
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public void getPrevSuggestions() {
		offset -= PAGE_SIZE;
		oracle.getSuggestions(offset);
	}

	@Override
	public void getNextSuggestions() {
		offset += PAGE_SIZE;
		oracle.getSuggestions(offset);
	}

	@Override
	public UserGroupSuggestion getSelectedSuggestion() {
		return selectedSuggestion;
	}

	@Override
	public void setSelectedSuggestion(UserGroupSuggestion selectedSuggestion) {
		this.selectedSuggestion = selectedSuggestion;
		if (selectedSuggestion != null) {
			view.setSelectedText("Currently selected: " + selectedSuggestion.getName());
			if (callback != null) {
				callback.invoke(selectedSuggestion);
			}
		} else {
			view.setSelectedText("");
		}
	}

	public String getText() {
		return view.getText();
	}

	public void setText(String text) {
		view.setText(text);
	}

	public void clear() {
		view.clear();
	}

	@Override
	public void addItemSelectedHandler(CallbackP<UserGroupSuggestion> callback) {
		this.callback = callback;
	}

	@Override
	public void showLoading() {
		view.showLoading();
	}

	@Override
	public void hideLoading() {
		view.hideLoading();
	}

	@Override
	public void showErrorMessage(String message) {
		view.showErrorMessage(message);
	}

	@Override
	public void updateFieldStateForSuggestions(int numResults, int offset) {
		view.updateFieldStateForSuggestions(numResults, offset);
	}

	@Override
	public void handleOracleException(Throwable caught) {
		view.showErrorMessage(caught.getMessage());
	}

	public void setFocus(boolean focused) {
		view.setFocus(focused);
	}

	public void selectAll() {
		view.selectAll();
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return view.addKeyDownHandler(handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		view.fireEvent(event);
	}

	@Override
	public int getTabIndex() {
		return view.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		view.setAccessKey(key);
	}

	@Override
	public void setTabIndex(int index) {
		view.setTabIndex(index);
	}

}
