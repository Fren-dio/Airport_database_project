package org.example.Database;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Vector;

public class AirplaneDatabaseApp {
    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTable table;
    private Connection connection;
    private String currentTable = "Planes";

    public AirplaneDatabaseApp() {
        frame = new JFrame("Информация о самолетах");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1200, 600);
        frame.setLocationRelativeTo(null);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        initUI();
    }

    public void show() {
        frame.setVisible(true);
    }

    private void initUI() {
        frame.setLayout(new BorderLayout());

        // Главная панель с отступами
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель для кнопок с выравниванием по левому краю
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Группы кнопок с выравниванием
        JPanel group1 = createAlignedGroup("Основные таблицы:",
                createTableButton("Самолеты", "Planes"),
                createTableButton("Типы самолетов", "PlaneTypeCharacteristics"),
                createTableButton("Пассажирские", "PassengerPlanes"),
                createTableButton("Грузовые", "CargoPlanes"),
                createTableButton("Специальные", "SpecialPlanes")
        );

        JPanel group2 = createAlignedGroup("Технические проверки:",
                createTableButton("Техосмотры", "PlanesAndTechCheckUps"),
                createTableButton("Ремонты", "PlanesAndRepairCheckUps"),
                createTableButton("Заправки", "PlanesAndOilFillings"),
                createTableButton("Уборки", "PlanesAndCabinCleanUps"),
                createTableButton("Питание", "PlanesAndFoodSupplies")
        );

        JPanel group3 = createAlignedGroup("Статусы:",
                createTableButton("Готовность", "ReadyStatuses"),
                createTableButton("Статусы полета", "FlightStatuses"),
                createTableButton("Статусы готовности", "PlanesAndReadyStatuses"),
                createTableButton("Статусы полетов", "PlanesAndFlightStatuses")
        );

        JPanel group4 = createAlignedGroup("Действия:",
                createActionButton("Обновить данные", e -> refreshData()),
                createActionButton("Добавить запись", this::showAddRecordDialog)
        );

        // Добавляем группы с отступами
        buttonPanel.add(group1);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(group2);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(group3);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(group4);

        // Добавляем панель с кнопками и таблицу
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        frame.add(mainPanel);
    }

    private JPanel createAlignedGroup(String labelText, JComponent... components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(150, label.getPreferredSize().height));
        panel.add(label);

        for (JComponent component : components) {
            panel.add(component);
        }

        return panel;
    }

    private JButton createTableButton(String text, String tableName) {
        JButton button = new JButton(text);
        button.setMinimumSize(new Dimension(200, button.getPreferredSize().height));
        button.setMaximumSize(new Dimension(200, button.getPreferredSize().height));
        button.setPreferredSize(new Dimension(200, button.getPreferredSize().height));
        button.addActionListener(e -> showTable(tableName));
        return button;
    }

    private void showTable(String tableName) {
    }

    private JButton createActionButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setMinimumSize(new Dimension(200, button.getPreferredSize().height));
        button.setMaximumSize(new Dimension(200, button.getPreferredSize().height));
        button.setPreferredSize(new Dimension(200, button.getPreferredSize().height));
        button.addActionListener(listener);
        return button;
    }



    private void showAddRecordDialog(ActionEvent e) {
        switch (currentTable) {
            case "Planes": showAddPlaneDialog(); break;
            case "PlaneTypeCharacteristics": showAddPlaneTypeDialog(); break;
            case "PassengerPlanes": showAddPassengerPlaneDialog(); break;
            case "CargoPlanes": showAddCargoPlaneDialog(); break;
            case "SpecialPlanes": showAddSpecialPlaneDialog(); break;
            case "PlanesAndTechCheckUps": showAddTechCheckupDialog(); break;
            case "PlanesAndRepairCheckUps": showAddRepairCheckupDialog(); break;
            case "PlanesAndOilFillings": showAddOilFillingDialog(); break;
            case "PlanesAndFoodSupplies": showAddFoodSupplyDialog(); break;
            case "ReadyStatuses": showAddReadyStatusDialog(); break;
            case "FlightStatuses": showAddFlightStatusDialog(); break;
            default: showError("Добавление записей в эту таблицу не реализовано");
        }
    }

    private void showAddFlightStatusDialog() {
    }

    private void showAddReadyStatusDialog() {
    }

    private void showAddFoodSupplyDialog() {
    }

    private void showAddPlaneDialog() {

    }

    private void showAddPlaneTypeDialog() {

    }

    private void showAddPassengerPlaneDialog() {

    }

    private void showAddCargoPlaneDialog() {

    }

    private void showAddSpecialPlaneDialog() {

    }

    private void showAddTechCheckupDialog() {

    }

    private void refreshData() {
        showTable(currentTable);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void showAddRepairCheckupDialog() {
        // Аналогично showAddTechCheckupDialog, но для ремонтов
    }

    private void showAddOilFillingDialog() {
        // Аналогично showAddTechCheckupDialog, но для заправок
    }


}
