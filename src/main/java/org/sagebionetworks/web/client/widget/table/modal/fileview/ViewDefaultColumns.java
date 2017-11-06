package org.sagebionetworks.web.client.widget.table.modal.fileview;

import static com.google.common.util.concurrent.Futures.getDone;
import static com.google.common.util.concurrent.Futures.whenAllComplete;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.sagebionetworks.web.client.DisplayUtils.getDisplayName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseFutureClient;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class ViewDefaultColumns {
	private SynapseFutureClient futureClient;
	private List<ColumnModel> defaultFileViewColumns, 
		defaultProjectViewColumns,
		defaultFilesAndTablesColumns;
		

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
		ListenableFuture<List<ColumnModel>> fileTablesViewColumnsFuture = futureClient.getDefaultColumnsForView(ViewType.file_and_table);
		
		FluentFuture.from(whenAllComplete(fileViewColumnsFuture, projectViewColumnsFuture, fileTablesViewColumnsFuture)
				.call(() -> {
							defaultFileViewColumns = fileViewColumnsFuture.get();
							defaultProjectViewColumns = projectViewColumnsFuture.get();
							defaultFilesAndTablesColumns = fileTablesViewColumnsFuture.get();
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

	public Set<String> getDefaultFileViewColumnNames() {
		return getColumnNames(defaultFileViewColumns);
	}

	public List<ColumnModel> getDefaultViewColumns(ViewType type, boolean isClearIds) {
		if (type == null) {
			return null;
		}
		switch(type) {
			case file:
				return isClearIds ? clearIds(defaultFileViewColumns) : defaultFileViewColumns;
			case file_and_table:
				return isClearIds ? clearIds(defaultFilesAndTablesColumns) : defaultFilesAndTablesColumns;
			case project:
				return isClearIds ? clearIds(defaultProjectViewColumns) : defaultProjectViewColumns;
			default :
				popupUtils.showErrorMessage("Unrecognized view type:" + type);
				return null;
			}
	}

	public Set<String> getDefaultProjectViewColumnNames() {
		return getColumnNames(defaultProjectViewColumns);
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
