package org.sagebionetworks.web.client.widget.table.modal.fileview;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.web.client.SynapseClientAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class FileViewDefaultColumns {
	private SynapseClientAsync synapseClient;
	private List<ColumnModel> defaultColumns;
	private List<ColumnModel> defaultColumnsWithoutIds;
	
	private Set<String> defaultColumnNames;
	
	@Inject
	public FileViewDefaultColumns(SynapseClientAsync synapseClient){
		this.synapseClient = synapseClient;
	}
	
	public void getDefaultColumns(boolean isClearIds, final AsyncCallback<List<ColumnModel>> callback) {
		if (isClearIds) {
			if (defaultColumnsWithoutIds == null) {
				initDefaultColumnsWithoutIds(callback);
			} else {
				callback.onSuccess(defaultColumnsWithoutIds);
			}
		} else {
			if (defaultColumns == null) {
				initDefaultColumnsWithIds(callback);
			} else {
				callback.onSuccess(defaultColumns);
			}
		}
	}
	
	public void getDefaultColumnNames(final AsyncCallback<Set<String>> callback) {
		if (defaultColumnNames == null) {
			initDefaultColumnsWithIds(new AsyncCallback<List<ColumnModel>>() {
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
				@Override
				public void onSuccess(List<ColumnModel> columns) {
					defaultColumnNames = new HashSet<String>();
					for (ColumnModel cm : columns) {
						defaultColumnNames.add(cm.getName());	
					}
					callback.onSuccess(defaultColumnNames);
				}
			});
		} else {
			callback.onSuccess(defaultColumnNames);
		}
	}
	
	private void initDefaultColumnsWithIds(final AsyncCallback<List<ColumnModel>> callback) {
		synapseClient.getDefaultColumnsForView(ViewType.file, new AsyncCallback<List<ColumnModel>>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			@Override
			public void onSuccess(List<ColumnModel> columns) {
				defaultColumns = columns;
				callback.onSuccess(columns);
			}
		});
	}
	
	private void initDefaultColumnsWithoutIds(final AsyncCallback<List<ColumnModel>> callback) {
		synapseClient.getDefaultColumnsForView(ViewType.file, new AsyncCallback<List<ColumnModel>>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			@Override
			public void onSuccess(List<ColumnModel> columns) {
				defaultColumnsWithoutIds = columns;
				// SWC-3264: in order for these to look like new columns in the TableUpdateTransactionRequest change set, set column ids to null
				for (ColumnModel cm : defaultColumnsWithoutIds) {
					cm.setId(null);
				}
				callback.onSuccess(columns);
			}
		});
	}
}
