package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * A simple model dialog to ask for reasons specifying why a user was rejected
 *
 */
public interface RejectReasonView extends IsWidget {


    /**
     * @return String representation of rejected reason
     */
    String getValue();

    /**
     * Show an error message..
     * @param error
     */
    void showError(String error);

    void setValue (String value);

    /**
     * Show the view
     */
    void show();

    /**
     * Hide the dialog.
     */
    void hide();

    /**
     * Clear name and errors.
     */
    void clear();

    /**
     * Set loading state.
     * @param isLoading
     */
    void setLoading(boolean isLoading);

    /**
     * Set Presenter
     * @param presenter
     */
    void setPresenter (Presenter presenter);

    boolean optionOneIsUsed();

    boolean optionTwoIsUsed();

    boolean optionThreeIsUsed();

    boolean optionFourIsUsed();

    boolean optionFiveIsUsed();

    String getCustomTextResponse();

    /**
     * Presenter interface
     */
    public interface Presenter {
        void updateResponse();
        void onSave();
    }


}
