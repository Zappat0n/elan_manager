package ui.formMedia;

import bd.BDManager;
import bd.MySet;
import bd.model.TableMedia;
import drive.DriveGovernor;
import ui.components.DateLabelFormatter;
import drive.MediaPicture;
import utils.CacheManager;
import utils.ImageUtils;
import utils.MyLogger;
import utils.SettingsManager;
import utils.data.RawData;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;
import java.util.*;

public class MediaForm {
    private static final String TAG = MediaForm.class.getSimpleName();
    private static BDManager bdManager;
    private static SettingsManager settingsManager;
    private static CacheManager cacheManager;
    private JPanel mainPanel;
    private JList listClassrooms;
    private JList listStudents;
    private JPanel panelImgs;
    private JTextArea textAreaComments;
    private JLabel labelPresentation;
    private JLabel labelPresentationSub;
    private UtilDateModel dateModel;
    private JDatePickerImpl datePickerImplPictureDate;
    private JButton buttonUpdate;
    private JButton buttonDelete;
    private JLabel labelMainImg;
    private JLabel labelProgress;
    private JList listNCAreas;
    private JList listNCSubareas;
    private JList listNCTargets;
    private JCheckBox cBTargetOrOutcome;
    private JButton buttonTurnRight;
    private JButton buttonUpload;
    private JButton buttonTurnLeft;
    public DriveGovernor driveGovernor;
    private HashMap<JRadioButton, MediaPicture> mediaFiles;
    private ArrayList<Integer> subareas;
    private ArrayList<Integer> targets;
    MediaPicture currentPicture;
    JRadioButton currentItem;

    public static JPanel main(SettingsManager settingsManager, BDManager bdManager, CacheManager cacheManager) {
        MediaForm.bdManager = bdManager;
        MediaForm.settingsManager = settingsManager;
        MediaForm.cacheManager = cacheManager;
        MediaForm form = new MediaForm();
        return form.mainPanel;
    }

