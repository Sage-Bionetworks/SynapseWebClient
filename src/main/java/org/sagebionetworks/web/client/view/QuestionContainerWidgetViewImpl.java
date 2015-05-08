package org.sagebionetworks.web.client.view;


import java.util.HashSet;
import java.util.Set;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.sagebionetworks.web.client.place.Wiki;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class QuestionContainerWidgetViewImpl implements QuestionContainerWidgetView, IsWidget {
	
	@UiField
	FlowPanel questionContainer;
	
	@UiField
	Heading questionHeader;
	
	@UiField
	Anchor moreInfoLink;
		
	@UiField
	Icon successIcon;
	
	@UiField
	Icon failureIcon;
	
	Widget widget;
	
	Set<RadioButton> radioButtons;
	Set<CheckBox> checkBoxes;
		
	public interface Binder extends UiBinder<Widget, QuestionContainerWidgetViewImpl> {}
		
	@Inject
	public QuestionContainerWidgetViewImpl(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}
	
	@Override
	public void showSuccess(boolean isShown) {
		successIcon.setVisible(isShown);
	}
	
	@Override
	public void showFailure(boolean isShown) {
		failureIcon.setVisible(isShown);
	}
	
	@Override
	public void addAnswer(Widget answerContainer) {
		questionContainer.insert(answerContainer, questionContainer.getWidgetCount()-1);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void addStyleName(String style) {
		questionContainer.addStyleName(style);
	}
	
	@Override
	public void addRadioButton(Long questionIndex, String answerPrompt, ClickHandler clickHandler, boolean isSelected) {
		SimplePanel answerContainer = new SimplePanel();
		answerContainer.addStyleName("radio padding-left-30 control-label");
		RadioButton answerButton = new RadioButton("question-"+questionIndex);
		answerButton.setValue(isSelected);
		answerButton.setHTML(SimpleHtmlSanitizer.sanitizeHtml(answerPrompt));
		answerButton.addClickHandler(clickHandler);
		answerContainer.add(answerButton.asWidget());
		addAnswer(answerContainer.asWidget());
		radioButtons.add(answerButton);
	}
	
	@Override
	public void addCheckBox(Long questionIndex, String answerPrompt, ClickHandler clickHandler, boolean isSelected) {
		SimplePanel answerContainer = new SimplePanel();
		answerContainer.addStyleName("checkbox padding-left-30 control-label");
		final CheckBox checkbox= new CheckBox();
		checkbox.setValue(isSelected);
		checkbox.setHTML(SimpleHtmlSanitizer.sanitizeHtml(answerPrompt));
		checkbox.addClickHandler(clickHandler);
		answerContainer.add(checkbox);
		addAnswer(answerContainer);
		checkBoxes.add(checkbox);
	}
	
	@Override
	public void configureMoreInfo(String ownerObjectId, String wikiTypeName, String wikiPageId) {
		Wiki place = new Wiki(ownerObjectId, wikiTypeName, wikiPageId);
		moreInfoLink.setHref("#!Wiki:" + place.toToken());
	}
	
	
	@Override 
	public void configure(Long questionNumber, String questionPrompt) {
		questionHeader.add(new InlineHTML("<small class=\"margin-right-10\">"+questionNumber+".</small>"+SimpleHtmlSanitizer.sanitizeHtml(questionPrompt).asString()+"</small>"));
		radioButtons = new HashSet<RadioButton>();
		checkBoxes = new HashSet<CheckBox>();
	}
	
	@Override
	public void setIsEnabled(boolean isEnabled) {
		for (RadioButton radioButton: radioButtons) {
			radioButton.setEnabled(isEnabled);
		}
		for (CheckBox checkBox: checkBoxes) {
			checkBox.setEnabled(isEnabled);
		}
	}
	
}
