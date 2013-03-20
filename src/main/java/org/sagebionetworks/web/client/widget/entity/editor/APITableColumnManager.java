package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.widget.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableWidget;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableColumnManager implements APITableColumnManagerView.Presenter,
		SynapseWidgetPresenter {

	private APITableColumnManagerView view;
	private List<APITableColumnConfig> configs;
	
	@Inject
	public APITableColumnManager(APITableColumnManagerView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(List<APITableColumnConfig> configs) {
		this.configs = configs;
		view.configure(configs);
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
		APITableColumnConfig newConfig = new APITableColumnConfig();
		newConfig.setRendererName(rendererName);
		String[] inputColNamesArray = inputColumnNames.split(",");
		Set<String> inputColumnNamesSet = new HashSet<String>();
		for (int i = 0; i < inputColNamesArray.length; i++) {
			inputColumnNamesSet.add(inputColNamesArray[i].trim());
		}
		newConfig.setInputColumnNames(inputColumnNamesSet);
		if (displayColumnName == null || displayColumnName.trim().length()==0) {
			displayColumnName = APITableWidget.getSingleInputColumnName(newConfig);
		}
		newConfig.setDisplayColumnName(displayColumnName);
		configs.add(newConfig);
		view.configure(configs);
	}
	
	@Override
	public void deleteColumnConfig(String tokenId) {
		if(tokenId != null) {
			// find config and remove it
			for(APITableColumnConfig data : configs) {
				if(tokenId.equals(data.toString())) {
					configs.remove(data);
					view.configure(configs);
					return;
				}
			}
		} else {
			view.showErrorMessage("Column configuration token not set");
		}
	}
}
