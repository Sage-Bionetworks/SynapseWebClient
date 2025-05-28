package org.sagebionetworks.web.client.jsinterop.mui;

import org.sagebionetworks.web.client.jsinterop.ReactComponentType;
import org.sagebionetworks.web.client.jsinterop.SxProps;
import org.sagebionetworks.web.client.jsinterop.react.HasStyle;

public class Grid extends HasStyle<ReactComponentType<GridProps>, GridProps> {

  public Grid() {
    super(MaterialUI.Grid, GridProps.create(false));
  }

  public void setId(String id) {
    props.id = id;
    this.render();
  }

  public void setContainer(boolean container) {
    props.container = container;
    this.render();
  }

  public void setSize(BreakpointMap size) {
    props.size = size;
    this.render();
  }

  public void setOffset(BreakpointMap offset) {
    props.offset = offset;
    this.render();
  }

  /**
   * @deprecated Use {@link #setSize(BreakpointMap)} instead.
   */
  @Deprecated
  public void setXs(int xs) {
    if (props.size == null) {
      props.size = BreakpointMap.create();
    }
    props.size.xs = xs;
    this.render();
  }

  /**
   * @deprecated Use {@link #setSize(BreakpointMap)} instead.
   */
  @Deprecated
  public void setSm(int sm) {
    if (props.size == null) {
      props.size = BreakpointMap.create();
    }
    props.size.sm = sm;
    this.render();
  }

  /**
   * @deprecated Use {@link #setSize(BreakpointMap)} instead.
   */
  @Deprecated
  public void setMd(int md) {
    if (props.size == null) {
      props.size = BreakpointMap.create();
    }
    props.size.md = md;
    this.render();
  }

  /**
   * @deprecated Use {@link #setSize(BreakpointMap)} instead.
   */
  @Deprecated
  public void setLg(int lg) {
    if (props.size == null) {
      props.size = BreakpointMap.create();
    }
    props.size.lg = lg;
    this.render();
  }

  /**
   * @deprecated Use {@link #setSize(BreakpointMap)} instead.
   */
  @Deprecated
  public void setXl(int xl) {
    if (props.size == null) {
      props.size = BreakpointMap.create();
    }
    props.size.xl = xl;
    this.render();
  }

  /**
   * @deprecated Use {@link #setOffset(BreakpointMap)} instead.
   */
  @Deprecated
  public void setXsOffset(int xsOffset) {
    if (props.offset == null) {
      props.offset = BreakpointMap.create();
    }
    props.offset.xs = xsOffset;
    this.render();
  }

  /**
   * @deprecated Use {@link #setOffset(BreakpointMap)} instead.
   */
  @Deprecated
  public void setSmOffset(int smOffset) {
    if (props.offset == null) {
      props.offset = BreakpointMap.create();
    }
    props.offset.sm = smOffset;
    this.render();
  }

  /**
   * @deprecated Use {@link #setOffset(BreakpointMap)} instead.
   */
  @Deprecated
  public void setMdOffset(int mdOffset) {
    if (props.offset == null) {
      props.offset = BreakpointMap.create();
    }
    props.offset.md = mdOffset;
    this.render();
  }

  /**
   * @deprecated Use {@link #setOffset(BreakpointMap)} instead.
   */
  @Deprecated
  public void setLgOffset(int lgOffset) {
    if (props.offset == null) {
      props.offset = BreakpointMap.create();
    }
    props.offset.lg = lgOffset;
    this.render();
  }

  /**
   * @deprecated Use {@link #setOffset(BreakpointMap)} instead.
   */
  @Deprecated
  public void setXlOffset(int xlOffset) {
    if (props.offset == null) {
      props.offset = BreakpointMap.create();
    }
    props.offset.xl = xlOffset;
    this.render();
  }

  public void setSx(SxProps sx) {
    props.sx = sx;
    this.render();
  }

  public void setMt(String mt) {
    if (props.sx == null) {
      props.sx = SxProps.create();
    }
    props.sx.mt = mt;
    this.render();
  }

  public void setPl(String pl) {
    props.sx.pl = pl;
    this.render();
  }

  public void setRowSpacing(String rowSpacing) {
    props.rowSpacing = rowSpacing;
    this.render();
  }

  public void setColumnSpacing(String columnSpacing) {
    props.columnSpacing = columnSpacing;
    this.render();
  }
}
