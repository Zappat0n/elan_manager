package pdfs;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;

import java.awt.geom.Point2D;

public class MyPDFGraphicsStreamEngine extends PDFGraphicsStreamEngine {
    public MyPDFGraphicsStreamEngine(PDPage page) {
        super(page);
    }

    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {

    }

    @Override
    public void drawImage(PDImage pdImage) {

    }

    @Override
    public void clip(int windingRule) {

    }

    @Override
    public void moveTo(float x, float y) {

    }

    @Override
    public void lineTo(float x, float y) {

    }

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {

    }

    @Override
    public Point2D getCurrentPoint() {
        return null;
    }

    @Override
    public void closePath() {

    }

    @Override
    public void endPath() {

    }

    @Override
    public void strokePath() {

    }

    @Override
    public void fillPath(int windingRule) {

    }

    @Override
    public void fillAndStrokePath(int windingRule) {

    }

    @Override
    public void shadingFill(COSName shadingName) {

    }
}
