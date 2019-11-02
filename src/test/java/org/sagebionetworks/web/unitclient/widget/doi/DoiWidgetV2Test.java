package org.sagebionetworks.web.unitclient.widget.doi;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.doi.v2.DoiAssociation;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2View;

@RunWith(MockitoJUnitRunner.class)
public class DoiWidgetV2Test {
	private DoiWidgetV2 doiWidget;

	@Mock
	DoiWidgetV2View mockView;

	@Mock
	private DoiAssociation mockDoi;
	@Mock
	SynapseJavascriptClient mockJsClient;

	private static final String uri = "10.5072/test-uri";

	@Before
	public void before() {
		doiWidget = new DoiWidgetV2(mockView, mockJsClient);
		when(mockJsClient.getDoiAssociation(anyString(), any(ObjectType.class), anyLong())).thenReturn(getDoneFuture(mockDoi));
	}

	@Test
	public void testConfigure() throws Exception {
		when(mockDoi.getDoiUri()).thenReturn(uri);
		doiWidget.configure(mockDoi);
		verify(mockView).hide();
		verify(mockView).clear();
		verify(mockView).showDoi(uri);
	}

	@Test
	public void testConfigureNullDoi() throws Exception {
		doiWidget.configure(null);
		verify(mockView).hide();
		verify(mockView).clear();
		verify(mockView, never()).showDoi(uri);
	}

	@Test
	public void testConfigureNullUri() throws Exception {
		when(mockDoi.getDoiUri()).thenReturn(null);
		doiWidget.configure(mockDoi);
		verify(mockView).hide();
		verify(mockView).clear();
		verify(mockView, never()).showDoi(uri);
	}

	@Test
	public void testConfigureUsingObjectType() {
		when(mockDoi.getDoiUri()).thenReturn(uri);
		String objectId = "syn12333";
		ObjectType objectType = ObjectType.ENTITY;
		Long version = 4L;

		doiWidget.configure(objectId, objectType, version);

		verify(mockJsClient).getDoiAssociation(objectId, objectType, version);
		verify(mockView, atLeastOnce()).hide();
		verify(mockView, atLeastOnce()).clear();
		verify(mockView).showDoi(uri);
	}

	@Test
	public void testConfigureUsingObjectTypeFailure() {
		when(mockJsClient.getDoiAssociation(anyString(), any(ObjectType.class), anyLong())).thenReturn(getFailedFuture());

		doiWidget.configure("syn123", ObjectType.ENTITY, 44L);

		verify(mockView, atLeastOnce()).hide();
		verify(mockView, atLeastOnce()).clear();
		verify(mockView, never()).showDoi(anyString());
	}

}
