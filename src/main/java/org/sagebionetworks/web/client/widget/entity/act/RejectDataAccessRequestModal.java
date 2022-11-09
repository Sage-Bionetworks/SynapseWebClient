package org.sagebionetworks.web.client.widget.entity.act;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.shared.WebConstants;

public class RejectDataAccessRequestModal
  implements RejectDataAccessRequestModalView.Presenter, IsWidget {

  // Template rejection format
  public static String TEMPLATE_HEADER_THANKS =
    "Thank you for submitting your data access request. Before I can accept your request, please address the following:\n";
  public static String TEMPLATE_HEADER_SIGNATURE =
    "\nPlease contact us at act@sagebionetworks.org if you have any questions.\n" +
    "\n" +
    "Regards,\n" +
    "Access and Compliance Team (ACT)\n" +
    "act@sagebionetworks.org";

  // If no options are shown for rejected reason
  public static String ERROR_MESSAGE =
    "Error: Please select at least one checkbox and generate a response or manually enter a response";

  private RejectDataAccessRequestModalView view;
  CallbackP<String> callback;
  private SynapseProperties synapseProperties;
  PortalGinInjector ginInjector;
  private boolean isLoaded = false;

  @Inject
  public RejectDataAccessRequestModal(
    RejectDataAccessRequestModalView view,
    SynapseProperties synapseProperties,
    PortalGinInjector ginInjector
  ) {
    this.view = view;
    this.synapseProperties = synapseProperties;
    this.ginInjector = ginInjector;
    this.view.setPresenter(this);
  }

  public void show(CallbackP<String> callback) {
    this.view.clear();
    this.callback = callback;
    if (!isLoaded) {
      // get the json file that define the reasons
      String jsonSynID = synapseProperties.getSynapseProperty(
        WebConstants.ACT_DATA_ACCESS_REJECTION_REASONS_PROPERTY_KEY
      );
      // get the entity (to find it's file handle ID)
      RejectReasonWidget.getJSON(
        jsonSynID,
        ginInjector,
        new AsyncCallback<JSONObjectAdapter>() {
          @Override
          public void onSuccess(JSONObjectAdapter json) {
            view.clearReasons();
            try {
              JSONArrayAdapter jsonArray = json.getJSONArray("reasons");
              for (int i = 0; i < jsonArray.length(); i++) {
                view.addReason(jsonArray.getString(i));
              }
              isLoaded = true;
            } catch (JSONObjectAdapterException e) {
              view.showError(e.getMessage());
            }
            view.show();
          }

          @Override
          public void onFailure(Throwable caught) {
            view.showError(caught.getMessage());
            view.show();
          }
        }
      );
    } else {
      view.show();
    }
  }

  @Override
  public void updateResponse() {
    String output = view.getSelectedCheckboxText();
    view.setValue(TEMPLATE_HEADER_THANKS + output + TEMPLATE_HEADER_SIGNATURE);
  }

  @Override
  public void onSave() {
    if (view.getValue().equals("")) {
      view.showError(ERROR_MESSAGE);
    } else {
      callback.invoke(view.getValue());
      view.hide();
    }
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
