package org.sagebionetworks.web.client.widget.table.modal.fileview;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class ViewDefaultColumns {
	private SynapseClientAsync synapseClient;
	private List<ColumnModel> defaultFileViewColumns, 
		defaultFileViewColumnsWithoutIds, 
		defaultProjectViewColumns,
		defaultProjectViewColumnsWithoutIds;

	private AdapterFactory adapterFactory;
	PopupUtilsView popupUtils;

	@Inject
	public ViewDefaultColumns(SynapseClientAsync synapseClient, AdapterFactory adapterFactory,
			PopupUtilsView popupUtils) {
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.popupUtils = popupUtils;
		init();
	}

	public void init() {
		synapseClient.getDefaultColumnsForView(ViewType.file, new AsyncCallback<List<ColumnModel>>() {
			@Override
			public void onFailure(Throwable caught) {
				popupUtils.showErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(List<ColumnModel> columns) {
				defaultFileViewColumns = columns;
				defaultFileViewColumnsWithoutIds = new ArrayList<ColumnModel>(defaultFileViewColumns.size());
				try {
					for (ColumnModel cm : defaultFileViewColumns) {
						ColumnModel cmCopy = new ColumnModel(cm.writeToJSONObject(adapterFactory.createNew()));
						cmCopy.setId(null);
						defaultFileViewColumnsWithoutIds.add(cmCopy);
					}
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
		});
		synapseClient.getDefaultColumnsForView(ViewType.project, new AsyncCallback<List<ColumnModel>>() {
			@Override
			public void onFailure(Throwable caught) {
				popupUtils.showErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(List<ColumnModel> columns) {
				defaultProjectViewColumns = columns;
				defaultProjectViewColumnsWithoutIds = new ArrayList<ColumnModel>(defaultProjectViewColumns.size());
				try {
					for (ColumnModel cm : defaultProjectViewColumns) {
						ColumnModel cmCopy = new ColumnModel(cm.writeToJSONObject(adapterFactory.createNew()));
						cmCopy.setId(null);
						defaultProjectViewColumnsWithoutIds.add(cmCopy);
					}
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
		});
	}

	public List<ColumnModel> getDefaultFileViewColumns(boolean isClearIds) {
		return isClearIds ? defaultFileViewColumnsWithoutIds : defaultFileViewColumns;
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

	public List<ColumnModel> getDefaultProjectViewColumns(boolean isClearIds) {
		return isClearIds ? defaultProjectViewColumnsWithoutIds : defaultProjectViewColumns;
	}

	public Set<String> getDefaultProjectViewColumnNames() {
		return getColumnNames(defaultProjectViewColumns);
	}

}
