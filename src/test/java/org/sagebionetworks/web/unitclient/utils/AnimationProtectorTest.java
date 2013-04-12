package org.sagebionetworks.web.unitclient.utils;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

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
		when(mockView.isSlideSupportedByView()).thenReturn(true);
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

	@Test
	public void testShowHiddenWhenSlidesUnsupported() {
		when(mockView.isSlideSupportedByView()).thenReturn(false);
		setViewVisible(false);
		animation.show();
		verifyNoSlides();
	}

	@Test
	public void testHideVisibleWhenSlidesUnsupported() {
		when(mockView.isSlideSupportedByView()).thenReturn(false);
		setViewVisible(true);
		animation.hide();
		verifyNoSlides();
	}


	@Test
	public void testAnimationToggle() throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		Class<? extends AnimationProtector> clazz = animation.getClass();
		Field field = clazz.getDeclaredField("animating");
		field.setAccessible(true);
		field.set(animation, true);

		setViewVisible(true);
		animation.toggle();
		animation.hide();
		animation.show();
		setViewVisible(false);
		animation.toggle();
		animation.hide();
		animation.show();

		verifyNoSlides();
	}

	private void setViewVisible(boolean setVisible) {
		when(mockView.isContainerVisible()).thenReturn(setVisible);
	}

	private void verifyNoSlides() {
		verify(mockView, never()).slideContainerIn((Direction)any(), (FxConfig)any());
		verify(mockView, never()).slideContainerOut((Direction)any(), (FxConfig)any());
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
