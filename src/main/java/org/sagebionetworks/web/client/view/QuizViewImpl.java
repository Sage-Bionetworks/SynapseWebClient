package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Heading;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.download.CertificateWidget;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
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
	DivElement quizHeader;
	
	@UiField
	DivElement quizSuccessUI;
	
	@UiField
	DivElement quizFailureUI;
	
	@UiField
	Heading failureScoreContainer;
	
	@UiField
	Heading successScoreContainer; 
	
	@UiField
	FlowPanel testContainer;
	@UiField
	Button submitButton;
	
	@UiField
	SimplePanel successContainer;
	
	@UiField
	Button tutorialButton1;
	
	@UiField
	Anchor tryAgainLink;
	
	@UiField
	SpanElement loadingUI;
	
	private Presenter presenter;
	private CertificateWidget certificateWidget;
	private Header headerWidget;
	private Footer footerWidget;
	public interface Binder extends UiBinder<Widget, QuizViewImpl> {}
	boolean isSubmitInitialized;
	
	@Inject
	public QuizViewImpl(Binder uiBinder,
			Header headerWidget, 
			Footer footerWidget,
			SageImageBundle sageImageBundle, 
			LoginWidget loginWidget, 
			CertificateWidget certificateWidget,
			PortalGinInjector ginInjector) {
		initWidget(uiBinder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.certificateWidget = certificateWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		successContainer.setWidget(certificateWidget.asWidget());
		
		isSubmitInitialized = false;
		ClickHandler gotoGettingStartedNewWindow = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(DisplayUtils.getHelpPlaceHistoryToken(WebConstants.GETTING_STARTED), "", "");
			}
		};
		
		tutorialButton1.addClickHandler(gotoGettingStartedNewWindow);
		quizHeader.setInnerHTML("<h3>Certification Quiz</h3>");
		
		tryAgainLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new org.sagebionetworks.web.client.place.Quiz(WebConstants.CERTIFICATION));
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
		DisplayUtils.show(loadingUI);
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void reset() {
		quizContainer.setVisible(true);
		submitButton.setVisible(true);
		submitButton.setEnabled(true);
	}
	

	@Override
	public void clear() {
		hideAll();
		testContainer.clear();
		hideLoading();
	}
	
	@Override
	public void setQuizHeader(String quizHeader) {
		this.quizHeader.setInnerHTML(SimpleHtmlSanitizer.sanitizeHtml(quizHeader).asString());
	}
	
	@Override
	public void addQuestionContainerWidget(Widget widget) {
		testContainer.add(widget);
	}
	
	@Override
	public void addSubmitHandler(ClickHandler handler) { 
		submitButton.addClickHandler(handler);
	}
	
	@Override
	public void setSubmitEnabled(boolean isEnabled) {
		submitButton.setEnabled(isEnabled);
	}
	
	@Override
	public void setSubmitVisible(boolean isVisible) {
		submitButton.setVisible(isVisible);
	}
	
	@Override
	public void showScore(String scoreContainerText) {
		successScoreContainer.setText(scoreContainerText);
		failureScoreContainer.setText(scoreContainerText);
	}
	
	@Override
	public void showSuccess(UserProfile profile, PassingRecord passingRecord) {
		hideAll();
		DisplayUtils.show(quizSuccessUI);
		DisplayUtils.hide(quizFailureUI);
		certificateWidget.configure(profile, passingRecord);
		successContainer.setVisible(true);
		quizContainer.setVisible(true);
		DisplayUtils.scrollToTop();
	}
	
	@Override
	public void showFailure(PassingRecord passingRecord) {
		hideAll();
		//show failure message and quiz
		DisplayUtils.hide(quizSuccessUI);
		DisplayUtils.show(quizFailureUI);
		quizFailureUI.scrollIntoView();
		quizContainer.setVisible(true);
	}
	
	@Override
	public void hideAll() {
		quizContainer.setVisible(false);
		successContainer.setVisible(false);
		DisplayUtils.hide(quizFailureUI);
		DisplayUtils.hide(quizSuccessUI);
	}

	@Override
	public void hideLoading() {
		DisplayUtils.hide(loadingUI);
	}
}
