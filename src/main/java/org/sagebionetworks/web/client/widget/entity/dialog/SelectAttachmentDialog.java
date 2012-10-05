package org.sagebionetworks.web.client.widget.entity.dialog;

import java.util.List;

import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.web.client.SynapseClientAsync;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * Select an attachment.
 */
public class SelectAttachmentDialog {
	
	public interface Callback {
		/**
		 * When the user selects ok this will be called.
		 * 
		 */
		public void onSelectAttachment(AttachmentData result);
	}

	/**
	 * Show the file attachment dialog
	 * 
	 * @param callback
	 */
	public static void showSelectAttachmentDialog(String baseUrl, String entityId, List<AttachmentData> attachments, SynapseClientAsync synapseClient, String windowTitle, String buttonText, final Callback callback ) {
		final Dialog dialog = new Dialog();
		dialog.setMaximizable(false);
		dialog.setSize(400, 175);
		dialog.setPlain(true);
		dialog.setModal(true);
		dialog.setBlinkModal(true);
		dialog.setButtons(Dialog.OKCANCEL);
		dialog.setHideOnButtonClick(true);
		dialog.setHeading(windowTitle);
		dialog.setLayout(new FitLayout());
		dialog.setBorders(false);
		
		final VisualAttachmentsList attachmentList = new VisualAttachmentsList(new VisualAttachmentsListViewImpl(), synapseClient);
		attachmentList.configure(baseUrl, entityId, attachments);
		Button okButton = dialog.getButtonById("ok");
		okButton.addSelectionListener(new SelectionListener<ButtonEvent>() {			
			@Override
			public void componentSelected(ButtonEvent ce) {
				callback.onSelectAttachment(attachmentList.getSelectedAttachment());
			}
		});
		ScrollPanel wrapper = new ScrollPanel(attachmentList.asWidget());
		dialog.add(wrapper);
		dialog.show();

	}
}
