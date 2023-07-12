package org.sagebionetworks.web.client.widget.doi;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.doi.v2.DoiAssociation;
import org.sagebionetworks.web.client.SynapseJavascriptClient;

public class DoiWidgetV2 implements IsWidget {

  public static final String DOI_ORG = "https://doi.org/";
  private DoiWidgetV2View view;
  private SynapseJavascriptClient jsClient;

  @Inject
  public DoiWidgetV2(DoiWidgetV2View view, SynapseJavascriptClient jsClient) {
    this.view = view;
    this.jsClient = jsClient;
  }

  public Widget asWidget() {
    return view.asWidget();
  }

  public void configure(
    String objectId,
    ObjectType objectType,
    Long versionNumber
  ) {
    clear();
    // get the associated DOI Association
    jsClient
      .getDoiAssociation(objectId, objectType, versionNumber)
      .addCallback(
        new FutureCallback<DoiAssociation>() {
          @Override
          public void onSuccess(@Nullable DoiAssociation doiAssociation) {
            configure(doiAssociation);
          }

          @Override
          public void onFailure(Throwable t) {
            // no op
          }
        },
        directExecutor()
      );
  }

  public void configure(DoiAssociation newDoi) {
    clear();
    if (newDoi != null && newDoi.getDoiUri() != null) {
      // SWC-5979: make into a valid link (that the user can copy)
      view.showDoi(DOI_ORG + newDoi.getDoiUri());
    }
  }

  public void setLabelVisible(boolean visible) {
    view.setLabelVisible(visible);
  }

  public void clear() {
    view.hide();
    view.clear();
  }
}
