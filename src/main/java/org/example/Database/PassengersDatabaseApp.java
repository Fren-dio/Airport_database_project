package org.example.Database;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;

public class PassengersDatabaseApp {

    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTable table;
    private Connection connection;
    private String currentTable = "";

    public void show() {
        frame.setVisible(true);
    }
}
