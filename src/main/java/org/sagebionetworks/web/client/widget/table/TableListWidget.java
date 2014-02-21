package org.sagebionetworks.web.client.widget.table;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableListWidget implements TableListWidgetView.Presenter, WidgetRendererPresenter {
	
	public interface ClickHandler {
		void onClick(TableEntity table);
	}

	private TableListWidgetView view;
	
	private ClickHandler tableClickHandler;
	
	@Inject
	public TableListWidget(TableListWidgetView view) {
		this.view = view;
		view.setPresenter(this);
	}	
	
	public void configure(String ownerId, boolean canEdit) {
		// TODO : get tables list from owner project id
		throw new RuntimeException("NYI");
	}
	
	public void configure(List<TableEntity> tables, boolean canEdit) {
		view.configure(tables, canEdit);
	}
    
	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();		
	}

	@Override
	public void configure(
			WikiPageKey wikiKey,
			Map<String, String> widgetDescriptor,
			org.sagebionetworks.web.client.utils.Callback widgetRefreshRequired,
			Long wikiVersionInView) {
		// TODO Auto-generated method stub
		
	}
	
	public void setOnTableClick(ClickHandler handler) {
		this.tableClickHandler = handler;
	}

	@Override
	public void tableClicked(TableEntity table) {
		if(tableClickHandler != null) {
			tableClickHandler.onClick (table);
		}
	}

}
