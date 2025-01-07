package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.CardConfiguration;
import org.sagebionetworks.web.client.jsinterop.GenericCardSchema;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.table.explore.QueryWrapperPlotNav;

public class DataCatalogPageViewImpl implements DataCatalogPageView {

  SimplePanel container;

  private Header headerWidget;
  private SynapseReactClientFullContextPropsProvider propsProvider;

  @Inject
  public DataCatalogPageViewImpl(
    Header headerWidget,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    container = new SimplePanel();
    this.headerWidget = headerWidget;
    this.propsProvider = propsProvider;
  }

  @Override
  public void render() {
    Window.scrollTo(0, 0); // scroll user to top of page
    headerWidget.configure();
    Map<String, String> columnAliases = new HashMap<String, String>();
    columnAliases.put("id", "On Synapse");

    String[] secondaryLabels = { "contributors", "individuals", "id", "link" };
    GenericCardSchema genericCardSchema = GenericCardSchema.create(
      "DATASET",
      "name",
      "community",
      "description",
      secondaryLabels
    );
    CardConfiguration cardConfiguration = CardConfiguration.create(
      "GENERIC_CARD",
      4,
      genericCardSchema
    );

    QueryWrapperPlotNav plotNav = new QueryWrapperPlotNav(
      propsProvider,
      "SELECT * FROM syn61609402 WHERE includedInDataCatalog = 'true'",
      null,
      null,
      null,
      null,
      true,
      false,
      true,
      true,
      true,
      columnAliases,
      cardConfiguration,
      "Data Catalog"
    );
    container.clear();
    container.setWidget(plotNav);
  }

  @Override
  public Widget asWidget() {
    return container.asWidget();
  }
}
