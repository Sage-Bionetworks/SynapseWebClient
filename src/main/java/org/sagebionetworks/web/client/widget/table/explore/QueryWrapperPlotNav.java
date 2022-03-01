package org.sagebionetworks.web.client.widget.table.explore;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnQueryCallback;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnQueryResultBundleCallback;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class QueryWrapperPlotNav extends ReactComponentDiv {
	public QueryWrapperPlotNav(
			SynapseContextPropsProvider contextPropsProvider,
			String sql,
			String initQueryJson,
			OnQueryCallback onQueryChange,
			OnQueryResultBundleCallback onQueryResultBundleChange,
			boolean hideSqlEditorControl) {
		QueryWrapperPlotNavProps props = QueryWrapperPlotNavProps.create(sql, initQueryJson, onQueryChange, onQueryResultBundleChange, hideSqlEditorControl);
		
		ReactDOM.render(
			React.createElementWithSynapseContext(
					SRC.SynapseComponents.QueryWrapperPlotNav,
					props,
					contextPropsProvider.getJsInteropContextProps()
			),
			getElement()
		);
	}
}
