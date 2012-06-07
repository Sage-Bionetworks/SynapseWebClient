package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.widget.login.AcceptTermsOfUseCallback;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;

public class TermsOfUseHelper {

	public static void showTermsOfUse(String content, final AcceptTermsOfUseCallback callback) {
        final Dialog window = new Dialog();
        window.setMaximizable(false);
        window.setSize(600, 700);
        window.setPlain(true); 
        window.setModal(true); 
        window.setBlinkModal(true); 
        window.setHeading("Synapse Terms of Use"); 
        window.setLayout(new FitLayout());
        window.setScrollMode(Scroll.ALWAYS);
        window.okText = "I Agree";
        window.setButtons(Dialog.OKCANCEL);
        window.setHideOnButtonClick(true);
        window.add(new HTML(SafeHtmlUtils.fromSafeConstant(content)));
        // List for the button selection
        Button saveButton = window.getButtonById(Dialog.OK);
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
            	if (callback!=null) callback.accepted();
            }
        });
        // show the window
        window.show();		
	}
}
