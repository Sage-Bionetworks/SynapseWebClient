package org.sagebionetworks.web.client.widget.entity.act;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.shared.WebConstants;

public class RejectReasonWidget
  implements RejectReasonView.Presenter, IsWidget {

  // Template rejection format
  public static String TEMPLATE_HEADER_THANKS =
    "Thank you for submitting your Synapse" +
    " profile for validation. Before I can accept your request:\n";
  public static String TEMPLATE_HEADER_SIGNATURE =
    "\nPlease contact us at act@sagebionetworks.org if you have any questions.\n" +
    "\n" +
    "Regards,\n" +
    "Access and Compliance Team (ACT)\n" +
    "act@sagebionetworks.org";

  // If no options are shown for rejected reason
  public static String ERROR_MESSAGE =
    "Error: Please select at least one checkbox and generate a response or manually enter a response";

  // Common reasons for user rejection

  private RejectReasonView view;
  CallbackP<String> callback;
  private SynapseProperties synapseProperties;
  private boolean isLoaded = false;
  PortalGinInjector ginInjector;

  @Inject
  public RejectReasonWidget(
    RejectReasonView view,
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
        WebConstants.ACT_PROFILE_VALIDATION_REJECTION_REASONS_PROPERTY_KEY
      );
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

  /**
   *
   * @param jsonSynID Synapse ID of the file in Synapse that contains the JSON
   * @param ginInjector
   * @param callback
   */
  public static void getJSON(
    String jsonSynID,
    PortalGinInjector ginInjector,
    AsyncCallback<JSONObjectAdapter> callback
  ) {
    ginInjector
      .getSynapseJavascriptClient()
      .getEntity(
        jsonSynID,
        new AsyncCallback<Entity>() {
          @Override
          public void onSuccess(Entity result) {
            FileHandleAssociation fha = new FileHandleAssociation();
            fha.setAssociateObjectType(FileHandleAssociateType.FileEntity);
            fha.setAssociateObjectId(jsonSynID);
            fha.setFileHandleId(((FileEntity) result).getDataFileHandleId());

            ginInjector
              .getPresignedURLAsyncHandler()
              .getFileResult(
                fha,
                new AsyncCallback<FileResult>() {
                  @Override
                  public void onSuccess(FileResult result) {
                    RequestBuilderWrapper requestBuilder = ginInjector.getRequestBuilder();
                    requestBuilder.configure(
                      RequestBuilder.GET,
                      result.getPreSignedURL()
                    );
                    requestBuilder.setHeader(
                      WebConstants.CONTENT_TYPE,
                      WebConstants.TEXT_PLAIN_CHARSET_UTF8
                    );
                    try {
                      requestBuilder.sendRequest(
                        null,
                        new RequestCallback() {
                          @Override
                          public void onResponseReceived(
                            Request request,
                            Response response
                          ) {
                            int statusCode = response.getStatusCode();
                            if (statusCode == Response.SC_OK) {
                              String text = response.getText();
                              try {
                                // parse json, and add each reason
                                JSONObjectAdapter json = ginInjector
                                  .getJSONObjectAdapter()
                                  .createNew(text);
                                callback.onSuccess(json);
                              } catch (JSONObjectAdapterException e) {
                                onError(null, e);
                              }
                            } else {
                              onError(
                                null,
                                new IllegalArgumentException(
                                  "Unable to retrieve message ACT rejection reasons. Reason: " +
                                  response.getStatusText()
                                )
                              );
                            }
                          }

                          @Override
                          public void onError(
                            Request request,
                            Throwable exception
                          ) {
                            callback.onFailure(exception);
                          }
                        }
                      );
                    } catch (final Exception e) {
                      callback.onFailure(e);
                    }
                  }

                  @Override
                  public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                  }
                }
              );
          }

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }
        }
      );
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
