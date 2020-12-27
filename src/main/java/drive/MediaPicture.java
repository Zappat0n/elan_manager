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
    public Integer presentationSub = null;
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
        String year = _date.substring(0, 3);
        String month = _date.substring(4, 5);
        String day = _date.substring(6, 7);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.valueOf(year), Integer.valueOf(month)-1, Integer.valueOf(day));
        return new Date(calendar.getTimeInMillis());
    }

    public String getName() {
        return date.toString().replace("-","") + "-" + presentation + (presentationSub != -1 ? "-"+presentationSub : "");
    }
}
