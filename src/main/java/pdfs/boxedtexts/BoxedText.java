package pdfs.boxedtexts;

import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by angel on 27/05/17.
 */
public class BoxedText {
    public final float leading;
    public final float innerMargin;
    public final float lineWidth;
    public final ArrayList<String> lines;
    public final ArrayList<Integer> shortlines;

    public BoxedText(String text, int fontSize, float width, PDFont font) throws IOException {
        leading = 1.5f * fontSize;
        lines = new ArrayList();
        shortlines = new ArrayList();
        int lastSpace = -1;
        if (text == null) text = "";

        innerMargin = 4;
        lineWidth = width - innerMargin *2 - 8;
        while (text.length() > 0) {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex !=-1 && text.substring(0,spaceIndex).contains("\n")) spaceIndex = text.indexOf("\n")+1;
            if (spaceIndex < 0)
                spaceIndex = text.length();
            String subString = text.substring(0, spaceIndex);
            if (subString.contains("\n")) {
                subString = subString.replace("\n", "");
                float size = fontSize * font.getStringWidth(subString) / 1000;
                if (size > lineWidth) {
                    if (lastSpace < 0) lastSpace = spaceIndex;
                    subString = text.substring(0, lastSpace);
                    lines.add(subString);
                    text = text.substring(lastSpace).trim();//+ "\n";
                    lastSpace = -1;
                } else {
                    lastSpace = spaceIndex-1;
                    subString = text.substring(0, lastSpace);
                    lines.add(subString);
                    shortlines.add(lines.indexOf(subString));
                    text = text.substring(lastSpace).trim();
                    lastSpace = -1;
                }
            } else {
                float size = fontSize * font.getStringWidth(subString) / 1000;
                if (size > lineWidth) {
                    if (lastSpace < 0) lastSpace = spaceIndex;
                    subString = text.substring(0, lastSpace);
                    lines.add(subString);
                    text = text.substring(lastSpace).trim();
                    lastSpace = -1;
                } else if (spaceIndex == text.length()) {
                    lines.add(text);
                    shortlines.add(lines.indexOf(subString));
                    text = "";
                } else {
                    lastSpace = spaceIndex;
                }
            }
        }
    }

    public float getHeight() {
        int num = (lines.size() != 0) ? lines.size() : 3;
        return (num + 1) * leading;
    }
}
