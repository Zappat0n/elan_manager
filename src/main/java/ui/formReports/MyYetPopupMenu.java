package ui.formReports;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;

public class MyYetPopupMenu extends JPopupMenu {

    public MyYetPopupMenu() {
        Action[] textActions = {new DefaultEditorKit.CutAction(), new DefaultEditorKit.CopyAction(),
                new DefaultEditorKit.PasteAction(),};
        for (Action textAction : textActions) add(new JMenuItem(textAction));

    }
}
