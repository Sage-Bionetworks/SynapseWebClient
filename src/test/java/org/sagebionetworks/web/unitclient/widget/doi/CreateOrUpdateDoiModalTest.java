package org.sagebionetworks.web.unitclient.widget.doi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.doi.v2.Doi;
import org.sagebionetworks.repo.model.doi.v2.DoiCreator;
import org.sagebionetworks.repo.model.doi.v2.DoiRequest;
import org.sagebionetworks.repo.model.doi.v2.DoiResourceType;
import org.sagebionetworks.repo.model.doi.v2.DoiResourceTypeGeneral;
import org.sagebionetworks.repo.model.doi.v2.DoiResponse;
import org.sagebionetworks.repo.model.doi.v2.DoiTitle;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.doi.CreateOrUpdateDoiModal;
import org.sagebionetworks.web.client.widget.doi.CreateOrUpdateDoiModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.common.util.concurrent.FluentFuture;

@RunWith(MockitoJUnitRunner.class)
public class CreateOrUpdateDoiModalTest {

	private static final String objectId = "syn123";
	private static final ObjectType objectType = ObjectType.ENTITY;
	private static final Long objectVersion = 2L;
	private static final DoiResourceTypeGeneral rtg = DoiResourceTypeGeneral.Collection;
	private static final Long pubYear = 2005L;
	private static final String creatorsAsString = "author 1\nauthor 2";
	private static final String titlesAsString = "title 1\ntitle 2";

	private static List<DoiCreator> creators;
	private static List<DoiTitle> titles;
	private static DoiResourceType resourceType;

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
	EntityUpdatedHandler mockEntityUpdatedHandler;
	@Mock
	PopupUtilsView mockPopupUtilsView;
	@Mock
	FluentFuture<Doi> mockFluentFuture;

	@Captor
	ArgumentCaptor<AsynchronousProgressHandler> asyncProgressHandlerCaptor;

	@Captor
	ArgumentCaptor<DoiRequest> doiRequestCaptor;


	@Before
	public void setup() {
		presenter = new CreateOrUpdateDoiModal(mockView, mockJobTrackingWidget, mockSynapseClient, mockSynAlert, mockPopupUtilsView);
		creators = new ArrayList<>();
		DoiCreator creator1 = new DoiCreator();
		DoiCreator creator2 = new DoiCreator();
		creator1.setCreatorName("author 1");
		creator2.setCreatorName("author 2");
		creators.add(creator1);
		creators.add(creator2);

		titles = new ArrayList<>();
		DoiTitle title1 = new DoiTitle();
		DoiTitle title2 = new DoiTitle();
		title1.setTitle("title 1");
		title2.setTitle("title 2");
		titles.add(title1);
		titles.add(title2);

		resourceType = new DoiResourceType();
		resourceType.setResourceTypeGeneral(rtg);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setSynAlert(mockSynAlert);
		verify(mockView).setJobTrackingWidget(mockJobTrackingWidget);
		verify(mockView).setPresenter(presenter);
		verify(mockView).setModalTitle(CreateOrUpdateDoiModal.DOI_MODAL_TITLE);
	}

	@Test
	public void testGetExistingDoiSuccessAndPopulateForms() {
		// Set up a DOI to populate forms
		Doi doi = createDoi();
		when(mockSynapseClient.getDoi(objectId, objectType, objectVersion)).thenReturn(getDoneFuture(createDoi()));

		// Call under test
		presenter.getExistingDoi(objectId, objectType, objectVersion);

		verify(mockView).show();
		verify(mockSynapseClient).getDoi(objectId, objectType, objectVersion);
		verify(mockView).setCreators(anyString());
		verify(mockView).setTitles(anyString());
		verify(mockView).setResourceTypeGeneral(rtg.name());
		verify(mockView).setPublicationYear(pubYear);
		assertEquals(presenter.getDoi(), doi);
	}

	@Test
	public void testGetExistingDoiNotFoundExceptionFailure() {
		// Make sure the DOI has none of the fields filled out beforehand
		presenter.setDoi(new Doi());

		when(mockSynapseClient.getDoi(objectId, objectType, objectVersion)).thenReturn(getFailedFuture(new NotFoundException()));

		// Call under test
		presenter.getExistingDoi(objectId, objectType, objectVersion);

		verify(mockView).show();
		assertEquals(objectId, presenter.getDoi().getObjectId());
		assertEquals(objectType, presenter.getDoi().getObjectType());
		assertEquals(objectVersion, presenter.getDoi().getObjectVersion());
	}

	@Test
	public void testGetExistingDoiOtherFailure() {
		// Make sure the DOI has none of the fields filled out beforehand
		presenter.setDoi(new Doi());

		Throwable error = new Throwable("error message");
		when(mockSynapseClient.getDoi(objectId, objectType, objectVersion)).thenReturn(getFailedFuture(error));

		// Call under test
		presenter.getExistingDoi(objectId, objectType, objectVersion);


		verify(mockView, never()).show();
		verify(mockPopupUtilsView).showErrorMessage(error.getMessage());
		assertNotEquals(objectId, presenter.getDoi().getObjectId());
		assertNotEquals(objectType, presenter.getDoi().getObjectType());
		assertNotEquals(objectVersion, presenter.getDoi().getObjectVersion());
	}

