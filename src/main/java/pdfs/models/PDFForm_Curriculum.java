package pdfs.models;

import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import utils.data.RawData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class PDFForm_Curriculum {
    private static final String TAG = PDFForm_Curriculum.class.getSimpleName();
    protected static final String[] STAGE_NAME = {"EARLY YEARS", "CHILDREN'S HOUSE", "PRIMARY"};
    protected final int SIZE_TITLE = 18;
    protected final int SIZE_AREA = 10;
    protected final int SIZE_SUBAREA = 8;
    protected final int SIZE_PRESENTATION = 6;
    protected final int SIZE_PRESENTATION_SUB = 6;



    private final CacheManager cacheManager;
    private final double firstYear;
    private final double lastYear;
    protected final SortedMap<Integer, LinkedHashMap<Integer, ArrayList<Integer>>> data;

    private final PDDocument doc;
    protected final int line_space = 8;
    protected final float margin = 30;
    protected PDPage page;
    private PDFont font = null;
    int position;

    public PDFForm_Curriculum(SettingsManager settingsManager, CacheManager cacheManager, int stage){
        this.cacheManager = cacheManager;
        firstYear = RawData.yearsmontessori[stage][0];
        lastYear = RawData.yearsmontessori[stage][1];
        data = new TreeMap<>();
        doc = new PDDocument();
        try {
            font = PDType0Font.load(doc, getClass().getResourceAsStream("Verdana.ttf"));
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        }
        initialize();
    }

    private void initialize() {
        for (double year : cacheManager.presentationsPerYearAndSubarea.keySet()) {
            if (year >= firstYear && year < lastYear) {
                LinkedHashMap<Integer, ArrayList<Integer>> map = cacheManager.presentationsPerYearAndSubarea.get(year);
                for (Integer subarea : map.keySet()) {
                    int area = getAreaForSubarea(subarea);
                    if (area == -1) MyLogger.d(TAG, "DATA ERROR!!!!!");
                    LinkedHashMap<Integer, ArrayList<Integer>> data_area = data.get(area);
                    if (data_area == null) data_area = new LinkedHashMap<>();
                    data_area.put(subarea, map.get(subarea));
                    data.put(area, data_area);
                }
            }
        }
    }

    private int getAreaForSubarea (int subarea) {
        for (int area : cacheManager.subareasMontessoriPerArea.keySet()) {
            for (int sub : cacheManager.subareasMontessoriPerArea.get(area)) {
                if (sub==subarea) return area;
            }
        }
        return -1;
    }

    protected void nextPage() {
        page = createPage();
        doc.addPage(page);
        position = 800;
    }

    PDPage createPage() {
        PDPage page = new PDPage(PDRectangle.A4);
        page.setRotation(0);
        return page;
    }

    protected Integer addLine(String message, int x, int y, int fontSize, Boolean bold) throws IOException {
        if (position - fontSize - line_space < 30) {
            nextPage();
            y = 800;
        }
        PDPageContentStream contents = new PDPageContentStream(doc, page,
                PDPageContentStream.AppendMode.APPEND, true);
        contents.beginText();
        contents.setFont(!bold ? font : PDType1Font.HELVETICA_BOLD, fontSize);
        contents.setStrokingColor(Color.black);
        contents.setNonStrokingColor(Color.black);
        contents.newLineAtOffset(x, y);
        contents.showText(message);
        contents.endText();
        contents.close();
        return y - fontSize - line_space;
    }

    protected void saveFile(String fileName){
        try {
            doc.save(fileName);
            MyLogger.d(TAG, "Pdf saved: " + fileName);
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        } finally {
            try {
                doc.close();
            } catch (IOException e) {
                MyLogger.e(TAG, e);
            }
        }

    }
}
