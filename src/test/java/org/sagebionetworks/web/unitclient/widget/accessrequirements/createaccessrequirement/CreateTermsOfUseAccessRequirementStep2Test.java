package org.sagebionetworks.web.unitclient.widget.accessrequirements.createaccessrequirement;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateTermsOfUseAccessRequirementStep2;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateTermsOfUseAccessRequirementStep2View;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditor;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public class CreateTermsOfUseAccessRequirementStep2Test {
	
	CreateTermsOfUseAccessRequirementStep2 widget;
	@Mock
	ModalPresenter mockModalPresenter;
	
	@Mock
	CreateTermsOfUseAccessRequirementStep2View mockView;
	@Mock
	WikiMarkdownEditor mockWikiMarkdownEditor;
	@Mock
	WikiPageWidget mockWikiPageRenderer;
	@Mock
	TermsOfUseAccessRequirement mockTermsOfUseAccessRequirement;
	@Captor
	ArgumentCaptor<WikiPageKey> wikiPageKeyCaptor;
	public static final Long AR_ID = 8765L;
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new CreateTermsOfUseAccessRequirementStep2(mockView, mockWikiMarkdownEditor, mockWikiPageRenderer);
		widget.setModalPresenter(mockModalPresenter);
		when(mockTermsOfUseAccessRequirement.getId()).thenReturn(AR_ID);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setWikiPageRenderer(any(IsWidget.class));
		verify(mockView).setPresenter(widget);
		verify(mockWikiPageRenderer).setModifiedCreatedByHistoryVisible(false);
		verify(mockWikiMarkdownEditor).setDeleteButtonVisible(false);
	}
	
	@Test
	public void testConfigureWithWiki() {
		widget.configure(mockTermsOfUseAccessRequirement);
		verify(mockView).setOldTermsVisible(false);
		verify(mockView).setOldTerms("");
		verify(mockWikiPageRenderer).configure(wikiPageKeyCaptor.capture(), eq(false), eq((WikiPageWidget.Callback)null), eq(false));
		WikiPageKey key = wikiPageKeyCaptor.getValue();
		assertEquals(AR_ID.toString(), key.getOwnerObjectId());
		assertEquals(ObjectType.ACCESS_REQUIREMENT.toString(), key.getOwnerObjectType());
		
		// on edit of wiki
		widget.onEditWiki();
		verify(mockWikiMarkdownEditor).configure(eq(key), any(CallbackP.class));
		
		//on finish
		widget.onPrimary();
		verify(mockModalPresenter).onFinished();
	}
	
	@Test
	public void testConfigureWithOldTermsOfUse() {
		String tou = "these are the old conditions";
		when(mockTermsOfUseAccessRequirement.getTermsOfUse()).thenReturn(tou);
		widget.configure(mockTermsOfUseAccessRequirement);
		verify(mockView).setOldTermsVisible(true);
		verify(mockView).setOldTerms(tou);
		verify(mockWikiPageRenderer).configure(wikiPageKeyCaptor.capture(), eq(false), eq((WikiPageWidget.Callback)null), eq(false));
		WikiPageKey key = wikiPageKeyCaptor.getValue();
		assertEquals(AR_ID.toString(), key.getOwnerObjectId());
		assertEquals(ObjectType.ACCESS_REQUIREMENT.toString(), key.getOwnerObjectType());
		
		// on edit of wiki
		widget.onEditWiki();
		verify(mockWikiMarkdownEditor).configure(eq(key), any(CallbackP.class));
		
		//on finish
		widget.onPrimary();
		verify(mockModalPresenter).onFinished();
	}
	
}
