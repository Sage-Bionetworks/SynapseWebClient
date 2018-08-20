package org.sagebionetworks.web.client.presenter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.RejectReasonView;


public class RejectReasonWidget implements RejectReasonView.Presenter, IsWidget {


    // Template rejection format
    public static String TEMPLATE_HEADER_HELLO = "Hi ";
    public static String TEMPLATE_HEADER_THANKS = ",\n\nThank you for submitting your Synapse" +
            " profile for validation. Before I can accept your request, you need to:\n";
    public static String TEMPLATE_HEADER_SIGNATURE = "Let us know if you have any questions.\n\nRegards,\nact@sagebase.org";

    // If no options are shown for rejected reason
    public static String ERROR_MESSAGE = "Error: Please select at least one checkbox and generate a response or manually enter a response";

    // Common reasons for user rejection
    public static String REJECT_TAKE_SYNAPSE_QZ = "Take and pass the Synapse Certification quiz. ";
    public static String REJECT_ADD_INFO = "Add at least one piece of information (education, employment, etc.) to your ORCID profile and set it to \"public.\" ";
    public static String REJECT_PHYSICALLY_INITIAL = "Physically initial each box, sign and date the Synapse Oath. ";
    public static String REJECT_SUBMIT_DOCS = "Submit an accepted form of identity attestation documentation (e.g., letter from a signing official). ";
    public static String REJECT_CUSTOM_REASON = "custom text to insert";

    private String displayName;
    private RejectReasonView view;
    CallbackP<String> callback;

    @Inject
    public RejectReasonWidget (RejectReasonView view) {
        this.view = view;
        this.view.setPresenter(this);
    }


    public void show(String displayName, CallbackP<String> callback) {
    	this.displayName = displayName;
        this.view.clear();
        this.callback = callback;
        view.show();
    }

    @Override
    public void updateResponse() {
        String output = "";

        if (view.isOptionOneUsed()) {
            output += "\n\t" + REJECT_TAKE_SYNAPSE_QZ + "\n";
        }
        if (view.isOptionTwoUsed()) {
            output += "\n\t" + REJECT_ADD_INFO + "\n";
        }
        if (view.isOptionThreeUsed()) {
            output += "\n\t" + REJECT_PHYSICALLY_INITIAL + "\n";
        }
        if (view.isOptionFourUsed()) {
            output += "\n\t" + REJECT_SUBMIT_DOCS + "\n";
        }
        if (view.isOptionFiveUsed()) {
            output += "\n\t" + view.getCustomTextResponse() + "\n";
        }

        if (output.equals("") && view.getValue().equals("")) {
            view.showError(ERROR_MESSAGE);
            return;
        } else {
            view.clearError();
        }

        view.setValue(TEMPLATE_HEADER_HELLO + displayName + TEMPLATE_HEADER_THANKS + output + "\n" + TEMPLATE_HEADER_SIGNATURE);
    }

    @Override
    public void onSave () {
        if (view.getValue().equals("")) {
            view.showError(ERROR_MESSAGE);
        } else {
            callback.invoke(view.getValue());
            view.clear();
            view.hide();
        }
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    };

}
