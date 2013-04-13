package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.widget.login.AcceptTermsOfUseCallback;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;

public class TermsOfUseHelper {

	private static final MarginData spacer = new MarginData(0, 15, 15, 15);
	
	public static void showTermsOfUse(String content, final AcceptTermsOfUseCallback callback) {
        final Dialog window = new Dialog();
        window.setMaximizable(false);
        window.setWidth((int)(Window.getClientWidth() * .9));
        window.setHeight((int)(Window.getClientHeight() * .9));
        window.setPlain(true); 
        window.setModal(true); 
        window.setHeading("Synapse Terms of Use"); 
        window.setLayout(new FlowLayout());
        window.setScrollMode(Scroll.AUTO);
        window.okText = "I Agree";
        window.setButtons(Dialog.OKCANCEL);
        window.setHideOnButtonClick(true);
        LayoutContainer lc = new LayoutContainer();
        lc.addStyleName("whiteBackground");
        lc.add(new HTML(SafeHtmlUtils.fromSafeConstant(content)), spacer);
        // List for the button selection
        Button saveButton = window.getButtonById(Dialog.OK);
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
            	if (callback!=null) callback.accepted();
            }
        });
        window.add(lc);
        // show the window
        window.show();		
	}
}
