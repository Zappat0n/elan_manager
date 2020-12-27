package pdfs.tables;

import java.awt.*;
import java.util.Optional;

@SuppressWarnings("ALL")
public class Cell {

    public enum HorizontalAlignment {
        LEFT, CENTER, RIGHT
    }

    private Row row;

    private HorizontalAlignment alignment = HorizontalAlignment.LEFT;
    private String text;
    private Color backgroundColor;
    private Color textColor = Color.BLACK;

    private float paddingLeft = 4;
    private float paddingRight = 4;
    private float paddingTop = 2;
    private float paddingBottom = 4;

    private float borderWidthTop = 0;
    private float borderWidthLeft = 0;
    private float borderWidthRight = 0;
    private float borderWidthBottom = 0;

    private Color borderColor;

    private Cell(Object object) {
        this.text = String.valueOf(object);
    }

    public void setText(Object object) { this.text = String.valueOf(object);}

    public static Cell withText(Object object) {
        return new Cell(object);
    }

    public Cell withAllBorders() {
        int borderWith = 1;
        return withAllBorders(borderWith);
    }

    private Cell withAllBorders(int borderWith) {
        return this
                .setBorderWidthBottom(borderWith)
                .setBorderWidthLeft(borderWith)
                .setBorderWidthRight(borderWith)
                .setBorderWidthTop(borderWith);
    }

    public Cell withAllBordersButRight(int borderWith) {
        return this
                .setBorderWidthBottom(borderWith)
                .setBorderWidthLeft(borderWith)
                .setBorderWidthTop(borderWith);
    }

    public Cell withAllBordersButLeft(int borderWith) {
        return this
                .setBorderWidthBottom(borderWith)
                .setBorderWidthRight(borderWith)
                .setBorderWidthTop(borderWith);
    }

    private Row getRow() {
        return row;
    }

    public void setRow(final Row row) {
        this.row = row;
    }

    public String getText() {
        return text;
    }

    public boolean hasBackgroundColor() {
        return backgroundColor != null;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Cell setBackgroundColor(final Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public boolean hasText() {
        return text != null;
    }

    public float getPaddingLeft() {
        return paddingLeft;
    }

    public Cell setPaddingLeft(final float paddingLeft) {
        this.paddingLeft = paddingLeft;
        return this;
    }

    public float getPaddingRight() {
        return paddingRight;
    }

    public Cell setPaddingRight(final float paddingRight) {
        this.paddingRight = paddingRight;
        return this;
    }

    public float getPaddingTop() {
        return paddingTop;
    }

    public Cell setPaddingTop(final float paddingTop) {
        this.paddingTop = paddingTop;
        return this;
    }

    public float getPaddingBottom() {
        return paddingBottom;
    }

    public Cell setPaddingBottom(final float paddingBottom) {
        this.paddingBottom = paddingBottom;
        return this;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return alignment;
    }

    public Cell setHorizontalAlignment(final HorizontalAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public boolean hasBorderTop() {
        return getBorderWidthTop() > 0;
    }

    public boolean hasBorderBottom() {
        return getBorderWidthBottom() > 0;
    }

    public boolean hasBorderLeft() {
        return getBorderWidthLeft() > 0;
    }

    public boolean hasBorderRight() {
        return getBorderWidthRight() > 0;
    }

    public float getBorderWidthTop() {
        return borderWidthTop;
    }

    private Cell setBorderWidthTop(float borderWidthTop) {
        this.borderWidthTop = borderWidthTop;
        return this;
    }

    public float getBorderWidthLeft() {
        return borderWidthLeft;
    }

    private Cell setBorderWidthLeft(float borderWidthLeft) {
        this.borderWidthLeft = borderWidthLeft;
        return this;
    }

    public float getBorderWidthRight() {
        return borderWidthRight;
    }

    private Cell setBorderWidthRight(float borderWidthRight) {
        this.borderWidthRight = borderWidthRight;
        return this;
    }

    public float getBorderWidthBottom() {
        return borderWidthBottom;
    }

    private Cell setBorderWidthBottom(float borderWidthBottom) {
        this.borderWidthBottom = borderWidthBottom;
        return this;
    }

    public float getHeightWithoutFontSize() {
        return this.paddingBottom + this.paddingTop;
    }

    public Color getBorderColor() {
        Optional<Color> optBorderColor = Optional.ofNullable(borderColor);
        return optBorderColor.orElse(getRow().getBorderColor());
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public Color getParentBorderColor() {
        return getRow().getBorderColor();
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Cell withTextColor(Color color) {
        this.textColor = color;
        return this;
    }

    public float getHeight(){
        return this.paddingTop + (row.getTable().getFontHeight() + this.paddingBottom) * text.split("\n").length;
    }

}

