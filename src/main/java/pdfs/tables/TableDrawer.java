package pdfs.tables;

import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.*;
import java.io.IOException;

public class TableDrawer {

    private final float tableStartX;
    private final float tableStartY;
    private final PDPageContentStream contentStream;
    private final Table table;

    public TableDrawer(final PDPageContentStream contentStream, final Table table, final float startX, final float startY) {
        this.contentStream = contentStream;
        this.table = table;
        tableStartX = startX;
        tableStartY = startY - table.getFontHeight();
    }

    public void draw() throws IOException {
        drawBackgroundAndText();
        drawBorders();
    }

    private void drawBackgroundAndText() throws IOException {
        float startX;
        float startY = tableStartY;

        for (Row row : table.getRows()) {
            int lines = 0;
            Color color = row.getCells().get(0).getBackgroundColor();
            for (int i = 0; i < row.getCells().size(); i++) {
                StringBuilder whole_text = new StringBuilder();
                String rawText = row.getCells().get(i).getText();
                boolean addColum = (rawText.contains("<ADD COLUMN>"));
                if (addColum) rawText = rawText.replace("<ADD COLUMN>", "");
                for (String text_line : rawText.split("\n")) {
                    StringBuilder text = new StringBuilder(text_line);
                    final float cellwidth = table.getFontSize() * table.getFont().getStringWidth(text.toString()) / 1000;
                    final float colwidth  = table.getColumns().get(i).getWidth() +
                            ((addColum && table.getColumns().size() > i+1) ?  table.getColumns().get(i+1).getWidth() : 0)
                            - row.getCells().get(i).getPaddingLeft()*2;
                    if (cellwidth >= colwidth) {
                        float linewidth;
                        int pos = text.indexOf(" ");
                        int oldpos = 0;
                        String sub = text.substring(0, pos);
                        linewidth = table.getFontSize() * table.getFont().getStringWidth(sub) / 1000;
                        String oldtext = text.toString();
                        text = new StringBuilder();
                        String line;
                        while (!oldtext.equals("")) {
                            while (linewidth < colwidth) {
                                if (sub.equals(oldtext)) break;
                                oldpos = pos;
                                pos = oldtext.indexOf(" ", pos+1);
                                sub = (pos!=-1) ? oldtext.substring(0, pos) : oldtext;
                                linewidth = table.getFontSize() * table.getFont().getStringWidth(sub) / 1000;
                            }
                            if (pos!= -1) {
                                line = oldtext.substring(0, oldpos);
                            } else {
                                if (linewidth < colwidth) {
                                    line = oldtext;
                                } else {
                                    line = oldtext.substring(0, oldpos);
                                    text.append(line).append("\n");
                                    oldtext = oldtext.replace(line, "");
                                    oldtext = oldtext.trim();
                                    text.append(oldtext);
                                    break;
                                }
                            }
                            text.append(line).append("\n");
                            oldtext = oldtext.replace(line, "");
                            oldtext = oldtext.trim();
                            pos = 0;
                            linewidth = 0;
                        }
                    }
                    whole_text.append(text.toString()).append("\n");
                }
                if (whole_text.substring(whole_text.length()-1, whole_text.length()).equals("\n"))
                    whole_text.deleteCharAt(whole_text.length()-1);
                row.getCells().get(i).setBackgroundColor(color).setText(whole_text.toString());
                lines = Math.max(whole_text.toString().split("\n").length, lines);
            }

            int columnCounter = 0;
            float rowHeight = table.getFontHeight() + row.getHeightWithoutFontHeight(); //row.getCells().get(0).getHeight();
            //float rowHeight = table.getFontHeight() * lines + row.getHeightWithoutFontHeight();

            row.setLines(lines);
            startX = tableStartX;
            startY -= rowHeight;
            for (Cell cell : row.getCells()) {
                final float columnWidth = table.getColumns().get(columnCounter).getWidth();
                // Handle the cell's background color
                if (cell.hasBackgroundColor()) {
                    drawCellBackground(cell, startX, startY, columnWidth, rowHeight);
                }

                // Handle the cell's text
                if (cell.hasText()) {
                    drawCellText(cell, columnWidth, startX, startY);
                }

                startX += columnWidth;
                columnCounter++;
            }
            startY -= table.getFontHeight() * (row.getLines() - 1);
        }
    }

