package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.ForumSearchProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.ForumSearchProps.OnSearchResultsVisibleHandler;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class ForumSearchWrapper extends ReactComponentDiv {
	public ForumSearchWrapper(
			SynapseContextPropsProvider contextPropsProvider,
			String forumId,
			String projectId,
			OnSearchResultsVisibleHandler onSearchResultsVisible) {
		ForumSearchProps props = ForumSearchProps.create(forumId, projectId, onSearchResultsVisible);
		ReactDOM.render(
			React.createElementWithSynapseContext(
					SRC.SynapseComponents.ForumSearch,
					props,
					contextPropsProvider.getJsInteropContextProps()
			),
			getElement()
		);
	}
}
