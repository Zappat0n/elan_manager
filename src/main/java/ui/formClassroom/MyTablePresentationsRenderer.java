package ui.formClassroom;

import utils.CacheManager;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MyTablePresentationsRenderer extends JLabel implements TableCellRenderer {
    /*
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
    final ClassroomFormData formData;

    public MyTablePresentationsRenderer(CacheManager cacheManager, ClassroomFormData formData) {
        this.cacheManager = cacheManager;
        this.formData = formData;
    }

    @Override
    public JLabel getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                int row, int column) {
        setText((String)value);
        setOpaque(true);
        Color bg;
        Double age = 0d;
        if (column >0) age = formData.dates.get(column-1);

        Integer presentation = Integer.valueOf(formData.presentations.get(row).split("[.]")[0]);
        Double year = (Double)cacheManager.presentations.get(presentation)[3];
        if (age > year || age == 0d) bg = getColor(year);
        else bg = Color.white;

        if (column == 0) {
            setBackground(bg);
            setForeground(Color.black);
        } else {
            MyTableModelPresentations model = (MyTableModelPresentations) table.getModel();
            int val = model.data[row][column-1];
            if (val<4) {
                setBackground(bg);
                setForeground(Color.black);
            } else {
                setBackground(Color.blue);
                setForeground(bg);
            }

        }
        if (isSelected) {
            setBackground(Color.black);
            setForeground(Color.white);
        }
        return this;
    }


    public Color getColor (Double age) {
        for (int i = 0; i < years.length; i++) {
            if (age < years[i]) return colors[i];
        }
        return new Color(120,120,120);
    }
}
