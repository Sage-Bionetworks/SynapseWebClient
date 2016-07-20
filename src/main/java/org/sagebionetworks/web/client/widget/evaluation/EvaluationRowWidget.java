package org.sagebionetworks.web.client.widget.evaluation;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.evaluation.model.Evaluation;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;


public class EvaluationRowWidget implements IsWidget {
	@UiField
	Text evaluationNameText;
	@UiField
	Button shareButton;
	@UiField
	Button editButton;
	Widget widget;
	
	public interface Binder extends UiBinder<Widget, EvaluationRowWidget> {}
	private static Binder uiBinder = GWT.create(Binder.class);
	private Evaluation evaluation;
	private EvaluationActionHandler handler;
	
	
	public interface EvaluationActionHandler {
		void onEditClicked(Evaluation evaluation);
		void onShareClicked(Evaluation evaluation);
	}
	
	public EvaluationRowWidget() {
		widget = uiBinder.createAndBindUi(this);
		shareButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handler.onShareClicked(evaluation);
			}
		});
		editButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handler.onEditClicked(evaluation);
			}
		});
	}
	
	public void configure(Evaluation evaluation, EvaluationActionHandler handler) {
		this.evaluation = evaluation;
		this.handler = handler;
		evaluationNameText.setText(evaluation.getName());
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
}
