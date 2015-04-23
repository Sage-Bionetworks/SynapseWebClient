package org.sagebionetworks.web.client.widget.entity.registration;


import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.ModalSize;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
import org.gwtbootstrap3.client.ui.html.Small;
import org.sagebionetworks.web.client.widget.modal.Dialog;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class QuestionContainerWidgetViewImpl extends Composite implements QuestionContainerWidgetView {
	
	@UiField
	FlowPanel questionContainer;
	
	@UiField
	Small questionNumber;
	
	@UiField
	Anchor moreInfoPanel;
	
	private Presenter presenter;
	
	@Inject
	public QuestionContainerWidgetViewImpl() {
	}
	
	@Override
	public void addAnswer(Widget answerContainer) {
		questionContainer.add(answerContainer);
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
	public void addStyleName(String style) {
		questionContainer.addStyleName(style);
	}

}
