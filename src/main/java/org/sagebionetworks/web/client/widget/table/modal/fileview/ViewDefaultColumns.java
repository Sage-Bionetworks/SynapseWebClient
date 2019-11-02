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
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import com.google.common.util.concurrent.FluentFuture;
import com.google.inject.Inject;

public class ViewDefaultColumns {
	private SynapseJavascriptClient jsClient;
	private List<ColumnModel> defaultFileViewColumns, defaultProjectViewColumns;


	private AdapterFactory adapterFactory;
	PopupUtilsView popupUtils;

	@Inject
	public ViewDefaultColumns(SynapseJavascriptClient jsClient, AdapterFactory adapterFactory, PopupUtilsView popupUtils) {
		this.jsClient = jsClient;
		this.adapterFactory = adapterFactory;
		this.popupUtils = popupUtils;
		init();
	}

	public void init() {
		FluentFuture<List<ColumnModel>> fileViewColumnsFuture = jsClient.getDefaultColumnsForView(ViewType.file);
		FluentFuture<List<ColumnModel>> projectViewColumnsFuture = jsClient.getDefaultColumnsForView(ViewType.project);
		FluentFuture.from(whenAllComplete(fileViewColumnsFuture, projectViewColumnsFuture).call(() -> {
			defaultFileViewColumns = fileViewColumnsFuture.get();
			defaultProjectViewColumns = projectViewColumnsFuture.get();
			return null;
		}, directExecutor())).catching(Throwable.class, e -> {
			popupUtils.showErrorMessage(e.getMessage());
			return null;
		}, directExecutor());
	}

	private Set<String> getColumnNames(List<ColumnModel> columns) {
		Set<String> defaultColumnNames = new HashSet<String>(columns.size());
		for (ColumnModel cm : columns) {
			defaultColumnNames.add(cm.getName());
		}
		return defaultColumnNames;
	}

	public Set<String> getDefaultViewColumnNames(boolean includesFiles) {
		if (includesFiles) {
			return getColumnNames(defaultFileViewColumns);
		} else {
			return getColumnNames(defaultProjectViewColumns);
		}
	}

	public List<ColumnModel> getDefaultViewColumns(boolean includesFiles, boolean isClearIds) {
		if (includesFiles) {
			return isClearIds ? clearIds(defaultFileViewColumns) : defaultFileViewColumns;
		} else {
			return isClearIds ? clearIds(defaultProjectViewColumns) : defaultProjectViewColumns;
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
