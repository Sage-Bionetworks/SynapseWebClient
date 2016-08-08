package org.sagebionetworks.web.client.widget.evaluation;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitterViewImpl.Binder;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AdministerEvaluationsListViewImpl implements AdministerEvaluationsListView {
	public interface Binder extends UiBinder<Widget, AdministerEvaluationsListViewImpl> {}
	
	private Presenter presenter;
	@UiField
	Div rows;
	@UiField
	Div widgetsContainer;
	@UiField
	Button newEvaluationButton;
	
	Widget widget;
	@Inject
	public AdministerEvaluationsListViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		newEvaluationButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onNewEvaluationClicked();
			}
		});
	}
	
	@Override
	public void addRow(Evaluation evaluation) {
		EvaluationRowWidget newRow = new EvaluationRowWidget();
		newRow.configure(evaluation, presenter);
		rows.add(newRow.asWidget());
	}
	
	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	@Override
	public void clearRows() {
		rows.clear();
	}
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void add(IsWidget w) {
		widgetsContainer.add(w);
	}
}
