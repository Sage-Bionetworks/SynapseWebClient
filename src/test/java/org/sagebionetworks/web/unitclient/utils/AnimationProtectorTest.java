package org.sagebionetworks.web.unitclient.utils;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.utils.AnimationProtector;
import org.sagebionetworks.web.client.utils.AnimationProtectorView;

import com.extjs.gxt.ui.client.Style.Direction;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.google.gwt.event.dom.client.ClickHandler;

public class AnimationProtectorTest {

	private AnimationProtector animation;
	private AnimationProtectorView mockView;

	@Before
	public void setup() {
		mockView = mock(AnimationProtectorView.class);
		when(mockView.isContainerRendered()).thenReturn(true);
		animation = new AnimationProtector(mockView);
		verify(mockView).addClickHandler((ClickHandler)any());
	}

	@Test
	public void testShowVisible() {
		setViewVisible(true);
		animation.show();
		verifyNoSlides();
	}

	@Test
	public void testShowHidden() {
		setViewVisible(false);
		animation.show();
		verifyShown();
	}

	@Test
	public void testHideVisible() {
		setViewVisible(true);
		animation.hide();
		verifyHidden();
	}

	@Test
	public void testHideHidden() {
		setViewVisible(false);
		animation.hide();
		verifyNoSlides();
	}

	@Test
	public void testToggleVisible() {
		setViewVisible(true);
		animation.toggle();
		verifyHidden();
	}

	@Test
	public void testToggleHidden() {
		setViewVisible(false);
		animation.toggle();
		verifyShown();
	}

	private void setViewVisible(boolean setVisible) {
		when(mockView.isContainerVisible()).thenReturn(setVisible);
	}

	private void verifyNoSlides() {
		verify(mockView, times(0)).slideContainerIn((Direction)any(), (FxConfig)any());
		verify(mockView, times(0)).slideContainerOut((Direction)any(), (FxConfig)any());
	}

	private void verifyHidden() {
		// SlideOut actually means to hide
		verify(mockView).slideContainerOut((Direction)any(), (FxConfig)any());
	}

	private void verifyShown() {
		// SlideIn actually means to show
		verify(mockView).setContainerVisible(eq(true));
		verify(mockView).slideContainerIn((Direction)any(), (FxConfig)any());
	}
}
