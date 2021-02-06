package ui.formChildData;

import bd.BDManager;
import bd.MySet;
import bd.model.TableEvents;
import bd.model.TableStudents;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import utils.data.RawData;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.sql.Connection;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by angel on 28/03/17.
 */
public class ChildDataForm {
    private static final String TAG = ChildDataForm.class.getSimpleName();
    //public static JFrame frame;
    public static final Date endOfTerm = new Date(Long.parseLong("1548979200000"));
    private JPanel mainPanel;
    private JComboBox<String> comboBoxClassrooms;
    private JComboBox<String> comboBoxStudents;
    private JLabel labelBirthDate;
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JTextArea textArea3;
    private JTextArea textArea4;
    private JTextArea textArea5;
    private JTextArea textArea6;
    private JSplitPane sPEYEndReport1;
    private JSplitPane sPEYEndReport2;
    private JSplitPane sPEYEndReport3;
    private JTabbedPane tPCdbPracticalLife;
    private JTabbedPane tPCdbSensorial;
    private JTabbedPane tPCdbLanguage;
    private JTabbedPane tPCdbMath;
    private JSplitPane sPEYEndReport4;
    private JTabbedPane tPTallerMath;
    private JTabbedPane tPTallerGeometry;
    private JTabbedPane tPTallerGeography;
    private JTabbedPane tPTallerHistory;
    private JTabbedPane tPTargetsEYs;
    private JTabbedPane tPTargetsFS;
    private JTabbedPane tPTargetsY1;
    private JTabbedPane tPTargetsY2;
    private JTabbedPane tPTargetsY3;
    private JTabbedPane tPTargetsY4;
    private JTabbedPane tPTargetsY5;
    private JTabbedPane tPTargetsY6;
    private JSplitPane sPEYEndReport5;
    private JTabbedPane tPEndOfFS;
    private JTabbedPane tPEndOfY1;
    private JCheckBox showDialogCheckBox;
    private JTabbedPane tpTallerBiology;
    private JTabbedPane tpTallerSpanish;
    private JTabbedPane tPCdbSensorialExtensions;
    private JDatePickerImpl dateStarting;
    private JDatePickerImpl dateFirstTerm;
    private JDatePickerImpl dateSecondTerm;
    private JDatePickerImpl dateEnd;
    private JPanel firstTerm;
    private JPanel secondTerm;
    private JPanel thirdTerm;
    public static BDManager bdManager;
    private static SettingsManager settingsManager;
    private static CacheManager cacheManager;
    private HashMap<String, Integer> students;
    private Integer studentId;
    private String studentName;
    private HashMap<Double, HashMap<Integer, String>>oldTexts;
    private HashMap<Double, HashMap<Integer, JTextArea>>textAreas;
    private ChildDataFormRenderer renderer;
    private HashMap<String, ChildDataFormListModel> models;
    private JTabbedPane[][] montessoriPanes;

    public UtilDateModel dateModelStart;
    public UtilDateModel dateModelFirstT;
    public UtilDateModel dateModelSecondT;
    public UtilDateModel dateModelEnd;

    public static JPanel main(BDManager bdManager, SettingsManager settingsManager, CacheManager cacheManager) {
        ChildDataForm.bdManager = bdManager;
        ChildDataForm.settingsManager = settingsManager;
        ChildDataForm.cacheManager = cacheManager;
        return new ChildDataForm().mainPanel;
    }


