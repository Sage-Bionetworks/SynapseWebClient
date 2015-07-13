package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestOracleImpl.UserGroupSuggestion;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserGroupSuggestBox implements UserGroupSuggestBoxView.Presenter, SynapseWidgetPresenter {
	public static final int DELAY = 750;	// milliseconds
	public static final int PAGE_SIZE = 10;
	
	private UserGroupSuggestBoxView view;
	private UserGroupSuggestOracle oracle;
	
	private String baseFileHandleUrl;
	private String baseProfileAttachmentUrl;
	
	private UserGroupSuggestion selectedSuggestion;
	private int offset;		// suggestion offset for paging
	private CallbackP<UserGroupSuggestion> callback;
	
	
	@Inject
	public UserGroupSuggestBox(UserGroupSuggestBoxView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			SageImageBundle sageImageBundle) {
		this.view = view;		
//		oracle = view.getUserGroupSuggestOracle();
		view.setPresenter(this);
	}
	
	public void setOracle(UserGroupSuggestOracle oracle) {
		this.view.configure(oracle);
		this.oracle = oracle;
		oracle.configure(this, PAGE_SIZE);
	}
	
	public void configureURLs(String baseFileHandleUrl, String baseProfileAttachmentUrl) {
		this.baseFileHandleUrl = baseFileHandleUrl;
		this.baseProfileAttachmentUrl = baseProfileAttachmentUrl;
	}
	
	public String getBaseFileHandleUrl()		{	return baseFileHandleUrl;		}
	public String getBaseProfileAttachmentUrl() {	return baseProfileAttachmentUrl;	}
	
	@Override
	public Widget asWidget() {
		//view.setPresenter(this);
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
	public UserGroupSuggestion getSelectedSuggestion() {
		return selectedSuggestion;
	}

	@Override
	public void setSelectedSuggestion(UserGroupSuggestion selectedSuggestion) {
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

	// Is this the correct passthrough?
	@Override
	public void updateFieldStateForSuggestions(
			UserGroupHeaderResponsePage result, int offset) {
		view.updateFieldStateForSuggestions(result, offset);
	}

	@Override
	public void handleOracleException(Throwable caught) {
		// show errors
	}
}