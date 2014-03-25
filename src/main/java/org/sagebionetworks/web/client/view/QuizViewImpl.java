package org.sagebionetworks.web.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.place.Help;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class QuizViewImpl extends Composite implements QuizView {
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;

	@UiField
	HTMLPanel quizContainer;
	@UiField
	FlowPanel testContainer;
	@UiField
	Button submitButton;
	
	@UiField
	HTMLPanel successContainer;
	@UiField
	Button continueButton;
	@UiField
	HeadingElement userNameCertificate;
	
	@UiField
	HTMLPanel failureContainer;
	@UiField
	Button tutorialButton;

	
	
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private SageImageBundle sageImageBundle;
	private Window loadingWindow;
	private Header headerWidget;
	private Footer footerWidget;
	public interface Binder extends UiBinder<Widget, QuizViewImpl> {}
	boolean isSubmitInitialized;
	Map<String, String> userAnswers; 
	
	
	@Inject
	public QuizViewImpl(Binder uiBinder, IconsImageBundle icons,
			Header headerWidget, Footer footerWidget,
			SageImageBundle sageImageBundle, LoginWidget loginWidget) {
		initWidget(uiBinder.createAndBindUi(this));
		this.iconsImageBundle = icons;
		this.sageImageBundle = sageImageBundle;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		isSubmitInitialized = false;
		userAnswers = new HashMap<String, String>();
		
		tutorialButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new Help(WebConstants.USER_CERTIFICATION_TUTORIAL));
			}
		});
		
		continueButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.goToLastPlace();
			}
		});


	}

	@Override
	public void setPresenter(Presenter loginPresenter) {
		this.presenter = loginPresenter;
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		com.google.gwt.user.client.Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
		if(loadingWindow == null) {
			loadingWindow = DisplayUtils.createLoadingWindow(sageImageBundle, "");
		}
		loadingWindow.show();

	}


	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}


	@Override
	public void clear() {
		testContainer.clear();
		userAnswers.clear();
		hideLoading();
	}
	
	@Override
	public void showTest(Object questionsAndAnswers) {
		hideAll();
		//clear old questions
		clear();
		
		//add a question with answers
		List<String> answers = new ArrayList<String>();
		answers.add("42 m/s");
		answers.add("African or European?");
		answers.add("Huh? I... I don't know that!");
		testContainer.add(addQuestion("1", "What... is the air-speed velocity of an unladen swallow?", answers));
		
		answers = new ArrayList<String>();
		answers.add("Yes");
		answers.add("No");
		answers.add("42 m/s");
		testContainer.add(addQuestion("2", "Can I ask a rhetorical question? Well, can I?", answers));
		
		//initialize if necessary
		if (!isSubmitInitialized) {
			isSubmitInitialized = true;
			submitButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					//TODO: gather answers and pass them back to the presenter
					presenter.submitAnswers(userAnswers);
				}
			});
		}
		quizContainer.setVisible(true);
    }
	
	@Override
	public void showSuccess(UserProfile profile) {
		hideAll();
		userNameCertificate.setInnerHTML(DisplayUtils.getDisplayName(profile));
		successContainer.setVisible(true);
	}
	
	private FlowPanel addQuestion(String questionNumber, final String question, List<String> answers) {
		FlowPanel questionContainer = new FlowPanel();
		questionContainer.addStyleName("margin-bottom-40 margin-left-15");
		questionContainer.add(new HTMLPanel("<h5 class=\"inline-block\"><small>"+questionNumber+". </small>"+question+"</small></h5>"));
		//now add possible answers
		for (final String answer : answers) {
			SimplePanel answerContainer = new SimplePanel();
			answerContainer.addStyleName("radio margin-left-15");
			RadioButton answerButton = new RadioButton("question-"+questionNumber, answer);
			answerButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					userAnswers.put(question, answer);
				}
			});
			answerContainer.add(answerButton);
			questionContainer.add(answerContainer);
		}
		return questionContainer;
	}
	
	
	private void hideAll() {
		quizContainer.setVisible(false);
		successContainer.setVisible(false);
		failureContainer.setVisible(false);
	}

	@Override
	public void hideLoading() {
		if(loadingWindow != null) loadingWindow.hide();
	}
}
