package drive;

import java.awt.image.BufferedImage;
import java.sql.Date;
import java.util.Calendar;

public class MediaPicture {
    public BufferedImage image;
    public final Integer id;
    public final String fileId;
    public final Integer student;
    public Date date;
    public final Integer presentation;
    public final Integer presentationSub;
    public String comments;

    public MediaPicture(BufferedImage image, Integer id, String fileId, Integer student, Date date, Integer presentation,
                        Integer presentationSub, String comments) {
        this.image = image;
        this.id = id;
        this.fileId = fileId;
        this.student = student;
        this.date = date;
        this.presentation = presentation;
        this.presentationSub = presentationSub;
        this.comments = comments;
    }

    private Date calculateDate(String _date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(_date.substring(0, 3)));
        calendar.set(Calendar.MONTH, Integer.parseInt(_date.substring(4, 5))-1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(_date.substring(6, 7)));
        return new Date(calendar.getTimeInMillis());
    }

    public String getName() {
        return date.toString().replace("-","") + "-" + presentation + (presentationSub != -1 ? "-"+presentationSub : "");
    }
}
