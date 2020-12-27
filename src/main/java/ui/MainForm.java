package ui;

import main.ApplicationLoader;
import ui.formChildData.ChildDataForm;
import ui.formClassroom.ClassroomForm;
import ui.formClassroomTargets.ClassroomFormTargets;
import ui.formConfig.ConfigForm;
import ui.formCurriculum.CurriculumForm;
import ui.formDatabase.contacts.DBContactsForm;
import ui.formMedia.MediaForm;
import ui.formReports.ReportsForm;
import ui.formUpload.UploadForm;
import utils.MyLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * Created by angel on 5/02/17.
 */
class MainForm {
    private static final String TAG = MainForm.class.getSimpleName();
    public static JFrame frame;
    private JPanel mainPanel;
    private JButton buttonChild;
    private JButton buttonAddData;
    private JButton buttonClassroom;
    private JButton buttonTargets;
    private JPanel containerPanel;
    private JButton buttonConfig;
    private JButton buttonUpload;
    private JButton buttonReports;
    private JButton buttonCurriculum;
    private JButton buttonDatabase;
    private JButton buttonMedia;

    public MainForm() {
    }

    public static void main(String[] args) {
        frame = new JFrame("Elan Manager");
        MainForm form = new MainForm();
        frame.setContentPane(form.mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                MyLogger.emailLog();
            }
        });
    }

    private void createUIComponents(){
        containerPanel = new JPanel();
        containerPanel.setLayout(new GridLayout(1, 1));
        buttonAddData = new JButton();
        buttonAddData.addActionListener(e -> SwingUtilities.invokeLater(() -> AddDataForm.main(
                ApplicationLoader.bdManager, ApplicationLoader.settingsManager, ApplicationLoader.cacheManager)));
        buttonChild = new JButton();
        buttonChild.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (!ReportsForm.yetChanged) {
                addPanelToContainer(ChildDataForm.main(ApplicationLoader.bdManager, ApplicationLoader.settingsManager,
                        ApplicationLoader.cacheManager));
            } else JOptionPane.showMessageDialog(mainPanel, "Please save before leaving");
        }));
        buttonTargets = new JButton();
        buttonTargets.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (!ReportsForm.yetChanged) {
                addPanelToContainer(ClassroomFormTargets.main(ApplicationLoader.bdManager, ApplicationLoader.settingsManager, ApplicationLoader.cacheManager));
            } else JOptionPane.showMessageDialog(mainPanel, "Please save before leaving");
        }));
        buttonClassroom = new JButton();
        buttonClassroom.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (!ReportsForm.yetChanged) {
                addPanelToContainer(ClassroomForm.main(ApplicationLoader.bdManager, ApplicationLoader.settingsManager, ApplicationLoader.cacheManager));
            } else JOptionPane.showMessageDialog(mainPanel, "Please save before leaving");
        }));
        buttonConfig = new JButton();
        buttonConfig.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (!ReportsForm.yetChanged) {
                addPanelToContainer(ConfigForm.main(ApplicationLoader.settingsManager, ApplicationLoader.bdManager, ApplicationLoader.cacheManager));
            } else JOptionPane.showMessageDialog(mainPanel, "Please save before leaving");
        }));
        buttonUpload = new JButton();
        buttonUpload.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (!ReportsForm.yetChanged) {
                addPanelToContainer(UploadForm.main(
                        ApplicationLoader.settingsManager, ApplicationLoader.bdManager, ApplicationLoader.cacheManager));
            } else JOptionPane.showMessageDialog(mainPanel, "Please save before leaving");
        }));
        buttonReports = new JButton();
        buttonReports.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (!ReportsForm.yetChanged) {
                addPanelToContainer(ReportsForm.main(ApplicationLoader.settingsManager, ApplicationLoader.bdManager,
                        ApplicationLoader.cacheManager, frame));
            } else JOptionPane.showMessageDialog(mainPanel, "Please save before leaving");
        }));
        buttonCurriculum = new JButton();
        buttonCurriculum.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (!ReportsForm.yetChanged) {
                addPanelToContainer(CurriculumForm.main(ApplicationLoader.settingsManager, ApplicationLoader.bdManager,
                        ApplicationLoader.cacheManager));
            } else JOptionPane.showMessageDialog(mainPanel, "Please save before leaving");
        }));
        buttonDatabase = new JButton();
        buttonDatabase.addActionListener(actionEvent -> {
            final JPopupMenu menu = new JPopupMenu("Menu");
            JMenuItem m1 = new JMenuItem("Contacts");
            m1.addActionListener(e -> {
                if (!ReportsForm.yetChanged) {
                    addPanelToContainer(DBContactsForm.main(ApplicationLoader.settingsManager, ApplicationLoader.bdManager,
                            ApplicationLoader.cacheManager));
                } else JOptionPane.showMessageDialog(mainPanel, "Please save before leaving");
            });
            menu.add(m1);
            menu.add("B");
            menu.add("C");
            menu.show(buttonDatabase, buttonDatabase.getWidth() + 10, 0);
        });
        buttonMedia = new JButton();
        buttonMedia.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (!ReportsForm.yetChanged) {
                addPanelToContainer(MediaForm.main(
                        ApplicationLoader.settingsManager, ApplicationLoader.bdManager, ApplicationLoader.cacheManager));
            } else JOptionPane.showMessageDialog(mainPanel, "Please save before leaving");
        }));

        addPanelToContainer(ConfigForm.main(ApplicationLoader.settingsManager, ApplicationLoader.bdManager, ApplicationLoader.cacheManager));

    }

    private void addPanelToContainer(JPanel panel) {
        containerPanel.removeAll();
        containerPanel.add(panel);
        containerPanel.updateUI();
    }
}
