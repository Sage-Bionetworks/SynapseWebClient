package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesViewImpl.GetOrderHintCallback;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Tree;

public interface WikiSubpagesView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Configure the view with the parent id
	 * @param entityId
	 * @param title
	 */
	public void configure(WikiSubpageNavigationTree navTree, WikiSubpageOrderEditorTree editorTree, FlowPanel wikiSubpagesContainer, FlowPanel wikiPageContainer);
	void hideSubpages();
	void showSubpages();
	
	// List<String> getCurrentOrderHintIdList();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		Callback getUpdateOrderHintCallback(GetOrderHintCallback getCurrentOrderListCallback);
	}
}
