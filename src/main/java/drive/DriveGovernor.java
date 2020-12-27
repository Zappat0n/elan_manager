package drive;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import bd.BDManager;
import ui.formUpload.UploadForm;
import utils.CacheManager;
import utils.MyLogger;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DriveGovernor {
    private static final String TAG = DriveGovernor.class.getSimpleName();
    public static final String STUDENTS_FOLDER_DOCUMENTS    = "Documents";
    public static final String STUDENTS_FOLDER_PHOTOS       = "Photos";
    public static final String STUDENTS_FOLDER_REPORTS      = "Reports";
    public static final String STUDENTS_SHARED_FOLDER= "0AMt46GfJ6PSTUk9PVA";
    private static final String SNAILS_FOLDER_ID     = "0AL9BmckdklKAUk9PVA";
    private static final String ROOTS_FOLDER_ID      = "0AFY8Tf_T1bcWUk9PVA";
    private static final String SEEDS_FOLDER_ID      = "0AFZmquVv7ReUUk9PVA";
    private static final String VOLCANOES_FOLDER_ID  = "0AC3PKp7z-C4cUk9PVA";
    private static final String BUTTON_RED = "Button-Blank-Red-icon.png";
    private static final String BUTTON_YELLOW = "Button-Blank-Yellow-icon.png";
    private static final String BUTTON_GREEN = "Signal-Receiver-icon.png";//"Button-Next-icon.png";

    public GoogleDriveManager manager;
    final JButton buttonUpload;
    final BDManager bdManager;
    final CacheManager cacheManager;

    public DriveGovernor(BDManager bdManager, CacheManager cacheManager, JButton buttonUpload) {
        this.buttonUpload = buttonUpload;
        this.bdManager = bdManager;
        this.cacheManager = cacheManager;
        try {
            if (buttonUpload != null)
                buttonUpload.setIcon(new ImageIcon(DriveGovernor.class.getResource(BUTTON_YELLOW)));
            manager = new GoogleDriveManager();
        } catch (IOException | GeneralSecurityException e) {
            if (buttonUpload != null)
                buttonUpload.setIcon(new ImageIcon(DriveGovernor.class.getResource(BUTTON_RED)));
            UploadForm.logger.e(e);
            MyLogger.e(TAG, e);
            return;
        }
        if (buttonUpload != null)
            buttonUpload.setIcon(new ImageIcon(DriveGovernor.class.getResource(BUTTON_GREEN)));
    }

    public String createFolder(String parentId, String name) {
        try {
            return manager.createFolder(parentId, name);
        } catch (IOException e) {
            UploadForm.logger.e(e);
            MyLogger.e(TAG, e);
            return null;
        }
    }

    public String createStudentsFolders(String parentFolder, String folderType) {
        String folderDocuments = createFolder(parentFolder, STUDENTS_FOLDER_DOCUMENTS);
        String folderPhotos = createFolder(parentFolder, STUDENTS_FOLDER_PHOTOS);
        String folderReports = createFolder(parentFolder, STUDENTS_FOLDER_REPORTS);
        return switch (folderType) {
            case STUDENTS_FOLDER_DOCUMENTS -> folderDocuments;
            case STUDENTS_FOLDER_PHOTOS -> folderPhotos;
            case STUDENTS_FOLDER_REPORTS -> folderReports;
            default -> null;
        };
    }

    private String getStudentFolder(Integer student) throws IOException {
        FileList folders = manager.getDriveContent(STUDENTS_SHARED_FOLDER);
        for (File f: folders.getFiles()) {
            if (f.getName().contains(String.format("%04d", student))) return f.getId();
        }
        return createFolder(STUDENTS_SHARED_FOLDER, String.format("%04d", student) + " " + cacheManager.students.get(student)[0]);
    }

    public String getStudentFolder(String folderTpye, Integer student) {
        String folderId = null;
            try {
                String studentFolder = getStudentFolder(student);
                FileList folders = manager.getFolderContent(studentFolder,folderTpye);

                if (folders.getFiles().size() > 0)
                    folderId = folders.getFiles().get(0).getId();
                else
                    folderId = createStudentsFolders(studentFolder, folderTpye);
            } catch (Exception e) {
                MyLogger.e(TAG, e);
            }
        return folderId;
    }

    public String uploadFile(java.io.File file, String folderId) throws IOException {
        return manager.uploadFile(file, folderId, file.getName());
    }

    public String uploadPicture(Integer student, Date date, int presentation, Integer presentationSub,
                                BufferedImage image, String comments) {
        String folderId = (String) cacheManager.students.get(student)[4];
        String name = new SimpleDateFormat("yyyyMMdd").format(date) + "-" +presentation +
                ((presentationSub==-1)? "":"-" + presentationSub);
        String fileId;
        try {
            fileId = manager.uploadMediaFile(folderId, date, name, image);
            bdManager.addMedia(new java.sql.Date(date.getTime()),student, presentation, presentationSub, comments,  fileId);
            return fileId;
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        }
        return null;
    }

    public void uploadReport(Integer student, java.io.File file) {
        //name, birthday, drive_main, drive_documents, drive_photos, drive_reports
        String folderId = (String) cacheManager.students.get(student)[5];
        try {
            uploadFile(file, folderId);
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        }
    }

    public String uploadDocument(Integer student, java.io.File file) {
        //name, birthday, drive_main, drive_documents, drive_photos, drive_reports
        String folderId = (String) cacheManager.students.get(student)[3];
        try {
            return uploadFile(file, folderId);
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        }
        return null;
    }

    public void uploadPicture(int student, Date date, int presentation, Integer presentationSub,
                              java.io.File file, String comments) {
        String folderId = (String) cacheManager.students.get(student)[4];
        String name;

        try {
            name = checkNameForFile(folderId, new SimpleDateFormat("yyyyMMdd").format(date) + "-" +presentation +
                    ((presentationSub==-1)? "":"-" + presentationSub));
            String fileId = manager.uploadMediaFile(file, folderId, name);
            bdManager.addMedia(new java.sql.Date(date.getTime()),student, presentation, presentationSub, comments,  fileId);
            UploadForm.logger.d("^OK: " + file.getName());
        } catch (IOException e) {
            UploadForm.logger.e(e);
            MyLogger.e(TAG,e);
        }
    }

    private String checkNameForFile(String folderId, String name) throws IOException {
        FileList list = manager.getFolderContent(folderId, name);
        if (list.getFiles().size() == 0) return name;
        else return name + "_" + list.getFiles().size();
    }
}
