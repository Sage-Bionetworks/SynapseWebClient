package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UrlCache;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserTeamConfigViewImpl extends SimplePanel implements UserTeamConfigView {

	private Presenter presenter;
	UrlCache urlCache;
	SynapseJSNIUtils synapseJSNIUtils;
	UserGroupSuggestBox suggestBox;
	
	@Inject
	public UserTeamConfigViewImpl(UrlCache urlCache, SynapseJSNIUtils synapseJSNIUtils, UserGroupSuggestBox suggestBox) {
		this.urlCache = urlCache;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.suggestBox = suggestBox;
	}
	
	@Override
	public void initView() {
		clear();
		suggestBox.configureURLs(synapseJSNIUtils.getBaseFileHandleUrl(), synapseJSNIUtils.getBaseProfileAttachmentUrl());
		suggestBox.setPlaceholderText("Enter name...");
		SimplePanel panel = new SimplePanel();
		panel.setWidget(suggestBox.asWidget());
		panel.addStyleName("margin-10");
		setWidget(panel);
	}
	@Override
	public void checkParams() throws IllegalArgumentException {
		if (suggestBox.getSelectedSuggestion() == null)
			throw new IllegalArgumentException("No user or team was selected.");
	}
	@Override
	public String getId() {
		return suggestBox.getSelectedSuggestion().getHeader().getOwnerId();
	}
	
	@Override
	public void setId(String id) {
		//TODO: to allow editing existing badge
	}
	
	@Override
	public String isIndividual() {
		return suggestBox.getSelectedSuggestion().getHeader().getIsIndividual().toString();
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
}
