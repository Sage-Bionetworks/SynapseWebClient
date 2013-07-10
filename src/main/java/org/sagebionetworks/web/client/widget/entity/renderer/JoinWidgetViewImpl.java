package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.evaluation.model.UserEvaluationState;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.presenter.ProfileFormWidget;
import org.sagebionetworks.web.client.presenter.ProfileFormWidget.ProfileUpdatedCallback;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class JoinWidgetViewImpl extends LayoutContainer implements JoinWidgetView {

	public static final String SUBMISSION_GUIDE_STEP_3_PAGE_ID = "56913";
	public static final String SUBMISSION_GUIDE_STEP_2_PAGE_ID = "56912";
	public static final String SUBMISSION_GUIDE_STEP_1_PAGE_ID = "56911";
	public static final String SUBMISSION_GUIDE_ID = "syn1968135";
	private Presenter presenter;
	private ProfileFormWidget profileForm;
	private MarkdownWidget step1, step2, step3;
	@Inject
	public JoinWidgetViewImpl(ProfileFormWidget profileForm, MarkdownWidget step1,  MarkdownWidget step2,  MarkdownWidget step3) {
		this.profileForm = profileForm;
		this.step1 = step1;
		this.step2 = step2;
		this.step3 = step3;
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
				megaButton.setStyleAttribute("float", "none;");
				
				Anchor applyForChallengeLink = new Anchor();
				applyForChallengeLink.setText("Join!");
				applyForChallengeLink.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						presenter.register();
					}
				});
				megaButton.add(applyForChallengeLink);
				SimplePanel wrapper = new SimplePanel();
				wrapper.add(megaButton);
				add(wrapper);
			}
			else if (UserEvaluationState.EVAL_OPEN_USER_REGISTERED.equals(state)) {
				//add info on how to get started!
				FlowPanel p = new FlowPanel();
				p.addStyleName("mainPageHeader inline-block");
				p.add(new HTML(DisplayConstants.JOINED_EVALUATION_HTML));
				
				Button button = new Button("Submit To Challenge");
				button.removeStyleName("gwt-Button");
				button.addStyleName("btn btn-large");
				button.addClickHandler(new ClickHandler() {			
					@Override
					public void onClick(ClickEvent event) {
						presenter.submitToChallengeClicked();
					}
				});
				p.add(button);
				add(p);
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
	public void showAnonymousRegistrationMessage() {
		MessageBox box = new MessageBox();
	    box.setButtons(MessageBox.OK);
	    box.setIcon(MessageBox.INFO);
	    box.setTitle("Login or Register");
	    box.addCallback(new Listener<MessageBoxEvent>() {
			@Override
			public void handleEvent(MessageBoxEvent be) {
				presenter.gotoLoginPage();
			}
		});
	    box.setMinWidth(320);
	    box.setMessage(DisplayConstants.ANONYMOUS_JOIN_EVALUATION);
	    box.show();
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
        com.extjs.gxt.ui.client.widget.button.Button touButton = dialog.getButtonById(Dialog.OK);
        touButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				touAcceptanceCallback.invoke();
			}
        });
        dialog.setHideOnButtonClick(true);		
		dialog.show();		
	}
	
	public void showProfileForm(UserProfile profile, final AsyncCallback<Void> presenterCallback) {
		
		profileForm.hideCancelButton();
		final Window dialog = new Window();
		
		//hide the dialog when something happens, and call back to the presenter
		ProfileUpdatedCallback profileUpdatedCallback = new ProfileUpdatedCallback() {
			
			@Override
			public void profileUpdateSuccess() {
				hideAndContinue();
			}
			
			@Override
			public void profileUpdateCancelled() {
				hideAndContinue();
			}
			
			public void hideAndContinue() {
				dialog.hide();
				presenterCallback.onSuccess(null);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				dialog.hide();
				presenterCallback.onFailure(caught);
			}
		};
		
		profileForm.configure(profile, profileUpdatedCallback);
     	dialog.setMaximizable(false);
        dialog.setSize(640, 480);
        dialog.setPlain(true); 
        dialog.setModal(true); 
        dialog.setAutoHeight(true);
        dialog.setResizable(false);
        dialog.add(profileForm.asWidget());
 		dialog.setHeading("About You");
 		
		dialog.show();
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void clear() {
	}

	@Override
	public void showSubmissionUserGuide() {
		WikiPageKey wikiKey = new WikiPageKey(SUBMISSION_GUIDE_ID, ObjectType.ENTITY.toString(), SUBMISSION_GUIDE_STEP_1_PAGE_ID);
		step1.loadMarkdownFromWikiPage(wikiKey, true);
		
		wikiKey = new WikiPageKey(SUBMISSION_GUIDE_ID, ObjectType.ENTITY.toString(), SUBMISSION_GUIDE_STEP_2_PAGE_ID);
		step2.loadMarkdownFromWikiPage(wikiKey, true);
		
		wikiKey = new WikiPageKey(SUBMISSION_GUIDE_ID, ObjectType.ENTITY.toString(), SUBMISSION_GUIDE_STEP_3_PAGE_ID);
		step3.loadMarkdownFromWikiPage(wikiKey, true);
		
		final Dialog dialog = new Dialog();
       	dialog.setMaximizable(false);
        dialog.setPlain(true); 
        dialog.setModal(true);
        dialog.setWidth(900);
        //dialog.setAutoWidth(true);
        dialog.setAutoHeight(true);
        dialog.setResizable(false);
        dialog.okText = "Next";
        dialog.cancelText = "Skip Tutorial";
        dialog.setButtons(Dialog.OKCANCEL);
        dialog.add(wrap(step1.asWidget()));
 		dialog.setHeading("Step 1: Create a Project");
 		
        final com.extjs.gxt.ui.client.widget.button.Button button = dialog.getButtonById(Dialog.OK);
        final com.extjs.gxt.ui.client.widget.button.Button cancelButton = dialog.getButtonById(Dialog.CANCEL);
        cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				//if the user cancels, then go straight to the submit entity dialog
				dialog.hide();
				presenter.submissionUserGuideSkipped();
			}
        });
        button.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				dialog.removeAll();
				dialog.add(wrap(step2.asWidget()));
				dialog.setHeading("Step 2: Upload Your Work");
				dialog.layout(true);
				button.removeAllListeners();
				button.addSelectionListener(new SelectionListener<ButtonEvent>(){
					@Override
					public void componentSelected(ButtonEvent ce) {
						dialog.removeAll();
						dialog.add(wrap(step3.asWidget()));
						dialog.setHeading("Step 3: Submit an Entry For Scoring");
						button.removeAllListeners();
						button.setText("OK");
						dialog.layout(true);
						button.addSelectionListener(new SelectionListener<ButtonEvent>(){
							@Override
							public void componentSelected(ButtonEvent ce) {
								dialog.hide();
							}
				        });
					}
		        });
		        
			}
        });
		dialog.show();		
	}
	
	public Widget wrap(Widget widget) {
		widget.addStyleName("margin-10");
		return widget;
	}
	
	/*
	 * Private Methods
	 */

}
