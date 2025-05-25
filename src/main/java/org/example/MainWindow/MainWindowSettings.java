package org.example.MainWindow;

import java.awt.*;

public class MainWindowSettings {

    private int weight;
    private int height;

    public MainWindowSettings() {
        weight = 640;
        height = 480;
    }

    public void setSize(Dimension dim) {
        weight = dim.getSize().width;
        height = dim.getSize().height;
    }

    public void setHeight(int val) {
        height = val;
    }
    public void setWeight(int val) {
        weight = val;
    }

    public int getWeight(){
        return weight;
    }

    public int getHeight() {
        return height;
    }
}
