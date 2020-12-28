package ui.formUpload;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import bd.BDManager;
import drive.DriveGovernor;
import ui.components.DateLabelFormatter;
import ui.dialogs.CreatePresentation;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


public class UploadForm {
    private static final String TAG = UploadForm.class.getSimpleName();
    private static final String[] EXTENSIONS = new String[]{"gif", "png", "jpg", "JPG", "jpeg", "bmp"};
    private static final FilenameFilter imageFilter = (dir, name) -> {
        for (final String ext : EXTENSIONS)
            if (name.endsWith("." + ext)) return true;
        return false;
    };
    private JPanel mainPanel;
    public static JFrame frame;
    protected static BDManager bdmanager;
    protected static CacheManager cacheManager;
    protected static SettingsManager settingsManager;
    private JButton buttonLoad;
    private JButton buttonUpload;
    private JComboBox<String> cBClassroom;
    private JComboBox<String> cBArea;
    private JComboBox<String> cBSubarea;
    private JList<String> listPresentations;
    private JList<String> listPresentationsSub;
    private JList<String> listStudents;
    private JLabel labelDate;
    private ArrayList<BufferedImage> imgs;
    private ArrayList<Integer> areas;
    private ArrayList<Integer> students;
    private ArrayList<Integer> subareas;
    private LinkedHashMap<String, Integer[]> presentationssearched;
    private ArrayList<Integer> presentations;
    private ArrayList<Integer> presentationssub;
    private DriveGovernor driveGovernor;
    private JPanel panelImgs;
    private JLabel labelMainImg;
    private JPanel panelLabel;
    private UtilDateModel dateModel;
    private JDatePickerImpl datePicker;
    private JSplitPane rightSplitPane;
    private JTextArea tAComments;
    private JList<String> listLog;
    private JSplitPane spBottom;
    private JComboBox cbStage;
    private JList<String> listSearchedPresentations;
    private JTextField tFSearch;
    private JTabbedPane tabPresentations;
    private JButton buttonTurnLeft;
    private JButton buttonTurnRight;
    private JButton buttonCreatePresentation;
    private File mainImageFile;
    private BufferedImage mainImage;
    private BufferedImage mainOriginalImage;
    public static Logger logger;
    private Boolean imageRotated = false;
    Integer stage;

    public static JPanel main(SettingsManager settingsManager, BDManager bdManager, CacheManager cacheManager) {
        UploadForm.settingsManager = settingsManager;
        UploadForm.bdmanager = bdManager;
        UploadForm.cacheManager = cacheManager;
        UploadForm formUpload = new UploadForm();
        //formUpload.spBottom.setDividerLocation(formUpload.mainPanel.getParent().getWidth() - 300);
        return formUpload.mainPanel;
    }

