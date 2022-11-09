package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.inject.Inject;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoDialog;

public class CertifiedUserControllerImpl implements CertifiedUserController {

  PortalGinInjector ginInjector;
  AuthenticationController authController;
  QuizInfoDialog quizInfoDialog;
  PlaceChanger placeChanger;

  @Inject
  public CertifiedUserControllerImpl(
    PortalGinInjector ginInjector,
    AuthenticationController authController,
    GlobalApplicationState globalAppState
  ) {
    super();
    this.ginInjector = ginInjector;
    this.authController = authController;
    this.placeChanger = globalAppState.getPlaceChanger();
  }

  public QuizInfoDialog getQuizInfoDialog() {
    if (quizInfoDialog == null) {
      quizInfoDialog = ginInjector.getQuizInfoDialog();
    }
    return quizInfoDialog;
  }

  @Override
  public void checkUploadToEntity(EntityBundle toUpdate, Callback callback) {
    // Go directly to upload if certification is not required, or if the user is certified
    if (
      !toUpdate.getPermissions().getIsCertificationRequired() ||
      toUpdate.getPermissions().getIsCertifiedUser()
    ) {
      callback.invoke();
    } else {
      getQuizInfoDialog().show();
    }
  }

  @Override
  public void checkCreateEntity(
    EntityBundle bundle,
    String entityClassName,
    Callback callback
  ) {
    // Anyone can create a project
    if (Project.class.getName().equals(entityClassName)) {
      callback.invoke();
    } else {
      // Go directly to create the entity if certification is not required, or if the user is certified
      if (
        !bundle.getPermissions().getIsCertificationRequired() ||
        bundle.getPermissions().getIsCertifiedUser()
      ) {
        callback.invoke();
      } else {
        getQuizInfoDialog().show();
      }
    }
  }

  @Override
  public void checkDeleteEntity(EntityBundle toDelete, Callback callback) {
    // Currently both certified users and non-certified users can delete entities.
    if (!authController.isLoggedIn()) {
      // not logged in
      placeChanger.goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
    } else {
      callback.invoke();
    }
  }

  @Override
  public void checkUpdateEntity(EntityBundle toUpdate, Callback callback) {
    if (!authController.isLoggedIn()) {
      // not logged in
      placeChanger.goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
    } else {
      callback.invoke();
    }
  }
}
