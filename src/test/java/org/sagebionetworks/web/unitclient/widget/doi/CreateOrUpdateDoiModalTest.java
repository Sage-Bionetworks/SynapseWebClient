package org.sagebionetworks.web.unitclient.widget.doi;

import static com.google.gwt.thirdparty.guava.common.util.concurrent.MoreExecutors.directExecutor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
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
import com.google.common.util.concurrent.FutureCallback;
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
	private static final String principalId = "123";
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
	public void testConfigureAndShow() {
		when(mockEntity.getId()).thenReturn(objectId);
		when(mockSynapseClient.getDoi(objectId, objectType, objectVersion)).thenReturn(mockDoiFuture);
		// Call under test
		presenter.configureAndShow(mockEntity, objectVersion, principalId);

		verify(mockView).reset();
		verify(mockSynAlert).clear();
	}

	@Test
	public void testConfigureAndShowSuccessAndPopulateForms() {
		when(mockEntity.getId()).thenReturn(objectId);
		boolean doiExists = true;
		// Set up a DOI to populate forms
		Doi doi = createDoi();
		when(mockSynapseClient.getDoi(objectId, ObjectType.ENTITY, objectVersion)).thenReturn(getDoneFuture(doi));

		// Call under test
		presenter.configureAndShow(mockEntity, objectVersion, principalId);

		verify(mockView).show();
		verify(mockSynapseClient).getDoi(objectId, ObjectType.ENTITY, objectVersion);
		verify(mockView).setCreators(anyString());
		verify(mockView).setTitles(anyString());
		verify(mockView).setResourceTypeGeneral(rtg.name());
		verify(mockView).setPublicationYear(pubYear);
		assertEquals(presenter.getDoi(), doi);
	}


	@Test
	public void testConfigureAndShowNotFoundExceptionFailure() {
		when(mockEntity.getId()).thenReturn(objectId);
		when(mockEntity.getName()).thenReturn(entityName);
		when(mockDateTimeUtils.getYear(any(Date.class))).thenReturn(pubYearString);
		Doi expectedDoi = new Doi();
		expectedDoi.setObjectId(objectId);
		expectedDoi.setObjectType(ObjectType.ENTITY);
		expectedDoi.setObjectVersion(objectVersion);
		expectedDoi.setCreators(new ArrayList<>());
		expectedDoi.getCreators().add(new DoiCreator());
		expectedDoi.getCreators().get(0).setCreatorName(lastName + ", " + firstName);
		expectedDoi.setTitles(new ArrayList<>());
		expectedDoi.getTitles().add(new DoiTitle());
		expectedDoi.getTitles().get(0).setTitle(entityName);
		expectedDoi.setPublicationYear(pubYear);
		expectedDoi.setResourceType(new DoiResourceType());
		expectedDoi.getResourceType().setResourceTypeGeneral(DoiResourceTypeGeneral.Dataset);
		boolean doiExists = false;
		// Make sure the DOI has none of the fields filled out beforehand
		presenter.setDoi(new Doi());

		when(mockSynapseClient.getDoi(objectId, ObjectType.ENTITY, objectVersion)).thenReturn(getFailedFuture(new NotFoundException()));
		when(mockSynapseClient.getUserProfile(principalId)).thenReturn(getDoneFuture(mockUserProfile));
		when(mockUserProfile.getLastName()).thenReturn(lastName);
		when(mockUserProfile.getFirstName()).thenReturn(firstName);


		// Call under test
		presenter.configureAndShow(mockEntity, objectVersion, principalId);

		verify(mockView).show();
		assertEquals(expectedDoi, presenter.getDoi());
	}

	@Test
	public void testConfigureAndShowOtherFailure() {
		when(mockEntity.getId()).thenReturn(objectId);
		// Make sure the DOI has none of the fields filled out beforehand
		presenter.setDoi(new Doi());

		Throwable error = new Throwable("error message");
		when(mockSynapseClient.getDoi(objectId, ObjectType.ENTITY, objectVersion)).thenReturn(getFailedFuture(error));

		// Call under test
		presenter.configureAndShow(mockEntity, objectVersion, principalId);


		verify(mockView, never()).show();
		verify(mockPopupUtilsView).showErrorMessage(error.getMessage());
		assertNotEquals(objectId, presenter.getDoi().getObjectId());
		assertNotEquals(objectType, presenter.getDoi().getObjectType());
		assertNotEquals(objectVersion, presenter.getDoi().getObjectVersion());
	}

	@Test
	public void testCreateNewDoiWithFormatNameSuccess() {
		when(mockEntity.getId()).thenReturn(objectId);
		when(mockEntity.getName()).thenReturn(entityName);
		when(mockUserProfile.getFirstName()).thenReturn(firstName);
		when(mockUserProfile.getLastName()).thenReturn(lastName);
		when(mockSynapseClient.getUserProfile(principalId)).thenReturn(getDoneFuture(mockUserProfile));
		when(mockDateTimeUtils.getYear(any(Date.class))).thenReturn(pubYearString);

		// Call under test
		presenter.createNewDoiAndShow(mockEntity, objectVersion, principalId);

		Doi result = presenter.getDoi();
		assertEquals(objectId, result.getObjectId());
		assertEquals(ObjectType.ENTITY, result.getObjectType());
		assertEquals(objectVersion, result.getObjectVersion());
		assertEquals(CreateOrUpdateDoiModal.getSuggestedResourceTypeGeneral(
				EntityTypeUtils.getEntityTypeForEntityClassName(mockEntity.getClass().getName())
		), result.getResourceType().getResourceTypeGeneral());
		assertEquals(entityName, result.getTitles().get(0).getTitle());
		assertEquals(CreateOrUpdateDoiModal.formatPersonalName(lastName, firstName), result.getCreators().get(0).getCreatorName());
		assertEquals(pubYear, result.getPublicationYear());
		verify(mockView).showOverwriteWarning(false);
		verify(mockView).show();
	}

	@Test
	public void testCreateNewDoiAndShowWithFormatNameFailure() {
		when(mockEntity.getId()).thenReturn(objectId);
		when(mockEntity.getName()).thenReturn(entityName);
		String errorMessage = "error";
		when(mockSynapseClient.getUserProfile(principalId)).thenReturn(getFailedFuture(new Throwable(errorMessage)));
		when(mockDateTimeUtils.getYear(any(Date.class))).thenReturn(pubYearString);


		// Call under test
		presenter.createNewDoiAndShow(mockEntity, objectVersion, principalId);

		Doi result = presenter.getDoi();
		assertEquals(objectId, result.getObjectId());
		assertEquals(ObjectType.ENTITY, result.getObjectType());
		assertEquals(objectVersion, result.getObjectVersion());
		assertEquals(CreateOrUpdateDoiModal.getSuggestedResourceTypeGeneral(
				EntityTypeUtils.getEntityTypeForEntityClassName(mockEntity.getClass().getName())
		), result.getResourceType().getResourceTypeGeneral());
		assertEquals(entityName, result.getTitles().get(0).getTitle());
		assertEquals(new ArrayList<>(), result.getCreators());
		assertEquals(pubYear, result.getPublicationYear());
		verify(mockPopupUtilsView).showErrorMessage(errorMessage);
		verify(mockView).showOverwriteWarning(false);
		verify(mockView).show();
	}


	@Test
	public void testOnSaveDoiSuccess() {
		when(mockEntity.getId()).thenReturn(objectId);
		when(mockSynapseClient.getDoi(objectId, objectType, objectVersion)).thenReturn(mockDoiFuture);
		presenter.configureAndShow(mockEntity, objectVersion, principalId);
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

		NotFoundException failureException = new NotFoundException();
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
		Date testingDate = new Date();
		testingDate.setTime(100); // 100 ms after unix epoch (1970-01-01)
		String expectedYear = "1970";
		when(mockDateTimeUtils.getCurrentDate()).thenReturn(testingDate);
		when(mockDateTimeUtils.getYear(testingDate)).thenReturn(expectedYear);
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
	public void testGetFormattedCreatorNameFromPrincipalIdSuccess() {
		when(mockUserProfile.getLastName()).thenReturn(lastName);
		when(mockUserProfile.getFirstName()).thenReturn(firstName);
		when(mockSynapseClient.getUserProfile(principalId)).thenReturn(getDoneFuture(mockUserProfile));
		FluentFuture<String> formattedNameFuture = presenter.getFormattedCreatorNameFromPrincipalId(principalId);
		// Null check + empty string check + formatting would be 3 invocations of each field
		verify(mockUserProfile, times(3)).getLastName();
		verify(mockUserProfile, times(3)).getFirstName();

		formattedNameFuture.addCallback(new FutureCallback<String>() {
			@Override
			public void onSuccess(@NullableDecl String result) {
				// Name should be formatted according to the static method
				assertEquals(CreateOrUpdateDoiModal.formatPersonalName(lastName, firstName), result);
			}

			@Override
			public void onFailure(Throwable t) {
				fail("Expected to succeed");
			}
		}, directExecutor());
	}

	@Test
	public void testGetFormattedCreatorNameFromPrincipalIdMissingName() {
		when(mockUserProfile.getLastName()).thenReturn("Other name is missing");
		when(mockUserProfile.getFirstName()).thenReturn("");
		when(mockSynapseClient.getUserProfile(principalId)).thenReturn(getDoneFuture(mockUserProfile));
		FluentFuture<String> formattedNameFuture = presenter.getFormattedCreatorNameFromPrincipalId(principalId);
		// Null check + empty string check would be 2 invocations of each field
		verify(mockUserProfile, times(2)).getLastName();
		verify(mockUserProfile, times(2)).getFirstName();

		formattedNameFuture.addCallback(new FutureCallback<String>() {
			@Override
			public void onSuccess(@NullableDecl String result) {
				// Since a name was missing, the formatted name should just be an empty string
				assertEquals("", result);
			}

			@Override
			public void onFailure(Throwable t) {
				fail("Expected to succeed");
			}
		}, directExecutor());
	}

	@Test
	public void testGetFormattedCreatorNameFromPrincipalIdFailure() {
		when(mockSynapseClient.getUserProfile(principalId)).thenReturn(getFailedFuture());
		FluentFuture<String> failedFuture = presenter.getFormattedCreatorNameFromPrincipalId(principalId);
		failedFuture.addCallback(new FutureCallback<String>() {
			@Override
			public void onSuccess(@NullableDecl String result) {
				fail("Expected future to fail.");
			}

			@Override
			public void onFailure(Throwable t) {
				// As expected
			}
		}, directExecutor());
	}

	@Test
	public void testFormatName() {
		String expected = lastName + ", " + firstName;
		assertEquals(expected, CreateOrUpdateDoiModal.formatPersonalName(lastName, firstName));
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
