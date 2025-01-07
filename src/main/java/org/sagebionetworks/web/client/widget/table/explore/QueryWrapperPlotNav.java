package org.sagebionetworks.web.client.widget.table.explore;

import java.util.Map;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.CardConfiguration;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnQueryCallback;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnQueryResultBundleCallback;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnViewSharingSettingsHandler;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class QueryWrapperPlotNav extends ReactComponent {

  public QueryWrapperPlotNav(
    SynapseReactClientFullContextPropsProvider contextPropsProvider,
    String sql,
    String initQueryJson,
    OnQueryCallback onQueryChange,
    OnQueryResultBundleCallback onQueryResultBundleChange,
    OnViewSharingSettingsHandler onViewSharingSettingsHandler,
    boolean hideSqlEditorControl,
    Boolean defaultShowPlots,
    Boolean defaultShowSearchBox,
    Boolean hideCopyToClipboard,
    Boolean hideDownload,
    Map<String, String> columnAliases,
    CardConfiguration cardConfiguration,
    String name
  ) {
    QueryWrapperPlotNavProps props = QueryWrapperPlotNavProps.create(
      sql,
      initQueryJson,
      onQueryChange,
      onQueryResultBundleChange,
      onViewSharingSettingsHandler,
      hideSqlEditorControl,
      defaultShowPlots,
      defaultShowSearchBox,
      hideCopyToClipboard,
      hideDownload,
      columnAliases,
      cardConfiguration,
      name
    );

    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.QueryWrapperPlotNav,
      props,
      contextPropsProvider.getJsInteropContextProps()
    );
    this.render(component);
  }
}
