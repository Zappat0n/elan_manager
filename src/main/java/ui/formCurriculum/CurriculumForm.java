package ui.formCurriculum;

import com.google.common.io.Files;
import links.SWImportLinks;
import links.SWRecordLinks;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import ui.components.DateLabelFormatter;
import ui.formCurriculum.curriculumTypes.CurriculumSubareaYear;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import utils.data.RawData;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class CurriculumForm {
    private static final String TAG = CurriculumForm.class.getSimpleName();
    private static SettingsManager settingsManager;
    private static CacheManager cacheManager;
    private JPanel mainPanel;
    private JList<String> listStages;
    private JTable tableCurriculum;
    private JList<String> listAreas;
    private JButton buttonPdf;
    private JButton buttonTxt;
    private JButton buttonLoadLinks;
    private JTable tableImportLinks;
    private JList<String> listSubareas;
    private JTable tableLinks;
    private JList<String> listNCAreas;
    private JList<String> listNCSubareas;
    private JTable tableNationalCurriculum;
    private JButton buttonNCText;
    private JButton buttonNCPdf;
    private JList<String> listStagesNC;
    private JRadioButton rBOutcomes;
    private JRadioButton rBTargets;
    private JDatePickerImpl datePickerIni;
    private JDatePickerImpl datePickerEnd;
    private UtilDateModel dateModelIni;
    private UtilDateModel dateModelEnd;
    private JButton buttonGenerateLinks;
    private JProgressBar progressBarLinks;
    private ArrayList<Integer> areasList;
    private ArrayList<Integer> areasNCList;
    private ArrayList<Integer> subareasList;
    private ArrayList<Integer> subareasNCList;
    private Curriculum curriculum;
    private TableLinksGovernor linksGovernor;

    public static JPanel main(SettingsManager settingsManager, CacheManager cacheManager) {
        CurriculumForm.settingsManager = settingsManager;
        CurriculumForm.cacheManager = cacheManager;
        CurriculumForm form = new CurriculumForm();
        return form.mainPanel;
    }

    private void createUIComponents() {
        areasList = new ArrayList<>();
        areasNCList = new ArrayList<>();
        subareasList = new ArrayList<>();
        subareasNCList = new ArrayList<>();
        listStages = new JList<>();
        listStagesNC = new JList<>(RawData.stagesNC);
        listAreas = new JList<>(new DefaultListModel<>());
        listNCAreas = new JList<>(new DefaultListModel<>());
        listSubareas = new JList<>(new DefaultListModel<>());
        listNCSubareas = new JList<>(new DefaultListModel<>());
        buttonPdf = new JButton();
        buttonNCPdf = new JButton();
        buttonTxt = new JButton();
        buttonNCText = new JButton();
        buttonLoadLinks = new JButton();
        buttonGenerateLinks = new JButton();

        dateModelIni = new UtilDateModel();
        dateModelEnd = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        datePickerIni = new JDatePickerImpl(new JDatePanelImpl(dateModelIni, p), new DateLabelFormatter());
        datePickerEnd = new JDatePickerImpl(new JDatePanelImpl(dateModelEnd, p), new DateLabelFormatter());
        Calendar cal = Calendar.getInstance();
        dateModelEnd.setValue(cal.getTime());
        cal.add(Calendar.MONTH, -3);
        dateModelIni.setValue(cal.getTime());

        DefaultTableModel modelLinks = new DefaultTableModel();
        tableLinks = new JTable(modelLinks) {
            public String getToolTipText(@Nonnull MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                try {
                    tip = getValueAt(rowIndex, colIndex).toString();
                } catch (RuntimeException e1) {
                    MyLogger.e(TAG, e1);
                }
                return tip;
            }
        };
        tableCurriculum = new JTable(new DefaultTableModel()) {
            public String getToolTipText(@Nonnull MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                try {
                    tip = getValueAt(rowIndex, colIndex).toString();
                } catch (RuntimeException e1) {
                    MyLogger.e(TAG, e1);
                }
                return tip;
            }
        };
        modelLinks.setColumnCount(4);
        tableLinks.getColumnModel().getColumn(0).setHeaderValue("Presentation");
        tableLinks.getColumnModel().getColumn(1).setHeaderValue("Presentation_sub");
        tableLinks.getColumnModel().getColumn(2).setHeaderValue("Outcome");
        tableLinks.getColumnModel().getColumn(3).setHeaderValue("Target");
        linksGovernor = new TableLinksGovernor(modelLinks);

        ((DefaultTableModel)tableCurriculum.getModel()).setColumnCount(3);
        tableCurriculum.getColumnModel().getColumn(0).setHeaderValue("Subarea");
        tableCurriculum.getColumnModel().getColumn(1).setHeaderValue("Presentation");
        tableCurriculum.getColumnModel().getColumn(2).setHeaderValue("Exercises");

        tableNationalCurriculum = new JTable(new DefaultTableModel());

        ((DefaultTableModel)tableNationalCurriculum.getModel()).setColumnCount(2);
        tableNationalCurriculum.getColumnModel().getColumn(0).setHeaderValue("Subarea");
        tableNationalCurriculum.getColumnModel().getColumn(1).setHeaderValue("Target");

        buttonLoadLinks.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(mainPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                SWImportLinks sw = new SWImportLinks(file, rBOutcomes.isSelected());
                sw.execute();
            }
        });

        buttonGenerateLinks.addActionListener(e -> {
            SWRecordLinks swRecordLinks = new SWRecordLinks(new java.sql.Date(dateModelIni.getValue().getTime()),
                    new java.sql.Date(dateModelEnd.getValue().getTime()), progressBarLinks);
            swRecordLinks.execute();
        });


        listStages.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            DefaultListModel<String> model = (DefaultListModel<String>) listAreas.getModel();
            model.clear();
            areasList.clear();
            Object[] areas = cacheManager.stageAreaSubareaMontessori.get(listStages.getSelectedIndex()).keySet().toArray();
            Arrays.sort(areas);
            for (Object area : areas) {
                areasList.add((Integer)area);
                model.addElement(cacheManager.areasMontessori.get(area)[settingsManager.language]);
            }
        });

        listStagesNC.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            DefaultListModel<String> model = (DefaultListModel<String>) listNCAreas.getModel();
            model.clear();
            areasNCList.clear();
            int stage = RawData.yearsNC[listStagesNC.getSelectedIndex()];
            ArrayList<Integer> areas = cacheManager.areasTargetPerStage.get(stage);
            for (Integer area : areas) {
                areasNCList.add(area);
                model.addElement(cacheManager.areasTarget.get(area)[settingsManager.language]);
            }
        });

        listAreas.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || listAreas.getSelectedIndex() == -1)  return;
            DefaultListModel<String> model = (DefaultListModel<String>) listSubareas.getModel();
            model.clear();
            subareasList.clear();
            int stage = listStages.getSelectedIndex();
            int area = areasList.get(listAreas.getSelectedIndex());

            HashSet<Integer> subs = cacheManager.stageAreaSubareaMontessori.get(stage).get(area);
            if (subs != null) {
                curriculum = new CurriculumSubareaYear(stage, area, false);
                for (Integer sub : subs) {
                    model.addElement((String)cacheManager.subareasMontessori.get(sub)[settingsManager.language]);
                    subareasList.add(sub);
                }
                tableCurriculum.setModel(curriculum.getTableModel());
            }
        });

        listNCAreas.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || listNCAreas.getSelectedIndex() == -1)  return;
            DefaultListModel<String> model = (DefaultListModel<String>) listNCSubareas.getModel();
            model.clear();
            subareasNCList.clear();
            int stage = listStagesNC.getSelectedIndex();
            int area = areasNCList.get(listNCAreas.getSelectedIndex());

            ArrayList<Integer> subs = cacheManager.subareasTargetPerArea.get(area);
            if (subs != null) {
                curriculum = new CurriculumSubareaYear(stage, area, true);
                for (Integer sub : subs) {
                    model.addElement((String)cacheManager.subareasTarget.get(sub)[settingsManager.language]);
                    subareasNCList.add(sub);
                }
                tableNationalCurriculum.setModel(curriculum.getTableModel());
            }
        });

        listSubareas.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || listSubareas.getSelectedIndex() == -1)  return;
            linksGovernor.clear();
            linksGovernor.loadSubarea(listStages.getSelectedIndex(), subareasList.get(listSubareas.getSelectedIndex()));
        });

        buttonNCText.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text Documents", "txt"));
            fileChooser.setDialogTitle("Specify a file to save");
            fileChooser.setAcceptAllFileFilterUsed(true);
            int userSelection = fileChooser.showSaveDialog(mainPanel);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String fileName = fileToSave.getAbsolutePath();
                try {
                    if (!Files.getFileExtension(fileName).equals("txt")) fileName += ".txt";
                    curriculum.writeToFile(fileName);
                } catch (IOException ex) {
                    MyLogger.e(TAG, ex);
                }
            }
        });

        buttonTxt.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text Documents", "txt"));
            fileChooser.setDialogTitle("Specify a file to save");
            fileChooser.setAcceptAllFileFilterUsed(true);
            int userSelection = fileChooser.showSaveDialog(mainPanel);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String fileName = fileToSave.getAbsolutePath();
                try {
                    if (!Files.getFileExtension(fileName).equals("txt")) fileName += ".txt";
                    curriculum.writeToFile(fileName);
                } catch (IOException ex) {
                    MyLogger.e(TAG, ex);
                }
            }
        });
    }

    private static class TableLinksGovernor {
        final DefaultTableModel model;

        public TableLinksGovernor(DefaultTableModel model) {
            this.model = model;
        }

        @SuppressWarnings("SuspiciousMethodCalls")
        public void loadSubarea(int stage, int subarea) {
            for (String code : cacheManager.links.keySet()) {
                String[] values = code.split("\\.");
                Object[] presentation = cacheManager.presentations.get(values[0]);    //name, nombre, subarea,year,priority
                Object[] presentation_sub = !values[1].equals("0") ? cacheManager.presentationsSub.get(values[1]) : null;

                if (presentation == null) continue;
                if ((Integer)presentation[2] != subarea ||(Double)presentation[3] < RawData.yearsmontessori[stage][0] ||
                        (Double)presentation[3] > RawData.yearsmontessori[stage][1] ) continue;

                CacheManager.PresentationLinks links = cacheManager.links.get(code);
                for (int target : links.targets)
                    addRow((String)presentation[settingsManager.language],
                            presentation_sub != null ? (String)presentation_sub[settingsManager.language] : null,
                            null, (String)cacheManager.targets.get(target)[settingsManager.language]);
                for (int outcome : links.outcomes)
                    addRow((String)presentation[settingsManager.language],
                            presentation_sub != null ? (String)presentation_sub[settingsManager.language] : null,
                            (String)cacheManager.outcomes.get(outcome)[settingsManager.language], null);
            }
        }

        private void addRow(String presentation, String presentation_sub, String outcome, String target) {
            model.addRow(new Object[]{presentation, presentation_sub,
                    outcome, target});
        }

        public void clear() {
            model.setRowCount(0);
        }
    }

    private static class TableCurriculumGovernor {
        final DefaultTableModel model;

        public TableCurriculumGovernor(DefaultTableModel model) {
            this.model = model;
        }

        @SuppressWarnings("SuspiciousMethodCalls")
        public void loadSubarea(int stage, int subarea) {
            for (String code : cacheManager.links.keySet()) {
                String[] values = code.split("\\.");
                Object[] presentation = cacheManager.presentations.get(values[0]);    //name, nombre, subarea,year,priority
                Object[] presentation_sub = !values[1].equals("0") ? cacheManager.presentationsSub.get(values[1]) : null;

                if (presentation == null) continue;
                if ((Integer)presentation[2] != subarea ||(Double)presentation[3] < RawData.yearsmontessori[stage][0] ||
                        (Double)presentation[3] > RawData.yearsmontessori[stage][1] ) continue;

                CacheManager.PresentationLinks links = cacheManager.links.get(code);
                for (int target : links.targets)
                    addRow((String)presentation[settingsManager.language],
                            presentation_sub != null ? (String)presentation_sub[settingsManager.language] : null,
                            null, (String)cacheManager.targets.get(target)[settingsManager.language]);
                for (int outcome : links.outcomes)
                    addRow((String)presentation[settingsManager.language],
                            presentation_sub != null ? (String)presentation_sub[settingsManager.language] : null,
                            (String)cacheManager.outcomes.get(outcome)[settingsManager.language], null);
            }
        }

        private void addRow(String presentation, String presentation_sub, String outcome, String target) {
            model.addRow(new Object[]{presentation, presentation_sub,
                    outcome, target});
        }

        public void clear() {
            model.setRowCount(0);
        }
    }

}
