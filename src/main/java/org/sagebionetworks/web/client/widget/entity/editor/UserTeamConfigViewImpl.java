package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UrlCache;
import org.sagebionetworks.web.client.widget.sharing.UserGroupSearchBox;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserTeamConfigViewImpl extends SimplePanel implements UserTeamConfigView {

	private Presenter presenter;
	ComboBox<ModelData> peopleCombo;
	UrlCache urlCache;
	SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public UserTeamConfigViewImpl(UrlCache urlCache, SynapseJSNIUtils synapseJSNIUtils) {
		this.urlCache = urlCache;
		this.synapseJSNIUtils = synapseJSNIUtils;
	}
	
	@Override
	public void initView() {
		clear();
		peopleCombo = UserGroupSearchBox.createUserGroupSearchSuggestBox(urlCache.getRepositoryServiceUrl(), synapseJSNIUtils.getBaseFileHandleUrl(), synapseJSNIUtils.getBaseProfileAttachmentUrl(), null);
		peopleCombo.setWidth(330);
		peopleCombo.setEmptyText("Enter name...");
		peopleCombo.setFieldLabel("Name");
		peopleCombo.setForceSelection(true);
		peopleCombo.setTriggerAction(TriggerAction.ALL);
		peopleCombo.addSelectionChangedListener(new SelectionChangedListener<ModelData>() {				
			@Override
			public void selectionChanged(SelectionChangedEvent<ModelData> se) {
			}
		});
		peopleCombo.addStyleName("margin-10");
		setWidget(peopleCombo);
	}
	@Override
	public void checkParams() throws IllegalArgumentException {
		if (!peopleCombo.isValid())
			throw new IllegalArgumentException(peopleCombo.getErrorMessage());
	}
	@Override
	public String getId() {
		ModelData selectedModel = peopleCombo.getValue();
		String principalId = (String) selectedModel.get(UserGroupSearchBox.KEY_PRINCIPAL_ID);
		return principalId;
	}
	
	@Override
	public void setId(String id) {
		//TODO: to allow editing existing badge
	}
	
	@Override
	public String isIndividual() {
		ModelData selectedModel = peopleCombo.getValue();
		Boolean isIndividual = (Boolean) selectedModel.get(UserGroupSearchBox.KEY_IS_INDIVIDUAL);
		return isIndividual.toString();
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
	public int getDisplayHeight() {
		return 40;
	}
	@Override
	public int getAdditionalWidth() {
		return 0;
	}
	
	/*
	 * Private Methods
	 */

}
