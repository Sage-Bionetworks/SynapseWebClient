package org.sagebionetworks.web.client.widget.table.modal.fileview;

import static com.google.common.util.concurrent.Futures.whenAllComplete;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseFutureClient;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;

public class ViewDefaultColumns {
	private SynapseFutureClient futureClient;
	private List<ColumnModel> defaultFileViewColumns, 
		defaultProjectViewColumns;
		

	private AdapterFactory adapterFactory;
	PopupUtilsView popupUtils;

	@Inject
	public ViewDefaultColumns(SynapseFutureClient futureClient, AdapterFactory adapterFactory,
			PopupUtilsView popupUtils) {
		this.futureClient = futureClient;
		this.adapterFactory = adapterFactory;
		this.popupUtils = popupUtils;
		init();
	}

	public void init() {
		ListenableFuture<List<ColumnModel>> fileViewColumnsFuture = futureClient.getDefaultColumnsForView(ViewType.file);
		ListenableFuture<List<ColumnModel>> projectViewColumnsFuture = futureClient.getDefaultColumnsForView(ViewType.project);
		FluentFuture.from(whenAllComplete(fileViewColumnsFuture, projectViewColumnsFuture)
				.call(() -> {
						defaultFileViewColumns = fileViewColumnsFuture.get();
						defaultProjectViewColumns = projectViewColumnsFuture.get();
						return null;
					},
					directExecutor())
				).catching(
					Throwable.class,
					e -> {
						popupUtils.showErrorMessage(e.getMessage());
						return null;
					},
					directExecutor()
				);
	}

	private Set<String> getColumnNames(List<ColumnModel> columns) {
		Set<String> defaultColumnNames = new HashSet<String>(columns.size());
		for (ColumnModel cm : columns) {
			defaultColumnNames.add(cm.getName());
		}
		return defaultColumnNames;
	}

	public Set<String> getDefaultViewColumnNames(ViewType type) {
		if (type == null) {
			return null;
		}
		switch(type) {
			case file:
			case file_and_table:
				return getColumnNames(defaultFileViewColumns);
			case project:
				return getColumnNames(defaultProjectViewColumns);
			default :
				popupUtils.showErrorMessage("Unrecognized view type when retrieving column names:" + type);
				return null;
		}
	}

	public List<ColumnModel> getDefaultViewColumns(ViewType type, boolean isClearIds) {
		if (type == null) {
			return null;
		}
		switch(type) {
			case file:
			case file_and_table:
				return isClearIds ? clearIds(defaultFileViewColumns) : defaultFileViewColumns;
			case project:
				return isClearIds ? clearIds(defaultProjectViewColumns) : defaultProjectViewColumns;
			default :
				popupUtils.showErrorMessage("Unrecognized view type:" + type);
				return null;
		}
	}
	
	private List<ColumnModel> clearIds(List<ColumnModel> columns) {
		List<ColumnModel> newColumns = new ArrayList<ColumnModel>(columns.size());
		try {
			for (ColumnModel cm : columns) {
				ColumnModel cmCopy = new ColumnModel(cm.writeToJSONObject(adapterFactory.createNew()));
				cmCopy.setId(null);
				newColumns.add(cmCopy);
			}
		} catch (JSONObjectAdapterException e) {
			popupUtils.showErrorMessage(e.getMessage());
		}
		return newColumns;
	}
}
