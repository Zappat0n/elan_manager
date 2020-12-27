package pdfs.tables;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Row {

    private Table table;
    private final List<Cell> cells;
    private Color borderColor;
    private int lines = 1;

    private Row(final List<Cell> cells) {
        super();
        this.cells = cells;
        for (final Cell cell : cells) {
            cell.setRow(this);
        }
    }

    public Table getTable() {
        return table;
    }

    void setTable(final Table table) {
        this.table = table;
    }

    public List<Cell> getCells() {
        return this.cells;
    }

    float getHeightWithoutFontHeight() {
        final Optional<Cell> highestCell = cells
                .stream()
                .max((cell1, cell2) -> Float.compare(cell1.getHeightWithoutFontSize(), cell2.getHeightWithoutFontSize()));
        return highestCell.orElseThrow(IllegalStateException::new).getHeightWithoutFontSize();
    }

    float getHeight(){
        Cell cell = null;
        float height = 0;
        for (Cell c : getCells()) {
            height = Math.max(c.getHeight(), height);
        }
        return height;
    }

    public Color getBorderColor() {
        Optional<Color> optBorderColor = Optional.ofNullable(borderColor);
        return optBorderColor.orElse(getTable().getBorderColor());
    }

    private void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public int getLines() {
        return lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    public static class RowBuilder {
        private final List<Cell> cells = new LinkedList<>();
        private Optional<Color> backgroundColor = Optional.empty();
        private Optional<Color> borderColor = Optional.empty();

        public void add(final Cell cell) {
            cells.add(cell);
        }

        public RowBuilder setBackgroundColor(Color backgroundColor) {
            this.backgroundColor = Optional.ofNullable(backgroundColor);
            return this;
        }

        public RowBuilder setBorderColor(Color borderColor) {
            this.borderColor = Optional.of(borderColor);
            return this;
        }

        public Row build() {
            cells.forEach(cell -> {
                if (!cell.hasBackgroundColor()) {
                    backgroundColor.ifPresent(cell::setBackgroundColor);
                }
            });
            Row row = new Row(cells);
            borderColor.ifPresent(row::setBorderColor);
            return row;
        }
    }

}

