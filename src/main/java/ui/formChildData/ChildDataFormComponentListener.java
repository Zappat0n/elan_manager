package ui.formChildData;

import javax.swing.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Created by angel on 31/03/17.
 */
class ChildDataFormComponentListener implements ComponentListener {
    private final int subs;

    public ChildDataFormComponentListener(int subs) {
        this.subs = subs;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        JSplitPane main = (JSplitPane) e.getComponent();
        int width = main.getWidth();
        main.setDividerLocation(width/subs);
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}
