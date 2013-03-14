package org.sagebionetworks.web.client.widget.entity;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.google.gwt.user.client.ui.IsWidget;

public interface PreviewWidgetView extends IsWidget{
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void setImagePreview(String fullFileUrl, String previewUrl);
	public void setCodePreview(String url);
	public void setBlockQuotePreview(String url);
	public void setTablePreview(String url);
	public void setTreePreview(TreeStore<ModelData> store);
	public void showErrorMessage(String message);
	public void clear();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}


}
