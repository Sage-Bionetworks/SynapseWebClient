package org.sagebionetworks.web.client.widget.accessrequirements;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.repo.model.dataaccess.Submission;
import org.sagebionetworks.repo.model.dataaccess.SubmissionOrder;
import org.sagebionetworks.repo.model.dataaccess.SubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.SubmissionState;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.BigPromptModalView;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class IntendedDataUseReportButton implements IsWidget {
	public static final String IDU_MODAL_FIELD_NAME = "Markdown";
	public static final String IDU_MODAL_TITLE = "Approved Intended Data Use Statements";
	public static final String GENERATE_REPORT_BUTTON_TEXT = "Generate IDU Report";
	public Button button;
	public IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	RestrictableObjectDescriptor subject;
	AccessRequirement ar;
	DataAccessClientAsync dataAccessClient;
	PopupUtilsView popupUtils;
	BigPromptModalView copyTextModal;
	Map<String, Submission> researchProjectId2Submission;
	DateTimeUtils dateTimeUtils;

	@Inject
	public IntendedDataUseReportButton(Button button, IsACTMemberAsyncHandler isACTMemberAsyncHandler, DataAccessClientAsync dataAccessClient, PopupUtilsView popupUtils, BigPromptModalView copyTextModal, DateTimeUtils dateTimeUtils) {
		this.button = button;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		this.popupUtils = popupUtils;
		this.copyTextModal = copyTextModal;
		copyTextModal.addStyleToModal("modal-fullscreen");
		copyTextModal.setTextAreaHeight("450px");
		this.dataAccessClient = dataAccessClient;
		fixServiceEntryPoint(dataAccessClient);
		this.dateTimeUtils = dateTimeUtils;
		button.setVisible(false);
		button.addStyleName("margin-left-10");
		button.addClickHandler(event -> {
			researchProjectId2Submission = new HashMap<>();
			gatherAllSubmissions(null);
		});
	}

	public void gatherAllSubmissions(String nextPageToken) {
		dataAccessClient.getDataAccessSubmissions(ar.getId(), nextPageToken, SubmissionState.APPROVED, SubmissionOrder.CREATED_ON, true, new AsyncCallback<SubmissionPage>() {
			@Override
			public void onSuccess(SubmissionPage page) {
				List<Submission> newSubmissions = page.getResults();
				for (Submission submission : newSubmissions) {
					researchProjectId2Submission.put(submission.getResearchProjectSnapshot().getId(), submission);
				}
				if (!newSubmissions.isEmpty() && page.getNextPageToken() != null) {
					gatherAllSubmissions(page.getNextPageToken());
				} else {
					showIDUs();
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				popupUtils.showErrorMessage("Unable to get all submissions: " + caught.getMessage());
			}
		});
	}

	public void showIDUs() {
		StringBuilder sb = new StringBuilder();
		for (Submission submission : researchProjectId2Submission.values()) {
			ResearchProject rp = submission.getResearchProjectSnapshot();
			String projectLead = rp.getProjectLead();
			String currentInstitution = rp.getInstitution();
			String currentIDU = rp.getIntendedDataUseStatement();
			sb.append("\n**Researcher:** ");
			sb.append(projectLead);
			sb.append("\n**Affiliation:** ");
			sb.append(currentInstitution);
			String lastModifiedOn = dateTimeUtils.getDateString(submission.getModifiedOn());
			sb.append("\n**Intended Data Use Statement (accepted on " + lastModifiedOn + "):**\n");
			sb.append(currentIDU);
			sb.append("\n\n-------------\n\n");
		}
		copyTextModal.configure(IDU_MODAL_TITLE, IDU_MODAL_FIELD_NAME, sb.toString());
		copyTextModal.show();
	}

	public void configure(AccessRequirement ar) {
		button.setText(GENERATE_REPORT_BUTTON_TEXT);
		button.setSize(ButtonSize.DEFAULT);
		button.setType(ButtonType.DEFAULT);
		this.subject = null;
		this.ar = ar;
		showIfACTMember();
	}

	private void showIfACTMember() {
		isACTMemberAsyncHandler.isACTActionAvailable(new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isACTMember) {
				button.setVisible(isACTMember);
			}
		});
	}

	public Widget asWidget() {
		return button.asWidget();
	}
}
