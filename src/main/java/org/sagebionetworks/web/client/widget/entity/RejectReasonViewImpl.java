package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import org.gwtbootstrap3.client.ui.*;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.presenter.RejectReasonWidget;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
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

        ClickHandler handler = event -> presenter.getResponse();

        generateButton.addClickHandler(handler);
        optionOne.setText(RejectReasonWidget.RESPONSE[0]);
        optionTwo.setText(RejectReasonWidget.RESPONSE[1]);
        optionThree.setText(RejectReasonWidget.RESPONSE[2]);
        optionFour.setText(RejectReasonWidget.RESPONSE[3]);

    }


    public boolean optionOneIsUsed () {
        return optionOne.getValue();
    }


    public boolean optionTwoIsUsed () {
        return optionTwo.getValue();
    }

    public boolean optionThreeIsUsed () {
        return optionThree.getValue();
    }

    public boolean optionFourIsUsed () {
        return optionFour.getValue();
    }

    public boolean optionFiveIsUsed () {
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
        this.alert.setVisible(false);
        this.primaryButton.state().reset();
    }

    @Override
    public void setLoading(boolean isLoading) {
        if(isLoading){
            this.primaryButton.state().loading();
        }else{
            this.primaryButton.state().reset();
        }
    }

}
