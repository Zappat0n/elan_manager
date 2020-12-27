package ui.formReports;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MyPopupListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            MyYetPopupMenu popup = new MyYetPopupMenu();
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public static class MyYetPopupMenu  extends JPopupMenu{

        public MyYetPopupMenu() {
            Action[] textActions = {new DefaultEditorKit.CutAction(), new DefaultEditorKit.CopyAction(),
                    new DefaultEditorKit.PasteAction(),};
            for (Action textAction : textActions) add(new JMenuItem(textAction));
        }
    }

}