	@Test
	public void testConfigureAndShow() {
		when(mockSynapseClient.getDoi(objectId, objectType, objectVersion)).thenReturn(mockFluentFuture);
		// Call under test
		presenter.configureAndShow(objectId, objectType, objectVersion, mockEntityUpdatedHandler);

		verify(mockView).reset();
		verify(mockSynAlert).clear();
	}

	@Test
	public void testOnSaveDoiSuccess() {
		when(mockSynapseClient.getDoi(objectId, objectType, objectVersion)).thenReturn(mockFluentFuture);
		presenter.configureAndShow(objectId, objectType, objectVersion, mockEntityUpdatedHandler);
		when(mockView.getCreators()).thenReturn(creatorsAsString);
		when(mockView.getTitles()).thenReturn(titlesAsString);
		when(mockView.getResourceTypeGeneral()).thenReturn(rtg.name());
		when(mockView.getPublicationYear()).thenReturn(pubYear);

		presenter.setDoi(new Doi());

		// Call under test
		presenter.onSaveDoi();

		verify(mockJobTrackingWidget).startAndTrackJob(eq(""), eq(false), eq(AsynchType.Doi),
				any(DoiRequest.class),  asyncProgressHandlerCaptor.capture());

		DoiResponse response = new DoiResponse();
		response.setDoi(createDoi());
		asyncProgressHandlerCaptor.getValue().onComplete(response);

		verify(mockView).setIsLoading(true);
		verify(mockView).setIsLoading(false);
		verify(mockView).hide();
		verify(mockJobTrackingWidget).startAndTrackJob(eq(""), eq(false), eq(AsynchType.Doi), doiRequestCaptor.capture(), any(AsynchronousProgressHandler.class));
		verify(mockEntityUpdatedHandler).onPersistSuccess(any(EntityUpdatedEvent.class));
		assertEquals(creators, doiRequestCaptor.getValue().getDoi().getCreators());
		assertEquals(titles, doiRequestCaptor.getValue().getDoi().getTitles());
		assertEquals(rtg, doiRequestCaptor.getValue().getDoi().getResourceType().getResourceTypeGeneral());
		assertEquals(pubYear, doiRequestCaptor.getValue().getDoi().getPublicationYear());
	}



	@Test
	public void testOnSaveDoiFailure() {
		when(mockView.getCreators()).thenReturn(creatorsAsString);
		when(mockView.getTitles()).thenReturn(titlesAsString);
		when(mockView.getResourceTypeGeneral()).thenReturn(rtg.name());
		when(mockView.getPublicationYear()).thenReturn(pubYear);

		presenter.setDoi(new Doi());

		// Call under test
		presenter.onSaveDoi();

		verify(mockJobTrackingWidget).startAndTrackJob(eq(""), eq(false), eq(AsynchType.Doi),
				any(DoiRequest.class), asyncProgressHandlerCaptor.capture());

		NotFoundException failureException = new NotFoundException();
		asyncProgressHandlerCaptor.getValue().onFailure(failureException);

		verify(mockView).setIsLoading(false);
		verify(mockJobTrackingWidget).startAndTrackJob(eq(""), eq(false), eq(AsynchType.Doi), any(DoiRequest.class), any(AsynchronousProgressHandler.class));
		verify(mockSynAlert).handleException(failureException);
	}

	@Test
	public void testOnSaveDoiCancel() {
		when(mockView.getCreators()).thenReturn(creatorsAsString);
		when(mockView.getTitles()).thenReturn(titlesAsString);
		when(mockView.getResourceTypeGeneral()).thenReturn(rtg.name());
		when(mockView.getPublicationYear()).thenReturn(pubYear);

		presenter.setDoi(new Doi());

		// Call under test
		presenter.onSaveDoi();

		verify(mockJobTrackingWidget).startAndTrackJob(eq(""), eq(false), eq(AsynchType.Doi),
				any(DoiRequest.class), asyncProgressHandlerCaptor.capture());

		NotFoundException failureException = new NotFoundException();
		asyncProgressHandlerCaptor.getValue().onCancel();

		verify(mockView).setIsLoading(false);
		verify(mockJobTrackingWidget).startAndTrackJob(eq(""), eq(false), eq(AsynchType.Doi), any(DoiRequest.class), any(AsynchronousProgressHandler.class));
	}

	@Test
	public void testPopulateFormsNonNull() {
		// All fields are filled out in the createDoi method
		Doi doi = createDoi();
		// Call under test
		presenter.populateForms(doi);
		verify(mockView).setCreators(creatorsAsString);
		verify(mockView).setTitles(titlesAsString);
		verify(mockView).setResourceTypeGeneral(rtg.name());
		verify(mockView).setPublicationYear(pubYear);
	}

