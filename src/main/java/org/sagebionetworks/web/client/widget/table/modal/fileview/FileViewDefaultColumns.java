package org.sagebionetworks.web.client.widget.table.modal.fileview;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class FileViewDefaultColumns {
	private SynapseClientAsync synapseClient;
	private List<ColumnModel> defaultColumns;
	private List<ColumnModel> defaultColumnsWithoutIds;
	
	private Set<String> defaultColumnNames;
	private AdapterFactory adapterFactory;
	
	@Inject
	public FileViewDefaultColumns(SynapseClientAsync synapseClient, AdapterFactory adapterFactory){
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
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
					defaultColumnNames = new HashSet<String>(columns.size());
					for (ColumnModel cm : columns) {
						defaultColumnNames.add(cm.getName());	
					}
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
		initDefaultColumnsWithIds(new AsyncCallback<List<ColumnModel>>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			@Override
			public void onSuccess(List<ColumnModel> defaultColumns) {
				try {
					defaultColumnsWithoutIds = new ArrayList<ColumnModel>(defaultColumns.size());
					for (ColumnModel cm : defaultColumns) {
						ColumnModel cmCopy = new ColumnModel(cm.writeToJSONObject(adapterFactory.createNew()));
						cmCopy.setId(null);
						defaultColumnsWithoutIds.add(cmCopy);
					}
					callback.onSuccess(defaultColumnsWithoutIds);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
		});
	}
}
