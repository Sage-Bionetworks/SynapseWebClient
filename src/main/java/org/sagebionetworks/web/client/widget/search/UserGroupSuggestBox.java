package org.sagebionetworks.web.client.widget.search;

import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestOracle.UserGroupSuggestion;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserGroupSuggestBox implements UserGroupSuggestBoxView.Presenter, SynapseWidgetPresenter {
	public static final int DELAY = 750;	// milliseconds
	public static final int PAGE_SIZE = 10;
	
	private UserGroupSuggestBoxView view;
	private UserGroupSuggestOracle oracle;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	
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
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		
		oracle = view.getUserGroupSuggestOracle();
		view.setPresenter(this);
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
		getSuggestions(oracle.getRequest(), oracle.getCallback());
	}

	@Override
	public void getNextSuggestions() {
		offset += PAGE_SIZE;
		getSuggestions(oracle.getRequest(), oracle.getCallback());
	}
	
	public void getSuggestions(final SuggestOracle.Request request, final SuggestOracle.Callback callback) {
		view.showLoading();
		
		String prefix = request.getQuery();
		final List<Suggestion> suggestions = new LinkedList<Suggestion>();
		synapseClient.getUserGroupHeadersByPrefix(prefix, PAGE_SIZE, offset, new AsyncCallback<UserGroupHeaderResponsePage>() {
			@Override
			public void onSuccess(UserGroupHeaderResponsePage result) {
				// Update view fields.
				view.updateFieldStateForSuggestions(result, offset);
				
				// Load suggestions.
				for (UserGroupHeader header : result.getChildren()) {
					suggestions.add(oracle.makeUserGroupSuggestion(header, view.getText()));
				}

				// Set up response
				SuggestOracle.Response response = new SuggestOracle.Response(suggestions);
				callback.onSuggestionsReady(request, response);
				
				view.hideLoading();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if (!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {                    
					view.showErrorMessage(caught.getMessage());
				}
			}

		});
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
	
	/**
	 * For testing. This would break the suggest box, as it does
	 * not update the view's oracle.
	 * @param oracle
	 */
	public void setOracle(UserGroupSuggestOracle oracle) {
		this.oracle = oracle;
	}
	
	@Override
	public void addItemSelectedHandler(CallbackP<UserGroupSuggestion> callback) {
		this.callback = callback;
	}
}