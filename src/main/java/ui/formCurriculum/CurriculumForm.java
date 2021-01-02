package ui.formCurriculum;

import com.google.common.io.Files;
import links.SWImportLinks;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class CurriculumForm {
    private static final String TAG = CurriculumForm.class.getSimpleName();
    private static SettingsManager settingsManager;
    private static CacheManager cacheManager;
    private JPanel mainPanel;
    private JList listStages;
    private JTable tableCurriculum;
    private JList<String> listAreas;
    private JButton buttonPdf;
    private JButton buttonTxt;
    private JButton buttonLoadLinks;
    private JTable tableImportLinks;
    private JList<String> listSubareas;
    private JTable tableLinks;
    private JButton button1;
    private ArrayList<Integer> areasList;
    private ArrayList<Integer> subareasList;
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
        subareasList = new ArrayList<>();
        listStages = new JList<>();
        listAreas = new JList<>(new DefaultListModel<>());
        listSubareas = new JList<>(new DefaultListModel<>());
        buttonPdf = new JButton();
        buttonTxt = new JButton();
        buttonLoadLinks = new JButton();
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

        buttonLoadLinks.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(mainPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                SWImportLinks sw = new SWImportLinks(file, true);
                sw.execute();
            }
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

        listAreas.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || listAreas.getSelectedIndex() == -1)  return;
            DefaultListModel<String> model = (DefaultListModel<String>) listSubareas.getModel();
            model.clear();
            subareasList.clear();
            int stage = listStages.getSelectedIndex();
            int area = areasList.get(listAreas.getSelectedIndex());

            HashSet<Integer> subs = cacheManager.stageAreaSubareaMontessori.get(stage).get(area);
            if (subs != null) {
                curriculum = new CurriculumSubareaYear(cacheManager, settingsManager, stage, area);
                for (Integer sub : subs) {
                    model.addElement((String)cacheManager.subareasMontessori.get(sub)[settingsManager.language]);
                    subareasList.add(sub);
                }
                tableCurriculum.setModel(curriculum.getTableModel());
            }
        });

        listSubareas.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || listSubareas.getSelectedIndex() == -1)  return;
            linksGovernor.clear();
            linksGovernor.loadSubarea(listStages.getSelectedIndex(), subareasList.get(listSubareas.getSelectedIndex()));
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

        public void loadSubarea(int stage, int subarea) {
            for (int[] link : cacheManager.links.keySet()) {
                Object[] presentation = cacheManager.presentations.get(link[0]);    //name, nombre, subarea,year,priority
                Object[] presentation_sub = link[1] != 0 ? cacheManager.presentationsSub.get(link[1]) : null;

                if (presentation == null) continue;
                if ((Integer)presentation[2] != subarea ||(Double)presentation[3] < RawData.yearsmontessori[stage][0] ||
                        (Double)presentation[3] > RawData.yearsmontessori[stage][1] ) continue;

                CacheManager.PresentationLinks links = cacheManager.links.get(link);
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

        public void loadSubarea(int stage, int subarea) {
            for (int[] link : cacheManager.links.keySet()) {
                Object[] presentation = cacheManager.presentations.get(link[0]);    //name, nombre, subarea,year,priority
                Object[] presentation_sub = link[1] != 0 ? cacheManager.presentationsSub.get(link[1]) : null;

                if (presentation == null) continue;
                if ((Integer)presentation[2] != subarea ||(Double)presentation[3] < RawData.yearsmontessori[stage][0] ||
                        (Double)presentation[3] > RawData.yearsmontessori[stage][1] ) continue;

                CacheManager.PresentationLinks links = cacheManager.links.get(link);
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
