package org.sagebionetworks.web.client.widget.table.explore;

import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnQueryCallback;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnQueryResultBundleCallback;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnViewSharingSettingsHandler;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class QueryWrapperPlotNav extends ReactComponentDiv {

  public QueryWrapperPlotNav(
    SynapseReactClientFullContextPropsProvider contextPropsProvider,
    String sql,
    String initQueryJson,
    OnQueryCallback onQueryChange,
    OnQueryResultBundleCallback onQueryResultBundleChange,
    OnViewSharingSettingsHandler onViewSharingSettingsHandler,
    boolean hideSqlEditorControl
  ) {
    QueryWrapperPlotNavProps props = QueryWrapperPlotNavProps.create(
      sql,
      initQueryJson,
      onQueryChange,
      onQueryResultBundleChange,
      onViewSharingSettingsHandler,
      hideSqlEditorControl
    );

    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.QueryWrapperPlotNav,
      props,
      contextPropsProvider.getJsInteropContextProps()
    );
    this.render(component);
  }
}
