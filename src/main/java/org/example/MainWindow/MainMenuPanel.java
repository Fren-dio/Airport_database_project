package org.example.MainWindow;

import javax.swing.*;

public class MainMenuPanel extends JMenu {

    public MainMenuPanel(String pointName) {
        super(pointName);
    }


    public void addMenuItem(String itemName, Runnable action) {
        JMenuItem menuItem = new JMenuItem(itemName);
        menuItem.addActionListener(e -> action.run());
        this.add(menuItem);
    }

}
