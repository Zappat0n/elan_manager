package ui.dialogs;

import bd.BDManager;
import utils.CacheManager;
import utils.SettingsManager;
import utils.data.RawData;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class CreatePresentation extends JDialog {
    private static final String TAG = CreatePresentation.class.getSimpleName();
    private static final Double[][] years = new Double[][]{{1.5, 2d, 2.5}, {3d, 3.5, 4d, 4.5, 5d, 5.5},
            {6d, 6.5, 7d, 7.5, 8d, 8.5, 9d, 9.5, 10d, 10.5, 11d, 11.5}};
    private final CacheManager cacheManager;
    private final SettingsManager settingsManager;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox cBArea;
    private JComboBox cBSubarea;
    private JComboBox cBPresentation;
    private JTextField textField1;
    private JTextField textField2;
    private JList listPresentations;
    private JButton buttonAddPresentation;
    private JList listPresentationsSub;
    private JComboBox cBYear;
    private JButton buttonAddPresentationSub;

    private final Integer stage;
    private Integer area;
    private Integer subarea;
    private final Integer presentation;
    private ArrayList<Integer> areas;
    private ArrayList<Integer> subareas;

    public CreatePresentation(CacheManager cacheManager, BDManager bdManager, SettingsManager settingsManager,
                              Integer stage, Integer area, Integer subarea, Integer presentation) {
        this.cacheManager = cacheManager;
        this.settingsManager = settingsManager;
        this.stage = stage;
        this.area = area;
        this.subarea = subarea;
        this.presentation = presentation;

        setContentPane(contentPane);
        setModal(true);
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static void main(CacheManager cacheManager, BDManager bdManager, SettingsManager settingsManager,
                            Integer stage, Integer area, Integer subarea, Integer presentation) {
        if (stage == null) return;
        CreatePresentation dialog = new CreatePresentation(cacheManager, bdManager, settingsManager, stage, area,
                subarea, presentation);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void createUIComponents() {
        areas = new ArrayList<>();
        subareas = new ArrayList<>();

        cBArea = new JComboBox();
        addAreas();
        cBSubarea = new JComboBox();

        if (area == null && areas.size() != 0) area = areas.get(0);
        if (area != null) addSubareas();

        cBPresentation = new JComboBox();

        if (stage != null)
        cBYear = new JComboBox(new DefaultComboBoxModel(years[stage]));


        listPresentations = new JList();
        buttonAddPresentation = new JButton();
        buttonAddPresentationSub = new JButton();

        addPresentations();
    }

    private void addAreas() {
        LinkedHashMap<Integer, HashSet<Integer>> newareas = cacheManager.stageAreaSubareaMontessori.get(stage);
        for (Integer ar : newareas.keySet()) {
            areas.add(ar);
            cBArea.addItem(cacheManager.areasMontessori.get(ar)[settingsManager.language]);
            if (ar.equals(area)) cBArea.setSelectedIndex(areas.indexOf(ar));
        }
    }

    private void addSubareas() {
        for (Integer id : cacheManager.stageAreaSubareaMontessori.get(stage).get(area)) {
            String name = (String)cacheManager.subareasMontessori.get(id)[settingsManager.language];
            subareas.add(id);
            cBSubarea.addItem(name);
            if (id.equals(subarea)) {
                cBSubarea.setSelectedIndex(subareas.indexOf(id));
                subarea = id;
            }
        }
        if (cBSubarea.getSelectedIndex() == 0) {
            subarea = subareas.get(0);
        }
    }

    private void addPresentations() {
        double min = RawData.yearsmontessori[stage][0];
        double max = RawData.yearsmontessori[stage][1];
        ArrayList<Integer> presentations = cacheManager.getPresentations(subarea, min, max);

        cBPresentation = new JComboBox(new DefaultComboBoxModel());
        listPresentations = new JList(new DefaultListModel());

        for (Integer id : presentations) {
            String name = (String) cacheManager.presentations.get(id)[settingsManager.language];
            ((DefaultComboBoxModel)cBPresentation.getModel()).addElement(name);
            ((DefaultListModel)listPresentations.getModel()).addElement(name);
        }

        cBPresentation.setSelectedIndex(presentations.indexOf(presentation != null ? presentation : presentations.get(0)));
        listPresentations.setSelectedIndex(presentations.indexOf(presentation != null ? presentation : presentations.get(0)));

    }



}
