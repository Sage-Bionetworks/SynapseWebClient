package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.evaluation.model.UserEvaluationState;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class JoinWidgetViewImpl extends LayoutContainer implements JoinWidgetView {

	private Presenter presenter;
	
	@Inject
	public JoinWidgetViewImpl() {
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, UserEvaluationState state) {
		this.removeAll();
		
		if (!UserEvaluationState.EVAL_REGISTRATION_UNAVAILABLE.equals(state)) {
			//show a register/unregister button
			if (UserEvaluationState.EVAL_OPEN_USER_NOT_REGISTERED.equals(state)) {
				LayoutContainer megaButton = new LayoutContainer();
				megaButton.setStyleName("mega-button");
				megaButton.setStyleAttribute("margin-top", "10px;");
				megaButton.setStyleAttribute("float", "left;");
				
				Anchor applyForChallengeLink = new Anchor();
				applyForChallengeLink.setText("Join!");
				applyForChallengeLink.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						presenter.register();
					}
				});
				megaButton.add(applyForChallengeLink);
				add(megaButton);
			}
		}
		
		this.layout(true);
	}
	
	@Override
	public void showError(String error) {
		add(new HTMLPanel(DisplayUtils.getMarkdownWidgetWarningHtml(error)));
		layout(true);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	@Override
	public void showAccessRequirement(
			String arText,
			final Callback touAcceptanceCallback) {
		final Dialog dialog = new Dialog();
       	dialog.setMaximizable(false);
        dialog.setSize(640, 480);
        dialog.setPlain(true); 
        dialog.setModal(true); 
        dialog.setAutoHeight(true);
        dialog.setResizable(false);
        ScrollPanel panel = new ScrollPanel(new HTML(arText));
        panel.addStyleName("margin-top-left-10");
        panel.setSize("605px", "450px");
        dialog.add(panel);
 		dialog.setHeading("Terms of Use");
		// agree to TOU, cancel
        dialog.okText = DisplayConstants.BUTTON_TEXT_ACCEPT_TERMS_OF_USE;
        dialog.setButtons(Dialog.OKCANCEL);
        Button touButton = dialog.getButtonById(Dialog.OK);
        touButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				touAcceptanceCallback.invoke();
			}
        });
        dialog.setHideOnButtonClick(true);		
		dialog.show();		
	}

	/*
	 * Private Methods
	 */

}
