package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Basic implementation of the TableEntityWidgetView with zero business logic
 * 
 * @author John
 * 
 */
public class TableEntityWidgetViewImpl extends Composite implements
		TableEntityWidgetView {

	public interface Binder extends UiBinder<Widget, TableEntityWidgetViewImpl> {
	}

	@UiField
	Button columnDetailsToggleButton;
	@UiField
	PanelBody columnDetailsPanel;
	@UiField
	Alert tableMessage;
	@UiField
	SimplePanel queryInputPanel;
	@UiField
	SimplePanel queryResultsPanel;

	PortalGinInjector ginInjector;
	ColumnModelsWidget columnModelsWidget;
	Presenter presenter;

	@Inject
	public TableEntityWidgetViewImpl(final Binder uiBinder,
			PortalGinInjector ginInjector) {
		initWidget(uiBinder.createAndBindUi(this));
		this.ginInjector = ginInjector;
		this.columnModelsWidget = ginInjector.createNewColumnModelsWidget();
		this.columnDetailsPanel.add(this.columnModelsWidget.asWidget());
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void configure(EntityBundle bundle, boolean isEditable) {
		this.columnModelsWidget.configure(bundle, isEditable, this.presenter);
	}


	@Override
	public void setQueryResultsVisible(boolean visible) {
		this.queryResultsPanel.setVisible(visible);
	}

	@Override
	public void showTableMessage(AlertType type, String message) {
		this.tableMessage.setType(type);
		this.tableMessage.setText(message);
	}

	@Override
	public void setTableMessageVisible(boolean visible) {
		this.tableMessage.setVisible(visible);
	}

	@Override
	public void setProgressWidget(
			AsynchronousProgressWidget asynchProgressWidget) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setQueryProgressVisible(boolean isVisible) {
		this.queryResultsPanel.setVisible(isVisible);
	}

	@Override
	public void setQueryResultsWidget(TableQueryResultWidget queryResultsWidget) {
		this.queryResultsPanel.add(queryResultsWidget);
		
	}

	@Override
	public void setQueryInputWidget(QueryInputWidget queryInputWidget) {
		this.queryInputPanel.add(queryInputWidget);
		
	}

	@Override
	public void setQueryInputVisible(boolean visible) {
		this.queryInputPanel.setVisible(visible);
	}


}
