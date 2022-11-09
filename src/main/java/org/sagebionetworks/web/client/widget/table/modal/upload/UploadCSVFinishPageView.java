package org.sagebionetworks.web.client.widget.table.modal.upload;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.List;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRow;

public interface UploadCSVFinishPageView extends IsWidget {
  void setTrackerVisible(boolean b);

  void setTableName(String fileName);

  String getTableName();

  void setColumnEditor(List<ColumnModelTableRow> editors);

  void addTrackerWidget(IsWidget jobTrackingWidget);
}
