package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditor;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Second page of creating an access requirement (Terms Of Use or old ACT)  
 * @author Jay
 *
 */
public class CreateBasicAccessRequirementStep2 implements ModalPage, CreateBasicAccessRequirementStep2View.Presenter {
	CreateBasicAccessRequirementStep2View view;
	ModalPresenter modalPresenter;
	AccessRequirement accessRequirement;
	WikiMarkdownEditor wikiMarkdownEditor;
	WikiPageWidget wikiPageRenderer;
	WikiPageKey wikiKey;
	
	@Inject
	public CreateBasicAccessRequirementStep2(
			CreateBasicAccessRequirementStep2View view,
			WikiMarkdownEditor wikiMarkdownEditor,
			WikiPageWidget wikiPageRenderer
		) {
		super();
		this.view = view;
		this.wikiMarkdownEditor = wikiMarkdownEditor;
		this.wikiPageRenderer = wikiPageRenderer;
		
		wikiMarkdownEditor.setDeleteButtonVisible(false);
		view.setWikiPageRenderer(wikiPageRenderer.asWidget());
		view.setPresenter(this);
		wikiPageRenderer.setModifiedCreatedByHistoryVisible(false);
	}
	
	/**
	 * Configure this widget before use.
	 * 
	 */
	public void configure(AccessRequirement accessRequirement) {
		this.accessRequirement = accessRequirement;
		wikiKey = new WikiPageKey(accessRequirement.getId().toString(), ObjectType.ACCESS_REQUIREMENT.toString(), null);
		String oldTerms = GovernanceServiceHelper.getAccessRequirementText(accessRequirement);
		boolean isExistOldTermsOfUse = oldTerms != null;
		view.setOldTermsVisible(isExistOldTermsOfUse);
		view.setOldTerms(isExistOldTermsOfUse ? oldTerms : "");
				
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
		//can update wiki only
		modalPresenter.onFinished();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setModalPresenter(ModalPresenter modalPresenter) {
		this.modalPresenter = modalPresenter;
		modalPresenter.setPrimaryButtonText(DisplayConstants.FINISH);
	}


}
