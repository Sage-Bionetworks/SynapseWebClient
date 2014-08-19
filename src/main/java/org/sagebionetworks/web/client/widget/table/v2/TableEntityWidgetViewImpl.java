package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonToolBar;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.ProgressBar;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Basic implementation of the TableEntityWidgetView with zero business logic
 * @author John
 *
 */
public class TableEntityWidgetViewImpl extends Composite implements TableEntityWidgetView {
	
	public interface Binder extends UiBinder<Widget, TableEntityWidgetViewImpl> {	}

	@UiField
	Button columnDetailsToggleButton;
	@UiField
	PanelBody columnDetailsPanel;
	@UiField
	Alert tableMessage;
	@UiField
	InputGroup queryInputGroup;
	@UiField
	TextBox queryInput;
	@UiField
	Button queryButton;
	@UiField
	Panel queryResultsPanel;
	@UiField
	ButtonToolBar resultsToolBar;
	@UiField
	Button editRowsButton;
	@UiField
	Button uploadCSVButton;
	@UiField
	Button downloadCSVButton;
	@UiField
	CellTable queryResults;
	@UiField
	Container progressContainer;
	@UiField
	Text progressText;
	@UiField
	ProgressBar progressBar;
	@UiField
	Button progressCancelButton;
	@UiField
	Modal editRowsModal;
	@UiField
	SimplePanel rowEditorModalPanel;
	@UiField
	Alert rowEditorAlert;
	@UiField
	Button saveRowsButton;
	@UiField
	Button cancelRowsButton;
	@UiField
	Alert queryResultAlert;
	
	PortalGinInjector ginInjector;
	ColumnModelsWidget columnModelsWidget;
	Presenter presenter;
	
	@Inject
	public TableEntityWidgetViewImpl(final Binder uiBinder, PortalGinInjector ginInjector){
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
	public void configure(EntityBundle bundle,
			boolean isEditable) {
		this.columnModelsWidget.configure(bundle, isEditable, this.presenter);
	}

	@Override
	public void setQueryInputVisible(boolean visible) {
		this.queryInputGroup.setVisible(visible);
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


}
