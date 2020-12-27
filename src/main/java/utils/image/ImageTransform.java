package utils.image;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class ImageTransform {

    private final AffineTransform at;
    private final int alturaImagen;
    private final int anchoImagen;
    private double grados;

    public ImageTransform(int alturaImagen, int anchuraImagen) {
        at = new AffineTransform();
        this.alturaImagen = alturaImagen;
        this.anchoImagen = anchuraImagen;
    }

    public void rotate(double grados) {
        this.grados = grados;
        at.rotate(Math.toRadians(grados), anchoImagen / 2.0, alturaImagen / 2.0);
    }

    public AffineTransform getTransform() {
        return at;
    }

    public void findTranslation() {
        Point2D p2din, p2dout;
        p2din = hallarPtoATraslacion();
        p2dout = at.transform(p2din, null);
        double ytrans = p2dout.getY();

        p2din = hallarPtoBTraslacion();
        p2dout = at.transform(p2din, null);
        double xtrans = p2dout.getX();
        AffineTransform tat = new AffineTransform();

        tat.translate(-xtrans, -ytrans);
        at.preConcatenate(tat);
    }

    private Point2D hallarPtoATraslacion() {
        Point2D p2din;
        if (grados >= 0 && grados <= 90) {
            p2din = new Point2D.Double(0.0, 0.0);
        } else if (grados > 90 && grados <= 180) {
            p2din = new Point2D.Double(0.0, alturaImagen);
        } else if (grados > 180 && grados <= 270) {
            p2din = new Point2D.Double(anchoImagen, alturaImagen);
        } else {
            p2din = new Point2D.Double(anchoImagen, 0.0);
        }
        return p2din;
    }

    private Point2D hallarPtoBTraslacion() {
        Point2D p2din;
        if (grados >= 0 && grados <= 90) {
            p2din = new Point2D.Double(0.0, alturaImagen);
        } else if (grados > 90 && grados <= 180) {
            p2din = new Point2D.Double(anchoImagen, alturaImagen);
        } else if (grados > 180 && grados <= 270) {
            p2din = new Point2D.Double(anchoImagen, 0.0);
        } else {
            p2din = new Point2D.Double(0.0, 0.0);
        }
        return p2din;
    }

}
