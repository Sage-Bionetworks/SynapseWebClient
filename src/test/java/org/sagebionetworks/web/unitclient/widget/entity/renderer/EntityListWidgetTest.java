package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SelectableListView;
import org.sagebionetworks.web.client.widget.entity.EntityListRowBadge;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListUtil;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidgetView;
import org.sagebionetworks.web.shared.WidgetConstants;
import com.google.gwt.user.client.ui.Widget;

public class EntityListWidgetTest {

	EntityListWidget widget;
	@Mock
	EntityListWidgetView mockView;

	Map<String, String> descriptor;

	String xsrfToken = "12345";
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	EntityListRowBadge mockEntityListRowBadge;
	EntityGroupRecord record456;
	String synId = "syn456";
	String note = "record456 has a note";
	Reference ref;
	@Mock
	Callback mockSelectionChangedCallback;
	@Mock
	SelectableListView mockSelectableListView;

	@Before
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		// create an entity group record for syn456
		record456 = new EntityGroupRecord();
		ref = new Reference();
		ref.setTargetId(synId);
		ref.setTargetVersionNumber(1L);
		record456.setEntityReference(ref);
		record456.setNote(note);
		when(mockPortalGinInjector.getEntityListRowBadge()).thenReturn(mockEntityListRowBadge);
		// create empty descriptor
		descriptor = new HashMap<String, String>();
		widget = new EntityListWidget(mockView, mockPortalGinInjector);
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testConfigure() {
		List<EntityGroupRecord> records = new ArrayList<EntityGroupRecord>();
		records.add(record456);
		String encoded = EntityListUtil.recordsToString(records);
		descriptor.put(WidgetConstants.ENTITYLIST_WIDGET_LIST_KEY, encoded);

		widget.configure(null, descriptor, null, null);
		// expect to show the description if no param is specified (backwards compatible)
		verify(mockView).clearRows();
		verify(mockView, times(2)).setTableVisible(true);
		verify(mockView, times(2)).setEmptyUiVisible(false);
		verify(mockView).setDescriptionHeaderVisible(true);
		verify(mockEntityListRowBadge).configure(ref);
		verify(mockEntityListRowBadge).setDescriptionVisible(true);
		verify(mockEntityListRowBadge).setNote(note);
		verify(mockEntityListRowBadge).setIsSelectable(false);
		verify(mockEntityListRowBadge).setSelectionChangedCallback(any(Callback.class));
		verify(mockView).addRow(any(Widget.class));

		assertEquals(mockEntityListRowBadge, widget.getRowWidgets().get(0));
	}

	@Test
	public void testConfigureHideDescriptionSetSelectable() {
		widget.setSelectable(mockSelectableListView);
		widget.setSelectionChangedCallback(mockSelectionChangedCallback);
		List<EntityGroupRecord> records = new ArrayList<EntityGroupRecord>();
		records.add(record456);
		String encoded = EntityListUtil.recordsToString(records);
		descriptor.put(WidgetConstants.ENTITYLIST_WIDGET_LIST_KEY, encoded);
		descriptor.put(WidgetConstants.ENTITYLIST_WIDGET_SHOW_DESCRIPTION_KEY, Boolean.FALSE.toString());

		widget.configure(null, descriptor, null, null);
		verify(mockView).setDescriptionHeaderVisible(false);
		verify(mockEntityListRowBadge).setDescriptionVisible(false);
		verify(mockEntityListRowBadge).setIsSelectable(true);
		verify(mockEntityListRowBadge).setSelectionChangedCallback(any(Callback.class));
	}

	@Test
	public void testConfigureNoRows() {
		descriptor.put(WidgetConstants.ENTITYLIST_WIDGET_SHOW_DESCRIPTION_KEY, Boolean.FALSE.toString());
		widget.configure(null, descriptor, null, null);
		verify(mockView).setTableVisible(false);
		verify(mockView).setEmptyUiVisible(true);
		verify(mockView).setDescriptionHeaderVisible(false);

		reset(mockView);
		widget.addRecord(new EntityGroupRecord());
		verify(mockView).addRow(any(Widget.class));
		verify(mockView).setTableVisible(true);
		verify(mockView).setEmptyUiVisible(false);
	}

}
