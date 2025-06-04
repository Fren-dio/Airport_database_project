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

@SuppressWarnings("ALL")
public class AirplaneDatabaseApp {
    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTable table;
    private Connection connection;
    private String currentTable = "Planes";

    public AirplaneDatabaseApp(Connection connection) {
        this.connection = connection;
        frame = new JFrame("Информация о самолетах");
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
                createActionButton("Добавить запись", this::showAddRecordDialog),
                createActionButton("Удалить запись", this::showDeleteRecordDialog)
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

    private void showAddRecordDialog(ActionEvent actionEvent) {
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void initDatabase() {
        try {
            showTable("Planes");
            // Проверка, что данные загружаются
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Planes")) {
                if (rs.next()) {
                    System.out.println("Total planes in database: " + rs.getInt(1));
                }
            }
        } catch (Exception e) {
            showError("Ошибка инициализации базы данных: " + e.getMessage());
            System.exit(1);
        }
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
                case "Planes":
                    deletePlane(selectedRow);
                    break;
                case "PlaneTypeCharacteristics":
                    deletePlaneType(selectedRow);
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
                case "PlanesAndTechCheckUps":
                    deleteTechCheckup(selectedRow);
                    break;
                case "PlanesAndRepairCheckUps":
                    deleteRepairCheckup(selectedRow);
                    break;
                case "PlanesAndOilFillings":
                    deleteOilFilling(selectedRow);
                    break;
                case "PlanesAndFoodSupplies":
                    deleteFoodSupply(selectedRow);
                    break;
                case "ReadyStatuses":
                    deleteReadyStatus(selectedRow);
                    break;
                case "FlightStatuses":
                    deleteFlightStatus(selectedRow);
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

    private void refreshData() {
        showTable(currentTable);
    }

    private void deletePlane(int selectedRow) throws SQLException {
        int planeId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Planes WHERE PlanesID = ?")) {
            stmt.setInt(1, planeId);
            stmt.executeUpdate();
        }
    }

    private void deletePlaneType(int selectedRow) throws SQLException {
        int typeId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM PlaneTypeCharacteristics WHERE TypeID = ?")) {
            stmt.setInt(1, typeId);
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

    private void deleteTechCheckup(int selectedRow) throws SQLException {
        int planeId = (int) table.getValueAt(selectedRow, 0);
        Date checkupDate = (Date) table.getValueAt(selectedRow, 1);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM PlanesAndTechCheckUps WHERE PlaneID = ? AND TechCheckUpDate = ?")) {
            stmt.setInt(1, planeId);
            stmt.setTimestamp(2, new Timestamp(checkupDate.getTime()));
            stmt.executeUpdate();
        }
    }

    private void deleteRepairCheckup(int selectedRow) throws SQLException {
        int planeId = (int) table.getValueAt(selectedRow, 0);
        Date checkupDate = (Date) table.getValueAt(selectedRow, 1);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM PlanesAndRepairCheckUps WHERE PlaneID = ? AND RepairCheckUpDate = ?")) {
            stmt.setInt(1, planeId);
            stmt.setTimestamp(2, new Timestamp(checkupDate.getTime()));
            stmt.executeUpdate();
        }
    }

