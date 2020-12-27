package ui.formReports.managers;

import bd.BDManager;
import bd.MySet;
import bd.model.TableMedia;
import drive.GoogleDriveManager;
import ui.formReports.models.PresentationPage;
import utils.CacheManager;
import utils.MyLogger;
import utils.NextPresentation;
import utils.SettingsManager;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.Date;

public class MediaManager {
    private final static String TAG = MediaManager.class.getSimpleName();
    private final CacheManager cacheManager;
    private final SettingsManager settingsManager;
    private final BDManager bdManager;
    private final PDDocument doc;
    private final PDFont font;
    private final float margin;
    private Integer student;
    private final Date initialDate;
    private final Date finalDate;
    private final String  fileName;
    DefaultListModel log;

    public MediaManager(CacheManager cacheManager , SettingsManager settingsManager, BDManager bdManager, Integer student,
                        Date initialDate, Date finalDate, PDDocument doc, PDFont font, float margin, String fileName,
                        DefaultListModel log) {
        this.cacheManager = cacheManager;
        this.settingsManager = settingsManager;
        this.bdManager = bdManager;
        this.student = student;
        this.initialDate = initialDate;
        this.finalDate = finalDate;
        this.doc = doc;
        this.font = font;
        this.margin = margin;
        this.fileName = fileName;
        this.log = log;
    }

    public void execute() {
        SWLoadMedia swLoadMedia = new SWLoadMedia();
        swLoadMedia.execute();
    }

    class SWLoadMedia extends SwingWorker {
        private GoogleDriveManager driveManager;

        public SWLoadMedia() {
            try {
                driveManager = new GoogleDriveManager();
            } catch (IOException e) {
                MyLogger.e(TAG, e);
            } catch (GeneralSecurityException e) {
                MyLogger.e(TAG, e);
            }
        }
/*
CacheManager cacheManager , SettingsManager settingsManager, PDDocument doc, float margin,
                            BufferedImage picture, Date date, int student, int presentation,
                            Integer presentationSub, PDFont font
 */
        @Override
        protected Object doInBackground() throws Exception {
            Connection co = null;
            try {
                co = bdManager.connect();
                MySet set = bdManager.getValues(co, BDManager.tableMedia, getCondition());
                while (set.next()) {
                    Integer student = set.getInt(TableMedia.student);
                    java.sql.Date date = set.getDate(TableMedia.date);
                    Integer presentation = set.getInt(TableMedia.presentation);
                    Integer presentation_sub = set.getInt(TableMedia.presentation_sub);
                    String fileId = set.getString(TableMedia.fileId);
                    String comments = set.getString(TableMedia.comment);
                    BufferedImage image = driveManager.download(fileId);
                    String[] links = getLinks(presentation, presentation_sub);
                    NextPresentation nextPresentation = new NextPresentation(cacheManager, settingsManager, presentation, presentation_sub);
                    nextPresentation.setLinksText(getLinks(nextPresentation.nextPresentation, nextPresentation.nextPresentationSub));
                    if (image != null) {
                        PresentationPage page = new PresentationPage(cacheManager, settingsManager, doc, margin, image, date,
                                student, presentation, presentation_sub, font, comments, links[0], links[1],
                                nextPresentation);
                        doc.addPage(page);
                    }
                }
            } catch (Exception ex) {
                MyLogger.e(TAG, ex);
                log.insertElementAt(ex.getMessage(), 0);
            } finally {
                BDManager.closeQuietly(co);
                doc.save(fileName);
                log.insertElementAt("EoY report created: " + fileName, 0);
                doc.close();
            }
            return null;
        }

        private String getCondition() {
            String condition = TableMedia.student + " = " + student + " AND " + TableMedia.date + " >= '" + initialDate + "' AND " +
                    TableMedia.date + " <= '" + finalDate + "' ORDER BY " + TableMedia.date + " ASC;";
            return condition;
        }

        public String[] getLinks(Integer presentation, Integer presentation_sub) {
            String[] result = {"", ""};
            Integer lastSubarea = -1;
            String pair = presentation + "-" + (presentation_sub != -1 ? presentation_sub : 0);

            if (cacheManager.links.containsKey(pair)) {
                for (Integer outcome : cacheManager.links.get(pair).outcomes) {
                    if (outcome != null && outcome != 0) {
                        Object[] data = cacheManager.outcomes.get(outcome); //name,nombre,subarea,start_month,end_month;
                        result[0] = getTargetText(result[0], lastSubarea, data);
                        lastSubarea = (Integer) data[2];
                    }
                }

                for (Integer target : cacheManager.links.get(pair).targets) {
                    if (target != null && target != 0) {
                        Object[] data = cacheManager.targets.get(target); //name,nombre,subarea,start_month,end_month;
                        result[1] = getTargetText(result[1], lastSubarea, data);
                        lastSubarea = (Integer) data[2];
                    }
                }
            }

            return result;
        }

        private String getTargetText(String text, Integer lastSubarea, Object[] data) {
            Integer subarea = (Integer) data[2];
            Integer area = cacheManager.targetsubareaarea.get(subarea);
            if (subarea != lastSubarea) {
                text += cacheManager.areasTarget.get(area)[settingsManager.language] + " - ";
                text += cacheManager.subareasTarget.get(subarea)[settingsManager.language] + "\n";
            }
            text += data[settingsManager.language] + "\n";
            return text;
        }
    }
}

