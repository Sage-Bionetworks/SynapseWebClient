package org.sagebionetworks.web.unitclient.widget.doi;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.doi.v2.DoiAssociation;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2View;

@RunWith(MockitoJUnitRunner.class)
public class DoiWidgetV2Test {
	private DoiWidgetV2 doiWidget;

	@Mock
	DoiWidgetV2View mockView;

	@Mock
	private DoiAssociation mockDoi;

	private static final String uri = "10.5072/test-uri";

	@Before
	public void before() {
		doiWidget = new DoiWidgetV2(mockView);
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
	
}
