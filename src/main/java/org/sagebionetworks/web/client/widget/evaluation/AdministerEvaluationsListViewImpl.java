package org.sagebionetworks.web.client.widget.evaluation;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.EvaluationCardProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationRowWidget.EvaluationActionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AdministerEvaluationsListViewImpl implements AdministerEvaluationsListView {
	public interface Binder extends UiBinder<Widget, AdministerEvaluationsListViewImpl> {
	}

	private EvaluationActionHandler presenter;
	private SynapseContextPropsProvider contextPropsProvider;
	@UiField
	Div rows;
	@UiField
	Div widgetsContainer;
	PortalGinInjector ginInjector;
	Widget widget;

	@Inject
	public AdministerEvaluationsListViewImpl(Binder binder, PortalGinInjector ginInjector, final SynapseContextPropsProvider contextPropsProvider) {
		this.ginInjector = ginInjector;
		widget = binder.createAndBindUi(this);
		this.contextPropsProvider = contextPropsProvider;
	}

	@Override
	public void addRow(Evaluation evaluation) {
		EvaluationRowWidget newRow = ginInjector.getEvaluationRowWidget();
		newRow.configure(evaluation, presenter);
		rows.add(newRow.asWidget());
	}


	@Override
	public void setPresenter(EvaluationActionHandler presenter) {
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

	@Override
	public void addReactComponent(Evaluation evaluation, EvaluationCardProps props) {
		ReactComponentDiv container = new ReactComponentDiv();
		container.setMarginTop(50);
		rows.add(container);

		ReactElement element = React.createElementWithSynapseContext(SRC.SynapseComponents.EvaluationCard, props, contextPropsProvider.getJsInteropContextProps());
		ReactDOM.render(element, container.getElement());
	}
}
