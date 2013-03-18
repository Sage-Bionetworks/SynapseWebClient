package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.widget.APITableColumnConfig;
import org.sagebionetworks.repo.model.widget.APITableColumnConfigList;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableColumnManager implements APITableColumnManagerView.Presenter,
		SynapseWidgetPresenter {

	private APITableColumnManagerView view;
	private APITableColumnConfigList configs;
	
	@Inject
	public APITableColumnManager(APITableColumnManagerView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(APITableColumnConfigList configs) {
		this.configs = configs;
		view.configure(configs.getColumnConfigList());
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void addColumnConfig(String rendererName, String inputColumnNames,
			String displayColumnName) {
		if (rendererName == null || inputColumnNames == null || rendererName.trim().length() == 0 || inputColumnNames.trim().length() == 0) {
			throw new IllegalArgumentException("Renderer and Input Columns are required");
		}
		List<APITableColumnConfig> columnConfigList = configs.getColumnConfigList();
		APITableColumnConfig newConfig = new APITableColumnConfig();
		newConfig.setRendererName(rendererName);
		String[] inputColNamesArray = inputColumnNames.split(",");
		Set<String> inputColumnNamesSet = new HashSet<String>();
		for (int i = 0; i < inputColNamesArray.length; i++) {
			inputColumnNamesSet.add(inputColNamesArray[i].trim());
		}
		newConfig.setInputColumnNames(inputColumnNamesSet);
		columnConfigList.add(newConfig);
	}
	
	@Override
	public void deleteColumnConfig(String tokenId) {
		List<APITableColumnConfig> columnConfigList = configs.getColumnConfigList();
		if(tokenId != null) {
			// find config and remove it
			for(APITableColumnConfig data : columnConfigList) {
				if(tokenId.equals(data.toString())) {
					columnConfigList.remove(data);
					return;
				}
			}
		} else {
			view.showErrorMessage("Column configuration token not set");
		}
	}
}
