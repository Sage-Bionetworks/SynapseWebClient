package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.HasAccessorRequirement;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditor;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Second page of creating an access requirement (Terms Of Use or old ACT)
 * 
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
	SynapseClientAsync synapseClient;
	SynapseAlert synAlert;
	PopupUtilsView popupUtils;

	@Inject
	public CreateBasicAccessRequirementStep2(CreateBasicAccessRequirementStep2View view, WikiMarkdownEditor wikiMarkdownEditor, WikiPageWidget wikiPageRenderer, SynapseClientAsync synapseClient, SynapseAlert synAlert, PopupUtilsView popupUtils) {
		super();
		this.view = view;
		this.wikiMarkdownEditor = wikiMarkdownEditor;
		this.wikiPageRenderer = wikiPageRenderer;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.synAlert = synAlert;
		this.popupUtils = popupUtils;
		wikiMarkdownEditor.setDeleteButtonVisible(false);
		view.setWikiPageRenderer(wikiPageRenderer.asWidget());
		view.setPresenter(this);
		view.setSynAlert(synAlert);
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
		boolean isExistOldTermsOfUse = oldTerms != null && oldTerms.length() > 0;
		view.setOldTermsVisible(isExistOldTermsOfUse);
		view.setOldTerms(isExistOldTermsOfUse ? oldTerms : "");
		view.setHasAccessorRequirementUIVisible(accessRequirement instanceof HasAccessorRequirement);
		if (accessRequirement instanceof HasAccessorRequirement) {
			HasAccessorRequirement hasAccessorRequirement = (HasAccessorRequirement) accessRequirement;
			view.setIsCertifiedUserRequired(hasAccessorRequirement.getIsCertifiedUserRequired());
			view.setIsValidatedProfileRequired(hasAccessorRequirement.getIsValidatedProfileRequired());
		}
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

	@Override
	public void onClearOldInstructions() {
		popupUtils.showConfirmDialog("Are you sure?", "Deleting the old instructions cannot be undone.  Continue?", new Callback() {
			@Override
			public void invoke() {
				onClearOldInstructionsAfterConfirm();
			}
		});

	}

	public void onClearOldInstructionsAfterConfirm() {
		if (accessRequirement instanceof TermsOfUseAccessRequirement) {
			((TermsOfUseAccessRequirement) accessRequirement).setTermsOfUse(null);
		} else if (accessRequirement instanceof ACTAccessRequirement) {
			((ACTAccessRequirement) accessRequirement).setActContactInfo(null);
		}
		synAlert.clear();
		synapseClient.createOrUpdateAccessRequirement(accessRequirement, new AsyncCallback<AccessRequirement>() {
			@Override
			public void onSuccess(AccessRequirement ar) {
				configure(ar);
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	private void configureWiki() {
		wikiPageRenderer.configure(wikiKey, false, null);
	}

	@Override
	public void onPrimary() {
		if (accessRequirement instanceof HasAccessorRequirement) {
			HasAccessorRequirement hasAccessorRequirement = (HasAccessorRequirement) accessRequirement;
			// update AR
			hasAccessorRequirement.setIsCertifiedUserRequired(view.isCertifiedUserRequired());
			hasAccessorRequirement.setIsValidatedProfileRequired(view.isValidatedProfileRequired());
			synapseClient.createOrUpdateAccessRequirement(accessRequirement, new AsyncCallback<AccessRequirement>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}

				@Override
				public void onSuccess(AccessRequirement result) {
					modalPresenter.onFinished();
				}
			});
		} else {
			// can update wiki only
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
		modalPresenter.setPrimaryButtonText(DisplayConstants.FINISH);
	}


}
