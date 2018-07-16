package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.*;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.presenter.RejectReasonWidget;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RejectReasonViewImpl implements RejectReasonView {

    public interface Binder extends UiBinder<Modal, RejectReasonViewImpl> {}

    @UiField
    Modal modal;

    // Generated Response Preview
    @UiField
    TextArea nameField;

    // Checkboxes
    @UiField
    CheckBox optionOne;
    @UiField
    CheckBox optionTwo;
    @UiField
    CheckBox optionThree;
    @UiField
    CheckBox optionFour;
    @UiField
    CheckBox optionFive;

    // Generate response button
    @UiField
    Button generateButton;

    // Text Box for custom checkbox
    @UiField
    TextArea customText;

    // alert if no responses submitted
    @UiField
    Alert alert;

    // Cancel and Submit Buttons
    @UiField
    Button primaryButton;
    @UiField
    Button defaultButton;

    Widget widget;
    Callback callback;

    // Presenter
    Presenter presenter;

    @Inject
    public RejectReasonViewImpl(Binder binder){
        widget = binder.createAndBindUi(this);

        defaultButton.addClickHandler(event -> modal.hide());
        primaryButton.addClickHandler(arg0 -> callback.invoke());
        primaryButton.addClickHandler(event -> presenter.onSave());
        primaryButton.addDomHandler(DisplayUtils.getPreventTabHandler(primaryButton), KeyDownEvent.getType());

        ClickHandler handler = event -> presenter.updateResponse();

        generateButton.addClickHandler(handler);
        optionOne.setText(RejectReasonWidget.REJECT_TAKE_SYNAPSE_QZ);
        optionTwo.setText(RejectReasonWidget.REJECT_ADD_INFO);
        optionThree.setText(RejectReasonWidget.REJECT_PHYSICALLY_INITIAL);
        optionFour.setText(RejectReasonWidget.REJECT_SUBMIT_DOCS);
        optionFive.setText(RejectReasonWidget.REJECT_CUSTOM_REASON);
    }

    public boolean isOptionOneUsed() {
        return optionOne.getValue();
    }

    public boolean isOptionTwoUsed() {
        return optionTwo.getValue();
    }

    public boolean isOptionThreeUsed() {
        return optionThree.getValue();
    }

    public boolean isOptionFourUsed() {
        return optionFour.getValue();
    }

    public boolean isOptionFiveUsed() {
        return optionFive.getValue();
    }

    public String getCustomTextResponse() {
        return customText.getText();
    }

    public void setPresenter (Presenter presenter) {
        this.presenter = presenter;
    }

    public void setValue(String value) {
        nameField.setText(value);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public String getValue() {
        return nameField.getText();
    }

    @Override
    public void showError(String error) {
        alert.setVisible(true);
        alert.setText(error);
    }

    @Override
    public void hide() {
        modal.hide();
    }

    @Override
    public void show() {
        modal.show();
        nameField.setFocus(true);
    }

    @Override
    public void clear() {
        this.clearError();
        this.primaryButton.state().reset();
        this.defaultButton.state().reset();
        this.defaultButton.state().reset();
        this.customText.clear();
        this.nameField.clear();
        this.optionOne.setValue(false);
        this.optionTwo.setValue(false);
        this.optionThree.setValue(false);
        this.optionFour.setValue(false);
        this.optionFive.setValue(false);
    }

    @Override
    public void clearError() {
        this.alert.setVisible(false);
    }

}
