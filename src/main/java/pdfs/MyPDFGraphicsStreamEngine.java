package pdfs;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;

import java.awt.geom.Point2D;
import java.io.IOException;

public class MyPDFGraphicsStreamEngine extends PDFGraphicsStreamEngine {
    /**
     * Constructor.
     *
     * @param page
     */
    public MyPDFGraphicsStreamEngine(PDPage page) {
        super(page);
    }

    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {

    }

    @Override
    public void drawImage(PDImage pdImage) throws IOException {

    }

    @Override
    public void clip(int windingRule) throws IOException {

    }

    @Override
    public void moveTo(float x, float y) throws IOException {

    }

    @Override
    public void lineTo(float x, float y) throws IOException {

    }

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {

    }

    @Override
    public Point2D getCurrentPoint() throws IOException {
        return null;
    }

    @Override
    public void closePath() throws IOException {

    }

    @Override
    public void endPath() throws IOException {

    }

    @Override
    public void strokePath() throws IOException {

    }

    @Override
    public void fillPath(int windingRule) throws IOException {

    }

    @Override
    public void fillAndStrokePath(int windingRule) throws IOException {

    }

    @Override
    public void shadingFill(COSName shadingName) throws IOException {

    }
}
