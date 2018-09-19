package org.sagebionetworks.web.unitclient.widget.doi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.doi.v2.Doi;
import org.sagebionetworks.repo.model.doi.v2.DoiCreator;
import org.sagebionetworks.repo.model.doi.v2.DoiRequest;
import org.sagebionetworks.repo.model.doi.v2.DoiResourceTypeGeneral;
import org.sagebionetworks.repo.model.doi.v2.DoiTitle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.doi.CreateOrUpdateDoiModal;
import org.sagebionetworks.web.client.widget.doi.CreateOrUpdateDoiModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.common.util.concurrent.FluentFuture;

@RunWith(MockitoJUnitRunner.class)
public class CreateOrUpdateDoiModalTest {

	private final String objectId = "syn123";
	private final ObjectType objectType = ObjectType.ENTITY;
	private final Long objectVersion = 2L;

	CreateOrUpdateDoiModal presenter;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	JobTrackingWidget mockJobTrackingWidget;
	@Mock
	CreateOrUpdateDoiModalView mockView;
	@Mock
	SynapseJavascriptClient mockSynapseClient;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	EntityUpdatedHandler mockEntityUpdatedHandler;
	@Mock
	FluentFuture<Doi> mockFluentFuture;

	@Before
	public void setup() {
		presenter = new CreateOrUpdateDoiModal(mockView, mockJobTrackingWidget, mockSynapseClient, mockSynAlert);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setSynAlert(mockSynAlert);
		verify(mockView).setJobTrackingWidget(mockJobTrackingWidget);
		verify(mockView).setPresenter(presenter);
		verify(mockView).setModalTitle(CreateOrUpdateDoiModal.DOI_MODAL_TITLE);
	}

	@Test
	public void testPopulateForms() {
	}

//	@Test
//	public void testGetExistingDoiSuccessAndPopulateForms() {
//		// Set up a DOI to populate forms
//		Doi doi = new Doi();
//		doi.setObjectId(objectId);
//		doi.setObjectType(objectType);
//		doi.setObjectVersion(objectVersion);
//		DoiResourceType resourceType = new DoiResourceType();
//		resourceType.setResourceTypeGeneral(DoiResourceTypeGeneral.Dataset);
//		List<DoiCreator> creators = new ArrayList<>();
//		DoiCreator creator = new DoiCreator();
//		creator.setCreatorName("Creator Name");
//		creators.add(creator);
//		doi.setCreators(creators);
//		List<DoiTitle> titles = new ArrayList<>();
//		DoiTitle title = new DoiTitle();
//		title.setTitle("Title");
//		titles.add(title);
//		doi.setTitles(titles);
//		doi.setPublicationYear(1990L);
//
//
//		when(mockSynapseClient.getDoi(objectId, objectType, objectVersion)).thenReturn(mockFluentFuture);
//		ArgumentCaptor<FutureCallback<Doi>> futureCallbackArgumentCaptor = new ArgumentCaptor<>();
//		ArgumentCaptor<Executor> executorArgumentCaptor = new ArgumentCaptor<>();
//
//		doAnswer(
//				new Answer<Void>() {
//					@Override
//					public Void answer(final InvocationOnMock invocation) {
//						FutureCallback<Doi> callback = (FutureCallback<Doi>) invocation.getArguments()[0];
//						callback.onSuccess(doi);
//						return null;
//					}
//				}
//		).when(mockFluentFuture).addCallback(futureCallbackArgumentCaptor.capture(), executorArgumentCaptor.capture());
//		// Call under test
//		presenter.getExistingDoi(objectId, objectType, objectVersion);
//		verify(mockSynapseClient).getDoi(objectId, objectType, objectVersion);
//	}

//	@Test
//	public void testGetExistingDoiFailure() {
//		when(mockSynapseClient.getDoi(objectId, objectType, objectVersion)).thenReturn(mockFluentFuture);
//
//		doAnswer(
//				new Answer<Void>() {
//					@Override
//					public Void answer(final InvocationOnMock invocation) throws Throwable {
//						FutureCallback<Doi> callback = (FutureCallback<Doi>) invocation.getArguments()[0];
//						callback.onSuccess(someTestEntry);
//						return null;
//					}
//				}
//		).when(mockObject).someMethod(any(P1Param.class), any(P2Param.class), any(InnerInterface.class));
//		// Call under test
//		presenter.getExistingDoi(objectId, objectType, objectVersion);
//		verify(mockSynapseClient).getDoi(objectId, objectType, objectVersion);
//	}


	@Test
	public void testConfigureAndShow() {
		when(mockSynapseClient.getDoi(objectId, objectType, objectVersion)).thenReturn(mockFluentFuture);
		// Call under test
		presenter.configureAndShow(objectId, objectType, objectVersion, mockEntityUpdatedHandler);

		verify(mockView).reset();
		verify(mockSynAlert).clear();
		verify(mockView).show();
	}

	@Test
	public void testOnSaveDoi() {
		DoiResourceTypeGeneral rtg = DoiResourceTypeGeneral.Dataset;
		Long pubYear = 2005L;

		Doi doi = new Doi();
		when(mockView.getAuthors()).thenReturn("author 1\nauthor 2");
		when(mockView.getTitles()).thenReturn("title");
		when(mockView.getResourceTypeGeneral()).thenReturn(rtg.name());
		when(mockView.getPublicationYear()).thenReturn(pubYear);

		List<DoiCreator> expectedAuthors = new ArrayList<>();
		DoiCreator creator1 = new DoiCreator();
		DoiCreator creator2 = new DoiCreator();
		creator1.setCreatorName("author 1");
		creator2.setCreatorName("author 2");
		expectedAuthors.add(creator1);
		expectedAuthors.add(creator2);


		List<DoiTitle> expectedTitles = new ArrayList<>();
		DoiTitle title = new DoiTitle();
		title.setTitle("title");
		expectedTitles.add(title);

		doi.setTitles(new ArrayList<>());
		doi.setCreators(new ArrayList<>());
		presenter.setDoi(doi);
		// Call under test
		presenter.onSaveDoi();

		verify(mockView).setIsLoading(true);
		ArgumentCaptor<AsynchronousProgressHandler> progressHandlerCaptor = ArgumentCaptor.forClass(AsynchronousProgressHandler.class);
		ArgumentCaptor<DoiRequest> doiRequestCaptor = ArgumentCaptor.forClass(DoiRequest.class);
		verify(mockJobTrackingWidget).startAndTrackJob(eq(""), eq(false), eq(AsynchType.Doi), doiRequestCaptor.capture(), progressHandlerCaptor.capture());
		assertEquals(expectedAuthors, doiRequestCaptor.getValue().getDoi().getCreators());
		assertEquals(expectedTitles, doiRequestCaptor.getValue().getDoi().getTitles());
		assertEquals(rtg, doiRequestCaptor.getValue().getDoi().getResourceType().getResourceTypeGeneral());
		assertEquals(pubYear, doiRequestCaptor.getValue().getDoi().getPublicationYear());
	}
}
