package org.sagebionetworks.web.client.widget.docker.modal;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

public class AddDockerCommitModal
  implements AddDockerCommitModalView.Presenter {

  public static final String ADD_DOCKER_COMMIT_MODAL_TITLE =
    "Add a Commit to External Docker Repository";

  private AddDockerCommitModalView view;
  private SynapseAlert synAlert;
  private SynapseJavascriptClient jsClient;
  private EventBus eventBus;
  private String dockerRepoId;
  private Callback commitAddedCallback;

  @Inject
  public AddDockerCommitModal(
    AddDockerCommitModalViewImpl view,
    SynapseAlert synAlert,
    SynapseJavascriptClient jsClient,
    EventBus eventBus
  ) {
    this.view = view;
    this.synAlert = synAlert;
    this.jsClient = jsClient;
    this.eventBus = eventBus;
    view.setPresenter(this);
    view.setAlert(synAlert.asWidget());
    view.setModalTitle(ADD_DOCKER_COMMIT_MODAL_TITLE);
  }

  public void configure(String dockerRepoId, Callback commitAddedCallback) {
    this.dockerRepoId = dockerRepoId;
    this.commitAddedCallback = commitAddedCallback;
  }

  public void configure(String dockerRepoId) {
    configure(dockerRepoId, null);
  }

  @Override
  public void onSave() {
    synAlert.clear();
    String digest = view.getDigest();
    String tag = view.getTag();

    if (digest == null || digest.isEmpty()) {
      synAlert.showError("Digest is required");
      return;
    }

    // create DockerCommit object using the auto-generated class
    DockerCommit dockerCommit = new DockerCommit();
    dockerCommit.setDigest(digest);
    dockerCommit.setTag(tag);

    // make the call to add the commit
    jsClient.addDockerCommit(
      dockerRepoId,
      dockerCommit,
      new AsyncCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
          view.hide();
          eventBus.fireEvent(new EntityUpdatedEvent(dockerRepoId));
          if (commitAddedCallback != null) {
            commitAddedCallback.invoke();
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
        }
      }
    );
  }

  @Override
  public void onCancel() {
    view.hide();
  }

  public IsWidget asWidget() {
    return view.asWidget();
  }

  public void show() {
    synAlert.clear();
    view.clear();
    view.show();
  }
}