    private void drawBorders() throws IOException {
        float startX;
        float startY = tableStartY;

        for (Row row : table.getRows()) {
            final float rowHeight = table.getFontHeight() * row.getLines() + row.getHeightWithoutFontHeight();
            int columnCounter = 0;

            startX = tableStartX;
            startY -= rowHeight;

            for (Cell cell : row.getCells()) {
                final float columnWidth = table.getColumns().get(columnCounter).getWidth();

                // Handle the cell's borders
                if (cell.hasBorderTop()) {
                    float borderWidth = cell.getBorderWidthTop();
                    float correctionLeft = cell.hasBorderLeft() ? cell.getBorderWidthLeft() / 2 : 0;
                    float correctionRight = cell.hasBorderRight() ? cell.getBorderWidthRight() / 2 : 0;
                    contentStream.setLineWidth(borderWidth);
                    contentStream.moveTo(startX - correctionLeft, startY + rowHeight);
                    contentStream.lineTo(startX + columnWidth + correctionRight, startY + rowHeight);
                    contentStream.setStrokingColor(cell.getBorderColor());
                    contentStream.stroke();
                    contentStream.setStrokingColor(cell.getParentBorderColor());
                }

                if (cell.hasBorderBottom()) {
                    float borderWidth = cell.getBorderWidthBottom();
                    float correctionLeft = cell.hasBorderLeft() ? cell.getBorderWidthLeft() / 2 : 0;
                    float correctionRight = cell.hasBorderRight() ? cell.getBorderWidthRight() / 2 : 0;
                    contentStream.setLineWidth(borderWidth);
                    contentStream.moveTo(startX - correctionLeft, startY);
                    contentStream.lineTo(startX + columnWidth + correctionRight, startY);
                    contentStream.setStrokingColor(cell.getBorderColor());
                    contentStream.stroke();
                    contentStream.setStrokingColor(cell.getParentBorderColor());

                }

                if (cell.hasBorderLeft()) {
                    float borderWidth = cell.getBorderWidthLeft();
                    float correctionTop = cell.hasBorderTop() ? cell.getBorderWidthTop() / 2 : 0;
                    float correctionBottom = cell.hasBorderBottom() ? cell.getBorderWidthBottom() / 2 : 0;
                    contentStream.setLineWidth(borderWidth);
                    contentStream.moveTo(startX, startY - correctionBottom);
                    contentStream.lineTo(startX, startY + rowHeight + correctionTop);
                    contentStream.setStrokingColor(cell.getBorderColor());
                    contentStream.stroke();
                    contentStream.setStrokingColor(cell.getParentBorderColor());
                }

                if (cell.hasBorderRight()) {
                    float borderWidth = cell.getBorderWidthRight();
                    float correctionTop = cell.hasBorderTop() ? cell.getBorderWidthTop() / 2 : 0;
                    float correctionBottom = cell.hasBorderBottom() ? cell.getBorderWidthBottom() / 2 : 0;
                    contentStream.setLineWidth(borderWidth);
                    contentStream.moveTo(startX + columnWidth, startY - correctionBottom);
                    contentStream.lineTo(startX + columnWidth, startY + rowHeight + correctionTop);
                    contentStream.setStrokingColor(cell.getBorderColor());
                    contentStream.stroke();
                    contentStream.setStrokingColor(cell.getParentBorderColor());
                }

                startX += columnWidth;
                columnCounter++;
            }
        }
    }

    private void drawCellBackground(final Cell cell, final float startX, final float startY, final float width, final float height)
            throws IOException {
        contentStream.setNonStrokingColor(cell.getBackgroundColor());

        contentStream.addRect(startX, startY, width, height);
        contentStream.fill();
        contentStream.closePath();

        // Reset NonStroking Color to default value
        contentStream.setNonStrokingColor(Color.BLACK);
    }

    private void drawCellText(final Cell cell, final float columnWidth, final float moveX, final float moveY) throws IOException {
        contentStream.beginText();
        contentStream.setNonStrokingColor(cell.getTextColor());
        contentStream.setFont(table.getFont(), table.getFontSize());

        float xOffset = moveX + cell.getPaddingLeft();
        float yOffset = moveY + cell.getPaddingBottom();
        final String[] lines = cell.getText().split("\n");
        Boolean firstLine = true;

        for ( String line : lines) {
            final float textWidth = (table.getFont().getStringWidth(line) / 1000f) * table.getFontSize();
            switch (cell.getHorizontalAlignment()){
                case RIGHT:
                    xOffset = moveX + (columnWidth - (textWidth + cell.getPaddingRight()));
                    break;
                case CENTER:
                    final float diff = (columnWidth - textWidth) / 2;
                    xOffset = moveX + diff;
                    break;
            }
            if (firstLine) {
                contentStream.newLineAtOffset(xOffset, yOffset);
                firstLine = false;
            } else contentStream.newLineAtOffset(0, -(table.getFontHeight()));// + cell.getHeightWithoutFontSize()));
            contentStream.showText(line);
            //yOffset -= 20;//(table.getFontHeight() + cell.getHeightWithoutFontSize());table.getRows().get(0).
        }
        contentStream.endText();
    }

}
