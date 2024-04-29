package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.CreateOrUpdateAccessRequirementWizardProps;
import org.sagebionetworks.web.client.place.AccessRequirementPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateOrUpdateAccessRequirementWizard;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.LegacyCreateAccessRequirementWizard;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.renderer.SingleButtonView;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;

public class CreateAccessRequirementButton
  implements SingleButtonView.Presenter, IsWidget {

  private SingleButtonView view;
  private CookieProvider cookies;

  public static final String CREATE_NEW_ACCESS_REQUIREMENT_BUTTON_TEXT =
    "Create New Access Requirement";
  public static final String EDIT_ACCESS_REQUIREMENT_BUTTON_TEXT =
    "Edit Access Requirement";
  public IsACTMemberAsyncHandler isACTMemberAsyncHandler;
  public PortalGinInjector ginInjector;
  RestrictableObjectDescriptor subject;
  AccessRequirement ar;
  Callback refreshCallback;

  @Inject
  public CreateAccessRequirementButton(
    SingleButtonView view,
    IsACTMemberAsyncHandler isACTMemberAsyncHandler,
    CookieProvider cookies,
    final PortalGinInjector ginInjector
  ) {
    this.view = view;
    this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
    this.ginInjector = ginInjector;
    this.cookies = cookies;
    view.setButtonVisible(false);
    view.addStyleNames("margin-left-10");
    view.setPresenter(this);
  }

  public void configure(AccessRequirement ar, Callback refreshCallback) {
    view.setButtonText(EDIT_ACCESS_REQUIREMENT_BUTTON_TEXT);
    view.setButtonSize(ButtonSize.DEFAULT);
    view.setButtonType(ButtonType.DEFAULT);
    view.setButtonIcon(IconType.EDIT);
    this.refreshCallback = refreshCallback;
    this.subject = null;
    this.ar = ar;
    showIfACTMember();
  }

  public void configure(
    RestrictableObjectDescriptor subject,
    Callback refreshCallback
  ) {
    view.setButtonText(CREATE_NEW_ACCESS_REQUIREMENT_BUTTON_TEXT);
    view.setButtonSize(ButtonSize.LARGE);
    view.setButtonType(ButtonType.PRIMARY);
    view.setButtonIcon(IconType.PLUS);
    this.refreshCallback = refreshCallback;
    this.subject = subject;
    this.ar = null;
    showIfACTMember();
  }

  private void showIfACTMember() {
    isACTMemberAsyncHandler.isACTActionAvailable(
      new CallbackP<Boolean>() {
        @Override
        public void invoke(Boolean isACTMember) {
          view.setButtonVisible(isACTMember);
        }
      }
    );
  }

  @Override
  public void onClick() {
    if (DisplayUtils.isInTestWebsite(cookies)) {
      useSrcWizard();
    } else {
      useSwcWizard();
    }
  }

  private void useSwcWizard() {
    LegacyCreateAccessRequirementWizard wizard =
      ginInjector.getLegacyCreateAccessRequirementWizard();
    if (subject != null) {
      wizard.configure(subject);
    } else if (ar != null) {
      wizard.configure(ar);
    }
    wizard.showModal(
      new WizardCallback() {
        @Override
        public void onFinished() {
          refreshCallback.invoke();
          view.clearWidgets();
        }

        @Override
        public void onCanceled() {
          refreshCallback.invoke();
          view.clearWidgets();
        }
      }
    );
  }

  private void useSrcWizard() {
    CreateOrUpdateAccessRequirementWizard wizard =
      ginInjector.getCreateOrUpdateAccessRequirementWizard();

    CreateOrUpdateAccessRequirementWizardProps.OnComplete onComplete =
      accessRequirementID -> {
        wizard.setOpen(false);
        view.clearWidgets();
        AccessRequirementPlace target = new AccessRequirementPlace("");
        target.putParam(
          AccessRequirementPlace.AR_ID_PARAM,
          accessRequirementID
        );
        ginInjector.getGlobalApplicationState().getPlaceChanger().goTo(target);
      };
    CreateOrUpdateAccessRequirementWizardProps.OnCancel onCancel = () -> {
      wizard.setOpen(false);
      refreshCallback.invoke();
      view.clearWidgets();
    };

    if (subject != null) {
      wizard.configure(subject, onComplete, onCancel);
    } else if (ar != null) {
      wizard.configure(ar, onComplete, onCancel);
    }
    wizard.setOpen(true);
    view.addWidget(wizard.asWidget());
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