	@Test
	public void testPopulateFormsNullDoi() {
		// Call under test
		presenter.populateForms(null);
		verify(mockView).setCreators("");
		verify(mockView).setTitles("");
		verify(mockView).setResourceTypeGeneral(DoiResourceTypeGeneral.Dataset.name());
		verify(mockView).setPublicationYear(2018L);
	}

	@Test
	public void testPopulateFormsNullCreators() {
		// All fields are filled out in the createDoi method
		Doi doi = createDoi();
		doi.setCreators(null);
		// Call under test
		presenter.populateForms(doi);
		verify(mockView).setCreators("");
		verify(mockView).setTitles(titlesAsString);
		verify(mockView).setResourceTypeGeneral(rtg.name());
		verify(mockView).setPublicationYear(pubYear);
	}

	@Test
	public void testPopulateFormsNullTitles() {
		// All fields are filled out in the createDoi method
		Doi doi = createDoi();
		doi.setTitles(null);
		// Call under test
		presenter.populateForms(doi);
		verify(mockView).setCreators(creatorsAsString);
		verify(mockView).setTitles("");
		verify(mockView).setResourceTypeGeneral(rtg.name());
		verify(mockView).setPublicationYear(pubYear);
	}

	@Test
	public void testPopulateFormsNullResourceType() {
		// All fields are filled out in the createDoi method
		Doi doi = createDoi();
		doi.setResourceType(null);
		// Call under test
		presenter.populateForms(doi);
		verify(mockView).setCreators(creatorsAsString);
		verify(mockView).setTitles(titlesAsString);
		verify(mockView).setResourceTypeGeneral(DoiResourceTypeGeneral.Dataset.name());
		verify(mockView).setPublicationYear(pubYear);
	}

	@Test
	public void testPopulateFormsNullPublicationYear() {
		// All fields are filled out in the createDoi method
		Doi doi = createDoi();
		doi.setPublicationYear(null);
		// Call under test
		presenter.populateForms(doi);
		verify(mockView).setCreators(creatorsAsString);
		verify(mockView).setTitles(titlesAsString);
		verify(mockView).setResourceTypeGeneral(rtg.name());
		verify(mockView).setPublicationYear(2018L);
	}

	@Test
	public void testParseCreatorStringEmptyString() {
		List<DoiCreator> expected = new ArrayList<>();
		String creators = "";
		List<DoiCreator> actual = CreateOrUpdateDoiModal.parseCreatorsString(creators);
		assertEquals(expected, actual);
	}

	@Test
	public void testParseCreatorStringMultipleCreators() {
		// The string and List of creators set up for other tests will work for this test
		List<DoiCreator> actual = CreateOrUpdateDoiModal.parseCreatorsString(creatorsAsString);
		assertEquals(creators, actual);
	}

	@Test
	public void testParseTitlesStringEmptyString() {
		List<DoiTitle> expected = new ArrayList<>();
		String titles = "";
		List<DoiTitle> actual = CreateOrUpdateDoiModal.parseTitlesString(titles);
		assertEquals(expected, actual);
	}

	@Test
	public void testParseTitlesStringMultipleTitles() {
		// The string and List of titles set up for other tests will work for this test
		List<DoiTitle> actual = CreateOrUpdateDoiModal.parseTitlesString(titlesAsString);
		assertEquals(titles, actual);
	}

	@Test
	public void testConvertCreatorsToCreatorStringEmptyList() {
		String expected = "";
		List<DoiCreator> list = new ArrayList<>();
		String actual = CreateOrUpdateDoiModal.convertMultipleCreatorsToString(list);
		assertEquals(expected, actual);
	}

	@Test
	public void testConvertCreatorsToCreatorStringPopulatedList() {
		// The string and List of creators set up for other tests will work for this test
		String actual = CreateOrUpdateDoiModal.convertMultipleCreatorsToString(creators);
		assertEquals(creatorsAsString, actual);
	}

	@Test
	public void testConvertTitlesToStringEmptyList() {
		String expected = "";
		List<DoiTitle> titles = new ArrayList<>();
		String actual = CreateOrUpdateDoiModal.convertMultipleTitlesToString(titles);
		assertEquals(expected, actual);
	}

	@Test
	public void testConvertTitlesToStringPopulatedList() {
		// The string and List of titles set up for other tests will work for this test
		String actual = CreateOrUpdateDoiModal.convertMultipleTitlesToString(titles);
		assertEquals(titlesAsString, actual);
	}

	@Test
	public void testHide() {
		presenter.hide();
		verify(mockView).hide();
	}

	@Test
	public void testAsWidget() {
		presenter.asWidget();
		verify(mockView).asWidget();
	}

	private static Doi createDoi() {
		Doi doi = new Doi();

		doi.setObjectId(objectId);
		doi.setObjectType(objectType);
		doi.setObjectVersion(objectVersion);
		doi.setTitles(titles);
		doi.setCreators(creators);
		doi.setPublicationYear(pubYear);
		doi.setResourceType(resourceType);
		return doi;
	}
}
