package org.sagebionetworks.web.client.widget.entity.dialog;

import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.Attachments;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * Select an attachment.
 */
public class SelectAttachmentDialog {
	
	/**
	 * Show the file attachment dialog
	 * 
	 * @param callback
	 */
	public static Dialog showAttachmentsManagerDialog(String baseUrl, EntityBundle bundle, Attachments attachmentsWidget, String windowTitle) {
		final Dialog dialog = new Dialog();
		dialog.setMaximizable(false);
		dialog.setSize(285, 230);
		dialog.setPlain(true);
		dialog.setModal(true);
		dialog.setBlinkModal(true);
		dialog.setButtons(Dialog.OK);
		dialog.setHideOnButtonClick(true);
		dialog.setHeading(windowTitle);
		dialog.setLayout(new FitLayout());
		dialog.setBorders(false);

		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
        LayoutContainer c = new LayoutContainer();
        HBoxLayout layout = new HBoxLayout();
        layout.setPadding(new Padding(5));
        layout.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);
        c.setLayout(layout);

        HBoxLayoutData flex = new HBoxLayoutData(new Margins(0, 5, 0, 0));
        flex.setFlex(1);

//        final String baseURl = GWT.getModuleBaseURL()+"attachment";
//        final String actionUrl =  baseURl+ "?" + DisplayUtils.ENTITY_PARAM_KEY + "=" + bundle.getEntity().getId();
//
//        Anchor addBtn = new Anchor();
//        addBtn.setHTML(DisplayUtils.getIconHtml(iconsImageBundle.add16()));
//        addBtn.addClickHandler(new ClickHandler() {
//			@Override
//			public void onClick(ClickEvent event) {
//				AddAttachmentDialog.showAddAttachmentDialog(actionUrl,sageImageBundle,DisplayConstants.ATTACHMENT_DIALOG_WINDOW_TITLE, DisplayConstants.ATTACHMENT_DIALOG_BUTTON_TEXT,new AddAttachmentDialog.Callback() {
//					@Override
//					public void onSaveAttachment(UploadResult result) {
//						if(result != null){
//							if(UploadStatus.SUCCESS == result.getUploadStatus()){
//								DisplayUtils.showInfo(DisplayConstants.TEXT_ATTACHMENT_SUCCESS, "");
//							}else{
//								DisplayUtils.showErrorMessage(DisplayConstants.ERRROR_ATTACHMENT_FAILED+result.getMessage());
//							}
//						}
//						bus.fireEvent(new EntityUpdatedEvent());
//					}
//				});
//			}
//		});
//        c.add(addBtn, new HBoxLayoutData(new Margins(0)));
        lc.add(attachmentsWidget.asWidget());
//        lc.add(c);
        lc.layout();
		
        ScrollPanel scrollWrapper = new ScrollPanel();
        scrollWrapper.add(lc);
        
		dialog.add(scrollWrapper);
		dialog.show();
		return dialog;
	}
}
