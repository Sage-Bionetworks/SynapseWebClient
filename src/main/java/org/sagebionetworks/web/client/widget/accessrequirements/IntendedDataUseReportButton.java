package org.sagebionetworks.web.client.widget.accessrequirements;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.BigPromptModalView;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class IntendedDataUseReportButton implements IsWidget {
	public static final String IDU_MODAL_FIELD_NAME = "Markdown";
	public static final String IDU_MODAL_TITLE = "Approved Intended Data Use Statements";
	public static final String GENERATE_REPORT_BUTTON_TEXT = "Generate IDU Report";
	public Button button;
	public IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	AccessRequirement ar;
	BigPromptModalView copyTextModal;
	IntendedDataUseGenerator iduGenerator;
	
	@Inject
	public IntendedDataUseReportButton(Button button, IsACTMemberAsyncHandler isACTMemberAsyncHandler, BigPromptModalView copyTextModal, IntendedDataUseGenerator iduGenerator) {
		this.button = button;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		this.iduGenerator = iduGenerator;
		this.copyTextModal = copyTextModal;
		copyTextModal.addStyleToModal("modal-fullscreen");
		copyTextModal.setTextAreaHeight("450px");
		button.setVisible(false);
		button.addStyleName("margin-left-10");
		CallbackP<String> mdCallback = md -> {
			showIDUs(md);
		};
		button.addClickHandler(event -> {
			iduGenerator.gatherAllSubmissions(ar.getId().toString(), mdCallback);
		});
	}

	public void showIDUs(String md) {
		copyTextModal.configure(IDU_MODAL_TITLE, IDU_MODAL_FIELD_NAME, md.toString());
		copyTextModal.show();
	}

	public void configure(AccessRequirement ar) {
		button.setText(GENERATE_REPORT_BUTTON_TEXT);
		button.setSize(ButtonSize.DEFAULT);
		button.setType(ButtonType.DEFAULT);
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
