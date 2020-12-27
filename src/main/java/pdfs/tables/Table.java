package pdfs.tables;

import utils.MyLogger;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Table {

    private List<Row> rows = new LinkedList<>();
    private List<Column> columns = new LinkedList<>();
    private PDFont font = PDType1Font.HELVETICA;

    private int fontSize;
    private int numberOfColumns = 0;
    private float width = 0;
    private float borderWidth = 0.2f;
    private Color borderColor = Color.BLACK;

    private Table(final List<Row> rows, final List<Column> columns) {
        this.rows = rows;
        this.columns = columns;
    }

    private void setFont(final PDFont font) {
        this.font = font;
    }

    private void setFontSize(final int _fontTitleSize, final int _fontSize) {
        fontSize = _fontSize;
    }

    private void setNumberOfColumns(final int numberOfColumns) {
        this.numberOfColumns = numberOfColumns;
    }

    private void setWidth(final float width) {
        this.width = width;
    }

    private void setBorderWidth(final float borderWidth) {
        this.borderWidth = borderWidth;
    }

    public float getWidth() {
        return width;
    }

    public PDFont getFont() {
        return font;
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public int getFontSize() {
        return fontSize;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<Row> getRows() {
        return rows;
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public float getFontHeight() {
        return font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
    }

    public float getHeight() {
        float height = 0;
        for (final Row row : rows) {
            double lines = row.getLines();
            if (lines == 1) {
                try {
                    for(int i = 0; i < row.getCells().size(); i++) {
                        for (String line : row.getCells().get(i).getText().split("\n")) {
                            float linewidth = row.getTable().getFontSize() *
                                    row.getTable().getFont().getStringWidth(line) / 1000;
                            float colwidth = row.getTable().getColumns().get(i).getWidth();
                            lines = Math.max(Math.floor(linewidth/colwidth) + 1, lines);
                        }
                    }
                } catch (IOException e) {
                    MyLogger.e("TableBuilder", e);
                    lines = 1;
                }
            }
            height += (row.getHeightWithoutFontHeight() + this.getFontHeight()) * lines;
        }
        return height;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    private void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }


    public static class TableBuilder {
        private final List<Row> rows = new LinkedList<>();
        private final List<Column> columns = new LinkedList<>();
        private int numberOfColumns = 0;
        private float width = 0;
        private final Table table = new Table(rows, columns);

        public void addRow(Row row) {
            if (row.getCells().size() != numberOfColumns) {
                throw new IllegalArgumentException(
                        "Number withText row cells does not match with number withText table columns");
            }
            row.setTable(table);
            rows.add(row);
        }

        public void addColumnOfWidth(int width) {
            this.addColumn(new Column(width));
        }

        public void addColumn(final Column column) {
            numberOfColumns++;
            columns.add(column);
            width += column.getWidth();
        }

        public void setFont(final PDFont font) {
            table.setFont(font);
        }

        public void setFontSize(final int fontTitleSize, final int fontSize) {
            table.setFontSize(fontTitleSize, fontSize);
        }

        public TableBuilder setBorderWidth(final float borderWidth) {
            table.setBorderWidth(borderWidth);
            return this;
        }

        public TableBuilder setBorderColor(final Color color) {
            table.setBorderColor(color);
            return this;
        }

        public Table build() {
            table.setWidth(width);
            table.setNumberOfColumns(numberOfColumns);
            return table;
        }
    }
}