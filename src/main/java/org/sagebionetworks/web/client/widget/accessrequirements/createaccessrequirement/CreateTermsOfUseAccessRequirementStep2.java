package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditor;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Second page of creating an access requirement (Terms Of Use)  
 * @author Jay
 *
 */
public class CreateTermsOfUseAccessRequirementStep2 implements ModalPage, CreateTermsOfUseAccessRequirementStep2View.Presenter {
	CreateTermsOfUseAccessRequirementStep2View view;
	ModalPresenter modalPresenter;
	TermsOfUseAccessRequirement accessRequirement;
	SynapseClientAsync synapseClient;
	WikiMarkdownEditor wikiMarkdownEditor;
	WikiPageWidget wikiPageRenderer;
	WikiPageKey wikiKey;
	
	@Inject
	public CreateTermsOfUseAccessRequirementStep2(
			CreateTermsOfUseAccessRequirementStep2View view,
			SynapseClientAsync synapseClient,
			WikiMarkdownEditor wikiMarkdownEditor,
			WikiPageWidget wikiPageRenderer
		) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		this.wikiMarkdownEditor = wikiMarkdownEditor;
		this.wikiPageRenderer = wikiPageRenderer;
		
		view.setWikiPageRenderer(wikiPageRenderer.asWidget());
		view.setPresenter(this);
		wikiPageRenderer.setModifiedCreatedByHistoryVisible(false);
	}
	
	/**
	 * Configure this widget before use.
	 * 
	 */
	public void configure(TermsOfUseAccessRequirement accessRequirement) {
		this.accessRequirement = accessRequirement;
		wikiKey = new WikiPageKey(accessRequirement.getId().toString(), ObjectType.ACCESS_REQUIREMENT.toString(), null);
		boolean isExistOldTermsOfUse = accessRequirement.getTermsOfUse() != null;
		view.setOldTermsVisible(isExistOldTermsOfUse);
		view.setOldTerms(isExistOldTermsOfUse ? accessRequirement.getTermsOfUse() : "");
				
		configureWiki();
	}
	
	@Override
	public void onEditWiki() {
		wikiMarkdownEditor.configure(wikiKey, new CallbackP<WikiPage>() {
			@Override
			public void invoke(WikiPage wikiPage) {
				configureWiki();
			}
		});
	}
	
	private void configureWiki() {
		wikiPageRenderer.configure(wikiKey, false, null, false);
	}
	
	@Override
	public void onPrimary() {
		// create/update access requirement
		if (accessRequirement.getTermsOfUse() != null) {
			accessRequirement.setTermsOfUse(null);
			modalPresenter.setLoading(true);
			synapseClient.createOrUpdateAccessRequirement(accessRequirement, new AsyncCallback<AccessRequirement>() {
				@Override
				public void onFailure(Throwable caught) {
					modalPresenter.setLoading(false);
					modalPresenter.setErrorMessage(caught.getMessage());
				}
				@Override
				public void onSuccess(AccessRequirement result) {
					modalPresenter.setLoading(false);
					modalPresenter.onFinished();
				}
			});	
		} else {
			modalPresenter.onFinished();
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setModalPresenter(ModalPresenter modalPresenter) {
		this.modalPresenter = modalPresenter;
		modalPresenter.setPrimaryButtonText(DisplayConstants.NEXT);
	}


}
