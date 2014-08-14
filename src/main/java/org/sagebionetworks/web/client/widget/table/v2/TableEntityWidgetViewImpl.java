package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
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
	SimplePanel columnDetailsPanel;
	
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
	public void configure(String tableId, TableBundle tableBundel, boolean isEditable) {
		this.columnModelsWidget.configure(tableId, tableBundel.getColumnModels(), isEditable);
	}

}
