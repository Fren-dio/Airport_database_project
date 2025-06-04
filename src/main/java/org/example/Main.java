package org.example;

import org.example.MainWindow.FrameWork;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        System.out.println("Start application using Swing and H2 Database");

        FrameWork mainWindow = new FrameWork();


    }
}