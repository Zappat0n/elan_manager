package utils;

import utils.image.ImageTransform;

import java.awt.*;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class ImageUtils {
    public static BufferedImage resizeImage(BufferedImage img, int destWidth, int destHeight) {
        int width = img.getWidth();
        int height = img.getHeight();
        double imgRatio = (double) width / height;

        if (height > destHeight) {
            height = destHeight;
            width = (int)Math.round(height * imgRatio);
        } else if (width > destWidth) {
            width = destWidth;
            height = (int) Math.round(width / imgRatio);
        }

        //Image tmp = img.getScaledInstance(height * img.getWidth() / img.getHeight(), height, Image.SCALE_SMOOTH);
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }

    public static BufferedImage rotateImage(BufferedImage img, double grades) {
        BufferedImage destinationImage;
        ImageTransform imTransform = new ImageTransform(img.getHeight(), img.getWidth());
        imTransform.rotate(grades);
        imTransform.findTranslation();
        AffineTransformOp ato = new AffineTransformOp(imTransform.getTransform(), AffineTransformOp.TYPE_BILINEAR);
        destinationImage = ato.createCompatibleDestImage(img, img.getColorModel());
        BufferedImage imageAfterRotation = ato.filter(img, destinationImage);
        BufferedImage imageConvertedToJpg = new BufferedImage(destinationImage.getWidth(), destinationImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        imageConvertedToJpg.createGraphics().drawImage(imageAfterRotation, 0, 0, Color.WHITE, null);
        return imageConvertedToJpg;
    }

    public static BufferedImage convertImageToJpg(BufferedImage img){
        BufferedImage newBufferedImage = new BufferedImage(img.getWidth(),
                img.getHeight(), BufferedImage.TYPE_INT_RGB);
        newBufferedImage.createGraphics().drawImage(img, 0, 0, Color.WHITE, null);
        return newBufferedImage;
    }

}
