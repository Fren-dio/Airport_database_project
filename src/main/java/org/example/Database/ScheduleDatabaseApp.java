package org.example.Database;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Vector;

@SuppressWarnings("ALL")
public class ScheduleDatabaseApp {
    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTable table;
    private Connection connection;
    private String currentTable = "Flights";

    public ScheduleDatabaseApp(Connection connection) {
        this.connection = connection;
        frame = new JFrame("Управление рейсами");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1300, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        initUI();
        initDatabase();
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
                createActionButton("Рейсы", e -> showFlights()),
                createTableButton("Расписание", "Schedule"),
                createTableButton("Локации", "Locations"),
                createTableButton("Самолеты", "Planes")
        );

        JPanel group2 = createAlignedGroup("Типы рейсов:",
                createTableButton("Внутренние", "InternalFlight"),
                createTableButton("Международные", "InternationalFlight"),
                createTableButton("Чартерные", "CharterFlight"),
                createTableButton("Грузовые", "CargoFlight"),
                createTableButton("Специальные", "SpecialFlight")
        );

        JPanel group3 = createAlignedGroup("Типы самолетов:",
                createTableButton("Пассажирские", "PassengerPlanes"),
                createTableButton("Грузовые", "CargoPlanes"),
                createTableButton("Специальные", "SpecialPlanes")
        );

        JPanel group4 = createAlignedGroup("Дополнительно:",
                createTableButton("Статусы рейсов", "FlightStatuses"),
                createTableButton("Погодные условия", "WeatherConditions"),
                createTableButton("Пассажиры в рейсе", "PassengersInFlight"),
                createTableButton("Дни недели", "WeekDays")
        );

        JPanel group5 = createAlignedGroup("Действия:",
                createActionButton("Добавить запись", this::showAddRecordDialog),
                createActionButton("Удалить запись", this::showDeleteRecordDialog),
                createActionButton("Обновить", e -> refreshData())
        );

        // Добавляем группы с отступами
        buttonPanel.add(group1);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(group2);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(group3);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(group4);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(group5);

