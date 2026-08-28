package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.CardConfiguration;
import org.sagebionetworks.web.client.jsinterop.GenericCardSchema;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.table.explore.QueryWrapperPlotNav;
import org.sagebionetworks.web.shared.WebConstants;

public class DataCatalogPageViewImpl implements DataCatalogPageView {

  SimplePanel container;

  private Header headerWidget;

  @Inject
  public DataCatalogPageViewImpl(Header headerWidget) {
    container = new SimplePanel();
    container.addStyleName("padding-30");
    this.headerWidget = headerWidget;
  }

  @Override
  public void render() {
    Window.scrollTo(0, 0); // scroll user to top of page
    headerWidget.configure();

    String[] secondaryLabels = { "contributors", "individuals", "id", "link" };
    GenericCardSchema genericCardSchema = GenericCardSchema.create(
      "dataset",
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
      WebConstants.DATA_CATALOG_SQL,
      null,
      null,
      newBundle -> {},
      true,
      false,
      true,
      true,
      true,
      null,
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
