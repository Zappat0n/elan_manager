package ui.formChildData;

import utils.LanguageManager;
import utils.SettingsManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.sql.Date;

/**
 * Created by angel on 28/03/17.
 */
public class ChildDataFormListItem extends JPanel {
    private static final Color color0 = Color.white;
    private static final Color color1 = Color.getHSBColor(60,25,100);
    private static final Color color2 = Color.pink;
    private static final Color color3 = Color.getHSBColor(33,40,100);
    private static final Color color4 = Color.blue;

    // int type = { 0 Outcome NC, 1 Target NC, 2 Montessori
    public final static Integer[] event_type = {9,10,11,4,2,5,1,6,7};
    public JTextArea label;
    private Boolean isTitle;
    private Integer item;
    private Integer subitem;
    private JCheckBox box1;
    private JCheckBox box2;
    private JCheckBox box3;
    private Date date;
    public Byte type;
    public Integer[] events;
    private SettingsManager settingsManager;

    public ChildDataFormListItem(String text) {
        setText(text, true);
    }

    public ChildDataFormListItem(SettingsManager settingsManager, String text, Integer item,
                                 Integer subitem, Byte type, Date date, Boolean bool1, Boolean bool2, Boolean bool3) {
        this.item = item;
        this.subitem = subitem;
        this.settingsManager = settingsManager;
        this.type = type;
        this.date = date;
        events = new Integer[3];
        box1 = new JCheckBox();
        box1.setSelected(bool1);
        box2 = new JCheckBox();
        box2.setSelected(bool2);
        box3 = new JCheckBox();
        box3.setSelected(bool3);
        setText(text, false);

        JPanel panel = new JPanel();
        panel.add(box1);
        panel.add(box2);
        panel.add(box3);
        add(panel, BorderLayout.LINE_END);
    }


    private void setText(String text, Boolean isTitle) {
        setLayout(new BorderLayout());
        this.isTitle = isTitle;
        label = new JTextArea();
        setColor();
        label.setText(text);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        //label.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
        //label.setHorizontalAlignment(JLabel.LEFT);
        if (!isTitle) {
            label.setFont(getFont().deriveFont(Font.PLAIN, getFont().getSize() - 2));
        } else {
            label.setFont(getFont().deriveFont(Font.BOLD, getFont().getSize() - 1));
        }
        add(label, BorderLayout.CENTER);
    }

    public JTextArea getLabel() {
        return label;
    }

    public String getText() {
        return label.getText();
    }

    public Boolean getBox1() {
        return box1.isSelected();
    }

    public Boolean getBox2() {
        return box2.isSelected();
    }

    public Boolean getBox3() {
        return box3.isSelected();
    }

    public Boolean isTitle() {
        return isTitle;
    }

    public Integer getId() {
        return item;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getEvent_type(int index) {
        return event_type[type * 3 + index];
    }

    public Byte getType() {
        return type;
    }

    public void setBox1(Integer eventId) {
        events[0] = eventId;
        box1.setSelected(true);
        setColor();
    }

    public void setBox2(Integer eventId) {
        events[1] = eventId;
        box2.setSelected(true);
        setColor();
    }

    public void setBox3(Integer eventId) {
        events[2] = eventId;
        box3.setSelected(true);
        setColor();
    }

    public void unsetBox1(){
        events[0] = null;
        box1.setSelected(false);
        setColor();
    }

    public void unsetBox2(){
        events[1] = null;
        box2.setSelected(false);
        setColor();
    }

    public void unsetBox3(){
        events[2] = null;
        box3.setSelected(false);
        setColor();
    }

    public void unsetAll(){
        if (box1 != null) {
            unsetBox1();
            unsetBox2();
            unsetBox3();
            setColor();
        }
    }

    public void boxChecked(Integer studentId, String studentName, int index,
                           ArrayList<ChildDataFormListItem> subitems) {

        if (events[index] == null) addItem(studentId, studentName, index, subitems);
        else removeItem(studentId, studentName, index, subitems);
    }

    private void addItem(Integer studentId, String studentName, int index, ArrayList<ChildDataFormListItem> subitems) {
        if (studentId == null) {
            JOptionPane.showMessageDialog(this,
                    LanguageManager.SELECT_CHILD[LanguageManager.index],
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (subitems != null && subitems.size() > 0) {
            if (JOptionPane.showConfirmDialog(this,
                    LanguageManager.INCLUDE_PRESENTATIONS_SUB[LanguageManager.index]) != JOptionPane.OK_OPTION)
                subitems = null;
        }

        if (settingsManager.getValue(SettingsManager.SHOWDIALOG).equals("1"))
            ChildDataFormWritetodbDialog.main(settingsManager, type, studentId, studentName, index, events,
                this, subitems,true);
        else ChildDataForm.addToBd(studentId, index, ChildDataFormListItem.this,
                new Date(new java.util.Date().getTime()), subitems);
    }

    private void removeItem(Integer studentId, String studentName, int index, ArrayList<ChildDataFormListItem> subitems) {
        if (studentId == null) {
            JOptionPane.showMessageDialog(this,
                    "Por favor seleccione un alumno", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (subitems != null && subitems.size() > 0) {
            if (JOptionPane.showConfirmDialog(this,
                    LanguageManager.REMOVE_PRESENTATIONS_SUB[LanguageManager.index]) != JOptionPane.OK_OPTION)
                subitems = null;
        }

        if (settingsManager.getValue(SettingsManager.SHOWDIALOG).equals("1"))
            ChildDataFormWritetodbDialog.main(settingsManager, type, studentId, studentName, index, events,
                this, subitems,false);
        else ChildDataForm.removeFromBd(ChildDataFormListItem.this, studentId, index, subitems);
    }

    public Integer getSubid() {
        return subitem;
    }

    public static Integer getType(int ev_type) {
        for (int i = 0; i < event_type.length; i++) {
            if (event_type[i] == ev_type) return ((Double)Math.floor(i/3)).intValue();
        }
        return null;
    }

    private void setColor() {
        if (type != null && type == 0) label.setForeground(Color.RED);
        else
        if (!isTitle)
            switch (getTerm(settingsManager, date)) {
                case 0 : label.setBackground(color0); break;
                case 1 : label.setBackground(color1); break;
                case 2 : label.setBackground(color2); break;
                case 3 : label.setBackground(color3); break;
                case 4 : label.setBackground(color4); break;
                default:label.setBackground(Color.WHITE);
            }
    }

    public static Byte getTerm(SettingsManager settingsManager, Date date) {
        if (date == null) return -1;

        if (date.compareTo(settingsManager.date_TT) == 1 ) return 4;
        else if (date.compareTo(settingsManager.date_ST) == 1 ) return 3;
        else if (date.compareTo(settingsManager.date_FT) == 1 ) return 2;
        else if (date.compareTo(settingsManager.date_SY) == 1 ) return 1;
        else return 0;
    }

    public static Color getColor(SettingsManager settingsManager, Date date){
        switch (getTerm(settingsManager, date)) {
            case 0 : return color0;
            case 1 : return color1;
            case 2 : return color2;
            case 3 : return color3;
            case 4 : return color4;
            default: return Color.WHITE;
        }
    }
}
