package org.sagebionetworks.web.unitclient.widget.docker.modal;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModal.ADD_EXTERNAL_REPO_MODAL_TITLE;
import static org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModal.SUCCESS_MESSAGE;
import static org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModal.SUCCESS_TITLE;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModal;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class AddExternalRepositoryModalTest {
	@Mock
	private AddExternalRepoModalView mockView;
	@Mock
	private SynapseAlert mockSynAlert;
	@Mock
	private SynapseJavascriptClient mockJsClient;
	@Mock
	private DockerRepository mockDockerEntity;
	@Mock
	private Callback mockCallback;

	AddExternalRepoModal modal;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		modal = new AddExternalRepoModal(mockView, mockSynAlert, mockJsClient);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(modal);
		verify(mockView).setAlert(any(Widget.class));
		verify(mockView).setModalTitle(ADD_EXTERNAL_REPO_MODAL_TITLE);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testOnSave() {
		modal.onSave();
		verify(mockView).getRepoName();
		verify(mockJsClient).createEntity(any(DockerRepository.class), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testOnSaveSuccess() {
		AsyncMockStubber.callSuccessWith(mockDockerEntity).when(mockJsClient).createEntity(any(DockerRepository.class), any(AsyncCallback.class));
		modal.configuration("syn123", mockCallback);
		modal.onSave();
		verify(mockView).hideDialog();
		verify(mockView).showSuccess(SUCCESS_TITLE, SUCCESS_MESSAGE);
		verify(mockCallback).invoke();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testOnSaveFailure() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockJsClient).createEntity(any(DockerRepository.class), any(AsyncCallback.class));
		modal.onSave();
		verify(mockView).resetButton();
		verify(mockSynAlert).handleException(any(Throwable.class));
	}

	@Test
	public void testShowDialog() {
		modal.show();
		verify(mockView).clear();
		verify(mockView).showDialog();
		verify(mockSynAlert).clear();
	}

	@Test
	public void testHideDialog() {
		modal.hide();
		verify(mockView).hideDialog();
	}

	@Test
	public void asWidgetTest() {
		modal.asWidget();
		verify(mockView).asWidget();
	}
}