    public static void close() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        Boolean isClosing = true;
    }

    private void createUIComponents() {
        dateModel = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(dateModel, p);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        dateModel.setValue(new Date());
        cBClassroom = new JComboBox<>();
        labelDate = new JLabel();
        cBArea = new JComboBox<>();
        cBSubarea = new JComboBox<>(new DefaultComboBoxModel<>());
        listStudents = new JList<>(new DefaultListModel<>());
        listPresentations = new JList<>(new DefaultListModel<>());
        listPresentationsSub = new JList<>(new DefaultListModel<>());
        listSearchedPresentations = new JList<>(new DefaultListModel<>());
        panelLabel = new JPanel();
        students = new ArrayList<>();
        subareas = new ArrayList<>();
        presentations = new ArrayList<>();
        presentationssearched = new LinkedHashMap<>();
        presentationssub = new ArrayList<>();
        imgs = new ArrayList<>();
        listLog = new JList<>(new DefaultListModel<>());
        buttonUpload = new JButton();
        buttonLoad = new JButton();
        labelMainImg = new JLabel();
        panelImgs = new JPanel();
        panelImgs.setBorder(new EmptyBorder(10, 10, 0, 0));
        BoxLayout layout = new BoxLayout(panelImgs, BoxLayout.PAGE_AXIS);
        panelImgs.setLayout(layout);
        panelLabel = new JPanel();
        tFSearch = new JTextField();
        logger = new Logger(listLog);
        spBottom = new JSplitPane();

        SwingUtilities.invokeLater(() -> {
            driveGovernor = new DriveGovernor(bdmanager, cacheManager, buttonUpload);
            areas = new ArrayList<>();
            cBClassroom.addItem("");
            for (String cl : RawData.classrooms) {
                cBClassroom.addItem(cl);
            }

            cBClassroom.addItemListener(e -> {
                DefaultListModel<String> model = (DefaultListModel<String>) listStudents.getModel();
                model.clear();
                students.clear();
                int classroom = cBClassroom.getSelectedIndex();
                if (classroom == 0) return;
                for (int id : cacheManager.studentsperclassroom.get(classroom)) {
                    model.addElement((String)cacheManager.students.get(id)[0]);
                    students.add(id);
                }
                cBArea.removeAllItems();
                areas.clear();
                stage = cacheManager.getStageofClassroom(cBClassroom.getSelectedIndex());
                if (stage == null) return;
                cBArea.insertItemAt("", 0);
                LinkedHashMap<Integer, HashSet<Integer>> newareas = cacheManager.stageAreaSubareaMontessori.get(stage);
                for (Integer ar : newareas.keySet()) {
                    areas.add(ar);
                    cBArea.addItem(cacheManager.areasMontessori.get(ar)[settingsManager.language]);
                }
            });

            cBArea.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                cBSubarea.removeAllItems();
                subareas.clear();
                DefaultListModel modelPresentations = (DefaultListModel) listPresentations.getModel();
                DefaultListModel modelPresentationsSub = (DefaultListModel) listPresentationsSub.getModel();
                modelPresentations.clear();
                modelPresentationsSub.clear();
                presentations.clear();
                presentationssub.clear();
                Integer area = areas.get(cBArea.getSelectedIndex()-1);
                Integer stage = cacheManager.getStage(cBClassroom.getSelectedIndex()+1);
                if (stage == null) return;
                LinkedHashMap<Integer, HashSet<Integer>> newareas = cacheManager.stageAreaSubareaMontessori.get(stage);
                HashSet<Integer> newsubareas =newareas.get(area);
                if ( newsubareas != null) {
                    cBSubarea.addItem("");
                    for (Integer id : newsubareas) {
                        String name = (String)cacheManager.subareasMontessori.get(id)[settingsManager.language];
                        subareas.add(id);
                        cBSubarea.addItem(name);
                    }
                }
            });

            cBSubarea.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                if (cBSubarea.getSelectedIndex() == 0) return;
                Integer stage = cacheManager.getStage(cBClassroom.getSelectedIndex()+1);
                if (stage == null) return;
                DefaultListModel<String> modelPresentations = (DefaultListModel<String>) listPresentations.getModel();
                //DefaultListModel modelPresentationsSub = (DefaultListModel) listPresentationsSub.getModel();
                modelPresentations.clear();
                //modelPresentationsSub.clear();
                presentationssub.clear();
                Integer subarea = subareas.get(cBSubarea.getSelectedIndex()-1);
                double min = RawData.yearsmontessori[stage][0];
                double max = RawData.yearsmontessori[stage][1];
                presentations = cacheManager.getPresentations(subarea, min, max);
                for (Integer id : presentations) {
                    modelPresentations.addElement((String)cacheManager.presentations.get(id)[settingsManager.language]);
                }
            });

            listPresentations.addListSelectionListener(e -> {
                if (listPresentations.getSelectedIndex() == -1) return;
                updatePresentationsSub(presentations.get(listPresentations.getSelectedIndex()));
            });

            listSearchedPresentations.addListSelectionListener(e -> {
                if (listSearchedPresentations.getSelectedIndex() == -1) return;
                String searched = (String) listSearchedPresentations.getSelectedValue();
                updatePresentationsSub(presentationssearched.get(searched)[0]);
            });

            listSearchedPresentations.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    JList l = (JList) e.getSource();
                    int index = l.locationToIndex(e.getPoint());
                    if (index > -1) {
                        String searched = (String) l.getModel().getElementAt(index);
                        Integer subarea = presentationssearched.get(searched)[1];
                        Object[] data = cacheManager.subareasMontessori.get(subarea);
                        Integer area = (Integer) data[2];
                        String area_name = (String) data[settingsManager.language];
                        l.setToolTipText(cacheManager.areasMontessori.get(area)[settingsManager.language] + " -> " + area_name);
                    }
                }
            });
        });

        buttonUpload.addActionListener(e -> {
            if (driveGovernor != null) {
                try {
                    Date date = !labelDate.getText().equals("") ? new SimpleDateFormat("dd/MM/yyyy").parse(labelDate.getText()) :
                            dateModel.getValue();
                    if (date == null) {
                        UploadForm.showMessage("Please select an estimated date for the picture");
                        return;
                    }
                    int classroom = cBClassroom.getSelectedIndex();
                    if (classroom == -1) {
                        UploadForm.showMessage("Please select the classroom");
                        return;
                    }
                    Integer student = listStudents.getSelectedIndex();
                    if (student != -1) student = students.get(student);
                    else {
                        UploadForm.showMessage("Please select a student");
                        return;
                    }
                    if (labelMainImg.getIcon() == null ) {
                        UploadForm.showMessage("Please select a picture");
                        return;
                    }
                    Integer presentation;
                    if (tabPresentations.getSelectedIndex() == 0) {
                        presentation = listPresentations.getSelectedIndex();
                        if (presentation != -1) presentation = presentations.get(presentation);
                        else if (listSearchedPresentations.getSelectedIndex() != -1) {
                            String searched = (String) listSearchedPresentations.getSelectedValue();
                            presentation = presentationssearched.get(searched)[0];
                        } else {
                            UploadForm.showMessage("Please select a presentation");
                            return;
                        }
                    } else {
                        presentation = listSearchedPresentations.getSelectedIndex();
                        if (presentation != -1) {
                            String searched = (String) listSearchedPresentations.getSelectedValue();
                            presentation = presentationssearched.get(searched)[0];
                        } else if (listPresentations.getSelectedIndex() != -1)
                            presentation = presentations.get(listPresentations.getSelectedIndex());
                        else {
                            UploadForm.showMessage("Please select a presentation");
                            return;
                        }
                    }

                    Integer presentationsub = listPresentationsSub.getSelectedIndex();
                    if (presentationsub != -1) presentationsub = presentationssub.get(presentationsub);

                    if (imageRotated) {
                        Boolean result = ImageIO.write(ImageUtils.convertImageToJpg(mainOriginalImage), "jpg", mainImageFile);//mainImageFile);
                        MyLogger.d(TAG, String.valueOf(result));
                    }

                    driveGovernor.uploadPicture(student, date, presentation, presentationsub, mainImageFile, tAComments.getText());
                } catch (Exception ex) {
                    MyLogger.e(TAG, ex);
                }
            }
        });

        buttonLoad.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            String lastDir = settingsManager.getValue(SettingsManager.LASTDIR);
            if (lastDir != null) chooser.setCurrentDirectory(new File(lastDir));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int returnVal = chooser.showOpenDialog(mainPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] files;
                if (!chooser.getSelectedFile().isFile()) {
                    settingsManager.addValue(SettingsManager.LASTDIR, chooser.getSelectedFile().getAbsolutePath());
                    files = chooser.getSelectedFile().listFiles(imageFilter);
                } else {
                    files = new File[]{chooser.getSelectedFile()};
                    settingsManager.addValue(SettingsManager.LASTDIR, chooser.getSelectedFile().getParent());
                }
                System.out.println("Selected files:" + Objects.requireNonNull(files).length);
                SWLoadImages load = new SWLoadImages(files, imgs, labelDate);
                load.execute();
            }
        });

        buttonTurnRight = new JButton();
        buttonTurnRight.addActionListener(e -> rotacionMainImagen(90));
        buttonTurnLeft = new JButton();
        buttonTurnLeft.addActionListener(e -> rotacionMainImagen(-90));
        tFSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {updateSearch(); }
            @Override
            public void removeUpdate(DocumentEvent documentEvent) {updateSearch(); }
            @Override
            public void changedUpdate(DocumentEvent documentEvent) {updateSearch(); }
        });

        buttonCreatePresentation = new JButton();
        buttonCreatePresentation.addActionListener( e-> {
            Integer area = cBArea.getSelectedIndex() != -1 ? areas.get(cBArea.getSelectedIndex()-1) : null;
            Integer subarea = (cBSubarea.getSelectedIndex() != 0 && cBSubarea.getSelectedIndex() != -1) ?
                    subareas.get(cBSubarea.getSelectedIndex() - 1) : null;
            Integer presentation = (listPresentations.getSelectedIndex() != 0 && listPresentations.getSelectedIndex() != -1) ?
                    presentations.get(listPresentations.getSelectedIndex() - 1) : null;
            CreatePresentation.main(cacheManager, bdmanager, settingsManager, stage, area, subarea, presentation);
        });
    }

    private void rotacionMainImagen(double grades) {
        mainOriginalImage = ImageUtils.rotateImage(mainOriginalImage, grades);
        mainImage = ImageUtils.rotateImage(mainImage, grades);
        setMainImage(mainImageFile, mainImage);
        imageRotated = true;
    }

    private void setMainImage(File f, BufferedImage image){
        mainImageFile = f;
        mainImage = ImageUtils.resizeImage(image, Math.round(panelLabel.getWidth()), Math.round(panelLabel.getHeight()));
        //((Long)Math.round(panelLabel.getWidth() * 0.9)).intValue(),((Long)Math.round(panelLabel.getHeight() * 0.95)).intValue());
        labelMainImg.setIcon(new ImageIcon(mainImage));
        labelMainImg.updateUI();
        imageRotated = false;
    }

    private void updatePresentationsSub(Integer presentation) {
        SwingUtilities.invokeLater(() -> {
            DefaultListModel<String> modelPresentationsSub = (DefaultListModel<String>) listPresentationsSub.getModel();
            modelPresentationsSub.clear();
            presentationssub.clear();
            ArrayList<Integer> list = cacheManager.presentationssubperpresentation.get(presentation);
            if (list != null && list.size() > 0)
                for (Integer id : list) {
                    modelPresentationsSub.addElement((String)cacheManager.presentationsSub.get(id)[settingsManager.language]);
                    presentationssub.add(id);
                }
        });
    }

    private void updateSearch() {
        SwingUtilities.invokeLater(() -> {
            DefaultListModel<String> model = (DefaultListModel<String>) listSearchedPresentations.getModel();
            String text = tFSearch.getText();
            model.clear();
            if (text.length() < 3 || cBClassroom.getSelectedIndex() == 0) return;
            // Name -> Id, subarea
            presentationssearched = cacheManager.searchPresentationWithText(text, cacheManager.getStage(cBClassroom.getSelectedIndex()));
            for (String name: presentationssearched.keySet()) {
                model.addElement(name);
            }
        });
    }

    public static void showMessage(String text) {
        JOptionPane.showMessageDialog(frame, text);
    }

    public static class Logger {
        private final DefaultListModel<String> model;

        public Logger(JList<String> list) {
            this.model = (DefaultListModel<String>) list.getModel();
        }

        public void d(String text){
            model.insertElementAt(text,0);
        }

        public void e(Exception e){
            model.insertElementAt("---ERROR---",0);
            model.insertElementAt(e.getMessage(),0);
        }
    }

    private class SWLoadImages extends SwingWorker {
        final File[] files;
        final ArrayList<BufferedImage> imgs;
        private final JLabel labelDate;


        public SWLoadImages(File[] files, ArrayList<BufferedImage> imgs, JLabel labelDate) {
            this.files = files;
            this.imgs = imgs;
            this.labelDate = labelDate;
        }

        @Override
        protected Object doInBackground() {
            panelImgs.removeAll();
            for (final File f : files) {
                try {
                    BufferedImage img;
                    img = ImageIO.read(f);
                    System.out.println(f.getName());
                    imgs.add(img);
                    addImageToForm(img, f);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        }

        private void addImageToForm(BufferedImage img, File f) {
            System.out.println("Loading: " + f.getAbsolutePath());
            ImageIcon icon = new ImageIcon(ImageUtils.resizeImage(img, 120, 120));
            System.out.println("Icon created");
            try {
                JLabel label = new JLabel(icon);
                System.out.println("Label created");
                label.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        System.out.println("Creating big image" + "");
                        labelDate.setText("");
                        mainOriginalImage = ImageUtils.resizeImage(img, img.getWidth(), img.getHeight());
                        setMainImage(f, img);
                        try {
                            Metadata metadata = ImageMetadataReader.readMetadata(f);
                            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                            if (directory != null) {
                                Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                                if (date == null) directory.getDate(ExifSubIFDDirectory.TAG_DATETIME);
                                if (date == null) directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
                                if (date != null) labelDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(date));
                                //else dateChooser.setEnabled(true);
                            }
                        } catch (ImageProcessingException | IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    @Override
                    public void mousePressed(MouseEvent e) { }
                    @Override
                    public void mouseReleased(MouseEvent e) { }
                    @Override
                    public void mouseEntered(MouseEvent e) { }
                    @Override
                    public void mouseExited(MouseEvent e) { }
                });
                panelImgs.add(label);
                System.out.println("Image added to panel");
                Component comp = Box.createRigidArea(new Dimension(0, 10));
                panelImgs.add(comp);
                panelImgs.updateUI();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
