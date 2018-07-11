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


    public static String TEMPLATE_HEADER_HELLO = "Hi ";

    public static String TEMPLATE_HEADER_THANKS = ",\n\nThank you for submitting your Synapse" +
            " profile for validation. Before I can accept your request, you need to:\n";

    public static String TEMPLATE_HEADER_SIGNATURE = "Let us know if you have any questions.\n\nRegards,\nact@sagebase.org";

    public static String ERROR_MESSAGE = "Error: Please select at least one checkbox and generate a response or manually enter a response";

    // TODO: Change out response to individual strings
    public static String [] RESPONSE = new String[]{
            "Take and pass the Synapse Certification quiz. ",
            "Add at least one piece of information (education, employment, etc.) to your ORCID profile and set it to “public. ",
            "Physically initial each box, sign and date the Synapse Oath. ",
            "Submit an accepted form of identity attestation documentation (e.g., letter from a signing official). ",
            "custom text to insert"
    };


    private String userName;
    private RejectReasonView view;
    UserProfileAsyncHandler handler;
    CallbackP<String> callback;

    @Inject
    public RejectReasonWidget (UserProfileAsyncHandler handler, RejectReasonView view) {
        this.handler = handler;
        this.view = view;
        this.view.setPresenter(this);
        this.userName = "";
        this.callback = null;
    }

    private void setUserProfileName(UserProfile user) {
        this.userName = DisplayUtils.getDisplayName(user);
    }

    /*
        For testing purposes only relay the userName to assert
        that its being properly found.
     */
    private String getUserName() {
        return this.userName;
    }

    // TODO: Remove when feature is in prod
    public void setUserProfileName(String user) {
        this.userName = user;
    }

    public void show(String userID, CallbackP<String> callback) {
        handler.getUserProfile(userID, new AsyncCallback<UserProfile>() {
            @Override
            public void onFailure(Throwable throwable) {
                view.showError("Could not find user id -- " + userID + "\n Error -- " +  throwable.getMessage());
                view.show();
            }

            @Override
            public void onSuccess(UserProfile user) {
                setUserProfileName(user);
                view.show();
            }
        });
        this.callback = callback;
    }

    // TODO: Remove when feature is in prod
    public void show(String userID) {
        handler.getUserProfile(userID, new AsyncCallback<UserProfile>() {
            @Override
            public void onFailure(Throwable throwable) {
                setUserProfileName("John Doe"); // TODO: Change in prod
                view.show();
            }

            @Override
            public void onSuccess(UserProfile user) {
                setUserProfileName(user);
                view.show();
            }
        });
    }

    @Override
    public void getResponse() {
        String output = "";

        if (view.optionOneIsUsed()) {
            output += "\n\t" + RESPONSE[0] + "\n";
        }
        if (view.optionTwoIsUsed()) {
            output += "\n\t" + RESPONSE[1] + "\n";
        }
        if (view.optionThreeIsUsed()) {
            output += "\n\t" + RESPONSE[2] + "\n";
        }
        if (view.optionFourIsUsed()) {
            output += "\n\t" + RESPONSE[3] + "\n";
        }
        if (view.optionFiveIsUsed()) {
            output += "\n\t" + view.getCustomTextResponse() + "\n";
        }

        if (output.equals("") && view.getValue().equals("")) {
            view.showError(ERROR_MESSAGE);
            return;
        } else {
            view.clear();
        }

        view.setValue(TEMPLATE_HEADER_HELLO + this.getUserName() + TEMPLATE_HEADER_THANKS + output + "\n" + TEMPLATE_HEADER_SIGNATURE);
    }

    public String getValue () {
        return view.getValue();
    }

    @Override
    public void onSave () {
        if (view.getValue().equals("")) {
            view.showError(ERROR_MESSAGE);
        } else {
            // TODO: Remove when feature is in prod
            if (callback != null) {
                callback.invoke(view.getValue());
            }
            view.clear();
            view.hide();
        }
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    };

}
