package ui.formClassroomTargets;

import utils.CacheManager;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MyTableRendererEvents extends JLabel implements TableCellRenderer {
    /*JList<String>
    public static final double[] years = {3, 3.5, 4, 4.5, 5, 5.5, 6};
    public static final Color[] colors = {
            Color.white, new Color(255,255,204),new Color(255,255,0),
            new Color(255,204,0), new Color(255,102,0),
            new Color(255,0,0), new Color(204,0,0)};

     */
    public static final double[] years = {2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5, 6, 6.5, 7, 7.5, 8, 8.5, 9, 9.5, 10, 10.5, 11, 11.5, 12};
    public static final Color[] colors = {
            new Color(255,255,255),
            new Color(255,255,128),
            new Color(255,255,0),
            new Color(255,232,0),
            new Color(255,209,0),
            new Color(255,185,0),
            new Color(255,139,0),
            new Color(255,93,0),
            new Color(255,0,0),
            new Color(209,46,0),
            new Color(162,93,0),
            new Color(139,116,0),
            new Color(93,162,0),
            new Color(23,232,0),
            new Color(0,255,0),
            new Color(0,209,46),
            new Color(0,162,93),
            new Color(0,139,116),
            new Color(0,93,162),
            new Color(0,70,185),
            new Color(0,0,255)};

    final CacheManager cacheManager;

    public MyTableRendererEvents(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public JLabel getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                int row, int column) {
        String text = (String)value;
        setOpaque(true);
        Font font = new Font(getFont().getName(), Font.PLAIN, 10);
        setFont(font);
        setText(text);

        if (!isSelected) {
            setForeground(Color.black);
            setBackground(Color.white);
        } else {
            setBackground(Color.black);
            setForeground(Color.white);
        }
        return this;
    }

}