    private void deleteOilFilling(int selectedRow) throws SQLException {
        int planeId = (int) table.getValueAt(selectedRow, 0);
        Date fillingDate = (Date) table.getValueAt(selectedRow, 1);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM PlanesAndOilFillings WHERE PlaneID = ? AND Date = ?")) {
            stmt.setInt(1, planeId);
            stmt.setTimestamp(2, new Timestamp(fillingDate.getTime()));
            stmt.executeUpdate();
        }
    }

    private void deleteFoodSupply(int selectedRow) throws SQLException {
        int planeId = (int) table.getValueAt(selectedRow, 0);
        Date supplyDate = (Date) table.getValueAt(selectedRow, 1);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM PlanesAndFoodSupplies WHERE PlaneID = ? AND Date = ?")) {
            stmt.setInt(1, planeId);
            stmt.setTimestamp(2, new Timestamp(supplyDate.getTime()));
            stmt.executeUpdate();
        }
    }

    private void deleteReadyStatus(int selectedRow) throws SQLException {
        int statusId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM ReadyStatuses WHERE StatusID = ?")) {
            stmt.setInt(1, statusId);
            stmt.executeUpdate();
        }
    }

    private void deleteFlightStatus(int selectedRow) throws SQLException {
        int statusId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM FlightStatuses WHERE StatusID = ?")) {
            stmt.setInt(1, statusId);
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

    private void showTable(String tableName) {
        currentTable = tableName;

        switch (tableName) {
            case "Planes":
                showPlanes();
                break;
            case "PlaneTypeCharacteristics":
                showPlaneTypes();
                break;
            case "PassengerPlanes":
                showPassengerPlanes();
                break;
            case "CargoPlanes":
                showCargoPlanes();
                break;
            case "SpecialPlanes":
                showSpecialPlanes();
                break;
            case "PlanesAndTechCheckUps":
                showTechCheckups();
                break;
            case "PlanesAndRepairCheckUps":
                showRepairCheckups();
                break;
            case "PlanesAndOilFillings":
                showOilFillings();
                break;
            case "PlanesAndFoodSupplies":
                showFoodSupplies();
                break;
            case "ReadyStatuses":
                showReadyStatuses();
                break;
            case "FlightStatuses":
                showFlightStatuses();
                break;
            case "PlanesAndReadyStatuses":
                showPlanesReadyStatuses();
                break;
            case "PlanesAndFlightStatuses":
                showPlanesFlightStatuses();
                break;
            default:
                showGenericTable(tableName);
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


    private void configureTable() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) table.getModel());
        table.setRowSorter(sorter);

        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setReorderingAllowed(false);

        // Автоматическая настройка ширины столбцов
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(150);
        }
    }

    private void configureTableWithDates() {
        configureTable();
        // Специальный рендерер для дат
        table.setDefaultRenderer(Date.class, new DefaultTableCellRenderer() {
            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Date) {
                    value = f.format((Date)value);
                }
                return super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);
            }
        });
    }

    private void showPlanes() {
        currentTable = "Planes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT p.PlanesID, a.Name AS Airport, pt.ModelName, " +
                             "CASE WHEN ps.PlaneID IS NOT NULL THEN 'Пассажирский' " +
                             "WHEN cs.PlaneID IS NOT NULL THEN 'Грузовой' " +
                             "WHEN sp.PlaneID IS NOT NULL THEN 'Специальный' " +
                             "ELSE 'Неизвестный' END AS PlaneType " +
                             "FROM Planes p " +
                             "LEFT JOIN Airports a ON p.HomeAirportID = a.AirportID " +
                             "LEFT JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID " +
                             "LEFT JOIN PassengerPlanes ps ON p.PlanesID = ps.PlaneID " +
                             "LEFT JOIN CargoPlanes cs ON p.PlanesID = cs.PlaneID " +
                             "LEFT JOIN SpecialPlanes sp ON p.PlanesID = sp.PlaneID")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Integer.class : String.class;
                }
            };

            model.addColumn("ID");
            model.addColumn("Аэропорт");
            model.addColumn("Модель");
            model.addColumn("Тип");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("PlanesID"),
                        rs.getString("Airport"),
                        rs.getString("ModelName"),
                        rs.getString("PlaneType")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки данных о самолетах: " + e.getMessage());
        }
    }

    private void showPlaneTypes() {
        currentTable = "PlaneTypeCharacteristics";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT ModelName, VendorID, MaxSpeed, MediumSpeed, MaxFlightDistance, " +
                             "FuelTankCapacity, CanRefuelingInAir, MinRanwaylenght, NormalRanwaylenght, " +
                             "MaxFlightAltitude, StandartFlightAltitude, fuelConsumption " +
                             "FROM PlaneTypeCharacteristics")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0:
                        case 1:
                            return String.class;
                        default:
                            return Float.class;
                    }
                }
            };

            model.addColumn("Модель");
            model.addColumn("Производитель");
            model.addColumn("Макс. скорость");
            model.addColumn("Ср. скорость");
            model.addColumn("Макс. дистанция");
            model.addColumn("Объем бака");
            model.addColumn("Дозаправка в воздухе");
            model.addColumn("Мин. длина ВПП");
            model.addColumn("Норм. длина ВПП");
            model.addColumn("Макс. высота");
            model.addColumn("Рабочая высота");
            model.addColumn("Расход топлива");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("ModelName"),
                        rs.getString("VendorID"),
                        rs.getFloat("MaxSpeed"),
                        rs.getFloat("MediumSpeed"),
                        rs.getFloat("MaxFlightDistance"),
                        rs.getFloat("FuelTankCapacity"),
                        rs.getString("CanRefuelingInAir").equals("Y") ? "Да" : "Нет",
                        rs.getFloat("MinRanwaylenght"),
                        rs.getFloat("NormalRanwaylenght"),
                        rs.getFloat("MaxFlightAltitude"),
                        rs.getFloat("StandartFlightAltitude"),
                        rs.getFloat("fuelConsumption")
                });
            }

            table.setModel(model);
            adjustColumnWidths();
        } catch (SQLException e) {
            showError("Ошибка загрузки данных о типах самолетов: " + e.getMessage());
        }
    }

    private void adjustColumnWidths() {
    }

    private void showPassengerPlanes() {
        currentTable = "PassengerPlanes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT p.PlaneID, pt.ModelName, p.MaxPassangers, p.AllLoadCapacity, p.BaggageLoadCapacity " +
                             "FROM PassengerPlanes p " +
                             "JOIN Planes pl ON p.PlaneID = pl.PlanesID " +
                             "JOIN PlaneTypeCharacteristics pt ON pl.PlaneTypeID = pt.TypeID")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Integer.class : columnIndex == 1 ? String.class : Float.class;
                }
            };

            model.addColumn("ID");
            model.addColumn("Модель");
            model.addColumn("Макс. пассажиров");
            model.addColumn("Общая загрузка");
            model.addColumn("Загрузка багажа");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("PlaneID"),
                        rs.getString("ModelName"),
                        rs.getInt("MaxPassangers"),
                        rs.getFloat("AllLoadCapacity"),
                        rs.getFloat("BaggageLoadCapacity")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки данных о пассажирских самолетах: " + e.getMessage());
        }
    }

    private void showCargoPlanes() {
        currentTable = "CargoPlanes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT p.PlaneID, pt.ModelName, p.MaxLoad, p.MaxVolume, " +
                             "p.CompartmentLength, p.CompartmentWeight, p.CompartmentHeight, " +
                             "p.TrapdoorLenght, p.TrapdoorWeight, p.TrapdoorHeight " +
                             "FROM CargoPlanes p " +
                             "JOIN Planes pl ON p.PlaneID = pl.PlanesID " +
                             "JOIN PlaneTypeCharacteristics pt ON pl.PlaneTypeID = pt.TypeID")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Integer.class : columnIndex == 1 ? String.class : Float.class;
                }
            };

            model.addColumn("ID");
            model.addColumn("Модель");
            model.addColumn("Макс. загрузка");
            model.addColumn("Макс. объем");
            model.addColumn("Длина отсека");
            model.addColumn("Вес отсека");
            model.addColumn("Высота отсека");
            model.addColumn("Длина люка");
            model.addColumn("Вес люка");
            model.addColumn("Высота люка");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("PlaneID"),
                        rs.getString("ModelName"),
                        rs.getFloat("MaxLoad"),
                        rs.getFloat("MaxVolume"),
                        rs.getFloat("CompartmentLength"),
                        rs.getFloat("CompartmentWeight"),
                        rs.getFloat("CompartmentHeight"),
                        rs.getFloat("TrapdoorLenght"),
                        rs.getFloat("TrapdoorWeight"),
                        rs.getFloat("TrapdoorHeight")
                });
            }

            table.setModel(model);
            adjustColumnWidths();
        } catch (SQLException e) {
            showError("Ошибка загрузки данных о грузовых самолетах: " + e.getMessage());
        }
    }

    private void showSpecialPlanes() {
        currentTable = "SpecialPlanes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT p.PlaneID, pt.ModelName, p.SpecialFlightType, p.FlightClient, " +
                             "p.CrewSpecialRequirements, p.SpecialFuelRequired, p.AdditionalEquipment " +
                             "FROM SpecialPlanes p " +
                             "JOIN Planes pl ON p.PlaneID = pl.PlanesID " +
                             "JOIN PlaneTypeCharacteristics pt ON pl.PlaneTypeID = pt.TypeID")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Integer.class : String.class;
                }
            };

            model.addColumn("ID");
            model.addColumn("Модель");
            model.addColumn("Тип полета");
            model.addColumn("Клиент");
            model.addColumn("Требования к экипажу");
            model.addColumn("Спец. топливо");
            model.addColumn("Доп. оборудование");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("PlaneID"),
                        rs.getString("ModelName"),
                        rs.getString("SpecialFlightType"),
                        rs.getString("FlightClient"),
                        rs.getString("CrewSpecialRequirements"),
                        rs.getString("SpecialFuelRequired").equals("Y") ? "Да" : "Нет",
                        rs.getString("AdditionalEquipment").equals("Y") ? "Да" : "Нет"
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки данных о спец. самолетах: " + e.getMessage());
        }
    }

    private void showTechCheckups() {
        currentTable = "PlanesAndTechCheckUps";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT p.PlanesID, pt.ModelName, t.TechCheckUpDate, " +
                             "CASE WHEN t.Passed = 'Y' THEN 'Пройден' ELSE 'Не пройден' END AS Status " +
                             "FROM PlanesAndTechCheckUps t " +
                             "JOIN Planes p ON t.PlaneID = p.PlanesID " +
                             "JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID " +
                             "ORDER BY t.TechCheckUpDate DESC")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Integer.class : columnIndex == 2 ? Date.class : String.class;
                }
            };

            model.addColumn("ID");
            model.addColumn("Модель");
            model.addColumn("Дата проверки");
            model.addColumn("Статус");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("PlanesID"),
                        rs.getString("ModelName"),
                        rs.getDate("TechCheckUpDate"),
                        rs.getString("Status")
                });
            }

            table.setModel(model);
            configureTableWithDates();
        } catch (SQLException e) {
            showError("Ошибка загрузки данных о техосмотрах: " + e.getMessage());
        }
    }


    private void showRepairCheckups() {
        currentTable = "PlanesAndRepairCheckUps";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT p.PlanesID, pt.ModelName, r.RepairCheckUpDate, " +
                             "CASE WHEN r.Passed = 'Y' THEN 'Пройден' ELSE 'Не пройден' END AS Status " +
                             "FROM PlanesAndRepairCheckUps r " +
                             "JOIN Planes p ON r.PlaneID = p.PlanesID " +
                             "JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID " +
                             "ORDER BY r.RepairCheckUpDate DESC")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Integer.class : columnIndex == 2 ? Date.class : String.class;
                }
            };

            model.addColumn("ID");
            model.addColumn("Модель");
            model.addColumn("Дата ремонта");
            model.addColumn("Статус");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("PlanesID"),
                        rs.getString("ModelName"),
                        rs.getDate("RepairCheckUpDate"),
                        rs.getString("Status")
                });
            }

            table.setModel(model);
            configureTableWithDates();
        } catch (SQLException e) {
            showError("Ошибка загрузки данных о ремонтах: " + e.getMessage());
        }
    }

    private void showOilFillings() {
        currentTable = "PlanesAndOilFillings";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT p.PlanesID, pt.ModelName, o.Date, o.TypeOfOil, o.CapcityOfOil " +
                             "FROM PlanesAndOilFillings o " +
                             "JOIN Planes p ON o.PlaneID = p.PlanesID " +
                             "JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID " +
                             "ORDER BY o.Date DESC")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0:
                            return Integer.class;
                        case 2:
                            return Date.class;
                        case 4:
                            return Integer.class;
                        default:
                            return String.class;
                    }
                }
            };

            model.addColumn("ID");
            model.addColumn("Модель");
            model.addColumn("Дата заправки");
            model.addColumn("Тип топлива");
            model.addColumn("Количество");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("PlanesID"),
                        rs.getString("ModelName"),
                        rs.getDate("Date"),
                        rs.getInt("TypeOfOil") == 1 ? "Авиакеросин" : "Другое",
                        rs.getInt("CapcityOfOil")
                });
            }

            table.setModel(model);
            configureTableWithDates();
        } catch (SQLException e) {
            showError("Ошибка загрузки данных о заправках: " + e.getMessage());
        }
    }

    private void showFoodSupplies() {
        currentTable = "PlanesAndFoodSupplies";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT p.PlanesID, pt.ModelName, f.Date, f.TypeOfSuppling, f.Amount " +
                             "FROM PlanesAndFoodSupplies f " +
                             "JOIN Planes p ON f.PlaneID = p.PlanesID " +
                             "JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID " +
                             "ORDER BY f.Date DESC")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0:
                            return Integer.class;
                        case 2:
                            return Date.class;
                        case 4:
                            return Integer.class;
                        default:
                            return String.class;
                    }
                }
            };

            model.addColumn("ID");
            model.addColumn("Модель");
            model.addColumn("Дата поставки");
            model.addColumn("Тип поставки");
            model.addColumn("Количество");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("PlanesID"),
                        rs.getString("ModelName"),
                        rs.getDate("Date"),
                        rs.getString("TypeOfSuppling"),
                        rs.getInt("Amount")
                });
            }

            table.setModel(model);
            configureTableWithDates();
        } catch (SQLException e) {
            showError("Ошибка загрузки данных о поставках питания: " + e.getMessage());
        }
    }

    private void showReadyStatuses() {
        currentTable = "ReadyStatuses";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT StatusID, StatusName FROM ReadyStatuses")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Integer.class : String.class;
                }
            };

            model.addColumn("ID");
            model.addColumn("Статус готовности");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("StatusID"),
                        rs.getString("StatusName")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки статусов готовности: " + e.getMessage());
        }
    }

    private void showFlightStatuses() {
        currentTable = "FlightStatuses";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT StatusID, StatusName FROM FlightStatuses")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Integer.class : String.class;
                }
            };

            model.addColumn("ID");
            model.addColumn("Статус полета");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("StatusID"),
                        rs.getString("StatusName")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки статусов полета: " + e.getMessage());
        }
    }

    private void showPlanesReadyStatuses() {
        currentTable = "PlanesAndReadyStatuses";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT p.PlanesID, pt.ModelName, rs.StatusName, pr.UpdateTime " +
                             "FROM PlanesAndReadyStatuses pr " +
                             "JOIN Planes p ON pr.PlaneID = p.PlanesID " +
                             "JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID " +
                             "JOIN ReadyStatuses rs ON pr.Status = rs.StatusID " +
                             "ORDER BY pr.UpdateTime DESC")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Integer.class : columnIndex == 3 ? Date.class : String.class;
                }
            };

            model.addColumn("ID");
            model.addColumn("Модель");
            model.addColumn("Статус готовности");
            model.addColumn("Время обновления");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("PlanesID"),
                        rs.getString("ModelName"),
                        rs.getString("StatusName"),
                        rs.getTimestamp("UpdateTime")
                });
            }

            table.setModel(model);
            configureTableWithDates();
        } catch (SQLException e) {
            showError("Ошибка загрузки статусов готовности самолетов: " + e.getMessage());
        }
    }

    private void showPlanesFlightStatuses() {
        currentTable = "PlanesAndFlightStatuses";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT p.PlanesID, pt.ModelName, fs.StatusName, pf.UpdateTime " +
                             "FROM PlanesAndFlightStatuses pf " +
                             "JOIN Planes p ON pf.PlaneID = p.PlanesID " +
                             "JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID " +
                             "JOIN FlightStatuses fs ON pf.Status = fs.StatusID " +
                             "ORDER BY pf.UpdateTime DESC")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Integer.class : columnIndex == 3 ? Date.class : String.class;
                }
            };

            model.addColumn("ID");
            model.addColumn("Модель");
            model.addColumn("Статус полета");
            model.addColumn("Время обновления");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("PlanesID"),
                        rs.getString("ModelName"),
                        rs.getString("StatusName"),
                        rs.getTimestamp("UpdateTime")
                });
            }

            table.setModel(model);
            configureTableWithDates();
        } catch (SQLException e) {
            showError("Ошибка загрузки статусов полета самолетов: " + e.getMessage());
        }
    }

}