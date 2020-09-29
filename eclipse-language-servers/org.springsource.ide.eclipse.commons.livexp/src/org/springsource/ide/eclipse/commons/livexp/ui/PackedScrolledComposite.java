package org.springsource.ide.eclipse.commons.livexp.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * Improved version of 'ScrolledComposite' that doesn't waste space for
 * scrollbars if the scrollbars are invisible.
 * 
 * See https://stackoverflow.com/questions/16516984/how-to-get-rid-of-wasted-space-in-swt-scrolledcomposite-when-the-scrollbars-are
 */
class PackedScrolledComposite extends ScrolledComposite
{
    Point scrollBarSize;  // Size of OS-specific scrollbar

    public PackedScrolledComposite(Composite parent, int style)
    {
        super(parent, style);

        Composite composite = new Composite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        composite.setSize(1, 1);
        scrollBarSize = composite.computeSize(0, 0);
        composite.dispose();
    }

    public Point computeSize(int wHint, int hHint, boolean changed)
    {
        Point point = super.computeSize(wHint, hHint, changed);
        point.x += ((getStyle() & SWT.V_SCROLL) != 0) ? -scrollBarSize.x : 0;
        point.y += ((getStyle() & SWT.H_SCROLL) != 0) ? -scrollBarSize.y : 0;

        return point;
    }
}