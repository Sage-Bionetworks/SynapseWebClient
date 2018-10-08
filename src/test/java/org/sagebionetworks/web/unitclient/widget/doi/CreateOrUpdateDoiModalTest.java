package org.sagebionetworks.web.unitclient.widget.doi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.doi.v2.Doi;
import org.sagebionetworks.repo.model.doi.v2.DoiCreator;
import org.sagebionetworks.repo.model.doi.v2.DoiRequest;
import org.sagebionetworks.repo.model.doi.v2.DoiResourceType;
import org.sagebionetworks.repo.model.doi.v2.DoiResourceTypeGeneral;
import org.sagebionetworks.repo.model.doi.v2.DoiResponse;
import org.sagebionetworks.repo.model.doi.v2.DoiTitle;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.doi.CreateOrUpdateDoiModal;
import org.sagebionetworks.web.client.widget.doi.CreateOrUpdateDoiModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.common.util.concurrent.FluentFuture;
import com.google.gwt.event.shared.EventBus;

@RunWith(MockitoJUnitRunner.class)
public class CreateOrUpdateDoiModalTest {

	private static final String objectId = "syn123";
	private static final ObjectType objectType = ObjectType.ENTITY;
	private static final Long objectVersion = 2L;
	private static final DoiResourceTypeGeneral rtg = DoiResourceTypeGeneral.Collection;
	private static final String pubYearString = "2005";
	private static final Long pubYear = 2005L;
	private static final String creatorsAsString = "author 1\nauthor 2";
	private static final String titlesAsString = "title 1\ntitle 2";
	private static final String lastName = "Last-name";
	private static final String firstName = "FIRST";
	private static final String entityName = "A Cool Entity";
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
	EventBus mockEventBus;
	@Mock
	Entity mockEntity;
	@Mock
	UserProfile mockUserProfile;
	@Mock
	PopupUtilsView mockPopupUtilsView;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	@Mock
	FluentFuture<Doi> mockDoiFuture;

	@Captor
	ArgumentCaptor<AsynchronousProgressHandler> asyncProgressHandlerCaptor;

	@Captor
	ArgumentCaptor<DoiRequest> doiRequestCaptor;


