package utils.image;

import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Juan Díez-Yanguas Barber
 * @author Jdiezfoto - http://jdiezfoto.es/
 */
public class ImageRotate {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Cargando Imagen ...");
        BufferedImage img = ImageRotate.loadJPGImage();

        System.out.println("Rotando Imagen ...");
        BufferedImage dst = ImageRotate.rotacionImagen(img, 25);

        System.out.println("Guardando Imagen ...");
        ImageRotate.saveJPGImage(dst);

        System.out.println("Rotacion Completada.");
    }

    public static BufferedImage rotacionImagen(BufferedImage origen, double grados) {
        BufferedImage destinationImage;
        ImageTransform imTransform = new ImageTransform(origen.getHeight(), origen.getWidth());
        imTransform.rotate(grados);
        imTransform.findTranslation();
        AffineTransformOp ato = new AffineTransformOp(imTransform.getTransform(), AffineTransformOp.TYPE_BILINEAR);
        destinationImage = ato.createCompatibleDestImage(origen, origen.getColorModel());
        return ato.filter(origen, destinationImage);
    }

    private static BufferedImage loadJPGImage() throws IOException {
        BufferedImage imagen = ImageIO.read(new File("arrow.jpg"));

        BufferedImage source = new BufferedImage(imagen.getWidth(),
                imagen.getHeight(), BufferedImage.TYPE_INT_RGB);
        source.getGraphics().drawImage(imagen, 0, 0, null);
        return source;
    }

    private static void saveJPGImage(BufferedImage im) throws IOException {
        ImageIO.write(im, "JPG", new File("IMG_Rotada.jpg"));
    }
}
