package org.sagebionetworks.web.unitclient.widget.entity.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.controller.EntityRefProvEntryView;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceEntry;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceListWidget;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceListWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceURLDialogWidget;
import org.sagebionetworks.web.client.widget.entity.controller.URLProvEntryView;

public class ProvenanceListWidgetTest {

	ProvenanceListWidgetView mockView;
	PortalGinInjector mockInjector;
	EntityFinder mockEntityFinder;
	ProvenanceURLDialogWidget mockUrlDialog;
	ProvenanceListWidget presenter;
	Reference mockRef;
	EntityRefProvEntryView mockEntityProvEntry;
	URLProvEntryView mockURLProvEntry;

	String urlName = "test";
	String urlAddress = "test.com";
	String targetId = "syn123";
	Long version = 1L;
	List<ProvenanceEntry> rows = new LinkedList<ProvenanceEntry>();

	@Before
	public void setup() {
		mockView = mock(ProvenanceListWidgetView.class);
		mockInjector = mock(PortalGinInjector.class);
		mockEntityFinder = mock(EntityFinder.class);
		mockUrlDialog = mock(ProvenanceURLDialogWidget.class);
		mockRef = mock(Reference.class);
		mockEntityProvEntry = mock(EntityRefProvEntryView.class);
		mockURLProvEntry = mock(URLProvEntryView.class);
		presenter = new ProvenanceListWidget(mockView, mockInjector);
		presenter.setEntityFinder(mockEntityFinder);
		presenter.setURLDialog(mockUrlDialog);
		when(mockInjector.getEntityRefEntry()).thenReturn(mockEntityProvEntry);
		when(mockInjector.getURLEntry()).thenReturn(mockURLProvEntry);
		when(mockRef.getTargetId()).thenReturn(targetId);
		when(mockRef.getTargetVersionNumber()).thenReturn(version);
		when(mockUrlDialog.getURLName()).thenReturn(urlName);
		when(mockUrlDialog.getURLAddress()).thenReturn(urlAddress);
		rows.add(mockEntityProvEntry);
		rows.add(mockURLProvEntry);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(presenter);
	}

	@Test
	public void testConfigure() {
		presenter.configure(rows);
		verify(mockView, times(2)).addRow(AdditionalMatchers.or(eq(mockEntityProvEntry), eq(mockURLProvEntry)));
		verify(mockEntityProvEntry).setRemoveCallback(any(Callback.class));
		verify(mockURLProvEntry).setRemoveCallback(any(Callback.class));
	}

	@Test
	public void testAddEntityRow() {
		presenter.addEntityRow();
		ArgumentCaptor<SelectedHandler> captor = ArgumentCaptor.forClass(SelectedHandler.class);
		verify(mockEntityFinder).clearState();
		verify(mockEntityFinder).configure(eq(true), captor.capture());
		verify(mockEntityFinder).show();
		captor.getValue().onSelected(mockRef);
		verify(mockEntityProvEntry).configure(targetId, version.toString());
		verify(mockEntityProvEntry).setAnchorTarget(anyString());
		verify(mockEntityProvEntry).setRemoveCallback(any(Callback.class));
		verify(mockView).addRow(mockEntityProvEntry);
		verify(mockEntityFinder).hide();
	}

	@Test
	public void testAddURLRow() {
		presenter.addURLRow();
		verify(mockUrlDialog).show();
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockUrlDialog).configure(captor.capture());
		captor.getValue().invoke();
		verify(mockInjector).getURLEntry();
		verify(mockUrlDialog).getURLName();
		verify(mockUrlDialog).getURLAddress();
		verify(mockInjector).getURLEntry();
		verify(mockURLProvEntry).configure(urlName, urlAddress);
		verify(mockURLProvEntry).setAnchorTarget(urlAddress);
		verify(mockURLProvEntry).setRemoveCallback(any(Callback.class));
		verify(mockView).addRow(mockURLProvEntry);
		verify(mockUrlDialog).hide();
	}
}