	@Before
	public void setup() {
		presenter = new CreateOrUpdateDoiModal(mockView, mockJobTrackingWidget, mockSynapseClient, mockSynAlert, mockPopupUtilsView, mockEventBus, mockDateTimeUtils);
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
	public void testConfigureAndShowDoiExists() {
		boolean doiExists = true;
		Doi doi = createDoi();
		when(mockEntity.getId()).thenReturn(objectId);
		when(mockSynapseClient.getDoi(objectId, objectType, objectVersion)).thenReturn(getDoneFuture(doi));

		// Call under test
		presenter.configureAndShow(mockEntity, objectVersion, mockUserProfile);

		// Ensure that populateForms() was called
		verify(mockView).setCreators(anyString());
		verify(mockView).setTitles(anyString());
		verify(mockView).setResourceTypeGeneral(anyString());
		verify(mockView).setPublicationYear(anyLong());

		InOrder viewInOrderVerifier = Mockito.inOrder(mockView);
		viewInOrderVerifier.verify(mockView).reset();
		viewInOrderVerifier.verify(mockView).showOverwriteWarning(doiExists);
		viewInOrderVerifier.verify(mockView).show();
		verify(mockSynAlert).clear();

		assertEquals(doi.getObjectId(), presenter.getDoi().getObjectId());
		assertEquals(doi.getObjectType(), presenter.getDoi().getObjectType());
		assertEquals(doi.getObjectVersion(), presenter.getDoi().getObjectVersion());
		assertEquals(doi.getCreators(), presenter.getDoi().getCreators());
		assertEquals(doi.getTitles(), presenter.getDoi().getTitles());
		assertEquals(doi.getResourceType(), presenter.getDoi().getResourceType());
		assertEquals(doi.getPublicationYear(), presenter.getDoi().getPublicationYear());
	}

	@Test
	public void testConfigureAndShowNotFoundExceptionFailure() {
		when(mockEntity.getId()).thenReturn(objectId);
		when(mockEntity.getName()).thenReturn(entityName);
		when(mockUserProfile.getLastName()).thenReturn(lastName);
		when(mockUserProfile.getFirstName()).thenReturn(firstName);
		when(mockDateTimeUtils.getYear(any(Date.class))).thenReturn(pubYearString);
		when(mockSynapseClient.getDoi(objectId, ObjectType.ENTITY, objectVersion)).thenReturn(getFailedFuture(new NotFoundException()));

		Doi expectedDoi = new Doi();
		expectedDoi.setObjectId(objectId);
		expectedDoi.setObjectType(ObjectType.ENTITY);
		expectedDoi.setObjectVersion(objectVersion);
		List<DoiCreator> expectedCreators = new ArrayList<>();
		DoiCreator expectedCreator = new DoiCreator();
		expectedCreator.setCreatorName(CreateOrUpdateDoiModal.getFormattedCreatorName(mockUserProfile));
		expectedCreators.add(expectedCreator);
		expectedDoi.setCreators(expectedCreators);
		List<DoiTitle> expectedTitles = new ArrayList<>();
		DoiTitle expectedTitle = new DoiTitle();
		expectedTitle.setTitle(entityName);
		expectedTitles.add(expectedTitle);
		expectedDoi.setTitles(expectedTitles);
		expectedDoi.setPublicationYear(pubYear);
		expectedDoi.setResourceType(new DoiResourceType());
		expectedDoi.getResourceType().setResourceTypeGeneral(DoiResourceTypeGeneral.Dataset);

		// Call under test
		presenter.configureAndShow(mockEntity, objectVersion, mockUserProfile);

		// Ensure that populateForms() was called
		verify(mockView).setCreators(anyString());
		verify(mockView).setTitles(anyString());
		verify(mockView).setResourceTypeGeneral(anyString());
		verify(mockView).setPublicationYear(anyLong());

		boolean doiExists = false;
		InOrder viewInOrderVerifier = Mockito.inOrder(mockView);
		viewInOrderVerifier.verify(mockView).reset();
		viewInOrderVerifier.verify(mockView).showOverwriteWarning(doiExists);
		viewInOrderVerifier.verify(mockView).show();
		verify(mockSynAlert).clear();

		assertEquals(expectedDoi.getObjectId(), presenter.getDoi().getObjectId());
		assertEquals(expectedDoi.getObjectType(), presenter.getDoi().getObjectType());
		assertEquals(expectedDoi.getObjectVersion(), presenter.getDoi().getObjectVersion());
		assertEquals(expectedDoi.getCreators(), presenter.getDoi().getCreators());
		assertEquals(expectedDoi.getTitles(), presenter.getDoi().getTitles());
		assertEquals(expectedDoi.getResourceType(), presenter.getDoi().getResourceType());
		assertEquals(expectedDoi.getPublicationYear(), presenter.getDoi().getPublicationYear());
	}

	@Test
	public void testConfigureAndShowOtherFailure() {
		when(mockEntity.getId()).thenReturn(objectId);
		Throwable error = new Throwable("error message");
		when(mockSynapseClient.getDoi(objectId, ObjectType.ENTITY, objectVersion)).thenReturn(getFailedFuture(error));

		// Call under test
		presenter.configureAndShow(mockEntity, objectVersion, mockUserProfile);

		verify(mockView, never()).show();
		verify(mockPopupUtilsView).showErrorMessage(error.getMessage());
	}

	@Test
	public void testCreateNewDoi() {
		when(mockEntity.getId()).thenReturn(objectId);
		when(mockEntity.getName()).thenReturn(entityName);
		when(mockUserProfile.getFirstName()).thenReturn(firstName);
		when(mockUserProfile.getLastName()).thenReturn(lastName);
		when(mockDateTimeUtils.getYear(any(Date.class))).thenReturn(pubYearString);

		DoiCreator expectedCreator = new DoiCreator();
		expectedCreator.setCreatorName(CreateOrUpdateDoiModal.getFormattedCreatorName(mockUserProfile));
		List<DoiCreator> expectedCreators = new ArrayList<>();
		expectedCreators.add(expectedCreator);

		DoiTitle expectedTitle = new DoiTitle();
		expectedTitle.setTitle(entityName);
		List<DoiTitle> expectedTitles = new ArrayList<>();
		expectedTitles.add(expectedTitle);

		// Call under test
		Doi result = presenter.createNewDoi(mockEntity, objectVersion, mockUserProfile);

		assertEquals(objectId, result.getObjectId());
		assertEquals(ObjectType.ENTITY, result.getObjectType());
		assertEquals(objectVersion, result.getObjectVersion());
		assertEquals(CreateOrUpdateDoiModal.getSuggestedResourceTypeGeneral(
				EntityTypeUtils.getEntityTypeForEntityClassName(mockEntity.getClass().getName())
		), result.getResourceType().getResourceTypeGeneral());
		assertEquals(expectedCreators, result.getCreators());
		assertEquals(expectedTitles, result.getTitles());
		assertEquals(pubYearString, result.getPublicationYear().toString());
	}

	@Test
	public void testOnSaveDoiSuccess() {
		when(mockView.getCreators()).thenReturn(creatorsAsString);
		when(mockView.getTitles()).thenReturn(titlesAsString);
		when(mockView.getResourceTypeGeneral()).thenReturn(rtg.name());
		when(mockView.getPublicationYear()).thenReturn(pubYear);

		Doi doi = new Doi();
		doi.setObjectId(objectId);
		presenter.setDoi(doi);

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
		verify(mockPopupUtilsView).showInfo(CreateOrUpdateDoiModal.DOI_CREATED_MESSAGE + objectId);
		verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
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

		asyncProgressHandlerCaptor.getValue().onCancel();

		verify(mockView).setIsLoading(false);
		verify(mockJobTrackingWidget).startAndTrackJob(eq(""), eq(false), eq(AsynchType.Doi), any(DoiRequest.class), any(AsynchronousProgressHandler.class));
	}

	@Test
	public void testPopulateFormsNonNull() {
		// All fields are filled out in the createDoi method
		Doi doi = createDoi();
		presenter.setDoi(doi);
		// Call under test
		presenter.populateForms();
		verify(mockView).setCreators(creatorsAsString);
		verify(mockView).setTitles(titlesAsString);
		verify(mockView).setResourceTypeGeneral(rtg.name());
		verify(mockView).setPublicationYear(pubYear);
	}

	@Test
	public void testPopulateFormsNullDoi() {
		presenter.setDoi(null);
		when(mockDateTimeUtils.getYear(any(Date.class))).thenReturn(pubYearString);
		// Call under test
		presenter.populateForms();
		verify(mockView).setCreators("");
		verify(mockView).setTitles("");
		verify(mockView).setResourceTypeGeneral(DoiResourceTypeGeneral.Dataset.name());
		verify(mockView).setPublicationYear(pubYear);
	}

	@Test
	public void testPopulateFormsNullCreators() {
		// All fields are filled out in the createDoi method
		Doi doi = createDoi();
		doi.setCreators(null);
		presenter.setDoi(doi);
		// Call under test
		presenter.populateForms();
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
		presenter.setDoi(doi);
		// Call under test
		presenter.populateForms();
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
		presenter.setDoi(doi);
		// Call under test
		presenter.populateForms();
		verify(mockView).setCreators(creatorsAsString);
		verify(mockView).setTitles(titlesAsString);
		verify(mockView).setResourceTypeGeneral(DoiResourceTypeGeneral.Dataset.name());
		verify(mockView).setPublicationYear(pubYear);
	}

	@Test
	public void testPopulateFormsNullPublicationYear() {
		String expectedYear = "1970";
		when(mockDateTimeUtils.getYear(any(Date.class))).thenReturn(expectedYear);
		// All fields are filled out in the createDoi method
		Doi doi = createDoi();
		doi.setPublicationYear(null);
		presenter.setDoi(doi);
		// Call under test
		presenter.populateForms();
		verify(mockView).setCreators(creatorsAsString);
		verify(mockView).setTitles(titlesAsString);
		verify(mockView).setResourceTypeGeneral(rtg.name());
		verify(mockView).setPublicationYear(1970L);
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
	public void testGetFormattedCreatorName() {
		when(mockUserProfile.getLastName()).thenReturn(lastName);
		when(mockUserProfile.getFirstName()).thenReturn(firstName);
		String actual = CreateOrUpdateDoiModal.getFormattedCreatorName(mockUserProfile);
		String expected = lastName + ", " + firstName;

		assertEquals(expected, actual);
	}

	@Test
	public void testGetFormattedCreatorNameNullUserProfile() {
		String actual = CreateOrUpdateDoiModal.getFormattedCreatorName(null);
		String expected = "";
		assertEquals(expected, actual);
	}

	@Test
	public void testGetFormattedCreatorNameNullLastName() {
		when(mockUserProfile.getLastName()).thenReturn(null);
		when(mockUserProfile.getFirstName()).thenReturn(firstName);
		String actual = CreateOrUpdateDoiModal.getFormattedCreatorName(mockUserProfile);
		String expected = "";

		assertEquals(expected, actual);
	}

	@Test
	public void testGetFormattedCreatorNameNullFirstName() {
		when(mockUserProfile.getLastName()).thenReturn(lastName);
		when(mockUserProfile.getFirstName()).thenReturn(null);
		String actual = CreateOrUpdateDoiModal.getFormattedCreatorName(mockUserProfile);
		String expected = "";

		assertEquals(expected, actual);
	}

	@Test
	public void testGetFormattedCreatorNameEmptyLastName() {
		when(mockUserProfile.getLastName()).thenReturn("");
		when(mockUserProfile.getFirstName()).thenReturn(firstName);
		String actual = CreateOrUpdateDoiModal.getFormattedCreatorName(mockUserProfile);
		String expected = "";

		assertEquals(expected, actual);
	}

	@Test
	public void testGetFormattedCreatorNameEmptyFirstName() {
		when(mockUserProfile.getLastName()).thenReturn(lastName);
		when(mockUserProfile.getFirstName()).thenReturn("");
		String actual = CreateOrUpdateDoiModal.getFormattedCreatorName(mockUserProfile);
		String expected = "";

		assertEquals(expected, actual);
	}

	@Test
	public void testGetSuggestedResourceTypeGeneral() {
		for (EntityType entityType : EntityType.values()) {
			// Call under test
			DoiResourceTypeGeneral actual = CreateOrUpdateDoiModal.getSuggestedResourceTypeGeneral(entityType);
			if (entityType.equals(EntityType.project) || entityType.equals(EntityType.folder)) {
				assertEquals(DoiResourceTypeGeneral.Collection, actual);
			} else {
				assertEquals(DoiResourceTypeGeneral.Dataset, actual);
			}
		}
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