    private void createUIComponents() {
        subareas = new ArrayList<>();
        targets = new ArrayList<>();
        mediaFiles = new HashMap<>();
        SwingUtilities.invokeLater(()-> {
            try {
                driveGovernor = new DriveGovernor(bdManager, cacheManager, null);
            } catch (Exception e) {
                MyLogger.e(TAG, e);
            }
        });

        panelImgs = new JPanel();
        panelImgs.setBorder(new EmptyBorder(10, 10, 0, 0));
        BoxLayout layout = new BoxLayout(panelImgs, BoxLayout.PAGE_AXIS);
        panelImgs.setLayout(layout);

        dateModel = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(dateModel, p);
        datePickerImplPictureDate = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        dateModel.setValue(new Date());

        listClassrooms = new JList(cacheManager.getClassroomsListModel());
        listStudents = new JList();
        listClassrooms.addListSelectionListener(e -> {
            if (listClassrooms.getSelectedIndex() != -1)
                listStudents.setModel(cacheManager.getStudentsListModel(listClassrooms.getSelectedIndex()+1));
        });

        listStudents.addListSelectionListener(e -> {
            if (listStudents.getValueIsAdjusting() || listStudents.getSelectedIndex() == -1 ||
                    listClassrooms.getSelectedIndex() == -1) return;
            Integer student = cacheManager.studentsperclassroom.get(listClassrooms.getSelectedIndex()+1).get(listStudents.getSelectedIndex());
            mediaFiles.clear();
            panelImgs.removeAll();
            SWLoadImages load = new SWLoadImages(student);
            load.execute();
        });

        buttonDelete = new JButton();
        buttonDelete.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                Integer result = driveGovernor.manager.delete(currentPicture.fileId);
                if (result == 1 || result == 404) {
                    Connection co = null;
                    try {
                        co = bdManager.connect();
                        bdManager.removeValue(co, BDManager.tableMedia, TableMedia.id + "=" + currentPicture.id, false);
                        panelImgs.remove(currentItem);
                        if (!mediaFiles.remove(currentItem, mediaFiles.get(currentItem))) MyLogger.d(TAG, "Error removing picture");
                        panelImgs.updateUI();
                        labelProgress.setText("");
                    } finally {
                        BDManager.closeQuietly(co);
                    }
                } else JOptionPane.showMessageDialog(mainPanel, "Error deleting picture", "ERROR",
                        JOptionPane.ERROR_MESSAGE);
            });
        });

        buttonUpdate = new JButton();
        buttonUpdate.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                Connection co = null;
                try {
                    co = bdManager.connect();
                    //id, date, student, presentation, presentation_sub, comment, fileId;
                    String[] keys = {TableMedia.date, TableMedia.comment};
                    java.sql.Date newDate = new java.sql.Date(dateModel.getValue().getTime());
                    String newComment = textAreaComments.getText();
                    String[] values = {newDate.toString(), newComment};
                    bdManager.updateValues(co, BDManager.tableMedia, keys, values, TableMedia.id + "=" + currentPicture.id);
                    currentPicture.date = newDate;
                    currentPicture.comments = newComment;
                } finally {
                    BDManager.closeQuietly(co);
                }
            });
        });

        listNCAreas = new JList(new DefaultListModel());
        RawData.cdbAreasTarget.forEach(i -> ((DefaultListModel)listNCAreas.getModel()).addElement(
                cacheManager.areasTarget.get(i)[settingsManager.language]));

        listNCAreas.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            subareas.clear();
            Integer area = RawData.cdbAreasTarget.get(listNCAreas.getSelectedIndex());
            DefaultListModel model = (DefaultListModel)listNCSubareas.getModel();
            model.clear();
            cacheManager.subareasTargetperarea.get(area).forEach(subarea -> {
                if (!cBTargetOrOutcome.isSelected()) {
                    Integer[] months = getMonths();
                    for (Integer month : months) {
                        ArrayList<Integer> outcomes = cacheManager.outcomespermonthandsubarea.get(month).get(subarea);
                        if (outcomes != null && !subareas.contains(subarea)) {
                            subareas.add(subarea);
                            String[] data = cacheManager.subareasTarget.get(subarea);
                            model.addElement(data[settingsManager.language]);
                        }
                    }
                } else {
                    Double[] years = getYears();
                    if (years != null)
                        for (Double year : years) {
                            ArrayList<Integer> targets = cacheManager.targetsperyearandsubarea.get(year).get(subarea);
                            if (targets != null && !subareas.contains(subarea)) {
                                subareas.add(subarea);
                                String[] data = cacheManager.subareasTarget.get(subarea);
                                model.addElement(data[settingsManager.language]);
                            }
                        }
                }
            });
        });
        listNCSubareas = new JList(new DefaultListModel());
        listNCSubareas.addListSelectionListener(e -> {
            Integer selArea = listNCAreas.getSelectedIndex();
            Integer selSubArea = listNCSubareas.getSelectedIndex();
            if (e.getValueIsAdjusting() || selArea == -1 || selSubArea == -1) return;
            DefaultListModel model = (DefaultListModel)listNCTargets.getModel();
            model.clear();
            Integer area = RawData.cdbAreasTarget.get(selArea);
            Integer subarea = cacheManager.subareasTargetperarea.get(area).get(selSubArea);
            if (!cBTargetOrOutcome.isSelected()) {
                Integer[] months = getMonths();
                if (months != null)
                    for (Integer month : months) {
                        ArrayList<Integer> outcomes = cacheManager.outcomespermonthandsubarea.get(month).get(subarea);
                        if (outcomes != null) {
                            outcomes.forEach(o -> model.addElement(cacheManager.outcomes.get(o)[settingsManager.language]));
                        }
                    }
            } else {
                Double[] years = getYears();
                if (years != null)
                    for (Double year : years) {
                        ArrayList<Integer> targets = cacheManager.targetsperyearandsubarea.get(year).get(subarea);
                        if (targets != null) {
                            targets.forEach(t -> model.addElement(cacheManager.targets.get(t)[settingsManager.language]));
                        }
                    }
            }
        });
        listNCTargets = new JList(new DefaultListModel());

        buttonTurnLeft = new JButton();
        buttonTurnLeft.addActionListener(actionEvent -> {
            if (currentPicture.image == null) return;
            currentPicture.image = ImageUtils.convertImageToJpg(currentPicture.image);
            currentPicture.image = ImageUtils.rotateImage(currentPicture.image, -90);
            labelMainImg.setIcon(new ImageIcon(currentPicture.image));

        });

        buttonTurnRight = new JButton();
        buttonTurnRight.addActionListener(actionEvent -> {
            if (currentPicture.image == null) return;
            currentPicture.image = ImageUtils.convertImageToJpg(currentPicture.image);
            currentPicture.image = ImageUtils.rotateImage(currentPicture.image, 90);
            labelMainImg.setIcon(new ImageIcon(currentPicture.image));
        });

        buttonUpload = new JButton();
        buttonUpload.addActionListener(actionEvent -> {
            MediaPicture picture = mediaFiles.get(currentItem);
            picture.image = currentPicture.image;
            driveGovernor.manager.updateFile(picture.fileId, dateModel.getValue(), picture);
        });
    }

    private Integer[] getMonths(){
        switch (listClassrooms.getSelectedIndex()) {
            case 0: return RawData.monthsOutcomesforEY;
            case 1: case 2: return RawData.monthsOutcomesforFS;
        }
        return null;
    }

    private Double[] getYears(){
        switch (listClassrooms.getSelectedIndex()) {
            case 0: return  new Double[] {2.5};
            case 1: return  new Double[] {5d, 6d};
        }
        return null;
    }


    private class SWLoadImages extends SwingWorker {
        ImageIcon blankImage;
        Integer student;

        public SWLoadImages(Integer student) {
            this.student = student;
        }

        private void processMediaPicture(MediaPicture picture) {
            ImageIcon icon = picture.image != null ?
                    new ImageIcon(ImageUtils.resizeImage(picture.image, 120, 120)) :
                    (blankImage != null ? blankImage : createBlankImage());
            JRadioButton item = new JRadioButton(icon);
            item.setText(picture.date.toString());
            item.setVerticalTextPosition(JLabel.BOTTOM);
            item.setVerticalAlignment(JLabel.CENTER);
            item.setHorizontalAlignment(JLabel.CENTER);
            item.setHorizontalTextPosition(JLabel.CENTER);
            item.setFont(item.getFont().deriveFont(Font.PLAIN, 8));
            item.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    for (Component comp : panelImgs.getComponents()) {
                        comp.setBackground(panelImgs.getBackground());
                    }
                    currentItem = (JRadioButton) mouseEvent.getComponent();
                    currentItem.setBackground(Color.yellow);

                    currentPicture = mediaFiles.get(currentItem);
                    if (currentPicture.image != null) labelMainImg.setIcon(new ImageIcon(currentPicture.image));
                    dateModel.setValue(new Date(currentPicture.date.getTime()));
                    labelPresentation.setText(
                            (String)cacheManager.presentations.get(currentPicture.presentation)[settingsManager.language]);
                    if (currentPicture.presentationSub != null && currentPicture.presentationSub != -1)
                        labelPresentationSub.setText(
                                (String)cacheManager.presentationssub.get(currentPicture.presentationSub)[settingsManager.language]);
                    if (currentPicture.comments != null) textAreaComments.setText(currentPicture.comments);
                }
                @Override
                public void mousePressed(MouseEvent mouseEvent) {}
                @Override
                public void mouseReleased(MouseEvent mouseEvent) {}
                @Override
                public void mouseEntered(MouseEvent mouseEvent) {}
                @Override
                public void mouseExited(MouseEvent mouseEvent) {}
            });
            mediaFiles.put(item, picture);
            panelImgs.add(item);
            panelImgs.add(Box.createRigidArea(new Dimension(0, 10)));
            panelImgs.updateUI();
        }

        private ImageIcon createBlankImage(){
            try {
                blankImage = new ImageIcon(ImageUtils.resizeImage(
                        ImageIO.read(getClass().getResourceAsStream("file-broken-icon.png")), 120, 120));
                return blankImage;
            } catch (IOException e) {
                MyLogger.e(TAG, e);
            }
            return null;
        }

        @Override
        protected Object doInBackground() {
            panelImgs.removeAll();
            Connection co = null;
            try {
                co = bdManager.connect();
                MySet set = bdManager.getValues(co, BDManager.tableMedia, TableMedia.student + "=" + student);
                int counter = 1;
                if (set.size() > 0)
                    while (set.next()) {
                        Integer id = set.getInt(TableMedia.id);
                        Integer student = set.getInt(TableMedia.student);
                        java.sql.Date date = set.getDate(TableMedia.date);
                        Integer presentation = set.getInt(TableMedia.presentation);
                        Integer presentation_sub = set.getInt(TableMedia.presentation_sub);
                        String fileId = set.getString(TableMedia.fileId);
                        String comments = set.getString(TableMedia.comment);
                        BufferedImage image = driveGovernor.manager.download(fileId);

                        MediaPicture _picture = new MediaPicture(image, id, fileId, student, date, presentation, presentation_sub, comments);
                        labelProgress.setText(counter++ + " / " + set.data.size());
                        processMediaPicture(_picture);
                    }
                else {
                    labelProgress.setText("0 / 0");
                    panelImgs.removeAll();
                    panelImgs.updateUI();
                }
            } catch (Exception ex) {
                MyLogger.e(TAG, ex);
            } finally {
                BDManager.closeQuietly(co);
            }
            return null;
        }
    }
}
