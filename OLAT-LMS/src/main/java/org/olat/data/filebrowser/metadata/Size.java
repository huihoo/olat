package org.olat.data.filebrowser.metadata;

public class Size {

    private int width;
    private int height;

    public Size() {
        //
    }

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "size[width=" + width + ":height=" + height + "]";
    }
}
