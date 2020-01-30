package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserTeamConfigViewImpl extends SimplePanel implements UserTeamConfigView {

	SynapseJSNIUtils synapseJSNIUtils;
	SynapseSuggestBox suggestBox;
	UserGroupSuggestionProvider oracle;

	@Inject
	public UserTeamConfigViewImpl(SynapseJSNIUtils synapseJSNIUtils, SynapseSuggestBox suggestBox, UserGroupSuggestionProvider provider) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.suggestBox = suggestBox;
		this.suggestBox.setSuggestionProvider(provider);
	}

	@Override
	public void initView() {
		clear();
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
		return suggestBox.getSelectedSuggestion().getId();
	}

	@Override
	public void setId(String id) {
		// TODO: to allow editing existing badge
	}

	@Override
	public String isIndividual() {
		return suggestBox.getSelectedSuggestion().isIndividual();
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}
}
