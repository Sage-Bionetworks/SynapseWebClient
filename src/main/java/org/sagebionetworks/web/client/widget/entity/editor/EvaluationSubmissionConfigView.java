package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.Map;
import org.sagebionetworks.web.client.widget.WidgetEditorView;
import org.sagebionetworks.web.shared.WikiPageKey;

public interface EvaluationSubmissionConfigView
  extends IsWidget, WidgetEditorView {
  void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor);

  String getUnavailableMessage();

  String getButtonText();

  String getChallengeProjectId();

  boolean isChallengeProjectIdSelected();

  String getEvaluationQueueId();

  boolean isFormSubmission();

  String getFormContainerId();

  String getFormJsonSchemaId();

  String getFormUiSchemaId();
}
