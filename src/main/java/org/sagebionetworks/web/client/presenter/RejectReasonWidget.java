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


    static String templateHeaderHi = "Hi ";

    static String templateHeaderThanks = ",\n\nThank you for submitting your Synapse" +
            " profile for validation. Before I can accept your request, you need to:\n";

    static String templateSignature = "Let us know if you have any questions.\n\nRegards,\nact@sagebase.org";

    public static String [] response = new String[]{
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

    // TODO: Remove when feature is in prod
    private void setUserProfileName(String user) {
        this.userName = user;
    }

    public void show(String userID, CallbackP<String> callback) {
        handler.getUserProfile(userID, new AsyncCallback<UserProfile>() {
            @Override
            public void onFailure(Throwable throwable) {
                 view.showError("Could not find user name");
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
            output += "\n\t" + response[0] + "\n";
        }
        if (view.optionTwoIsUsed()) {
            output += "\n\t" + response[1] + "\n";
        }
        if (view.optionThreeIsUsed()) {
            output += "\n\t" + response[2] + "\n";
        }
        if (view.optionFourIsUsed()) {
            output += "\n\t" + response[3] + "\n";
        }
        if (view.optionFiveIsUsed()) {
            output += "\n\t" + view.getCustomTextResponse() + "\n";
        }

        if (output.equals("") && view.getValue().equals("")) {
            view.showError("Error: Please select at least one checkbox and generate a response or manually enter a response");
            return;
        } else {
            view.clear();
        }

        view.setValue(templateHeaderHi + this.userName + templateHeaderThanks + output + "\n" + templateSignature);
    }

    public String getValue () {
        return view.getValue();
    }

    @Override
    public void onSave () {
        if (view.getValue().equals("")) {
            view.showError("Error: Please select at least one checkbox and generate a response or manually enter a response");
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