        // Добавляем панель с кнопками и таблицу
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        frame.add(mainPanel);
    }

    private void showDeleteRecordDialog(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("Пожалуйста, выберите запись для удаления");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Вы уверены, что хотите удалить выбранную запись?",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            switch (currentTable) {
                case "Flights":
                    deleteFlight(selectedRow);
                    break;
                case "Schedule":
                    deleteSchedule(selectedRow);
                    break;
                case "Locations":
                    deleteLocation(selectedRow);
                    break;
                case "Planes":
                    deletePlane(selectedRow);
                    break;
                case "InternalFlight":
                    deleteInternalFlight(selectedRow);
                    break;
                case "InternationalFlight":
                    deleteInternationalFlight(selectedRow);
                    break;
                case "CharterFlight":
                    deleteCharterFlight(selectedRow);
                    break;
                case "CargoFlight":
                    deleteCargoFlight(selectedRow);
                    break;
                case "SpecialFlight":
                    deleteSpecialFlight(selectedRow);
                    break;
                case "PassengerPlanes":
                    deletePassengerPlane(selectedRow);
                    break;
                case "CargoPlanes":
                    deleteCargoPlane(selectedRow);
                    break;
                case "SpecialPlanes":
                    deleteSpecialPlane(selectedRow);
                    break;
                case "FlightStatuses":
                    deleteFlightStatus(selectedRow);
                    break;
                case "WeatherConditions":
                    deleteWeatherCondition(selectedRow);
                    break;
                case "PassengersInFlight":
                    deletePassengersInFlight(selectedRow);
                    break;
                case "WeekDays":
                    deleteWeekDay(selectedRow);
                    break;
                default:
                    showError("Удаление записей из этой таблицы не реализовано");
            }

            refreshData();
            JOptionPane.showMessageDialog(frame, "Запись успешно удалена", "Успех", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            showError("Ошибка при удалении записи: " + ex.getMessage());
        }
    }

    // Методы для удаления записей из различных таблиц
    private void deleteFlight(int selectedRow) throws SQLException {
        int flightId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Flights WHERE FlightID = ?")) {
            stmt.setInt(1, flightId);
            stmt.executeUpdate();
        }
    }

    private void deleteSchedule(int selectedRow) throws SQLException {
        int journeyId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Schedule WHERE JourneyID = ?")) {
            stmt.setInt(1, journeyId);
            stmt.executeUpdate();
        }
    }

    private void deleteLocation(int selectedRow) throws SQLException {
        String locationName = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Locations WHERE LocationName = ?")) {
            stmt.setString(1, locationName);
            stmt.executeUpdate();
        }
    }

    private void deletePlane(int selectedRow) throws SQLException {
        int planeId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Planes WHERE PlanesID = ?")) {
            stmt.setInt(1, planeId);
            stmt.executeUpdate();
        }
    }

    private void deleteInternalFlight(int selectedRow) throws SQLException {
        int flightId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM InternalFlight WHERE FlightID = ?")) {
            stmt.setInt(1, flightId);
            stmt.executeUpdate();
        }
    }

    private void deleteInternationalFlight(int selectedRow) throws SQLException {
        int flightId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM InternationalFlight WHERE FlightID = ?")) {
            stmt.setInt(1, flightId);
            stmt.executeUpdate();
        }
    }

    private void deleteCharterFlight(int selectedRow) throws SQLException {
        int flightId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM CharterFlight WHERE FlightID = ?")) {
            stmt.setInt(1, flightId);
            stmt.executeUpdate();
        }
    }

    private void deleteCargoFlight(int selectedRow) throws SQLException {
        int flightId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM CargoFlight WHERE FlightID = ?")) {
            stmt.setInt(1, flightId);
            stmt.executeUpdate();
        }
    }

    private void deleteSpecialFlight(int selectedRow) throws SQLException {
        int flightId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM SpecialFlight WHERE FlightID = ?")) {
            stmt.setInt(1, flightId);
            stmt.executeUpdate();
        }
    }

    private void deletePassengerPlane(int selectedRow) throws SQLException {
        int planeId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM PassengerPlanes WHERE PlaneID = ?")) {
            stmt.setInt(1, planeId);
            stmt.executeUpdate();
        }
    }

    private void deleteCargoPlane(int selectedRow) throws SQLException {
        int planeId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM CargoPlanes WHERE PlaneID = ?")) {
            stmt.setInt(1, planeId);
            stmt.executeUpdate();
        }
    }

    private void deleteSpecialPlane(int selectedRow) throws SQLException {
        int planeId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM SpecialPlanes WHERE PlaneID = ?")) {
            stmt.setInt(1, planeId);
            stmt.executeUpdate();
        }
    }

    private void deleteFlightStatus(int selectedRow) throws SQLException {
        String statusName = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM FlightStatuses WHERE StatusName = ?")) {
            stmt.setString(1, statusName);
            stmt.executeUpdate();
        }
    }

    private void deleteWeatherCondition(int selectedRow) throws SQLException {
        String conditionName = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM WeatherConditions WHERE NameOfCondition = ?")) {
            stmt.setString(1, conditionName);
            stmt.executeUpdate();
        }
    }

    private void deletePassengersInFlight(int selectedRow) throws SQLException {
        int flightId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM PassengersInFlight WHERE FlightID = ?")) {
            stmt.setInt(1, flightId);
            stmt.executeUpdate();
        }
    }

    private void deleteWeekDay(int selectedRow) throws SQLException {
        String dayName = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM WeekDays WHERE DayName = ?")) {
            stmt.setString(1, dayName);
            stmt.executeUpdate();
        }
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
        button.setMinimumSize(new Dimension(180, button.getPreferredSize().height));
        button.setMaximumSize(new Dimension(180, button.getPreferredSize().height));
        button.setPreferredSize(new Dimension(180, button.getPreferredSize().height));
        button.addActionListener(e -> showTable(tableName));
        return button;
    }

    private JButton createActionButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setMinimumSize(new Dimension(180, button.getPreferredSize().height));
        button.setMaximumSize(new Dimension(180, button.getPreferredSize().height));
        button.setPreferredSize(new Dimension(180, button.getPreferredSize().height));
        button.addActionListener(listener);
        return button;
    }

    private void initDatabase() {
        try {
            connection = DatabaseManager.getConnection();
            showTable("Flights");
        } catch (Exception e) {
            showError("Ошибка инициализации базы данных: " + e.getMessage());
            System.exit(1);
        }
    }

    private void showAddRecordDialog(ActionEvent e) {
        switch (currentTable) {
            case "Flights":
                showAddFlightDialog();
                break;
            case "Schedule":
                showAddScheduleDialog();
                break;
            case "Locations":
                showAddLocationDialog();
                break;
            case "Planes":
                showAddPlaneDialog();
                break;
            case "InternalFlight":
                showAddInternalFlightDialog();
                break;
            case "InternationalFlight":
                showAddInternationalFlightDialog();
                break;
            case "CharterFlight":
                showAddCharterFlightDialog();
                break;
            case "CargoFlight":
                showAddCargoFlightDialog();
                break;
            case "SpecialFlight":
                showAddSpecialFlightDialog();
                break;
            case "PassengerPlanes":
                showAddPassengerPlaneDialog();
                break;
            case "CargoPlanes":
                showAddCargoPlaneDialog();
                break;
            case "SpecialPlanes":
                showAddSpecialPlaneDialog();
                break;
            case "FlightStatuses":
                showAddFlightStatusDialog();
                break;
            case "WeatherConditions":
                showAddWeatherConditionDialog();
                break;
            case "PassengersInFlight":
                showAddPassengersInFlightDialog();
                break;
            case "WeekDays":
                showAddWeekDayDialog();
                break;
            default:
                showError("Добавление записей в эту таблицу не реализовано");
        }
    }

    private void showAddWeekDayDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField dayIdField = new JTextField();
        JTextField dayNameField = new JTextField();

        panel.add(new JLabel("ID дня:"));
        panel.add(dayIdField);
        panel.add(new JLabel("Название дня:"));
        panel.add(dayNameField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить день недели",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (dayIdField.getText().isEmpty() || dayNameField.getText().isEmpty()) {
                    throw new Exception("Все поля обязательны для заполнения");
                }

                String sql = "INSERT INTO WeekDays VALUES (?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(dayIdField.getText()));
                    stmt.setString(2, dayNameField.getText());

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении дня недели: " + ex.getMessage());
            }
        }
    }

    private void showAddPassengersInFlightDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<String> flightCombo = new JComboBox<>();
        JTextField maxCapacityField = new JTextField();
        JTextField realCapacityField = new JTextField();
        JTextField withBaggageField = new JTextField();
        JTextField baggageItemsField = new JTextField();
        JTextField baggageWeightField = new JTextField();
        JTextField boughtTicketsField = new JTextField();
        JTextField passedTicketsField = new JTextField();
        JTextField passengersNotCameField = new JTextField();
        JTextField haveNotDocumentsField = new JTextField();
        JTextField haveNotPermissionField = new JTextField();
        JTextField deportedPassengersField = new JTextField();

        try {
            fillComboBox(flightCombo, "SELECT FlightID FROM Flights", "FlightID");
        } catch (SQLException ex) {
            showError("Ошибка загрузки рейсов: " + ex.getMessage());
        }

        panel.add(new JLabel("Номер рейса:"));
        panel.add(flightCombo);
        panel.add(new JLabel("Макс. вместимость:"));
        panel.add(maxCapacityField);
        panel.add(new JLabel("Факт. вместимость:"));
        panel.add(realCapacityField);
        panel.add(new JLabel("С багажом:"));
        panel.add(withBaggageField);
        panel.add(new JLabel("Кол-во багажа:"));
        panel.add(baggageItemsField);
        panel.add(new JLabel("Вес багажа:"));
        panel.add(baggageWeightField);
        panel.add(new JLabel("Куплено билетов:"));
        panel.add(boughtTicketsField);
        panel.add(new JLabel("Исп. билетов:"));
        panel.add(passedTicketsField);
        panel.add(new JLabel("Не пришли:"));
        panel.add(passengersNotCameField);
        panel.add(new JLabel("Без документов:"));
        panel.add(haveNotDocumentsField);
        panel.add(new JLabel("Без разрешения:"));
        panel.add(haveNotPermissionField);
        panel.add(new JLabel("Депортировано:"));
        panel.add(deportedPassengersField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить пассажиров в рейс",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String selectedFlight = (String) flightCombo.getSelectedItem();
                if (selectedFlight == null || selectedFlight.isEmpty()) {
                    throw new Exception("Необходимо выбрать рейс");
                }

                String sql = "INSERT INTO PassengersInFlight VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    int flightId = Integer.parseInt(selectedFlight.split(" - ")[0]);
                    stmt.setInt(1, flightId);
                    stmt.setInt(2, maxCapacityField.getText().isEmpty() ? 0 : Integer.parseInt(maxCapacityField.getText()));
                    stmt.setInt(3, realCapacityField.getText().isEmpty() ? 0 : Integer.parseInt(realCapacityField.getText()));
                    stmt.setInt(4, withBaggageField.getText().isEmpty() ? 0 : Integer.parseInt(withBaggageField.getText()));
                    stmt.setInt(5, baggageItemsField.getText().isEmpty() ? 0 : Integer.parseInt(baggageItemsField.getText()));
                    stmt.setInt(6, baggageWeightField.getText().isEmpty() ? 0 : Integer.parseInt(baggageWeightField.getText()));
                    stmt.setInt(7, boughtTicketsField.getText().isEmpty() ? 0 : Integer.parseInt(boughtTicketsField.getText()));
                    stmt.setInt(8, passedTicketsField.getText().isEmpty() ? 0 : Integer.parseInt(passedTicketsField.getText()));
                    stmt.setInt(9, passengersNotCameField.getText().isEmpty() ? 0 : Integer.parseInt(passengersNotCameField.getText()));
                    stmt.setInt(10, haveNotDocumentsField.getText().isEmpty() ? 0 : Integer.parseInt(haveNotDocumentsField.getText()));
                    stmt.setInt(11, haveNotPermissionField.getText().isEmpty() ? 0 : Integer.parseInt(haveNotPermissionField.getText()));
                    stmt.setInt(12, deportedPassengersField.getText().isEmpty() ? 0 : Integer.parseInt(deportedPassengersField.getText()));

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении пассажиров в рейс: " + ex.getMessage());
            }
        }
    }

    private void showAddWeatherConditionDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField conditionIdField = new JTextField();
        JTextField nameField = new JTextField();
        JCheckBox canFlightCheck = new JCheckBox("Можно летать");
        JCheckBox needWaitCheck = new JCheckBox("Нужно ждать");
        JCheckBox needCancelCheck = new JCheckBox("Нужно отменить");

        panel.add(new JLabel("ID условия:"));
        panel.add(conditionIdField);
        panel.add(new JLabel("Название:"));
        panel.add(nameField);
        panel.add(new JLabel(""));
        panel.add(canFlightCheck);
        panel.add(new JLabel(""));
        panel.add(needWaitCheck);
        panel.add(new JLabel(""));
        panel.add(needCancelCheck);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить погодное условие",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (conditionIdField.getText().isEmpty() || nameField.getText().isEmpty()) {
                    throw new Exception("ID и название обязательны для заполнения");
                }

                String sql = "INSERT INTO WeatherConditions VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(conditionIdField.getText()));
                    stmt.setString(2, nameField.getText());
                    stmt.setString(3, canFlightCheck.isSelected() ? "Y" : "N");
                    stmt.setString(4, needWaitCheck.isSelected() ? "Y" : "N");
                    stmt.setString(5, needCancelCheck.isSelected() ? "Y" : "N");

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении погодного условия: " + ex.getMessage());
            }
        }
    }

    private void showAddFlightStatusDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField statusIdField = new JTextField();
        JTextField statusNameField = new JTextField();

        panel.add(new JLabel("ID статуса:"));
        panel.add(statusIdField);
        panel.add(new JLabel("Название статуса:"));
        panel.add(statusNameField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить статус рейса",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (statusIdField.getText().isEmpty() || statusNameField.getText().isEmpty()) {
                    throw new Exception("Все поля обязательны для заполнения");
                }

                String sql = "INSERT INTO FlightStatuses VALUES (?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(statusIdField.getText()));
                    stmt.setString(2, statusNameField.getText());

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении статуса рейса: " + ex.getMessage());
            }
        }
    }

    private void showAddSpecialPlaneDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<String> planeCombo = new JComboBox<>();
        JTextField specialTypeField = new JTextField();
        JTextField flightClientField = new JTextField();
        JTextField crewRequirementsField = new JTextField();
        JCheckBox specialFuelCheck = new JCheckBox("Спец. топливо");
        JCheckBox additionalEquipmentCheck = new JCheckBox("Доп. оборудование");

        try {
            fillComboBox(planeCombo, "SELECT PlanesID FROM Planes", "PlanesID");
        } catch (SQLException ex) {
            showError("Ошибка загрузки самолетов: " + ex.getMessage());
        }

        panel.add(new JLabel("Самолет:"));
        panel.add(planeCombo);
        panel.add(new JLabel("Тип спецрейса:"));
        panel.add(specialTypeField);
        panel.add(new JLabel("Клиент:"));
        panel.add(flightClientField);
        panel.add(new JLabel("Требования к экипажу:"));
        panel.add(crewRequirementsField);
        panel.add(new JLabel(""));
        panel.add(specialFuelCheck);
        panel.add(new JLabel(""));
        panel.add(additionalEquipmentCheck);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить спецсамолет",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String selectedPlane = (String) planeCombo.getSelectedItem();
                if (selectedPlane == null || selectedPlane.isEmpty()) {
                    throw new Exception("Необходимо выбрать самолет");
                }

                String sql = "INSERT INTO SpecialPlanes VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    int planeId = Integer.parseInt(selectedPlane.split(" - ")[0]);
                    stmt.setInt(1, planeId);
                    stmt.setString(2, specialTypeField.getText());
                    stmt.setString(3, flightClientField.getText());
                    stmt.setString(4, crewRequirementsField.getText());
                    stmt.setString(5, specialFuelCheck.isSelected() ? "Y" : "N");
                    stmt.setString(6, additionalEquipmentCheck.isSelected() ? "Y" : "N");

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении спецсамолета: " + ex.getMessage());
            }
        }
    }

    private void showAddCargoPlaneDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<String> planeCombo = new JComboBox<>();
        JTextField maxLoadField = new JTextField();
        JTextField maxVolumeField = new JTextField();
        JTextField compartmentLengthField = new JTextField();
        JTextField compartmentWeightField = new JTextField();
        JTextField compartmentHeightField = new JTextField();
        JTextField trapdoorLengthField = new JTextField();
        JTextField trapdoorWeightField = new JTextField();
        JTextField trapdoorHeightField = new JTextField();

        try {
            fillComboBox(planeCombo, "SELECT PlanesID FROM Planes", "PlanesID");
        } catch (SQLException ex) {
            showError("Ошибка загрузки самолетов: " + ex.getMessage());
        }

        panel.add(new JLabel("Самолет:"));
        panel.add(planeCombo);
        panel.add(new JLabel("Макс. груз:"));
        panel.add(maxLoadField);
        panel.add(new JLabel("Макс. объем:"));
        panel.add(maxVolumeField);
        panel.add(new JLabel("Длина отсека:"));
        panel.add(compartmentLengthField);
        panel.add(new JLabel("Вес отсека:"));
        panel.add(compartmentWeightField);
        panel.add(new JLabel("Высота отсека:"));
        panel.add(compartmentHeightField);
        panel.add(new JLabel("Длина люка:"));
        panel.add(trapdoorLengthField);
        panel.add(new JLabel("Вес люка:"));
        panel.add(trapdoorWeightField);
        panel.add(new JLabel("Высота люка:"));
        panel.add(trapdoorHeightField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить грузовой самолет",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String selectedPlane = (String) planeCombo.getSelectedItem();
                if (selectedPlane == null || selectedPlane.isEmpty()) {
                    throw new Exception("Необходимо выбрать самолет");
                }

                String sql = "INSERT INTO CargoPlanes VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    int planeId = Integer.parseInt(selectedPlane.split(" - ")[0]);
                    stmt.setInt(1, planeId);
                    stmt.setFloat(2, maxLoadField.getText().isEmpty() ? 0 : Float.parseFloat(maxLoadField.getText()));
                    stmt.setFloat(3, maxVolumeField.getText().isEmpty() ? 0 : Float.parseFloat(maxVolumeField.getText()));
                    stmt.setFloat(4, compartmentLengthField.getText().isEmpty() ? 0 : Float.parseFloat(compartmentLengthField.getText()));
                    stmt.setFloat(5, compartmentWeightField.getText().isEmpty() ? 0 : Float.parseFloat(compartmentWeightField.getText()));
                    stmt.setFloat(6, compartmentHeightField.getText().isEmpty() ? 0 : Float.parseFloat(compartmentHeightField.getText()));
                    stmt.setFloat(7, trapdoorLengthField.getText().isEmpty() ? 0 : Float.parseFloat(trapdoorLengthField.getText()));
                    stmt.setFloat(8, trapdoorWeightField.getText().isEmpty() ? 0 : Float.parseFloat(trapdoorWeightField.getText()));
                    stmt.setFloat(9, trapdoorHeightField.getText().isEmpty() ? 0 : Float.parseFloat(trapdoorHeightField.getText()));

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении грузового самолета: " + ex.getMessage());
            }
        }
    }

    private void showAddPassengerPlaneDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<String> planeCombo = new JComboBox<>();
        JTextField maxPassengersField = new JTextField();
        JTextField allLoadCapacityField = new JTextField();
        JTextField baggageLoadCapacityField = new JTextField();

        try {
            fillComboBox(planeCombo, "SELECT PlanesID FROM Planes", "PlanesID");
        } catch (SQLException ex) {
            showError("Ошибка загрузки самолетов: " + ex.getMessage());
        }

        panel.add(new JLabel("Самолет:"));
        panel.add(planeCombo);
        panel.add(new JLabel("Макс. пассажиров:"));
        panel.add(maxPassengersField);
        panel.add(new JLabel("Общая грузоподъемность:"));
        panel.add(allLoadCapacityField);
        panel.add(new JLabel("Грузоподъемность багажа:"));
        panel.add(baggageLoadCapacityField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить пассажирский самолет",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String selectedPlane = (String) planeCombo.getSelectedItem();
                if (selectedPlane == null || selectedPlane.isEmpty()) {
                    throw new Exception("Необходимо выбрать самолет");
                }

                String sql = "INSERT INTO PassengerPlanes VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    int planeId = Integer.parseInt(selectedPlane.split(" - ")[0]);
                    stmt.setInt(1, planeId);
                    stmt.setInt(2, maxPassengersField.getText().isEmpty() ? 0 : Integer.parseInt(maxPassengersField.getText()));
                    stmt.setFloat(3, allLoadCapacityField.getText().isEmpty() ? 0 : Float.parseFloat(allLoadCapacityField.getText()));
                    stmt.setFloat(4, baggageLoadCapacityField.getText().isEmpty() ? 0 : Float.parseFloat(baggageLoadCapacityField.getText()));

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении пассажирского самолета: " + ex.getMessage());
            }
        }
    }

    private void showAddSpecialFlightDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField flightIdField = new JTextField();
        JTextField flightGoalField = new JTextField();
        JTextField aboutField = new JTextField();

        panel.add(new JLabel("Номер рейса:"));
        panel.add(flightIdField);
        panel.add(new JLabel("Цель рейса:"));
        panel.add(flightGoalField);
        panel.add(new JLabel("Описание:"));
        panel.add(aboutField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить спецрейс",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (flightIdField.getText().isEmpty()) {
                    throw new Exception("Номер рейса обязателен для заполнения");
                }

                String sql = "INSERT INTO SpecialFlight VALUES (?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(flightIdField.getText()));
                    stmt.setString(2, flightGoalField.getText());
                    stmt.setString(3, aboutField.getText());

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении спецрейса: " + ex.getMessage());
            }
        }
    }


    private void showAddCargoFlightDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField flightIdField = new JTextField();
        JTextField maxCapacityField = new JTextField();
        JTextField realCapacityField = new JTextField();

        panel.add(new JLabel("Номер рейса:"));
        panel.add(flightIdField);
        panel.add(new JLabel("Макс. грузоподъемность:"));
        panel.add(maxCapacityField);
        panel.add(new JLabel("Факт. грузоподъемность:"));
        panel.add(realCapacityField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить грузовой рейс",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (flightIdField.getText().isEmpty()) {
                    throw new Exception("Номер рейса обязателен для заполнения");
                }

                String sql = "INSERT INTO CargoFlight VALUES (?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(flightIdField.getText()));
                    stmt.setInt(2, maxCapacityField.getText().isEmpty() ? 0 : Integer.parseInt(maxCapacityField.getText()));
                    stmt.setInt(3, realCapacityField.getText().isEmpty() ? 0 : Integer.parseInt(realCapacityField.getText()));

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении грузового рейса: " + ex.getMessage());
            }
        }
    }

    private void showAddCharterFlightDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField flightIdField = new JTextField();
        JComboBox<String> agencyCombo = new JComboBox<>();
        JTextField minTicketsField = new JTextField();
        JTextField passengersCapField = new JTextField();

        try {
            fillComboBox(agencyCombo, "SELECT AgencyID, AgencyName FROM Agency", "AgencyID");
        } catch (SQLException ex) {
            showError("Ошибка загрузки агентств: " + ex.getMessage());
        }

        panel.add(new JLabel("Номер рейса:"));
        panel.add(flightIdField);
        panel.add(new JLabel("Агентство:"));
        panel.add(agencyCombo);
        panel.add(new JLabel("Мин. билетов:"));
        panel.add(minTicketsField);
        panel.add(new JLabel("Вместимость:"));
        panel.add(passengersCapField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить чартерный рейс",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (flightIdField.getText().isEmpty()) {
                    throw new Exception("Номер рейса обязателен для заполнения");
                }

                String sql = "INSERT INTO CharterFlight VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(flightIdField.getText()));

                    String selectedAgency = (String) agencyCombo.getSelectedItem();
                    int agencyId = selectedAgency != null ?
                            Integer.parseInt(selectedAgency.split(" - ")[0]) : 0;
                    stmt.setInt(2, agencyId);

                    stmt.setInt(3, minTicketsField.getText().isEmpty() ? 0 :
                            Integer.parseInt(minTicketsField.getText()));
                    stmt.setInt(4, passengersCapField.getText().isEmpty() ? 0 :
                            Integer.parseInt(passengersCapField.getText()));

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении чартерного рейса: " + ex.getMessage());
            }
        }
    }


    private void showAddFlightDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Выпадающие списки для внешних ключей
        JComboBox<String> journeyCombo = new JComboBox<>();
        JComboBox<String> planeCombo = new JComboBox<>();
        JComboBox<String> pylotTeamCombo = new JComboBox<>();
        JComboBox<String> technicTeamCombo = new JComboBox<>();
        JComboBox<String> fromLocationCombo = new JComboBox<>();
        JComboBox<String> toLocationCombo = new JComboBox<>();
        JComboBox<String> weatherCombo = new JComboBox<>();
        JComboBox<String> statusCombo = new JComboBox<>();

        // Заполняем комбобоксы данными
        try {
            fillComboBox(journeyCombo, "SELECT JourneyID FROM Schedule", "JourneyID");
            fillComboBox(planeCombo, "SELECT PlanesID, HomeAirportID FROM Planes", "PlanesID");
            fillComboBox(pylotTeamCombo, "SELECT TeamID, TeamName FROM Teams", "TeamID");
            fillComboBox(technicTeamCombo, "SELECT TeamID, TeamName FROM Teams", "TeamID");
            fillComboBox(fromLocationCombo, "SELECT LocationID, LocationName FROM Locations", "LocationID");
            fillComboBox(toLocationCombo, "SELECT LocationID, LocationName FROM Locations", "LocationID");
            fillComboBox(weatherCombo, "SELECT weatherID, NameOfCondition FROM WeatherConditions", "weatherID");
            fillComboBox(statusCombo, "SELECT StatusID, StatucName FROM FlightsStatusForHistory", "StatusID");
        } catch (SQLException ex) {
            showError("Ошибка загрузки данных: " + ex.getMessage());
        }

        JTextField maxPassengersField = new JTextField();
        JTextField maxLoadField = new JTextField();
        JTextField fromTimeField = new JTextField();
        JTextField scheduleFromTimeField = new JTextField();
        JTextField toTimeField = new JTextField();
        JTextField scheduleToTimeField = new JTextField();

        // Добавляем подсказки для формата даты
        fromTimeField.setToolTipText("Формат: YYYY-MM-DD HH:MM:SS");
        scheduleFromTimeField.setToolTipText("Формат: YYYY-MM-DD HH:MM:SS");
        toTimeField.setToolTipText("Формат: YYYY-MM-DD HH:MM:SS");
        scheduleToTimeField.setToolTipText("Формат: YYYY-MM-DD HH:MM:SS");

        panel.add(new JLabel("Номер рейса:"));
        JTextField flightIdField = new JTextField();
        panel.add(flightIdField);
        panel.add(new JLabel("Номер маршрута:"));
        panel.add(journeyCombo);
        panel.add(new JLabel("Номер самолета:"));
        panel.add(planeCombo);
        panel.add(new JLabel("Макс. пассажиров:"));
        panel.add(maxPassengersField);
        panel.add(new JLabel("Макс. груз:"));
        panel.add(maxLoadField);
        panel.add(new JLabel("Команда пилотов:"));
        panel.add(pylotTeamCombo);
        panel.add(new JLabel("Команда техников:"));
        panel.add(technicTeamCombo);
        panel.add(new JLabel("Откуда:"));
        panel.add(fromLocationCombo);
        panel.add(new JLabel("Время вылета:"));
        panel.add(fromTimeField);
        panel.add(new JLabel("План. время вылета:"));
        panel.add(scheduleFromTimeField);
        panel.add(new JLabel("Куда:"));
        panel.add(toLocationCombo);
        panel.add(new JLabel("Время прилета:"));
        panel.add(toTimeField);
        panel.add(new JLabel("План. время прилета:"));
        panel.add(scheduleToTimeField);
        panel.add(new JLabel("Погода:"));
        panel.add(weatherCombo);
        panel.add(new JLabel("Статус:"));
        panel.add(statusCombo);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить рейс",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (flightIdField.getText().isEmpty()) {
                    throw new Exception("Номер рейса обязательно для заполнения");
                }

                String sql = "INSERT INTO Flights VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(flightIdField.getText()));

                    String selectedJourney = (String) journeyCombo.getSelectedItem();
                    int journeyId = selectedJourney != null ?
                            Integer.parseInt(selectedJourney.split(" - ")[0]) : 0;
                    stmt.setInt(2, journeyId > 0 ? journeyId : 0);

                    String selectedPlane = (String) planeCombo.getSelectedItem();
                    int planeId = selectedPlane != null ?
                            Integer.parseInt(selectedPlane.split(" - ")[0]) : 0;
                    stmt.setInt(3, planeId);

                    stmt.setInt(4, maxPassengersField.getText().isEmpty() ? 0 :
                            Integer.parseInt(maxPassengersField.getText()));
                    stmt.setInt(5, maxLoadField.getText().isEmpty() ? 0 :
                            Integer.parseInt(maxLoadField.getText()));

                    String selectedPylotTeam = (String) pylotTeamCombo.getSelectedItem();
                    int pylotTeamId = selectedPylotTeam != null ?
                            Integer.parseInt(selectedPylotTeam.split(" - ")[0]) : 0;
                    stmt.setInt(6, pylotTeamId);

                    String selectedTechnicTeam = (String) technicTeamCombo.getSelectedItem();
                    int technicTeamId = selectedTechnicTeam != null ?
                            Integer.parseInt(selectedTechnicTeam.split(" - ")[0]) : 0;
                    stmt.setInt(7, technicTeamId);

                    String selectedFromLocation = (String) fromLocationCombo.getSelectedItem();
                    int fromLocationId = selectedFromLocation != null ?
                            Integer.parseInt(selectedFromLocation.split(" - ")[0]) : 0;
                    stmt.setInt(8, fromLocationId);

                    stmt.setTimestamp(9, fromTimeField.getText().isEmpty() ? null :
                            Timestamp.valueOf(fromTimeField.getText()));
                    stmt.setTimestamp(10, scheduleFromTimeField.getText().isEmpty() ? null :
                            Timestamp.valueOf(scheduleFromTimeField.getText()));

                    String selectedToLocation = (String) toLocationCombo.getSelectedItem();
                    int toLocationId = selectedToLocation != null ?
                            Integer.parseInt(selectedToLocation.split(" - ")[0]) : 0;
                    stmt.setInt(11, toLocationId);

                    stmt.setTimestamp(12, toTimeField.getText().isEmpty() ? null :
                            Timestamp.valueOf(toTimeField.getText()));
                    stmt.setTimestamp(13, scheduleToTimeField.getText().isEmpty() ? null :
                            Timestamp.valueOf(scheduleToTimeField.getText()));

                    String selectedWeather = (String) weatherCombo.getSelectedItem();
                    int weatherId = selectedWeather != null ?
                            Integer.parseInt(selectedWeather.split(" - ")[0]) : 0;
                    stmt.setInt(14, weatherId);

                    String selectedStatus = (String) statusCombo.getSelectedItem();
                    int statusId = selectedStatus != null ?
                            Integer.parseInt(selectedStatus.split(" - ")[0]) : 0;
                    stmt.setInt(15, statusId);

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении рейса: " + ex.getMessage());
            }
        }
    }

    private void fillComboBox(JComboBox<String> journeyCombo, String query, String journeyID) throws SQLException{
    }

    private void showAddScheduleDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField journeyIdField = new JTextField();
        JComboBox<String> startPointCombo = new JComboBox<>();
        JComboBox<String> transferPointCombo = new JComboBox<>();
        JComboBox<String> finishPointCombo = new JComboBox<>();
        JTextField ticketCostField = new JTextField();

        try {
            fillComboBox(startPointCombo, "SELECT LocationID, LocationName FROM Locations", "LocationID");
            fillComboBox(transferPointCombo, "SELECT LocationID, LocationName FROM Locations", "LocationID");
            fillComboBox(finishPointCombo, "SELECT LocationID, LocationName FROM Locations", "LocationID");
        } catch (SQLException ex) {
            showError("Ошибка загрузки локаций: " + ex.getMessage());
        }

        panel.add(new JLabel("Номер маршрута:"));
        panel.add(journeyIdField);
        panel.add(new JLabel("Начальная точка:"));
        panel.add(startPointCombo);
        panel.add(new JLabel("Промежуточная точка:"));
        panel.add(transferPointCombo);
        panel.add(new JLabel("Конечная точка:"));
        panel.add(finishPointCombo);
        panel.add(new JLabel("Стоимость билета:"));
        panel.add(ticketCostField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить маршрут",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (journeyIdField.getText().isEmpty()) {
                    throw new Exception("Номер маршрута обязательно для заполнения");
                }

                String sql = "INSERT INTO Schedule VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(journeyIdField.getText()));

                    String selectedStart = (String) startPointCombo.getSelectedItem();
                    int startId = selectedStart != null ?
                            Integer.parseInt(selectedStart.split(" - ")[0]) : 0;
                    stmt.setInt(2, startId);

                    String selectedTransfer = (String) transferPointCombo.getSelectedItem();
                    int transferId = selectedTransfer != null ?
                            Integer.parseInt(selectedTransfer.split(" - ")[0]) : 0;
                    stmt.setInt(3, transferId);

                    String selectedFinish = (String) finishPointCombo.getSelectedItem();
                    int finishId = selectedFinish != null ?
                            Integer.parseInt(selectedFinish.split(" - ")[0]) : 0;
                    stmt.setInt(4, finishId);

                    stmt.setInt(5, ticketCostField.getText().isEmpty() ? 0 :
                            Integer.parseInt(ticketCostField.getText()));

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении маршрута: " + ex.getMessage());
            }
        }
    }

    private void showAddLocationDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField locationIdField = new JTextField();
        JTextField locationNameField = new JTextField();

        panel.add(new JLabel("Номер локации:"));
        panel.add(locationIdField);
        panel.add(new JLabel("Название локации:"));
        panel.add(locationNameField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить локацию",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (locationIdField.getText().isEmpty() || locationNameField.getText().isEmpty()) {
                    throw new Exception("Все поля обязательны для заполнения");
                }

                String sql = "INSERT INTO Locations VALUES (?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(locationIdField.getText()));
                    stmt.setString(2, locationNameField.getText());

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении локации: " + ex.getMessage());
            }
        }
    }

    private void showAddPlaneDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField planeIdField = new JTextField();
        JComboBox<String> homeAirportCombo = new JComboBox<>();
        JComboBox<String> planeTypeCombo = new JComboBox<>();

        try {
            fillComboBox(homeAirportCombo, "SELECT AirportID, Name FROM Airports", "AirportID");
            fillComboBox(planeTypeCombo, "SELECT TypeID, ModelName FROM PlaneTypeCharacteristics", "TypeID");
        } catch (SQLException ex) {
            showError("Ошибка загрузки данных: " + ex.getMessage());
        }

        panel.add(new JLabel("Номер самолета:"));
        panel.add(planeIdField);
        panel.add(new JLabel("Домашний аэропорт:"));
        panel.add(homeAirportCombo);
        panel.add(new JLabel("Тип самолета:"));
        panel.add(planeTypeCombo);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить самолет",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (planeIdField.getText().isEmpty()) {
                    throw new Exception("Номер самолета обязательно для заполнения");
                }

                String sql = "INSERT INTO Planes VALUES (?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(planeIdField.getText()));

                    String selectedAirport = (String) homeAirportCombo.getSelectedItem();
                    int airportId = selectedAirport != null ?
                            Integer.parseInt(selectedAirport.split(" - ")[0]) : 0;
                    stmt.setInt(2, airportId);

                    String selectedType = (String) planeTypeCombo.getSelectedItem();
                    int typeId = selectedType != null ?
                            Integer.parseInt(selectedType.split(" - ")[0]) : 0;
                    stmt.setInt(3, typeId);

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении самолета: " + ex.getMessage());
            }
        }
    }

    private void showAddInternalFlightDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField flightIdField = new JTextField();
        JTextField minTicketsField = new JTextField();
        JTextField passengersCapField = new JTextField();

        panel.add(new JLabel("Номер рейса:"));
        panel.add(flightIdField);
        panel.add(new JLabel("Мин. билетов:"));
        panel.add(minTicketsField);
        panel.add(new JLabel("Вместимость:"));
        panel.add(passengersCapField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить внутренний рейс",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (flightIdField.getText().isEmpty()) {
                    throw new Exception("Номер рейса обязательно для заполнения");
                }

                String sql = "INSERT INTO InternalFlight VALUES (?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(flightIdField.getText()));
                    stmt.setInt(2, minTicketsField.getText().isEmpty() ? 0 :
                            Integer.parseInt(minTicketsField.getText()));
                    stmt.setInt(3, passengersCapField.getText().isEmpty() ? 0 :
                            Integer.parseInt(passengersCapField.getText()));

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении внутреннего рейса: " + ex.getMessage());
            }
        }
    }

    private void showAddInternationalFlightDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField flightIdField = new JTextField();
        JTextField minTicketsField = new JTextField();
        JTextField passengersCapField = new JTextField();

        panel.add(new JLabel("Номер рейса:"));
        panel.add(flightIdField);
        panel.add(new JLabel("Мин. билетов:"));
        panel.add(minTicketsField);
        panel.add(new JLabel("Вместимость:"));
        panel.add(passengersCapField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить международный рейс",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (flightIdField.getText().isEmpty()) {
                    throw new Exception("Номер рейса обязательно для заполнения");
                }

                String sql = "INSERT INTO InternationalFlight VALUES (?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(flightIdField.getText()));
                    stmt.setInt(2, minTicketsField.getText().isEmpty() ? 0 :
                            Integer.parseInt(minTicketsField.getText()));
                    stmt.setInt(3, passengersCapField.getText().isEmpty() ? 0 :
                            Integer.parseInt(passengersCapField.getText()));

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении международного рейса: " + ex.getMessage());
            }
        }
    }

    private void showFlights() {
        currentTable = "Flights";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT f.FlightID, " +
                             "s.JourneyID, " +
                             "p.PlanesID, " +
                             "f.MaxPassengers, " +
                             "f.MaxLoad, " +
                             "pt.TeamName AS PylotTeam, " +
                             "tt.TeamName AS TechnicTeam, " +
                             "fl.LocationName AS FromLocation, " +
                             "f.FromTime, " +
                             "f.ScheduleFromTime, " +
                             "tl.LocationName AS ToLocation, " +
                             "f.ToTime, " +
                             "f.ScheduleToTime, " +
                             "w.NameOfCondition AS Weather, " +
                             "fs.StatucName AS Status " +
                             "FROM Flights f " +
                             "LEFT JOIN Schedule s ON f.JourneyID = s.JourneyID " +
                             "LEFT JOIN Planes p ON f.PlaneID = p.PlanesID " +
                             "LEFT JOIN Teams pt ON f.PylotTeam = pt.TeamID " +
                             "LEFT JOIN Teams tt ON f.TechnicTeam = tt.TeamID " +
                             "LEFT JOIN Locations fl ON f.FromLocation = fl.LocationID " +
                             "LEFT JOIN Locations tl ON f.ToLocation = tl.LocationID " +
                             "LEFT JOIN WeatherConditions w ON f.WeatherFromLocation = w.weatherID " +
                             "LEFT JOIN FlightsStatusForHistory fs ON f.FlightStatus = fs.StatusID")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0: case 1: case 2: case 3: case 4: return Integer.class;
                        case 8: case 9: case 11: case 12: return Timestamp.class;
                        default: return String.class;
                    }
                }
            };

            model.addColumn("Номер рейса");
            model.addColumn("Номер маршрута");
            model.addColumn("Макс. пассажиров");
            model.addColumn("Макс. груз");
            model.addColumn("Команда пилотов");
            model.addColumn("Команда техников");
            model.addColumn("Откуда");
            model.addColumn("Время вылета");
            model.addColumn("План. вылет");
            model.addColumn("Куда");
            model.addColumn("Время прилета");
            model.addColumn("План. прилет");
            model.addColumn("Погода");
            model.addColumn("Статус");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("FlightID"),
                        rs.getInt("JourneyID"),
                        rs.getInt("MaxPassengers"),
                        rs.getInt("MaxLoad"),
                        rs.getString("PylotTeam"),
                        rs.getString("TechnicTeam"),
                        rs.getString("FromLocation"),
                        rs.getTimestamp("FromTime"),
                        rs.getTimestamp("ScheduleFromTime"),
                        rs.getString("ToLocation"),
                        rs.getTimestamp("ToTime"),
                        rs.getTimestamp("ScheduleToTime"),
                        rs.getString("Weather"),
                        rs.getString("Status")
                });
            }

            table.setModel(model);
            configureTableWithDates();
        } catch (SQLException e) {
            showError("Ошибка загрузки рейсов: " + e.getMessage());
        }
    }

    private void showSchedule() {
        currentTable = "Schedule";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT s.JourneyID, " +
                             "sl.LocationName AS StartPoint, " +
                             "stl.LocationName AS TransferPoint, " +
                             "sfl.LocationName AS FinishPoint, " +
                             "s.TicketCost " +
                             "FROM Schedule s " +
                             "LEFT JOIN Locations sl ON s.TrailStartPoint = sl.LocationID " +
                             "LEFT JOIN Locations stl ON s.TrailTransferPoint = stl.LocationID " +
                             "LEFT JOIN Locations sfl ON s.TrailFinishPoint = sfl.LocationID")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0: case 4: return Integer.class;
                        default: return String.class;
                    }
                }
            };

            model.addColumn("Начальная точка");
            model.addColumn("Промежуточная точка");
            model.addColumn("Конечная точка");
            model.addColumn("Стоимость билета");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("StartPoint"),
                        rs.getString("TransferPoint"),
                        rs.getString("FinishPoint"),
                        rs.getInt("TicketCost")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки расписания: " + e.getMessage());
        }
    }

    private void showLocations() {
        currentTable = "Locations";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT LocationID, LocationName FROM Locations")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0: return Integer.class;
                        default: return String.class;
                    }
                }
            };

            model.addColumn("Название локации");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("LocationName")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки локаций: " + e.getMessage());
        }
    }

    private void showPlanes() {
        currentTable = "Planes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT p.PlanesID, " +
                             "a.Name AS HomeAirport, " +
                             "pt.ModelName AS PlaneType " +
                             "FROM Planes p " +
                             "LEFT JOIN Airports a ON p.HomeAirportID = a.AirportID " +
                             "LEFT JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0: return Integer.class;
                        default: return String.class;
                    }
                }
            };

            model.addColumn("Номер самолета");
            model.addColumn("Домашний аэропорт");
            model.addColumn("Тип самолета");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("PlanesID"),
                        rs.getString("HomeAirport"),
                        rs.getString("PlaneType")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки самолетов: " + e.getMessage());
        }
    }

    private void showInternalFlights() {
        currentTable = "InternalFlight";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT f.FlightID, " +
                             "f.MinimumTickets, " +
                             "f.PassengersCapacity " +
                             "FROM InternalFlight f")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return Integer.class;
                }
            };

            model.addColumn("Номер рейса");
            model.addColumn("Мин. билетов");
            model.addColumn("Вместимость");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("FlightID"),
                        rs.getInt("MinimumTickets"),
                        rs.getInt("PassengersCapacity")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки внутренних рейсов: " + e.getMessage());
        }
    }

    private void showInternationalFlights() {
        currentTable = "InternationalFlight";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT f.FlightID, " +
                             "f.MinimumTickets, " +
                             "f.PassengersCapcity " +
                             "FROM InternationalFlight f")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return Integer.class;
                }
            };

            model.addColumn("Номер рейса");
            model.addColumn("Мин. билетов");
            model.addColumn("Вместимость");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("FlightID"),
                        rs.getInt("MinimumTickets"),
                        rs.getInt("PassengersCapcity")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки международных рейсов: " + e.getMessage());
        }
    }

    private void showCharterFlights() {
        currentTable = "CharterFlight";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT f.FlightID, " +
                             "a.AgencyName, " +
                             "f.MinimumTickets, " +
                             "f.PassengersCapcity " +
                             "FROM CharterFlight f " +
                             "LEFT JOIN Agency a ON f.AgencyID = a.AgencyID")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0: case 2: case 3: return Integer.class;
                        default: return String.class;
                    }
                }
            };

            model.addColumn("Номер рейса");
            model.addColumn("Агентство");
            model.addColumn("Мин. билетов");
            model.addColumn("Вместимость");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("FlightID"),
                        rs.getString("AgencyName"),
                        rs.getInt("MinimumTickets"),
                        rs.getInt("PassengersCapcity")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки чартерных рейсов: " + e.getMessage());
        }
    }

    private void configureTable() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) table.getModel());
        table.setRowSorter(sorter);

        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setReorderingAllowed(false);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(150);
        }
    }

    private void configureTableWithDates() {
        configureTable();
        table.setDefaultRenderer(Date.class, new DefaultTableCellRenderer() {
            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                if (value instanceof Date) {
                    value = f.format((Date)value);
                }
                return super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);
            }
        });
    }



    private void refreshData() {
        showTable(currentTable);
    }


    private void showTable(String tableName) {
        currentTable = tableName;

        switch (tableName) {
            case "Flights" -> showFlights();
            case "Schedule" -> showSchedule();
            case "Locations" -> showLocations();
            case "Planes" -> showPlanes();
            case "InternalFlight" -> showInternalFlights();
            case "InternationalFlight" -> showInternationalFlights();
            case "CharterFlight" -> showCharterFlights();
            case "CargoFlight" -> showCargoFlights();
            case "SpecialFlight" -> showSpecialFlights();
            case "PassengerPlanes" -> showPassengerPlanes();
            case "CargoPlanes" -> showCargoPlanes();
            case "SpecialPlanes" -> showSpecialPlanes();
            case "FlightStatuses" -> showFlightStatuses();
            case "WeatherConditions" -> showWeatherConditions();
            case "PassengersInFlight" -> showPassengersInFlight();
            case "WeekDays" -> showWeekDays();
            default -> showGenericTable(tableName);
        }
    }
    private void showCargoPlanes() {
        currentTable = "CargoPlanes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT cp.*, p.PlanesID, pt.ModelName " +
                     "FROM CargoPlanes cp " +
                     "JOIN Planes p ON cp.PlaneID = p.PlanesID " +
                     "JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Integer.class :
                            (columnIndex >= 1 && columnIndex <= 10) ? Float.class : String.class;
                }
            };

            // Добавляем столбцы
            model.addColumn("ID самолета");
            model.addColumn("Макс. нагрузка (кг)");
            model.addColumn("Макс. объем (м³)");
            model.addColumn("Длина отсека (м)");
            model.addColumn("Ширина отсека (м)");
            model.addColumn("Высота отсека (м)");
            model.addColumn("Длина люка (м)");
            model.addColumn("Ширина люка (м)");
            model.addColumn("Высота люка (м)");
            model.addColumn("Модель");

            // Заполняем данные
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("PlaneID"),
                        rs.getFloat("MaxLoad"),
                        rs.getFloat("MaxVolume"),
                        rs.getFloat("CompartmentLength"),
                        rs.getFloat("CompartmentWeight"),
                        rs.getFloat("CompartmentHeight"),
                        rs.getFloat("TrapdoorLenght"),
                        rs.getFloat("TrapdoorWeight"),
                        rs.getFloat("TrapdoorHeight"),
                        rs.getString("ModelName")
                });
            }

            table.setModel(model);
            configureTable();

        } catch (SQLException e) {
            showError("Ошибка загрузки грузовых самолетов: " + e.getMessage());
        }
    }

    private void showSpecialFlights() {
        currentTable = "SpecialFlight";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT sf.*, f.FlightID, l1.LocationName AS FromLocation, " +
                     "l2.LocationName AS ToLocation, p.PlanesID, pt.ModelName " +
                     "FROM SpecialFlight sf " +
                     "JOIN Flights f ON sf.FlightID = f.FlightID " +
                     "JOIN Locations l1 ON f.FromLocation = l1.LocationID " +
                     "JOIN Locations l2 ON f.ToLocation = l2.LocationID " +
                     "JOIN Planes p ON f.PlaneID = p.PlanesID " +
                     "JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }
            };

            // Добавляем столбцы
            model.addColumn("ID рейса");
            model.addColumn("Цель полета");
            model.addColumn("Описание");
            model.addColumn("Отправление");
            model.addColumn("Прибытие");
            model.addColumn("Самолет");
            model.addColumn("Модель");

            // Заполняем данные
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("FlightID"),
                        rs.getString("FlightGoal"),
                        rs.getString("About"),
                        rs.getString("FromLocation"),
                        rs.getString("ToLocation"),
                        rs.getInt("PlanesID"),
                        rs.getString("ModelName")
                });
            }

            table.setModel(model);
            configureTable();

        } catch (SQLException e) {
            showError("Ошибка загрузки специальных рейсов: " + e.getMessage());
        }
    }

    private void showCargoFlights() {
        currentTable = "CargoFlight";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT cf.*, f.FlightID, l1.LocationName AS FromLocation, " +
                     "l2.LocationName AS ToLocation, p.PlanesID, pt.ModelName, " +
                     "f.FromTime, f.ToTime, f.ScheduleFromTime, f.ScheduleToTime " +
                     "FROM CargoFlight cf " +
                     "JOIN Flights f ON cf.FlightID = f.FlightID " +
                     "JOIN Locations l1 ON f.FromLocation = l1.LocationID " +
                     "JOIN Locations l2 ON f.ToLocation = l2.LocationID " +
                     "JOIN Planes p ON f.PlaneID = p.PlanesID " +
                     "JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 || columnIndex == 2 || columnIndex == 3 ? Integer.class : String.class;
                }
            };

            // Добавляем столбцы
            model.addColumn("ID рейса");
            model.addColumn("Макс. груз (кг)");
            model.addColumn("Факт. груз (кг)");
            model.addColumn("Отправление");
            model.addColumn("Прибытие");
            model.addColumn("Самолет");
            model.addColumn("Модель");
            model.addColumn("Время вылета");
            model.addColumn("Время прилета");

            // Заполняем данные
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("FlightID"),
                        rs.getInt("MaxCapacity"),
                        rs.getInt("RealCapacity"),
                        rs.getString("FromLocation"),
                        rs.getString("ToLocation"),
                        rs.getInt("PlanesID"),
                        rs.getString("ModelName"),
                        rs.getTimestamp("FromTime"),
                        rs.getTimestamp("ToTime")
                });
            }

            table.setModel(model);
            configureTable();

        } catch (SQLException e) {
            showError("Ошибка загрузки грузовых рейсов: " + e.getMessage());
        }
    }


    // Универсальный метод для таблиц без специальной обработки
    private void showGenericTable(String tableName) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }
            };

            // Заполнение модели данными
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Добавляем названия столбцов
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }

            // Добавляем данные
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i-1] = rs.getObject(i);
                }
                model.addRow(row);
            }

            table.setModel(model);
            configureTable();

        } catch (SQLException e) {
            showError("Ошибка загрузки таблицы " + tableName + ": " + e.getMessage());
        }
    }



    private void showWeekDays() {
        currentTable = "WeekDays";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DayID, DayName FROM WeekDays")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0: return Integer.class;
                        default: return String.class;
                    }
                }
            };

            model.addColumn("ID дня");
            model.addColumn("Название дня");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("DayID"),
                        rs.getString("DayName")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки дней недели: " + e.getMessage());
        }
    }

    private void showWeatherConditions() {
        currentTable = "WeatherConditions";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT weatherID, NameOfCondition, CanFlight, NeedWait, NeedCancel FROM WeatherConditions")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0: return Integer.class;
                        case 2: case 3: case 4: return Boolean.class;
                        default: return String.class;
                    }
                }
            };

            model.addColumn("ID условия");
            model.addColumn("Название");
            model.addColumn("Можно летать");
            model.addColumn("Нужно ждать");
            model.addColumn("Нужно отменить");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("weatherID"),
                        rs.getString("NameOfCondition"),
                        rs.getString("CanFlight").equals("Y"),
                        rs.getString("NeedWait").equals("Y"),
                        rs.getString("NeedCancel").equals("Y")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки погодных условий: " + e.getMessage());
        }
    }

    private void showPassengersInFlight() {
        currentTable = "PassengersInFlight";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT p.FlightID, " +
                             "p.MaxCapcity, " +
                             "p.RealCapacity, " +
                             "p.WithBaggage, " +
                             "p.BaggageItems, " +
                             "p.BaggageWeight, " +
                             "p.BoughtTickets, " +
                             "p.PassedTickets, " +
                             "p.PassengersNotCame, " +
                             "p.HaveNotDocumets, " +
                             "p.HaveNotPermission, " +
                             "p.DeportedPassengers " +
                             "FROM PassengersInFlight p")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return Integer.class;
                }
            };

            model.addColumn("Номер рейса");
            model.addColumn("Макс. вместимость");
            model.addColumn("Факт. вместимость");
            model.addColumn("С багажом");
            model.addColumn("Кол-во багажа");
            model.addColumn("Вес багажа");
            model.addColumn("Куплено билетов");
            model.addColumn("Исп. билетов");
            model.addColumn("Не пришли");
            model.addColumn("Без документов");
            model.addColumn("Без разрешения");
            model.addColumn("Депортировано");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("FlightID"),
                        rs.getInt("MaxCapcity"),
                        rs.getInt("RealCapacity"),
                        rs.getInt("WithBaggage"),
                        rs.getInt("BaggageItems"),
                        rs.getInt("BaggageWeight"),
                        rs.getInt("BoughtTickets"),
                        rs.getInt("PassedTickets"),
                        rs.getInt("PassengersNotCame"),
                        rs.getInt("HaveNotDocumets"),
                        rs.getInt("HaveNotPermission"),
                        rs.getInt("DeportedPassengers")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки пассажиров в рейсе: " + e.getMessage());
        }
    }

    private void showFlightStatuses() {
        currentTable = "FlightStatuses";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT StatusID, StatusName FROM FlightStatuses")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0: return Integer.class;
                        default: return String.class;
                    }
                }
            };

            model.addColumn("ID статуса");
            model.addColumn("Название статуса");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("StatusID"),
                        rs.getString("StatusName")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки статусов рейсов: " + e.getMessage());
        }
    }

    private void showSpecialPlanes() {
        currentTable = "SpecialPlanes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT s.PlaneID, " +
                             "s.SpecialFlightType, " +
                             "s.FlightClient, " +
                             "s.CrewSpecialRequirements, " +
                             "s.SpecialFuelRequired, " +
                             "s.AdditionalEquipment " +
                             "FROM SpecialPlanes s")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0: return Integer.class;
                        case 4: case 5: return Boolean.class;
                        default: return String.class;
                    }
                }
            };

            model.addColumn("Номер самолета");
            model.addColumn("Тип спецрейса");
            model.addColumn("Клиент");
            model.addColumn("Требования к экипажу");
            model.addColumn("Спец. топливо");
            model.addColumn("Доп. оборудование");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("PlaneID"),
                        rs.getString("SpecialFlightType"),
                        rs.getString("FlightClient"),
                        rs.getString("CrewSpecialRequirements"),
                        rs.getString("SpecialFuelRequired").equals("Y"),
                        rs.getString("AdditionalEquipment").equals("Y")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки спецсамолетов: " + e.getMessage());
        }
    }

    private void showPassengerPlanes() {
        currentTable = "PassengerPlanes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT p.PlaneID, " +
                             "p.MaxPassangers, " +
                             "p.AllLoadCapacity, " +
                             "p.BaggageLoadCapacity " +
                             "FROM PassengerPlanes p")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0: case 1: return Integer.class;
                        case 2: case 3: return Float.class;
                        default: return Object.class;
                    }
                }
            };

            model.addColumn("Номер самолета");
            model.addColumn("Макс. пассажиров");
            model.addColumn("Общая грузоподъемность");
            model.addColumn("Грузоподъемность багажа");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("PlaneID"),
                        rs.getInt("MaxPassangers"),
                        rs.getFloat("AllLoadCapacity"),
                        rs.getFloat("BaggageLoadCapacity")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки пассажирских самолетов: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }





}