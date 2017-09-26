package org.sagebionetworks.web.client.widget.provenance;

import java.util.List;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.provenance.ExpandGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface ProvenanceWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 * resets the view to default state
	 */
	public void clear();

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		
		/**
		 * Asks the presenter to load info on the passed id
		 * @param nodeId
		 */
		void getInfo(String nodeId, AsyncCallback<KeyValueDisplay<String>> callback);

		void expand(ExpandGraphNode node);

		void findOldVersions();
		
	}

	public void setGraph(ProvGraph graph);

	public void setHeight(int height);
	
	public void markOldVersions(List<String> notCurrentNodeIds);
	
}
