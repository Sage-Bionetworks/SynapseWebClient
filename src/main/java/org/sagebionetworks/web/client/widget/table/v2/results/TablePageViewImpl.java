package org.sagebionetworks.web.client.widget.table.v2.results;

import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;

/**
 * UiBound implementation of a TableView with zero business logic.
 *
 * @author John
 *
 */

public class TablePageViewImpl implements TablePageView {

  public interface Binder extends UiBinder<Widget, TablePageViewImpl> {}

  @UiField
  Div loadingUI;

  @UiField
  Table table;

  @UiField
  TableRow header;

  @UiField
  TBody body;

  @UiField
  Div paginationPanel;

  @UiField
  Div tablePanel;

  Div widget;

  @UiField
  ScrollPanel topScrollBar;

  @UiField
  ScrollPanel tableScrollPanel;

  @UiField
  Div topScrollDiv;

  @UiField
  Div tableDiv;

  @UiField
  Span lastUpdatedOnSpan;

  @Inject
  public TablePageViewImpl(Binder binder, final GWTWrapper gwt) {
    widget = (Div) binder.createAndBindUi(this);

    gwt.scheduleExecution(
      new Callback() {
        @Override
        public void invoke() {
          if (tableScrollPanel.isAttached() && tableScrollPanel.isVisible()) {
            // match width
            topScrollDiv.setWidth(
              tableDiv.getElement().getScrollWidth() + "px"
            );
            boolean isScrollBarShowing =
              tableDiv.getElement().getScrollWidth() >
              tableDiv.getElement().getClientWidth();
            topScrollBar.setVisible(
              isScrollBarShowing && tableScrollPanel.getOffsetHeight() > 600
            );
            paginationPanel.setMarginLeft(
              tableScrollPanel.getAbsoluteLeft() - tablePanel.getAbsoluteLeft()
            );
            gwt.scheduleExecution(this, 400);
          }
        }
      },
      400
    );

    tableScrollPanel.addScrollHandler(
      new ScrollHandler() {
        @Override
        public void onScroll(ScrollEvent event) {
          topScrollBar.setHorizontalScrollPosition(
            tableScrollPanel.getHorizontalScrollPosition()
          );
        }
      }
    );
    topScrollBar.addScrollHandler(
      new ScrollHandler() {
        @Override
        public void onScroll(ScrollEvent event) {
          tableScrollPanel.setHorizontalScrollPosition(
            topScrollBar.getHorizontalScrollPosition()
          );
        }
      }
    );
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setTableHeaders(List<IsWidget> headers) {
    header.clear();
    body.clear();
    // Blank header for the selection.
    header.add(new TableHeader());
    for (IsWidget inHeader : headers) {
      header.add(inHeader);
    }
  }

  @Override
  public void addRow(RowWidget newRow) {
    body.add(newRow);
  }

  @Override
  public void addRows(List<RowWidget> rowWidgets) {
    body.removeFromParent();
    for (RowWidget rowWidget : rowWidgets) {
      addRow(rowWidget);
    }
    table.add(body);
  }

  @Override
  public void removeRow(RowWidget row) {
    body.remove(row);
  }

  @Override
  public void setPaginationWidget(IsWidget paginationWidget) {
    this.paginationPanel.add(paginationWidget);
  }

  @Override
  public void setPaginationWidgetVisible(boolean visible) {
    this.paginationPanel.setVisible(visible);
  }

  @Override
  public void setTableVisible(boolean visible) {
    tablePanel.setVisible(visible);
  }

  @Override
  public void hideLoading() {
    loadingUI.setVisible(false);
    if (!tablePanel.isAttached()) {
      widget.add(tablePanel);
    }
  }

  @Override
  public void showLoading() {
    loadingUI.setVisible(true);
    tablePanel.removeFromParent();
  }

  @Override
  public void setLastUpdatedOn(String lastUpdatedOn) {
    lastUpdatedOnSpan.setText(lastUpdatedOn);
  }
}