    private ChildDataForm() {
        textArea1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                eYObservationSave(e);
            }
        });
        textArea2.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                eYObservationSave(e);
            }
        });
        textArea3.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                eYObservationSave(e);
            }
        });
        textArea4.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                eYObservationSave(e);
            }
        });
        textArea5.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                eYObservationSave(e);
            }
        });
        textArea6.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                eYObservationSave(e);
            }
        });
    }

    private void eYObservationSave(FocusEvent event) {
        if (studentId == null) return;
        JTextArea textArea= (JTextArea) event.getComponent();
        int event_type= 99;
        int event_id = -1;
        String oldText;
        double year = -1d;
        int area = -1;
        switch (textArea.getName()) {
            case "textArea1" : {
                event_id = 1;
                year = 2.5;
                area = 1;
                break;
            }
            case "textArea2" : {
                event_id = 2;
                year = 2.5;
                area = 2;
                break;
            }
            case "textArea3" : {
                event_id = 3;
                year = 2.5;
                area = 3;
                break;
            }
            case "textArea4" : {
                event_id = 10;
                year = 2.5;
                area = 10;
                break;
            }
            case "textArea5" : {
                event_id = 91;
                year = 2.5;
                area = 91;
                break;
            }
            case "textArea6" : {
                event_id = 92;
                year = 2.5;
                area = 92;
                break;
            }
            case "5.4" : {
                event_id = 504;
                year = 5d;
                area = 4;
                break;
            }
            case "5.5" : {
                event_id = 505;
                year = 5d;
                area = 5;
                break;
            }
            case "5.6" : {
                event_id = 506;
                year = 5d;
                area = 6;
                break;
            }
            case "5.1" : {
                event_id = 501;
                year = 5d;
                area = 1;
                break;
            }
            case "5.7" : {
                event_id = 507;
                year = 5d;
                area = 7;
                break;
            }
            case "5.9" : {
                event_id = 509;
                year = 5d;
                area = 9;
                break;
            }
            case "5.10" : {
                event_id = 510;
                year = 5d;
                area = 10;
                break;
            }
            case "5.11" : {
                event_id = 511;
                year = 5d;
                area = 11;
                break;
            }
            case "5.13" : {
                event_id = 513;
                year = 5d;
                area = 13;
                break;
            }
            case "5.3" : {
                event_id = 503;
                year = 5d;
                area = 3;
                break;
            }
            case "5.NS" : {
                event_id = 591;
                year = 5d;
                area = 91;
                break;
            }
            case "5.PT" : {
                event_id = 592;
                year = 5d;
                area = 92;
                break;
            }
            case "6.8" : {
                event_id = 608;
                year = 6d;
                area = 8;
                break;
            }
            case "6.5" : {
                event_id = 605;
                year = 6d;
                area = 5;
                break;
            }
            case "6.9" : {
                event_id = 609;
                year = 6d;
                area = 9;
                break;
            }
            case "6.7" : {
                event_id = 607;
                year = 6d;
                area = 7;
                break;
            }
            case "6.10" : {
                event_id = 610;
                year = 6d;
                area = 10;
                break;
            }
            case "6.11" : {
                event_id = 611;
                year = 6d;
                area = 11;
                break;
            }
            case "6.13" : {
                event_id = 613;
                year = 6d;
                area = 13;
                break;
            }
            case "6.14" : {
                event_id = 614;
                year = 6d;
                area = 14;
                break;
            }
            case "6.1" : {
                event_id = 601;
                year = 6d;
                area = 1;
                break;
            }
            case "6.15" : {
                event_id = 615;
                year = 6d;
                area = 15;
                break;
            }
            case "6.16" : {
                event_id = 616;
                year = 6d;
                area = 16;
                break;
            }
            case "6.17" : {
                event_id = 617;
                year = 6d;
                area = 17;
                break;
            }
            case "6.3" : {
                event_id = 603;
                year = 6d;
                area = 3;
                break;
            }
            case "6.18" : {
                event_id = 618;
                year = 6d;
                area = 18;
                break;
            }
            case "6.NS" : {
                event_id = 691;
                year = 6d;
                area = 91;
                break;
            }
            case "6.PT" : {
                event_id = 692;
                year = 6d;
                area = 92;
                break;
            }
        }
        oldText = oldTexts.get(year).get(area);
        if (oldText==null) oldText = "";
        String text = textArea.getText();
        if (text.equals(oldText)) return;
        if (text.equals(""))
            if (JOptionPane.showConfirmDialog(mainPanel, "¿Borrar observación?") != JOptionPane.YES_OPTION) {
                textArea.setText(oldText);
                return;
            }
        Connection co = null;
        try {
            co = bdManager.connect();
            if (!text.equals(""))
                bdManager.addOrEditEventForStudentAndTypeAndId(co, new java.sql.Date(new java.util.Date().getTime()), studentId,
                        event_type, event_id, null, text);
            else
                bdManager.removeValue(co, BDManager.tableEvents,
                        TableEvents.student + "=" + studentId + " AND " +
                                TableEvents.event_type + "=" + event_type, true);
            oldTexts.get(year).put(area, text);
        } finally {
            BDManager.closeQuietly(co);
        }
    }

    private void createUIComponents() {
        dateModelStart = new UtilDateModel();
        dateModelFirstT = new UtilDateModel();
        dateModelSecondT = new UtilDateModel();
        dateModelEnd = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");

        JDatePanelImpl datePanelStart = new JDatePanelImpl(dateModelStart, p);
        dateStarting = new JDatePickerImpl(datePanelStart, new DateLabelFormatter());
        dateModelStart.setValue(new java.util.Date(settingsManager.date_SY.getTime()));

        JDatePanelImpl datePanelFirstT = new JDatePanelImpl(dateModelFirstT, p);
        dateFirstTerm = new JDatePickerImpl(datePanelFirstT, new DateLabelFormatter());
        dateModelFirstT.setValue(new java.util.Date(settingsManager.date_FT.getTime()));

        JDatePanelImpl datePanelSecondT = new JDatePanelImpl(dateModelSecondT, p);

        dateSecondTerm = new JDatePickerImpl(datePanelSecondT, new DateLabelFormatter());
        dateModelSecondT.setValue(new java.util.Date(settingsManager.date_ST.getTime()));

        JDatePanelImpl datePanelEnd = new JDatePanelImpl(dateModelEnd, p);
        dateEnd = new JDatePickerImpl(datePanelEnd, new DateLabelFormatter());
        dateModelEnd.setValue(new java.util.Date(settingsManager.date_TT.getTime()));

        showDialogCheckBox = new JCheckBox();
        showDialogCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.DESELECTED) settingsManager.addValue(SettingsManager.SHOWDIALOG, "0");
            else if (e.getStateChange() == ItemEvent.SELECTED) settingsManager.addValue(SettingsManager.SHOWDIALOG, "1");
        });
        oldTexts = new HashMap<>();
        oldTexts.put(2.5d, new HashMap<>());
        oldTexts.put(5d, new HashMap<>());
        oldTexts.put(6d, new HashMap<>());
        textAreas = new HashMap<>();
        oldTexts.put(2.5d, new HashMap<>());
        textAreas.put(5d, new HashMap<>());
        textAreas.put(6d, new HashMap<>());

        models = new HashMap<>();
        students = new HashMap<>();
        labelBirthDate = new JLabel();
        comboBoxStudents = new JComboBox<>();
        comboBoxStudents.addItem(null);
        students.put("", null);
        for (Integer id : cacheManager.studentsPerClassroom.get(1)) {
            String student = (String)cacheManager.students.get(id)[0];
            comboBoxStudents.addItem(student);
            students.put(student, id);
        }
        comboBoxStudents.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                studentName = (String) e.getItem();
                loadDataforStudent(studentName);
            }
        });
        comboBoxClassrooms = new JComboBox<>(cacheManager.getClassroomsNames());
        comboBoxClassrooms.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) loadStudentsForClassroom(
                    ((JComboBox)e.getSource()).getSelectedIndex()+1);
        });

        JSplitPane sPEYs1 = new JSplitPane();
        sPEYs1.addComponentListener(new ChildDataFormComponentListener(3));
        JSplitPane sPEYs2 = new JSplitPane();
        sPEYs2.addComponentListener(new ChildDataFormComponentListener(2));
        JSplitPane sPFS1 = new JSplitPane();
        sPFS1.addComponentListener(new ChildDataFormComponentListener(4));
        JSplitPane sPFS2 = new JSplitPane();
        sPFS2.addComponentListener(new ChildDataFormComponentListener(3));
        JSplitPane sPFS3 = new JSplitPane();
        sPFS3.addComponentListener(new ChildDataFormComponentListener(2));
        JSplitPane sPY11 = new JSplitPane();
        sPY11.addComponentListener(new ChildDataFormComponentListener(3));
        JSplitPane sPY12 = new JSplitPane();
        sPY12.addComponentListener(new ChildDataFormComponentListener(2));
        JSplitPane sPY21 = new JSplitPane();
        sPY21.addComponentListener(new ChildDataFormComponentListener(3));
        JSplitPane sPY22 = new JSplitPane();
        sPY22.addComponentListener(new ChildDataFormComponentListener(2));
        JSplitPane sPY31 = new JSplitPane();
        sPY31.addComponentListener(new ChildDataFormComponentListener(3));
        JSplitPane sPY32 = new JSplitPane();
        sPY32.addComponentListener(new ChildDataFormComponentListener(2));
        JSplitPane sPY41 = new JSplitPane();
        sPY41.addComponentListener(new ChildDataFormComponentListener(3));
        JSplitPane sPY42 = new JSplitPane();
        sPY42.addComponentListener(new ChildDataFormComponentListener(2));
        sPEYEndReport1 = new JSplitPane();
        sPEYEndReport1.addComponentListener(new ChildDataFormComponentListener(6));
        sPEYEndReport2 = new JSplitPane();
        sPEYEndReport2.addComponentListener(new ChildDataFormComponentListener(5));
        sPEYEndReport3 = new JSplitPane();
        sPEYEndReport3.addComponentListener(new ChildDataFormComponentListener(4));
        sPEYEndReport4 = new JSplitPane();
        sPEYEndReport4.addComponentListener(new ChildDataFormComponentListener(3));
        sPEYEndReport5 = new JSplitPane();
        sPEYEndReport5.addComponentListener(new ChildDataFormComponentListener(2));

        tPCdbPracticalLife = new JTabbedPane();
        tPCdbSensorial = new JTabbedPane();
        tPCdbLanguage = new JTabbedPane();
        tPCdbMath = new JTabbedPane();
        tPCdbSensorialExtensions = new JTabbedPane();
        tPTallerMath = new JTabbedPane();
        tPTallerGeometry = new JTabbedPane();
        tPTallerGeography = new JTabbedPane();
        tPTallerHistory = new JTabbedPane();
        tpTallerBiology = new JTabbedPane();
        tpTallerSpanish = new JTabbedPane();

        montessoriPanes = new JTabbedPane[][]{
                    {tPCdbPracticalLife, tPCdbSensorial, tPCdbLanguage, tPCdbMath, tPCdbSensorialExtensions},
                    {tPTallerMath, tPTallerGeometry, tPTallerGeography, tPTallerHistory, tpTallerBiology,
                            tpTallerSpanish}};

        tPTargetsEYs = new JTabbedPane();
        tPTargetsFS = new JTabbedPane();
        tPTargetsY1 = new JTabbedPane();
        tPTargetsY2 = new JTabbedPane();
        tPTargetsY3 = new JTabbedPane();
        tPTargetsY4 = new JTabbedPane();
        tPTargetsY5 = new JTabbedPane();
        tPTargetsY6 = new JTabbedPane();

        renderer = new ChildDataFormRenderer();
        createMontessoriTabs();
        createTargets();
        tPEndOfFS = new JTabbedPane();
        tPEndOfY1 = new JTabbedPane();
        fillTPEndOf();
    }

    private void fillTPEndOf(){
        for (Double year : oldTexts.keySet())
            for (int areaId : oldTexts.get(year).keySet()) oldTexts.get(year).put(areaId, "");

        JTabbedPane pane;
        for (Double year : oldTexts.keySet()) {
            List<Integer> areas;
            String firstname;
            switch (year.intValue()) {
                case 5: pane = tPEndOfFS; areas = RawData.areasTargetPerStageData[1]; firstname = "5."; break;
                case 6: pane = tPEndOfY1; areas = RawData.areasTargetPerStageData[2]; firstname = "6."; break;
                default: continue;
            }
            for (Integer area : areas) {
                JTextArea textArea = new JTextArea();
                textArea.setName(firstname+area);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        eYObservationSave(e);
                    }
                });
                textAreas.get(year).put(area,  textArea);
                pane.addTab(cacheManager.areasTarget.get(area)[0], new JScrollPane(textArea));
            }
            JTextArea textArea = new JTextArea();
            textArea.setName(firstname+"NS");
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    eYObservationSave(e);
                }
            });
            textAreas.get(year).put(91,  textArea);
            pane.addTab("Next Steps", new JScrollPane(textArea));

            textArea = new JTextArea();
            textArea.setName(firstname+"PT");
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    eYObservationSave(e);
                }
            });
            textAreas.get(year).put(92,  textArea);
            pane.addTab("Parents/Tutors Comments", new JScrollPane(textArea));
        }
    }

    private JList createMontessoriList(double startYear, double endYear, int subarea) {
        ChildDataFormListModel model = new ChildDataFormListModel(cacheManager, settingsManager, startYear, endYear, subarea);
        if (model.getSize() == 0) return null;
        else {
            JList<ChildDataFormListItem> list = new JList<>(model);
            list.setCellRenderer(renderer);
            list.addMouseListener(new ChildDataFormListMouseAdapter(this));
            models.put(getModelString(2, endYear, subarea), model);
            return list;
        }
    }

    private JList createTargetsList(double year, int area) {
        ChildDataFormListModel model = new ChildDataFormListModel(cacheManager, settingsManager, year, area);
        if (model.getSize() == 0) return null;
        else {
            JList<ChildDataFormListItem> list = new JList<>(model);
            list.setCellRenderer(renderer);
            list.addMouseListener(new ChildDataFormListMouseAdapter(this));
            models.put(getModelString(1, year, area), model);
            return list;
        }
    }

    private void createMontessoriTabs(){
        JTabbedPane tabbedPane;
        double startYear, endYear;
        Set<Integer> areas = null;
        for (int i = 0; i <= 1; i++) {
            switch (i) {
                case 0 : {
                    startYear = 0;
                    endYear = 5.5;
                    areas = cacheManager.stageAreaSubareaMontessori.get(1).keySet();
                    break;
                }
                case 1 : {
                    startYear = 6;
                    endYear = 7;
                    areas = cacheManager.stageAreaSubareaMontessori.get(2).keySet();
                    break;
                }
                default : {
                    startYear = 0;
                    endYear = 0;
                }
            }
            int n = 0;
            for (int area : areas) {
                ArrayList<Integer> subareas = cacheManager.subareasMontessoriPerArea.get(area);
                if (subareas != null) {
                    tabbedPane = montessoriPanes[i][n++];
                        for (Integer subarea : subareas) {
                            JList list = createMontessoriList(startYear, endYear, subarea);
                            if (list!= null) {
                                JScrollPane scrollPane = new JScrollPane(list);
                                tabbedPane.addTab((String)cacheManager.subareasMontessori.get(subarea)[settingsManager.language], scrollPane);
                            }
                        }
                } else n++;
            }
        }
    }

    private void createTargets(){
        JTabbedPane pane;
        double year;
        List<Integer> areas;
        for (int i = 0; i < 8; i++) {
            switch (i) {
                case 0 : {
                    year = 2.5;
                    pane = tPTargetsEYs;
                    break;
                }
                case 1 : {
                    year = 5;
                    pane = tPTargetsFS;
                    break;
                }
                case 2 : {
                    year = 6;
                    pane = tPTargetsY1;
                    break;
                }
                case 3 : {
                    year = 7;
                    pane = tPTargetsY2;
                    break;
                }
                case 4 : {
                    year = 8;
                    pane = tPTargetsY3;
                    break;
                }
                case 5 : {
                    year = 9;
                    pane = tPTargetsY4;
                    break;
                }
                case 6 : {
                    year = 10;
                    pane = tPTargetsY5;
                    break;
                }
                case 7 : {
                    year = 11;
                    pane = tPTargetsY6;
                    break;
                }
                default : {
                    year = 0;
                    pane = null;
                }
            }
            areas = RawData.areasTargetperStage.get(year);
            for (Integer area : areas) {
                JList list = createTargetsList(year, area);
                if (list != null) {
                    JScrollPane scrollPane = new JScrollPane(list);
                    assert pane != null;
                    pane.addTab(cacheManager.areasTarget.get(area)[0], scrollPane);
                }
            }
        }
    }

    private void loadDataforStudent(String student) {
        studentId = students.get(student);
        String text;
        if (studentId == null) {
            JOptionPane.showMessageDialog(mainPanel,
                    "Por favor seleccione un alumno", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection co = null;
        Integer event_type= null;
        Integer event_id = null;
        try {
            co = bdManager.connect();
            MySet set = bdManager.getValues(co, BDManager.tableStudents, "id = " + studentId);
            while (set.next()) {
                Date birthday = set.getDate(TableStudents.birth_date);
                String birthString = (birthday!= null) ? birthday.toString() : "";
                labelBirthDate.setText(birthString);
            }

            set = bdManager.getValues(co, BDManager.tableEvents, "student = " + studentId);
            clearFormData();
            while (set.next()) {
                event_type = set.getInt(TableEvents.event_type);
                event_id = set.getInt(TableEvents.event_id);

                switch (event_type) {
                    case 2: case 4: case 5:
                    case 1:
                    case 6:
                    case 7:
                    case 9:
                    case 10:
                    case 11:
                        markItem(set.getInt(TableEvents.id), event_id, set.getInt(TableEvents.event_sub), event_type,
                                set.getDate(TableEvents.date)); break;
                    case 99:
                        switch (event_id) {
                            case 1 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(2.5d).put(1, text);
                                textArea1.setText(text);
                                break;
                            }
                            case 2 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(2.5d).put(2, text);
                                textArea2.setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 3 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(2.5d).put(3, text);
                                textArea3.setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 10 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(2.5d).put(10, text);
                                textArea4.setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 91 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(2.5d).put(91, text);
                                textArea5.setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 92 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(2.5d).put(92, text);
                                textArea6.setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 504 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(5d).put(4, text);
                                textAreas.get(5d).get(4).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 505 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(5d).put(5, text);
                                textAreas.get(5d).get(5).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 506 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(5d).put(6, text);
                                textAreas.get(5d).get(6).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 501 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(5d).put(1, text);
                                textAreas.get(5d).get(1).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 507 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(5d).put(7, text);
                                textAreas.get(5d).get(7).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 509 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(5d).put(9, text);
                                textAreas.get(5d).get(9).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 510 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(5d).put(10, text);
                                textAreas.get(5d).get(10).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 511 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(5d).put(11, text);
                                textAreas.get(5d).get(11).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 513 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(5d).put(13, text);
                                textAreas.get(5d).get(13).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 503 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(5d).put(3, text);
                                textAreas.get(5d).get(3).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 591 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(5d).put(91, text);
                                textAreas.get(5d).get(91).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 592 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(5d).put(92, text);
                                textAreas.get(5d).get(92).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 608 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(8, text);
                                textAreas.get(6d).get(8).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 605 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(5, text);
                                textAreas.get(6d).get(5).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 609 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(9, text);
                                textAreas.get(6d).get(9).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 607 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(7, text);
                                textAreas.get(6d).get(7).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 610 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(10, text);
                                textAreas.get(6d).get(10).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 611 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(11, text);
                                textAreas.get(6d).get(11).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 613 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(13, text);
                                textAreas.get(6d).get(13).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 614 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(14, text);
                                textAreas.get(6d).get(14).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 601 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(1, text);
                                textAreas.get(6d).get(1).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 615 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(15, text);
                                textAreas.get(6d).get(15).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 616 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(16, text);
                                textAreas.get(6d).get(16).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 617 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(17, text);
                                textAreas.get(6d).get(17).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 603 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(3, text);
                                textAreas.get(6d).get(3).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 618 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(18, text);
                                textAreas.get(6d).get(18).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 691 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(91, text);
                                textAreas.get(6d).get(91).setText(set.getString(TableEvents.notes));
                                break;
                            }
                            case 692 : {
                                text = set.getString(TableEvents.notes);
                                oldTexts.get(6d).put(92, text);
                                textAreas.get(6d).get(92).setText(set.getString(TableEvents.notes));
                                break;
                            }
                        }
                }
            }
            mainPanel.repaint();
        } catch (Exception e) {
            MyLogger.e(TAG + "." + event_type+ "."+event_id, e);
        } finally {
            BDManager.closeQuietly(co);
        }
    }

    private void clearFormData() {
        for ( ChildDataFormListModel model: models.values()) model.unmarkAllItems();

        for (Double year : oldTexts.keySet())
            for (Integer area : oldTexts.get(year).keySet()) oldTexts.get(year).put(area,"");

        textArea1.setText("");
        textArea2.setText("");
        textArea3.setText("");
        textArea4.setText("");
        textArea5.setText("");
        textArea6.setText("");
        for (Double year : textAreas.keySet())
            for (Integer area : textAreas.get(year).keySet()) textAreas.get(year).get(area).setText("");
    }

    private void markItem(Integer id, Integer event_id, Integer event_sub, Integer event_type, Date date) {
        String value = getModelString(event_type, event_id);
        ChildDataFormListModel model = models.get(value);
        if (model!=null) model.markItem(id, event_id, event_sub, event_type, date);
    }

    public String getStudentName() {
        return studentName;
    }

    public Integer getStudentId() {
        return studentId;
    }

    private void loadStudentsForClassroom(int classroom) {
        students.clear();
        comboBoxStudents.removeAllItems();
        for (Integer id: cacheManager.studentsPerClassroom.get(classroom)) {
            String student = (String)cacheManager.students.get(id)[0];
            students.put(student, id);
            comboBoxStudents.addItem(student);
        }
    }

    public static void addToBd(int studentId, int index, ChildDataFormListItem item, java.sql.Date date,
                               ArrayList<ChildDataFormListItem> subitems){
        Connection co = null;
        try {
            co = ChildDataForm.bdManager.connect();
            if (subitems == null) subitems = new ArrayList<>();
            subitems.add(0, item);

            for (int i = index; i > -1 ; i--) {
                for (ChildDataFormListItem subitem: subitems) {
                    if (subitem.events[i] != null) continue;
                    Integer type;
                    switch (subitem.type) {
                        case 0 : type = ChildDataFormListModel.event_typeOutcome[i]; break;
                        case 1 : type = ChildDataFormListModel.event_typeNC[i]; break;
                        case 2 : type = ChildDataFormListModel.event_typeMontessori[i]; break;
                        default : type = null;
                    }
                    Integer id  = ChildDataForm.bdManager.addEvent(co, date, studentId, type, subitem.getId(),
                            subitem.getSubid(), null);
                    if (id != null) {
                        subitem.setDate(date);
                        switch (i) {
                            case 0 : {
                                subitem.setBox1(id);
                                subitem.repaint();
                                break;
                            }
                            case 1 : {
                                subitem.setBox2(id);
                                subitem.repaint();
                                break;
                            }
                            case 2 : {
                                subitem.setBox3(id);
                                subitem.repaint();
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(co);
        }
    }

    public static void removeFromBd(ChildDataFormListItem item, Integer studentId, int index,
                                    ArrayList<ChildDataFormListItem> subitems){
        Connection co = null;
        try {
            co = ChildDataForm.bdManager.connect();
            boolean confirm = true;
            if (subitems == null) subitems = new ArrayList<>();
            subitems.add(0, item);

            for (int i = index; i < 3 ; i++) {
                for (ChildDataFormListItem subitem : subitems) {
                    if (subitem.events[i] == null) continue;
                    String condition = "student=" + studentId+
                            " AND event_type="    + subitem.getEvent_type(i)+
                            " AND event_id="      + subitem.getId();
                    ChildDataForm.bdManager.removeValue(co, BDManager.tableEvents, condition, confirm);
                    if (confirm) confirm = false;
                    subitem.setDate(null);
                    switch (i) {
                        case 0 : {
                            subitem.unsetBox1();
                            subitem.repaint();
                            break;
                        }
                        case 1 : {
                            subitem.unsetBox2();
                            subitem.repaint();
                            break;
                        }
                        case 2 : {
                            subitem.unsetBox3();
                            subitem.repaint();
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(co);
        }
    }

    private String getModelString(int type, double year, int area) {
        return type + "/" + year + "/" + area;
    }

    private String getModelString(Integer event_type, int event_id) {
        Integer subarea=null;
        Double year;
        Integer area;
        Integer type = ChildDataFormListItem.getType(event_type);
        String result = null;
        if (type == null) return null;

        try {
            switch (type) {
                case 0 : {
                    type = 1;//{name,subarea,start_month,end_month,nombre}
                    Object[] outcome = cacheManager.outcomes.get(event_id);
                    subarea = (Integer) outcome[1];
                    area = cacheManager.targetSubareaArea.get(subarea);
                    result = type + "/" + outcome[3] + "/" + area;
                    break;
                }
                case 1 : {
                    Object[] target = cacheManager.targets.get(event_id);
                    subarea = (Integer) target[1];
                    area = cacheManager.targetSubareaArea.get(subarea);
                    year = (Double) target[2];
                    result = type + "/" + year + "/" + area;
                    break;
                }
                case 2 : {
                    Object[] presentation = cacheManager.presentations.get(event_id);
                    subarea = (Integer) presentation[1];
                    year = (Double) presentation[2];
                    if (year <= 5.5) year = 5.5;
                    else year = 7d;
                    result = type + "/" + year + "/" + subarea;
                }
            }
        } catch (Exception ex) {
            MyLogger.d(TAG, event_id + " : " + ex.toString());
        }
        if (subarea==null) return null;
        return result;
    }

    public static class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {

        private final String datePattern = "yyyy-MM-dd";
        private final SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) {
            if (value != null) {
                Calendar cal = (Calendar) value;
                return dateFormatter.format(cal.getTime());
            }

            return "";
        }
    }
}
