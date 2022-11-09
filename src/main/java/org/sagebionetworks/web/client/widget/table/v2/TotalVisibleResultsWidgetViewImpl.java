package org.sagebionetworks.web.client.widget.table.v2;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.HelpWidget;

public class TotalVisibleResultsWidgetViewImpl
  extends Composite
  implements TotalVisibleResultsWidgetView {

  public interface Binder
    extends UiBinder<Widget, TotalVisibleResultsWidgetViewImpl> {}

  @UiField
  Heading totalItemCountContainer;

  @UiField
  Span totalItemCountItemName;

  @UiField
  Span totalItemCount;

  @UiField
  Span unavailableItemCountContainer;

  @UiField
  Span unavailableItemCount;

  @UiField
  HelpWidget helpWidget;

  PortalGinInjector ginInjector;

  @Inject
  public TotalVisibleResultsWidgetViewImpl(
    final Binder uiBinder,
    PortalGinInjector ginInjector
  ) {
    initWidget(uiBinder.createAndBindUi(this));
    this.ginInjector = ginInjector;
  }

  @Override
  public void setTotalNumberOfResults(int count) {
    totalItemCount.setText(NumberFormat.getDecimalFormat().format(count));
  }

  @Override
  public void setNumberOfHiddenResults(int count) {
    unavailableItemCount.setText(
      NumberFormat.getDecimalFormat().format(count) + " Unavailable"
    );
  }

  @Override
  public void setNumberOfHiddenResultsVisible(boolean visible) {
    unavailableItemCountContainer.setVisible(visible);
  }

  @Override
  public void setHelpMarkdown(String md) {
    helpWidget.setHelpMarkdown(md);
  }
}
