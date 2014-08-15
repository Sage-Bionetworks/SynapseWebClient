package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonToolBar;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
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
	InputGroup queryInputGroup;
	@UiField
	TextBox queryInput;
	@UiField
	Button queryButton;
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
	Modal editRowsModal;
	@UiField
	SimplePanel rowEditorModalPanel;
	@UiField
	Alert rowEditorAlert;
	@UiField
	Button saveRowsButton;
	@UiField
	Button cancelRowsButton;
	
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
	public void configure(String tableId, TableBundle tableBundel, boolean isEditable, String queryString) {
		this.columnModelsWidget.configure(tableId, tableBundel.getColumnModels(), isEditable);
		this.queryInput.setText(queryString);
	}

}
