package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserGroupSuggestBox implements UserGroupSuggestBoxView.Presenter, SynapseWidgetPresenter, IsWidget {
	public static final int DELAY = 750;	// milliseconds
	public static final int PAGE_SIZE = 10;
	
	private UserGroupSuggestBoxView view;
	private SynapseSuggestOracle oracle;
	
	private String baseFileHandleUrl;
	private String baseProfileAttachmentUrl;
	private SynapseSuggestion selectedSuggestion;
	private int offset;		// suggestion offset for paging
	private CallbackP<SynapseSuggestion> callback;
	
	
	@Inject
	public UserGroupSuggestBox(UserGroupSuggestBoxView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			SageImageBundle sageImageBundle, SynapseSuggestOracle oracle) {
		this.oracle = oracle;
		this.view = view;
		this.view.configure(oracle);
		view.setPresenter(this);
	}
	
	public void setSuggestionProvider(SuggestionProvider provider) {
		oracle.configure(this, PAGE_SIZE, provider);
	}
	
	public void configureURLs(String baseFileHandleUrl, String baseProfileAttachmentUrl) {
		this.baseFileHandleUrl = baseFileHandleUrl;
		this.baseProfileAttachmentUrl = baseProfileAttachmentUrl;
	}
	
	public String getBaseFileHandleUrl()		{	return baseFileHandleUrl;		}
	public String getBaseProfileAttachmentUrl() {	return baseProfileAttachmentUrl;	}
	
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
		oracle.getSuggestions(offset, view.getText());
	}

	@Override
	public void getNextSuggestions() {
		offset += PAGE_SIZE;
		oracle.getSuggestions(offset, view.getText());
	}

	@Override
	public SynapseSuggestion getSelectedSuggestion() {
		return selectedSuggestion;
	}

	@Override
	public void setSelectedSuggestion(SynapseSuggestion selectedSuggestion) {
		this.selectedSuggestion = selectedSuggestion;
		if(callback != null && selectedSuggestion != null) {
			callback.invoke(selectedSuggestion);
		}
	}
	
	public String getText() {
		return view.getText();
	}
	
	public void clear() {
		view.clear();
	}
	
	@Override
	public void addItemSelectedHandler(CallbackP<SynapseSuggestion> callback) {
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

	// Is this the correct passthrough?
	@Override
	public void updateFieldStateForSuggestions(
			int numResults, int offset) {
		view.updateFieldStateForSuggestions(numResults, offset);
	}

	@Override
	public void handleOracleException(Throwable caught) {
		// show errors
	}
}