package org.sagebionetworks.web.client.view;


import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.ModalSize;
import org.gwtbootstrap3.client.ui.html.Small;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.widget.modal.Dialog;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
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
	
	public interface Binder extends UiBinder<Widget, QuestionContainerWidgetViewImpl> {}
		
	@Inject
	public QuestionContainerWidgetViewImpl(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}
	
	@Override
	public void setQuestionHeader(Widget questionHeader) {
		this.questionHeader.add(questionHeader);
	}
	
	@Override
	public void configureMoreInfo(String href) {
		moreInfoLink.setHref(href);
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

}
