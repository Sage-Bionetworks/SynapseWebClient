package org.sagebionetworks.web.client.widget.entity.dialog;

import org.sagebionetworks.web.client.DisplayConstants;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;

public class DeleteConfirmDialog {

	public interface Callback {
		/**
		 * When the user selects OK this will be called.
		 */
		public void onAccept();		
	}

	public static void showDialog(final Callback callback) {
		 Dialog d = new Dialog();
		 d.setHeading(DisplayConstants.LABEL_DELETE);
		 d.addText(DisplayConstants.PROMPT_SURE_DELETE + "?");
		 d.setBodyStyle("fontWeight:bold;padding:13px;");
		 d.setSize(300, 100);
		 d.setHideOnButtonClick(true);
		 d.setButtons(Dialog.OKCANCEL);
		 d.setModal(true);
		 d.show();
		 
		 Button okButton = d.getButtonById("ok");
		 okButton.addSelectionListener(new SelectionListener<ButtonEvent>() {			
			@Override
			public void componentSelected(ButtonEvent ce) {
				callback.onAccept();
			}
		});
		 
		 
	}

	
}
