package org.sagebionetworks.web.client.jsinterop.mui;

import elemental2.core.JsObject;
import org.sagebionetworks.web.client.jsinterop.ReactComponentType;
import org.sagebionetworks.web.client.jsinterop.SxProps;
import org.sagebionetworks.web.client.jsinterop.react.HasStyle;

/**
 * Abstract class for React MUI component widgets that have the Sx prop.
 * @param <T> the prop type.
 */
public abstract class HasSx<
  T extends ReactComponentType<P>, P extends PropsWithSx
>
  extends HasStyle<T, P> {

  public HasSx(T reactComponentType, P props) {
    super(reactComponentType, props);
  }

  private void createSxIfNull() {
    if (props.sx == null) {
      props.sx = SxProps.create();
    }
  }

  private void cloneSxAndRender() {
    // Clone the object so React treats it as a new prop and properly re-renders.
    this.props.sx = (SxProps) SxProps.assign(new JsObject(), this.props.sx);
    this.render();
  }

  public void setSx(SxProps sx) {
    this.props.sx = sx;
    cloneSxAndRender();
  }

  /**
   * Set the top margin
   * @param mt
   */
  public void setMt(String mt) {
    createSxIfNull();
    props.sx.mt = mt;
    cloneSxAndRender();
  }

  /**
   * Set the left padding
   * @param pl
   */
  public void setPl(String pl) {
    createSxIfNull();
    props.sx.pl = pl;
    cloneSxAndRender();
  }
}
