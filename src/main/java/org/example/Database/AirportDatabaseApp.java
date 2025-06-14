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

public class AirportDatabaseApp {
    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTable table;
    private Connection connection;
    private String currentTable = "AirportStaff";

    public AirportDatabaseApp(Connection connection) {
        this.connection = connection;
        frame = new JFrame("Информация об аэропорте");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1500, 600);
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
                createActionButton("Персонал", e -> showAirportStaff()),
                createTableButton("Отделы", "Departments"),
                createTableButton("Бригады", "Teams"),
                createTableButton("Типы сотрудников", "TypeOfAirportStaff")
        );

        JPanel group2 = createAlignedGroup("Специализации:",
                createTableButton("Пилоты", "Pylots"),
                createTableButton("Диспетчеры", "Dispatchers"),
                createTableButton("Техники", "Technics"),
                createTableButton("Кассиры", "Cashiers"),
                createTableButton("Охрана", "SecurityStaff"),
                createTableButton("Справочная", "HelperDepartment")
        );

        JPanel group3 = createAlignedGroup("Справочники:",
                createTableButton("Уровни квалификации", "TypeOfQualificationLevel"),
                createTableButton("Типы лицензий", "TypeOfPylotLicense"),
                createTableButton("Уровни допуска", "ClearanceLevelsList")
        );

        JPanel group4 = createAlignedGroup("Дополнительно:",
                createTableButton("Медосмотры пилотов", "PylotsAndMedCheckUps"),
                createTableButton("Дети сотрудников", "Childrens"),
                createTableButton("Связи сотрудник-ребенок", "WorkersAndChildrens")
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
                case "AirportStaff":
                    deleteAirportStaff(selectedRow);
                    break;
                case "Departments":
                    deleteDepartment(selectedRow);
                    break;
                case "Teams":
                    deleteTeam(selectedRow);
                    break;
                case "TypeOfAirportStaff":
                    deleteTypeOfStaff(selectedRow);
                    break;
                case "Pylots":
                    deletePylot(selectedRow);
                    break;
                case "Dispatchers":
                    deleteDispatcher(selectedRow);
                    break;
                case "Technics":
                    deleteTechnic(selectedRow);
                    break;
                case "Cashiers":
                    deleteCashier(selectedRow);
                    break;
                case "SecurityStaff":
                    deleteSecurityStaff(selectedRow);
                    break;
                case "HelperDepartment":
                    deleteHelper(selectedRow);
                    break;
                case "TypeOfQualificationLevel":
                    deleteQualification(selectedRow);
                    break;
                case "TypeOfPylotLicense":
                    deleteLicense(selectedRow);
                    break;
                case "ClearanceLevelsList":
                    deleteClearance(selectedRow);
                    break;
                case "Childrens":
                    deleteChild(selectedRow);
                    break;
                case "WorkersAndChildrens":
                    deleteWorkerChild(selectedRow);
                    break;
                case "PylotsAndMedCheckUps":
                    deleteMedCheckup(selectedRow);
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
    private void deleteAirportStaff(int selectedRow) throws SQLException {
        // Получаем ФИО из выбранной строки (первый столбец)
        String fio = (String) table.getValueAt(selectedRow, 0);

        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM AirportStaff WHERE WorkerID = " +
                        "(SELECT WorkerID FROM AirportStaff WHERE FIO = ?)")) {
            stmt.setString(1, fio);
            stmt.executeUpdate();
        }
    }

    private void deleteDepartment(int selectedRow) throws SQLException {
        String departmentName = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Departments WHERE DepartmentName = ?")) {
            stmt.setString(1, departmentName);
            stmt.executeUpdate();
        }
    }

    private void deleteTeam(int selectedRow) throws SQLException {
        String teamName = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Teams WHERE TeamName = ?")) {
            stmt.setString(1, teamName);
            stmt.executeUpdate();
        }
    }

    private void deleteTypeOfStaff(int selectedRow) throws SQLException {
        String typeName = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM TypeOfAirportStaff WHERE TypeName = ?")) {
            stmt.setString(1, typeName);
            stmt.executeUpdate();
        }
    }

    private void deletePylot(int selectedRow) throws SQLException {
        String fio = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Pylots WHERE WorkerID = (SELECT WorkerID FROM AirportStaff WHERE FIO = ?)")) {
            stmt.setString(1, fio);
            stmt.executeUpdate();
        }
    }

    private void deleteDispatcher(int selectedRow) throws SQLException {
        String fio = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Dispatchers WHERE WorkerID = (SELECT WorkerID FROM AirportStaff WHERE FIO = ?)")) {
            stmt.setString(1, fio);
            stmt.executeUpdate();
        }
    }

    private void deleteTechnic(int selectedRow) throws SQLException {
        String fio = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Technics WHERE WorkerID = (SELECT WorkerID FROM AirportStaff WHERE FIO = ?)")) {
            stmt.setString(1, fio);
            stmt.executeUpdate();
        }
    }

    private void deleteCashier(int selectedRow) throws SQLException {
        String fio = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Cashiers WHERE WorkerID = (SELECT WorkerID FROM AirportStaff WHERE FIO = ?)")) {
            stmt.setString(1, fio);
            stmt.executeUpdate();
        }
    }

    private void deleteSecurityStaff(int selectedRow) throws SQLException {
        String fio = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM SecurityStaff WHERE WorkerID = (SELECT WorkerID FROM AirportStaff WHERE FIO = ?)")) {
            stmt.setString(1, fio);
            stmt.executeUpdate();
        }
    }

    private void deleteHelper(int selectedRow) throws SQLException {
        String fio = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM HelperDepartment WHERE WorkerID = (SELECT WorkerID FROM AirportStaff WHERE FIO = ?)")) {
            stmt.setString(1, fio);
            stmt.executeUpdate();
        }
    }

    private void deleteQualification(int selectedRow) throws SQLException {
        String qualName = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM TypeOfQualificationLevel WHERE name = ?")) {
            stmt.setString(1, qualName);
            stmt.executeUpdate();
        }
    }

    private void deleteLicense(int selectedRow) throws SQLException {
        String licenseName = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM TypeOfPylotLicense WHERE name = ?")) {
            stmt.setString(1, licenseName);
            stmt.executeUpdate();
        }
    }

    private void deleteClearance(int selectedRow) throws SQLException {
        String levelName = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM ClearanceLevelsList WHERE nameOfLevel = ?")) {
            stmt.setString(1, levelName);
            stmt.executeUpdate();
        }
    }

    private void deleteChild(int selectedRow) throws SQLException {
        String childName = (String) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Childrens WHERE ChildName = ?")) {
            stmt.setString(1, childName);
            stmt.executeUpdate();
        }
    }

    private void deleteWorkerChild(int selectedRow) throws SQLException {
        String workerName = (String) table.getValueAt(selectedRow, 0);
        String childName = (String) table.getValueAt(selectedRow, 1);

        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM WorkersAndChildrens WHERE " +
                        "WorkerID = (SELECT WorkerID FROM AirportStaff WHERE FIO = ?) AND " +
                        "ChildID = (SELECT ChildID FROM Childrens WHERE ChildName = ?)")) {
            stmt.setString(1, workerName);
            stmt.setString(2, childName);
            stmt.executeUpdate();
        }
    }

    private void deleteMedCheckup(int selectedRow) throws SQLException {
        String fio = (String) table.getValueAt(selectedRow, 0);
        Date checkupDate = (Date) table.getValueAt(selectedRow, 1);

        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM PylotsAndMedCheckUps WHERE " +
                        "PylotID = (SELECT WorkerID FROM AirportStaff WHERE FIO = ?) AND " +
                        "CheckUpDate = ?")) {
            stmt.setString(1, fio);
            stmt.setDate(2, checkupDate);
            stmt.executeUpdate();
        }
    }


    // метод для создания выровненных групп
    private JPanel createAlignedGroup(String labelText, JComponent... components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(150, label.getPreferredSize().height)); // Фиксированная ширина для выравнивания
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


            showTable("AirportStaff");

        } catch (Exception e) {
            showError("Ошибка инициализации базы данных: " + e.getMessage());
            System.exit(1);
        }
    }





    private void showAddRecordDialog(ActionEvent e) {
        switch (currentTable) {
            case "AirportStaff": showAddStaffDialog(); break;
            case "Departments": showAddDepartmentDialog(); break;
            case "Teams": showAddTeamDialog(); break;
            case "TypeOfAirportStaff": showAddTypeDialog(); break;
            case "Pylots": showAddPylotDialog(); break;
            case "Dispatchers": showAddDispatcherDialog(); break;
            case "Technics": showAddTechnicDialog(); break;
            case "Cashiers": showAddCashierDialog(); break;
            case "SecurityStaff": showAddSecurityStaffDialog(); break;
            case "HelperDepartment": showAddHelperDialog(); break;
            case "TypeOfQualificationLevel": showAddQualificationDialog(); break;
            case "TypeOfPylotLicense": showAddLicenseDialog(); break;
            case "PylotsAndMedCheckUps": showAddMedCheckupDialog(); break;
            case "ClearanceLevelsList": showAddClearanceDialog(); break;
            case "Childrens": showAddChildDialog(); break;
            case "WorkersAndChildrens": showAddWorkerChildDialog(); break;
            default: showError("Добавление записей в эту таблицу не реализовано");
        }
    }

    private void showAddLicenseDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();

        panel.add(new JLabel("Название лицензии:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить тип лицензии",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (nameField.getText().isEmpty() || nameField.getText().isEmpty()) {
                    throw new Exception("ID и название лицензии обязательны для заполнения");
                }
                int nextId = getNextId("Teams", "TeamID");

                String sql = "INSERT INTO TypeOfPylotLicense VALUES (?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, nextId);
                    stmt.setString(2, nameField.getText());

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении типа лицензии: " + ex.getMessage());
            }
        }
    }

    private void showAddClearanceDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();

        panel.add(new JLabel("Название уровня:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить уровень допуска",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (nameField.getText().isEmpty()) {
                    throw new Exception("ID и название уровня обязательны для заполнения");
                }
                int nextId = getNextId("Teams", "TeamID");

                String sql = "INSERT INTO ClearanceLevelsList VALUES (?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, nextId);
                    stmt.setString(2, nameField.getText());

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении уровня допуска: " + ex.getMessage());
            }
        }
    }

    private void showAddChildDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();
        JTextField birthDayField = new JTextField();

        birthDayField.setToolTipText("Формат: YYYY-MM-DD");

        panel.add(new JLabel("Имя ребенка:"));
        panel.add(nameField);
        panel.add(new JLabel("Дата рождения (YYYY-MM-DD):"));
        panel.add(birthDayField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить ребенка",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (nameField.getText().isEmpty() || birthDayField.getText().isEmpty()) {
                    throw new Exception("Все поля обязательны для заполнения");
                }
                int nextId = getNextId("Teams", "TeamID");

                String sql = "INSERT INTO Childrens VALUES (?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, nextId);
                    stmt.setString(2, nameField.getText());
                    stmt.setDate(3, Date.valueOf(birthDayField.getText()));

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении ребенка: " + ex.getMessage());
            }
        }
    }

    private void showAddQualificationDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();

        panel.add(new JLabel("Название уровня:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить уровень квалификации",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (nameField.getText().isEmpty()) {
                    throw new Exception("ID и название уровня обязательны для заполнения");
                }
                int nextId = getNextId("Teams", "TeamID");

                String sql = "INSERT INTO TypeOfQualificationLevel VALUES (?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, nextId);
                    stmt.setString(2, nameField.getText());

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении уровня квалификации: " + ex.getMessage());
            }
        }
    }

    private void showAddPylotDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Выпадающий список для WorkerID
        JComboBox<String> workerCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT WorkerID, FIO FROM AirportStaff WHERE WorkerType = 1")) {
            while (rs.next()) {
                workerCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки пилотов: " + ex.getMessage());
        }

        JCheckBox medCheckBox = new JCheckBox("Прошел медосмотр");

        // Выпадающий список для лицензии
        JComboBox<String> licenseCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM TypeOfPylotLicense")) {
            while (rs.next()) {
                licenseCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки типов лицензий: " + ex.getMessage());
        }

        JTextField flightHoursField = new JTextField();

        // Выпадающий список для уровня квалификации
        JComboBox<String> qualCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM TypeOfQualificationLevel")) {
            while (rs.next()) {
                qualCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки уровней квалификации: " + ex.getMessage());
        }

        panel.add(new JLabel("Сотрудник:"));
        panel.add(workerCombo);
        panel.add(new JLabel("Медосмотр:"));
        panel.add(medCheckBox);
        panel.add(new JLabel("Лицензия:"));
        panel.add(licenseCombo);
        panel.add(new JLabel("Налёт часов:"));
        panel.add(flightHoursField);
        panel.add(new JLabel("Уровень квалификации:"));
        panel.add(qualCombo);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить пилота",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (workerCombo.getSelectedItem() == null || flightHoursField.getText().isEmpty()) {
                    throw new Exception("Не все обязательные поля заполнены");
                }

                String sql = "INSERT INTO Pylots VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    // Извлекаем ID из выбранного значения комбобокса
                    String selectedWorker = (String) workerCombo.getSelectedItem();
                    int workerId = selectedWorker != null ?
                            Integer.parseInt(selectedWorker.split(" - ")[0]) : 0;
                    stmt.setInt(1, workerId);

                    stmt.setString(2, medCheckBox.isSelected() ? "Y" : "N");

                    String selectedLicense = (String) licenseCombo.getSelectedItem();
                    int licenseId = selectedLicense != null ?
                            Integer.parseInt(selectedLicense.split(" - ")[0]) : 0;
                    stmt.setInt(3, licenseId);

                    stmt.setInt(4, Integer.parseInt(flightHoursField.getText()));

                    String selectedQual = (String) qualCombo.getSelectedItem();
                    int qualId = selectedQual != null ?
                            Integer.parseInt(selectedQual.split(" - ")[0]) : 0;
                    stmt.setInt(5, qualId);

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении пилота: " + ex.getMessage());
            }
        }
    }

    private void showAddTypeDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();

        panel.add(new JLabel("Название типа:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить тип сотрудника",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (nameField.getText().isEmpty()) {
                    throw new Exception("ID и название типа обязательны для заполнения");
                }
                int nextId = getNextId("Teams", "TeamID");

                String sql = "INSERT INTO TypeOfAirportStaff VALUES (?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, nextId);
                    stmt.setString(2, nameField.getText());

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении типа сотрудника: " + ex.getMessage());
            }
        }
    }

    private void showAddTeamDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();

        panel.add(new JLabel("Название бригады:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить бригаду",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (nameField.getText().isEmpty() || nameField.getText().isEmpty()) {
                    throw new Exception("ID и название бригады обязательны для заполнения");
                }

                int nextId = getNextId("Teams", "TeamID");

                String sql = "INSERT INTO Teams VALUES (?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, nextId);
                    stmt.setString(2, nameField.getText());

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении бригады: " + ex.getMessage());
            }
        }
    }

    private void showAddDepartmentDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();
        JTextField locationField = new JTextField();

        panel.add(new JLabel("Название отдела:"));
        panel.add(nameField);
        panel.add(new JLabel("Местоположение:"));
        panel.add(locationField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить отдел",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (nameField.getText().isEmpty()) {
                    throw new Exception("Название отдела обязательно для заполнения");
                }

                // Get next available ID
                int nextId = getNextId("Departments", "DepartmentID");

                String sql = "INSERT INTO Departments VALUES (?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, nextId);
                    stmt.setString(2, nameField.getText());
                    stmt.setString(3, locationField.getText().isEmpty() ? null : locationField.getText());

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении отдела: " + ex.getMessage());
            }
        }
    }

    // Helper method to get next available ID
    private int getNextId(String tableName, String idColumn) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(" + idColumn + ") FROM " + tableName)) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
            return 1; // if table is empty
        }
    }

    private void showAddStaffDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField fioField = new JTextField();

        // Выпадающий список для DepartID
        JComboBox<String> departCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DepartmentID, DepartmentName FROM Departments")) {
            while (rs.next()) {
                departCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки отделов: " + ex.getMessage());
        }

        // Выпадающий список для WorkerType
        JComboBox<String> typeCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT IdOfType, TypeName FROM TypeOfAirportStaff")) {
            while (rs.next()) {
                typeCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки типов сотрудников: " + ex.getMessage());
        }

        // Выпадающий список для TeamID
        JComboBox<String> teamCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TeamID, TeamName FROM Teams")) {
            while (rs.next()) {
                teamCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки команд: " + ex.getMessage());
        }

        JTextField employmentField = new JTextField();
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"M", "F"});
        JTextField birthDayField = new JTextField();
        JTextField salaryField = new JTextField();

        // Добавляем подсказки для формата даты
        employmentField.setToolTipText("Формат: YYYY-MM-DD");
        birthDayField.setToolTipText("Формат: YYYY-MM-DD");

        panel.add(new JLabel("ФИО:"));
        panel.add(fioField);
        panel.add(new JLabel("Отдел:"));
        panel.add(departCombo);
        panel.add(new JLabel("Тип сотрудника:"));
        panel.add(typeCombo);
        panel.add(new JLabel("Бригада:"));
        panel.add(teamCombo);
        panel.add(new JLabel("Дата приема (YYYY-MM-DD):"));
        panel.add(employmentField);
        panel.add(new JLabel("Пол:"));
        panel.add(genderCombo);
        panel.add(new JLabel("Дата рождения (YYYY-MM-DD):"));
        panel.add(birthDayField);
        panel.add(new JLabel("Зарплата:"));
        panel.add(salaryField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить сотрудника",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                // Проверка обязательных полей
                if (fioField.getText().isEmpty() ||
                        employmentField.getText().isEmpty() || birthDayField.getText().isEmpty() ||
                        salaryField.getText().isEmpty()) {
                    throw new Exception("Все обязательные поля должны быть заполнены");
                }
                int nextId = getNextId("AirportStaff", "WorkerID");

                String sql = "INSERT INTO AirportStaff VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, nextId);
                    stmt.setString(2, fioField.getText());

                    // Извлекаем ID из выбранного значения комбобокса
                    String selectedDepart = (String) departCombo.getSelectedItem();
                    int departId = selectedDepart != null ?
                            Integer.parseInt(selectedDepart.split(" - ")[0]) : 0;
                    stmt.setInt(3, departId);

                    String selectedType = (String) typeCombo.getSelectedItem();
                    int typeId = selectedType != null ?
                            Integer.parseInt(selectedType.split(" - ")[0]) : 0;
                    stmt.setInt(4, typeId);

                    String selectedTeam = (String) teamCombo.getSelectedItem();
                    int teamId = selectedTeam != null ?
                            Integer.parseInt(selectedTeam.split(" - ")[0]) : 0;
                    stmt.setInt(5, teamId);

                    stmt.setDate(6, Date.valueOf(employmentField.getText()));
                    stmt.setString(7, (String) genderCombo.getSelectedItem());
                    stmt.setDate(8, Date.valueOf(birthDayField.getText()));
                    stmt.setInt(9, Integer.parseInt(salaryField.getText()));

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении: " + ex.getMessage());
            }
        }
    }

    private void showAddTechnicDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Выпадающий список для WorkerID
        JComboBox<String> workerCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT WorkerID, FIO FROM AirportStaff WHERE WorkerType = 3")) {
            while (rs.next()) {
                workerCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки техников: " + ex.getMessage());
        }

        // Выпадающий список для уровня допуска
        JComboBox<String> clearanceCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nameOfLevel FROM ClearanceLevelsList")) {
            while (rs.next()) {
                clearanceCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки уровней допуска: " + ex.getMessage());
        }

        JTextField specializationField = new JTextField();

        panel.add(new JLabel("Сотрудник:"));
        panel.add(workerCombo);
        panel.add(new JLabel("Уровень допуска:"));
        panel.add(clearanceCombo);
        panel.add(new JLabel("Специализация:"));
        panel.add(specializationField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить техника",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (workerCombo.getSelectedItem() == null) {
                    throw new Exception("Не выбран сотрудник");
                }

                String sql = "INSERT INTO Technics VALUES (?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    // Извлекаем ID из выбранного значения комбобокса
                    String selectedWorker = (String) workerCombo.getSelectedItem();
                    int workerId = selectedWorker != null ?
                            Integer.parseInt(selectedWorker.split(" - ")[0]) : 0;
                    stmt.setInt(1, workerId);

                    String selectedClearance = (String) clearanceCombo.getSelectedItem();
                    int clearanceId = selectedClearance != null ?
                            Integer.parseInt(selectedClearance.split(" - ")[0]) : 0;
                    stmt.setInt(2, clearanceId);

                    stmt.setString(3, specializationField.getText().isEmpty() ? null : specializationField.getText());

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении техника: " + ex.getMessage());
            }
        }
    }

    private void showAddCashierDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Выпадающий список для WorkerID
        JComboBox<String> workerCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT WorkerID, FIO FROM AirportStaff WHERE WorkerType = 4")) {
            while (rs.next()) {
                workerCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки кассиров: " + ex.getMessage());
        }

        JCheckBox customerServiceCheck = new JCheckBox("Опыт обслуживания");
        JCheckBox paymentSkillsCheck = new JCheckBox("Навыки оплаты");
        JCheckBox bookingKnowledgeCheck = new JCheckBox("Знание системы бронирования");

        panel.add(new JLabel("Сотрудник:"));
        panel.add(workerCombo);
        panel.add(new JLabel("Опыт обслуживания:"));
        panel.add(customerServiceCheck);
        panel.add(new JLabel("Навыки оплаты:"));
        panel.add(paymentSkillsCheck);
        panel.add(new JLabel("Знание системы:"));
        panel.add(bookingKnowledgeCheck);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить кассира",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (workerCombo.getSelectedItem() == null) {
                    throw new Exception("Не выбран сотрудник");
                }

                String sql = "INSERT INTO Cashiers VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    // Извлекаем ID из выбранного значения комбобокса
                    String selectedWorker = (String) workerCombo.getSelectedItem();
                    int workerId = selectedWorker != null ?
                            Integer.parseInt(selectedWorker.split(" - ")[0]) : 0;
                    stmt.setInt(1, workerId);

                    stmt.setString(2, customerServiceCheck.isSelected() ? "Y" : "N");
                    stmt.setString(3, paymentSkillsCheck.isSelected() ? "Y" : "N");
                    stmt.setString(4, bookingKnowledgeCheck.isSelected() ? "Y" : "N");

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении кассира: " + ex.getMessage());
            }
        }
    }

    private void showAddSecurityStaffDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Выпадающий список для WorkerID
        JComboBox<String> workerCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT WorkerID, FIO FROM AirportStaff WHERE WorkerType = 5")) {
            while (rs.next()) {
                workerCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки сотрудников охраны: " + ex.getMessage());
        }

        // Выпадающий список для уровня допуска
        JComboBox<String> clearanceCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nameOfLevel FROM ClearanceLevelsList")) {
            while (rs.next()) {
                clearanceCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки уровней допуска: " + ex.getMessage());
        }

        JCheckBox emergencyCheck = new JCheckBox("Навыки ЧС");
        JCheckBox cctvCheck = new JCheckBox("Опыт с CCTV");
        JCheckBox inspectionCheck = new JCheckBox("Навыки досмотра");

        panel.add(new JLabel("Сотрудник:"));
        panel.add(workerCombo);
        panel.add(new JLabel("Уровень допуска:"));
        panel.add(clearanceCombo);
        panel.add(new JLabel("Навыки ЧС:"));
        panel.add(emergencyCheck);
        panel.add(new JLabel("Опыт с CCTV:"));
        panel.add(cctvCheck);
        panel.add(new JLabel("Навыки досмотра:"));
        panel.add(inspectionCheck);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить сотрудника охраны",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (workerCombo.getSelectedItem() == null) {
                    throw new Exception("Не выбран сотрудник");
                }

                String sql = "INSERT INTO SecurityStaff VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    // Извлекаем ID из выбранного значения комбобокса
                    String selectedWorker = (String) workerCombo.getSelectedItem();
                    int workerId = selectedWorker != null ?
                            Integer.parseInt(selectedWorker.split(" - ")[0]) : 0;
                    stmt.setInt(1, workerId);

                    String selectedClearance = (String) clearanceCombo.getSelectedItem();
                    int clearanceId = selectedClearance != null ?
                            Integer.parseInt(selectedClearance.split(" - ")[0]) : 0;
                    stmt.setInt(2, clearanceId);

                    stmt.setString(3, emergencyCheck.isSelected() ? "Y" : "N");
                    stmt.setString(4, cctvCheck.isSelected() ? "Y" : "N");
                    stmt.setString(5, inspectionCheck.isSelected() ? "Y" : "N");

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении сотрудника охраны: " + ex.getMessage());
            }
        }
    }

    private void showAddHelperDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Выпадающий список для WorkerID
        JComboBox<String> workerCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT WorkerID, FIO FROM AirportStaff WHERE WorkerType = 5")) {
            while (rs.next()) {
                workerCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки сотрудников охраны: " + ex.getMessage());
        }

        // Выпадающий список для уровня допуска
        JComboBox<String> clearanceCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nameOfLevel FROM ClearanceLevelsList")) {
            while (rs.next()) {
                clearanceCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки уровней допуска: " + ex.getMessage());
        }

        JCheckBox emergencyCheck = new JCheckBox("Навыки ЧС");
        JCheckBox cctvCheck = new JCheckBox("Опыт с CCTV");
        JCheckBox inspectionCheck = new JCheckBox("Навыки досмотра");

        panel.add(new JLabel("Сотрудник:"));
        panel.add(workerCombo);
        panel.add(new JLabel("Уровень допуска:"));
        panel.add(clearanceCombo);
        panel.add(new JLabel("Навыки ЧС:"));
        panel.add(emergencyCheck);
        panel.add(new JLabel("Опыт с CCTV:"));
        panel.add(cctvCheck);
        panel.add(new JLabel("Навыки досмотра:"));
        panel.add(inspectionCheck);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить сотрудника охраны",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (workerCombo.getSelectedItem() == null) {
                    throw new Exception("Не выбран сотрудник");
                }

                String sql = "INSERT INTO SecurityStaff VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    // Извлекаем ID из выбранного значения комбобокса
                    String selectedWorker = (String) workerCombo.getSelectedItem();
                    int workerId = selectedWorker != null ?
                            Integer.parseInt(selectedWorker.split(" - ")[0]) : 0;
                    stmt.setInt(1, workerId);

                    String selectedClearance = (String) clearanceCombo.getSelectedItem();
                    int clearanceId = selectedClearance != null ?
                            Integer.parseInt(selectedClearance.split(" - ")[0]) : 0;
                    stmt.setInt(2, clearanceId);

                    stmt.setString(3, emergencyCheck.isSelected() ? "Y" : "N");
                    stmt.setString(4, cctvCheck.isSelected() ? "Y" : "N");
                    stmt.setString(5, inspectionCheck.isSelected() ? "Y" : "N");

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении сотрудника охраны: " + ex.getMessage());
            }
        }
    }

    private void showAddDispatcherDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Выпадающий список для WorkerID
        JComboBox<String> workerCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT WorkerID, FIO FROM AirportStaff WHERE WorkerType = 2")) {
            while (rs.next()) {
                workerCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки диспетчеров: " + ex.getMessage());
        }

        // Выпадающий список для уровня допуска
        JComboBox<String> clearanceCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nameOfLevel FROM ClearanceLevelsList")) {
            while (rs.next()) {
                clearanceCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки уровней допуска: " + ex.getMessage());
        }

        panel.add(new JLabel("Сотрудник:"));
        panel.add(workerCombo);
        panel.add(new JLabel("Уровень допуска:"));
        panel.add(clearanceCombo);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить диспетчера",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (workerCombo.getSelectedItem() == null) {
                    throw new Exception("Не выбран сотрудник");
                }

                String sql = "INSERT INTO Dispatchers VALUES (?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    // Извлекаем ID из выбранного значения комбобокса
                    String selectedWorker = (String) workerCombo.getSelectedItem();
                    int workerId = selectedWorker != null ?
                            Integer.parseInt(selectedWorker.split(" - ")[0]) : 0;
                    stmt.setInt(1, workerId);

                    String selectedClearance = (String) clearanceCombo.getSelectedItem();
                    int clearanceId = selectedClearance != null ?
                            Integer.parseInt(selectedClearance.split(" - ")[0]) : 0;
                    stmt.setInt(2, clearanceId);

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении диспетчера: " + ex.getMessage());
            }
        }
    }

    private void showAddMedCheckupDialog() {

    }

    private void showAddWorkerChildDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Выпадающий список для WorkerID
        JComboBox<String> workerCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT WorkerID, FIO FROM AirportStaff")) {
            while (rs.next()) {
                workerCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки сотрудников: " + ex.getMessage());
        }

        // Выпадающий список для ChildID
        JComboBox<String> childCombo = new JComboBox<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ChildID, ChildName FROM Childrens")) {
            while (rs.next()) {
                childCombo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки детей: " + ex.getMessage());
        }

        panel.add(new JLabel("Сотрудник:"));
        panel.add(workerCombo);
        panel.add(new JLabel("Ребенок:"));
        panel.add(childCombo);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Добавить связь сотрудник-ребенок",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                if (workerCombo.getSelectedItem() == null || childCombo.getSelectedItem() == null) {
                    throw new Exception("Не выбраны сотрудник и/или ребенок");
                }

                String sql = "INSERT INTO WorkersAndChildrens VALUES (?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    // Извлекаем ID из выбранного значения комбобокса
                    String selectedWorker = (String) workerCombo.getSelectedItem();
                    int workerId = selectedWorker != null ?
                            Integer.parseInt(selectedWorker.split(" - ")[0]) : 0;
                    stmt.setInt(1, workerId);

                    String selectedChild = (String) childCombo.getSelectedItem();
                    int childId = selectedChild != null ?
                            Integer.parseInt(selectedChild.split(" - ")[0]) : 0;
                    stmt.setInt(2, childId);

                    stmt.executeUpdate();
                    refreshData();
                }
            } catch (Exception ex) {
                showError("Ошибка при добавлении связи: " + ex.getMessage());
            }
        }
    }



    private void showTable(String tableName) {
        currentTable = tableName;

        switch (tableName) {
            case "AirportStaff" -> showAirportStaff();
            case "TypeOfAirportStaff" -> showTypeOfAirportStaff();
            case "Departments" -> showDepartments();
            case "Teams" -> showTeams();
            case "Pylots" -> showPylots();
            case "Dispatchers" -> showDispatchers();
            case "Technics" -> showTechnics();
            case "Cashiers" -> showCashiers();
            case "SecurityStaff" -> showSecurityStaff();
            case "HelperDepartment" -> showHelperDepartment();
            case "TypeOfQualificationLevel" -> showQualificationLevels();
            case "TypeOfPylotLicense" -> showPylotLicenses();
            case "ClearanceLevelsList" -> showClearanceLevels();
            case "Childrens" -> showChildrens();
            case "WorkersAndChildrens" -> showWorkersChildren();
            case "PylotsAndMedCheckUps" -> showPylotsMedCheckups();
            default -> showGenericTable(tableName);
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

            updateTableModel(rs);
            adjustColumnWidths();

        } catch (SQLException e) {
            showError("Ошибка загрузки: " + e.getMessage());
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


    private void showPylots() {
        currentTable = "Pylots";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT " +
                             "s.FIO, " +
                             "CASE WHEN EXISTS (SELECT 1 FROM PylotsAndMedCheckUps m " +
                             "     WHERE m.PylotID = p.WorkerID AND m.Passed = 'Y' " +
                             "     AND m.CheckUpDate >= DATEADD('YEAR', -1, CURRENT_DATE())) " +
                             "THEN 'Да' ELSE 'Нет' END AS MedCheckStatus, " +
                             "l.name AS LicenseName, " +
                             "p.FlightHours, " +
                             "q.name AS QualificationName " +
                             "FROM Pylots p " +
                             "JOIN AirportStaff s ON p.WorkerID = s.WorkerID " +
                             "LEFT JOIN TypeOfPylotLicense l ON p.PylotLicense = l.id " +
                             "LEFT JOIN TypeOfQualificationLevel q ON p.Qualification_Level = q.id")) {

            // Создаем модель таблицы
            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 3: return Integer.class; // FlightHours
                        default: return String.class;
                    }
                }
            };

            // Добавляем столбцы
            model.addColumn("ФИО пилота");
            model.addColumn("Медосмотр актуален");
            model.addColumn("Тип лицензии");
            model.addColumn("Налёт часов");
            model.addColumn("Уровень квалификации");

            // Заполняем данными
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("FIO"),
                        rs.getString("MedCheckStatus"),
                        rs.getString("LicenseName"),
                        rs.getInt("FlightHours"),
                        rs.getString("QualificationName")
                });
            }

            table.setModel(model);

            // Настраиваем сортировку
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            // Устанавливаем компараторы
            sorter.setComparator(0, String.CASE_INSENSITIVE_ORDER); // ФИО
            sorter.setComparator(1, String.CASE_INSENSITIVE_ORDER); // Медосмотр
            sorter.setComparator(2, String.CASE_INSENSITIVE_ORDER); // Лицензия
            sorter.setComparator(3, Comparator.naturalOrder());      // Налёт часов
            sorter.setComparator(4, String.CASE_INSENSITIVE_ORDER); // Квалификация

            // Настраиваем ширину столбцов
            table.getColumnModel().getColumn(0).setPreferredWidth(300); // ФИО
            table.getColumnModel().getColumn(0).setMinWidth(300);
            table.getColumnModel().getColumn(0).setMaxWidth(300);
            table.getColumnModel().getColumn(1).setPreferredWidth(120);  // Медосмотр
            table.getColumnModel().getColumn(1).setMinWidth(120);
            table.getColumnModel().getColumn(1).setMaxWidth(120);
            table.getColumnModel().getColumn(2).setPreferredWidth(150);  // Лицензия
            table.getColumnModel().getColumn(2).setMinWidth(150);
            table.getColumnModel().getColumn(2).setMaxWidth(150);
            table.getColumnModel().getColumn(3).setPreferredWidth(80);   // Часы
            table.getColumnModel().getColumn(3).setMinWidth(80);
            table.getColumnModel().getColumn(3).setMaxWidth(80);
            table.getColumnModel().getColumn(4).setPreferredWidth(150);  // Квалификация
            table.getColumnModel().getColumn(4).setMinWidth(150);
            table.getColumnModel().getColumn(4).setMaxWidth(150);

            // Общие настройки таблицы
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setRowHeight(25);
            table.getTableHeader().setReorderingAllowed(false);
            table.setAutoCreateRowSorter(true);

        } catch (SQLException e) {
            showError("Ошибка загрузки данных пилотов: " + e.getMessage());
        }
    }
    private void showQualificationLevels() {
        currentTable = "TypeOfQualificationLevel";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM TypeOfQualificationLevel ORDER BY id")) {

            // Создаем модель таблицы
            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0: return Integer.class; // ID
                        default: return String.class; // Название
                    }
                }
            };

            // Добавляем столбцы
            model.addColumn("Уровень квалификации");

            // Заполняем данными
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("name")
                });
            }

            table.setModel(model);

            // Настраиваем сортировку
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            // Настраиваем ширину столбцов
            table.getColumnModel().getColumn(0).setPreferredWidth(200);  // Название
            table.getColumnModel().getColumn(0).setMinWidth(200);
            table.getColumnModel().getColumn(0).setMaxWidth(200);

            // Общие настройки таблицы
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setRowHeight(25);
            table.getTableHeader().setReorderingAllowed(false);
            table.setAutoCreateRowSorter(true);

        } catch (SQLException e) {
            showError("Ошибка загрузки уровней квалификации: " + e.getMessage());
        }
    }
    private void showPylotLicenses() {
        currentTable = "TypeOfPylotLicense";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM TypeOfPylotLicense ORDER BY id")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Integer.class : String.class;
                }
            };

            model.addColumn("Тип лицензии");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("name")
                });
            }

            table.setModel(model);

            // Настраиваем сортировку
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            // Настраиваем ширину столбцов
            table.getColumnModel().getColumn(0).setPreferredWidth(200);
            table.getColumnModel().getColumn(0).setMinWidth(200);
            table.getColumnModel().getColumn(0).setMaxWidth(200);
            table.getColumnModel().getColumn(1).setPreferredWidth(200);
            table.getColumnModel().getColumn(1).setMinWidth(200);
            table.getColumnModel().getColumn(1).setMaxWidth(200);


            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки типов лицензий: " + e.getMessage());
        }
    }
    private void showClearanceLevels() {
        currentTable = "ClearanceLevelsList";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nameOfLevel FROM ClearanceLevelsList ORDER BY id")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Integer.class : String.class;
                }
            };

            model.addColumn("Уровень допуска");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("nameOfLevel")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки уровней допуска: " + e.getMessage());
        }
    }
    private void showChildrens() {
        currentTable = "Childrens";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT c.ChildName, c.ChildBirthDay " +
                             "FROM Childrens c ORDER BY c.ChildName")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0: return Integer.class;
                        case 1: return Date.class;
                        default: return String.class;
                    }
                }
            };

            model.addColumn("Имя ребенка");
            model.addColumn("Дата рождения");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("ChildName"),
                        rs.getDate("ChildBirthDay")
                });
            }

            table.setModel(model);

            // Настраиваем сортировку
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            // Настраиваем ширину столбцов
            table.getColumnModel().getColumn(0).setPreferredWidth(300);
            table.getColumnModel().getColumn(0).setMinWidth(300);
            table.getColumnModel().getColumn(0).setMaxWidth(300);
            table.getColumnModel().getColumn(1).setPreferredWidth(200);
            table.getColumnModel().getColumn(1).setMinWidth(200);
            table.getColumnModel().getColumn(1).setMaxWidth(200);

            configureTableWithDates();
        } catch (SQLException e) {
            showError("Ошибка загрузки данных о детях: " + e.getMessage());
        }
    }
    private void showWorkersChildren() {
        currentTable = "WorkersAndChildrens";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT s.FIO AS WorkerName, c.ChildName " +
                             "FROM WorkersAndChildrens wc " +
                             "JOIN AirportStaff s ON wc.WorkerID = s.WorkerID " +
                             "JOIN Childrens c ON wc.ChildID = c.ChildID " +
                             "ORDER BY s.FIO")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return String.class;
                }
            };

            model.addColumn("Сотрудник");
            model.addColumn("Ребенок");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("WorkerName"),
                        rs.getString("ChildName")
                });
            }

            table.setModel(model);

            // Настраиваем сортировку
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            // Настраиваем ширину столбцов
            table.getColumnModel().getColumn(0).setPreferredWidth(300);
            table.getColumnModel().getColumn(0).setMinWidth(300);
            table.getColumnModel().getColumn(0).setMaxWidth(300);
            table.getColumnModel().getColumn(1).setPreferredWidth(300);
            table.getColumnModel().getColumn(1).setMinWidth(300);
            table.getColumnModel().getColumn(1).setMaxWidth(300);

            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки связей сотрудник-ребенок: " + e.getMessage());
        }
    }




    private void configureTable() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) table.getModel());
        table.setRowSorter(sorter);

        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);

        // Настройка ширины столбцов
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(200);
        }
    }

    private void configureTableWithDates() {
        configureTable();

        // Форматирование дат
        table.setDefaultRenderer(Date.class, new DefaultTableCellRenderer() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Date) {
                    value = sdf.format((Date)value);
                }
                return super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);
            }
        });
    }
    private void showPylotsMedCheckups() {
        currentTable = "PylotsAndMedCheckUps";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT s.FIO, " +
                             "m.CheckUpDate, " +
                             "CASE WHEN m.Passed = 'Y' THEN 'Пройден' ELSE 'Не пройден' END AS Status " +
                             "FROM PylotsAndMedCheckUps m " +
                             "JOIN AirportStaff s ON m.PylotID = s.WorkerID " +
                             "ORDER BY m.CheckUpDate DESC")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return switch (columnIndex) {
                        case 1 -> Date.class; // Дата медосмотра
                        default -> String.class;
                    };
                }
            };

            // Добавляем столбцы
            model.addColumn("ФИО пилота");
            model.addColumn("Дата медосмотра");
            model.addColumn("Статус");

            // Заполняем данными
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("FIO"),
                        rs.getDate("CheckUpDate"),
                        rs.getString("Status")
                });
            }

            table.setModel(model);

            // Настраиваем сортировку
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            // Компараторы для разных типов данных
            sorter.setComparator(0, String.CASE_INSENSITIVE_ORDER); // ФИО
            sorter.setComparator(1, Comparator.naturalOrder());     // Дата
            sorter.setComparator(2, String.CASE_INSENSITIVE_ORDER); // Статус

            // Настраиваем ширину столбцов
            table.getColumnModel().getColumn(0).setPreferredWidth(250); // ФИО
            table.getColumnModel().getColumn(0).setMinWidth(300);
            table.getColumnModel().getColumn(0).setMaxWidth(300);
            table.getColumnModel().getColumn(1).setPreferredWidth(120); // Дата
            table.getColumnModel().getColumn(1).setMinWidth(120);
            table.getColumnModel().getColumn(1).setMaxWidth(120);
            table.getColumnModel().getColumn(2).setPreferredWidth(100); // Статус
            table.getColumnModel().getColumn(2).setMinWidth(100);
            table.getColumnModel().getColumn(2).setMaxWidth(100);

            // Форматирование дат
            table.setDefaultRenderer(Date.class, new DefaultTableCellRenderer() {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int column) {
                    if (value instanceof Date) {
                        value = sdf.format((Date)value);
                    }
                    return super.getTableCellRendererComponent(table, value, isSelected,
                            hasFocus, row, column);
                }
            });

            // Общие настройки таблицы
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setRowHeight(25);
            table.getTableHeader().setReorderingAllowed(false);

        } catch (SQLException e) {
            showError("Ошибка загрузки данных медосмотров: " + e.getMessage());
        }
    }
    private void showDispatchers() {
        currentTable = "Dispatchers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT s.FIO, cl.nameOfLevel AS ClearanceLevel " +
                             "FROM Dispatchers d " +
                             "JOIN AirportStaff s ON d.WorkerID = s.WorkerID " +
                             "JOIN ClearanceLevelsList cl ON d.ClearanceLevel = cl.id " +
                             "ORDER BY s.FIO")) {

            // Создаем модель таблицы
            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return String.class; // Все столбцы текстовые
                }
            };

            // Добавляем столбцы
            model.addColumn("ФИО диспетчера");
            model.addColumn("Уровень допуска");

            // Заполняем данными
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("FIO"),
                        rs.getString("ClearanceLevel")
                });
            }

            table.setModel(model);

            // Настраиваем сортировку
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            // Устанавливаем компараторы
            sorter.setComparator(0, String.CASE_INSENSITIVE_ORDER); // ФИО
            sorter.setComparator(1, String.CASE_INSENSITIVE_ORDER); // Уровень допуска

            // Настраиваем ширину столбцов
            table.getColumnModel().getColumn(0).setPreferredWidth(300); // ФИО
            table.getColumnModel().getColumn(0).setMinWidth(300);
            table.getColumnModel().getColumn(0).setMaxWidth(300);
            table.getColumnModel().getColumn(1).setPreferredWidth(200); // Уровень допуска
            table.getColumnModel().getColumn(1).setMinWidth(200);
            table.getColumnModel().getColumn(1).setMaxWidth(200);

            // Общие настройки таблицы
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setRowHeight(25);
            table.getTableHeader().setReorderingAllowed(false);
            table.setAutoCreateRowSorter(true);

        } catch (SQLException e) {
            showError("Ошибка загрузки данных диспетчеров: " + e.getMessage());
        }
    }
    private void showTechnics() {
        currentTable = "Technics";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT s.FIO, cl.nameOfLevel AS ClearanceLevel, t.Specialization " +
                             "FROM Technics t " +
                             "JOIN AirportStaff s ON t.WorkerID = s.WorkerID " +
                             "LEFT JOIN ClearanceLevelsList cl ON t.ClearanceLevel = cl.id " +
                             "ORDER BY s.FIO")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return String.class;
                }
            };

            model.addColumn("ФИО техника");
            model.addColumn("Уровень допуска");
            model.addColumn("Специализация");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("FIO"),
                        rs.getString("ClearanceLevel"),
                        rs.getString("Specialization")
                });
            }

            table.setModel(model);

            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            sorter.setComparator(0, String.CASE_INSENSITIVE_ORDER);
            sorter.setComparator(1, String.CASE_INSENSITIVE_ORDER);
            sorter.setComparator(2, String.CASE_INSENSITIVE_ORDER);

            table.getColumnModel().getColumn(0).setPreferredWidth(250);
            table.getColumnModel().getColumn(0).setMinWidth(250);
            table.getColumnModel().getColumn(0).setMaxWidth(250);
            table.getColumnModel().getColumn(1).setPreferredWidth(150);
            table.getColumnModel().getColumn(1).setMinWidth(150);
            table.getColumnModel().getColumn(1).setMaxWidth(150);
            table.getColumnModel().getColumn(2).setPreferredWidth(200);
            table.getColumnModel().getColumn(2).setMinWidth(200);
            table.getColumnModel().getColumn(2).setMaxWidth(200);

            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setRowHeight(25);
            table.getTableHeader().setReorderingAllowed(false);

        } catch (SQLException e) {
            showError("Ошибка загрузки данных техников: " + e.getMessage());
        }
    }
    private void showCashiers() {
        currentTable = "Cashiers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT s.FIO, " +
                             "CASE WHEN c.CustomerServiceExperience = 'Y' THEN 'Да' ELSE 'Нет' END AS CustomerService, " +
                             "CASE WHEN c.PaymentProcessingSkills = 'Y' THEN 'Да' ELSE 'Нет' END AS PaymentSkills, " +
                             "CASE WHEN c.BookingSystemKnowledge = 'Y' THEN 'Да' ELSE 'Нет' END AS BookingKnowledge " +
                             "FROM Cashiers c " +
                             "JOIN AirportStaff s ON c.WorkerID = s.WorkerID " +
                             "ORDER BY s.FIO")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return String.class;
                }
            };

            model.addColumn("ФИО кассира");
            model.addColumn("Опыт обслуживания");
            model.addColumn("Навыки оплаты");
            model.addColumn("Знание системы");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("FIO"),
                        rs.getString("CustomerService"),
                        rs.getString("PaymentSkills"),
                        rs.getString("BookingKnowledge")
                });
            }

            table.setModel(model);

            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            for (int i = 0; i < table.getColumnCount(); i++) {
                sorter.setComparator(i, String.CASE_INSENSITIVE_ORDER);
            }

            table.getColumnModel().getColumn(0).setPreferredWidth(300);
            table.getColumnModel().getColumn(0).setMinWidth(300);
            table.getColumnModel().getColumn(0).setMaxWidth(300);
            table.getColumnModel().getColumn(1).setPreferredWidth(120);
            table.getColumnModel().getColumn(1).setMinWidth(120);
            table.getColumnModel().getColumn(1).setMaxWidth(120);
            table.getColumnModel().getColumn(2).setPreferredWidth(120);
            table.getColumnModel().getColumn(2).setMinWidth(120);
            table.getColumnModel().getColumn(2).setMaxWidth(120);
            table.getColumnModel().getColumn(3).setPreferredWidth(120);
            table.getColumnModel().getColumn(3).setMinWidth(120);
            table.getColumnModel().getColumn(3).setMaxWidth(120);

            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setRowHeight(25);
            table.getTableHeader().setReorderingAllowed(false);

        } catch (SQLException e) {
            showError("Ошибка загрузки данных кассиров: " + e.getMessage());
        }
    }
    private void showSecurityStaff() {
        currentTable = "SecurityStaff";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT s.FIO, " +
                             "cl.nameOfLevel AS ClearanceLevel, " +
                             "CASE WHEN ss.EmergencyResponseSkills = 'Y' THEN 'Да' ELSE 'Нет' END AS EmergencySkills, " +
                             "CASE WHEN ss.CCTVExperience = 'Y' THEN 'Да' ELSE 'Нет' END AS CCTVExp, " +
                             "CASE WHEN ss.InspectionSkills = 'Y' THEN 'Да' ELSE 'Нет' END AS InspectionSkills " +
                             "FROM SecurityStaff ss " +
                             "JOIN AirportStaff s ON ss.WorkerID = s.WorkerID " +
                             "LEFT JOIN ClearanceLevelsList cl ON ss.ClearanceLevel = cl.id " +
                             "ORDER BY s.FIO")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return String.class;
                }
            };

            model.addColumn("ФИО сотрудника");
            model.addColumn("Уровень допуска");
            model.addColumn("Навыки ЧС");
            model.addColumn("Опыт с CCTV");
            model.addColumn("Навыки досмотра");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("FIO"),
                        rs.getString("ClearanceLevel"),
                        rs.getString("EmergencySkills"),
                        rs.getString("CCTVExp"),
                        rs.getString("InspectionSkills")
                });
            }

            table.setModel(model);

            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            for (int i = 0; i < table.getColumnCount(); i++) {
                sorter.setComparator(i, String.CASE_INSENSITIVE_ORDER);
            }

            table.getColumnModel().getColumn(0).setPreferredWidth(250);
            table.getColumnModel().getColumn(0).setMinWidth(250);
            table.getColumnModel().getColumn(0).setMaxWidth(250);
            table.getColumnModel().getColumn(1).setPreferredWidth(150);
            table.getColumnModel().getColumn(1).setMinWidth(150);
            table.getColumnModel().getColumn(1).setMaxWidth(150);
            table.getColumnModel().getColumn(2).setPreferredWidth(100);
            table.getColumnModel().getColumn(2).setMinWidth(100);
            table.getColumnModel().getColumn(2).setMaxWidth(100);
            table.getColumnModel().getColumn(3).setPreferredWidth(100);
            table.getColumnModel().getColumn(3).setMinWidth(100);
            table.getColumnModel().getColumn(3).setMaxWidth(100);
            table.getColumnModel().getColumn(4).setPreferredWidth(120);
            table.getColumnModel().getColumn(4).setMinWidth(120);
            table.getColumnModel().getColumn(4).setMaxWidth(120);

            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setRowHeight(25);
            table.getTableHeader().setReorderingAllowed(false);

        } catch (SQLException e) {
            showError("Ошибка загрузки данных службы безопасности: " + e.getMessage());
        }
    }
    private void showHelperDepartment() {
        currentTable = "HelperDepartment";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT s.FIO, " +
                             "CASE WHEN hd.SchedulingSkills = 'Y' THEN 'Да' ELSE 'Нет' END AS Scheduling, " +
                             "CASE WHEN hd.HRProcessesKnowledge = 'Y' THEN 'Да' ELSE 'Нет' END AS HRKnowledge " +
                             "FROM HelperDepartment hd " +
                             "JOIN AirportStaff s ON hd.WorkerID = s.WorkerID " +
                             "ORDER BY s.FIO")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return String.class;
                }
            };

            model.addColumn("ФИО сотрудника");
            model.addColumn("Навыки планирования");
            model.addColumn("Знание HR-процессов");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("FIO"),
                        rs.getString("Scheduling"),
                        rs.getString("HRKnowledge")
                });
            }

            table.setModel(model);

            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            for (int i = 0; i < table.getColumnCount(); i++) {
                sorter.setComparator(i, String.CASE_INSENSITIVE_ORDER);
            }

            table.getColumnModel().getColumn(0).setPreferredWidth(250);
            table.getColumnModel().getColumn(0).setMinWidth(250);
            table.getColumnModel().getColumn(0).setMaxWidth(250);
            table.getColumnModel().getColumn(1).setPreferredWidth(150);
            table.getColumnModel().getColumn(1).setMinWidth(150);
            table.getColumnModel().getColumn(1).setMaxWidth(150);
            table.getColumnModel().getColumn(2).setPreferredWidth(150);
            table.getColumnModel().getColumn(2).setMinWidth(150);
            table.getColumnModel().getColumn(2).setMaxWidth(150);

            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setRowHeight(25);
            table.getTableHeader().setReorderingAllowed(false);

        } catch (SQLException e) {
            showError("Ошибка загрузки данных справочной службы: " + e.getMessage());
        }
    }


    private void showTypeOfAirportStaff() {
        currentTable = "TypeOfAirportStaff";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT TypeName FROM TypeOfAirportStaff")) {

            // Создаем модель таблицы
            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }
            };

            // Добавляем столбцы
            model.addColumn("Название должности работника аэропорта");

            // Заполняем данными
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("TypeName")
                });
            }

            table.setModel(model);

            // Настраиваем сортировку
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            // Устанавливаем компараторы для разных типов данных
            sorter.setComparator(0, String.CASE_INSENSITIVE_ORDER);

            // Настраиваем ширину столбцов
            table.getColumnModel().getColumn(0).setPreferredWidth(300);
            table.getColumnModel().getColumn(0).setMinWidth(300);
            table.getColumnModel().getColumn(0).setMaxWidth(300);

            // Общие настройки таблицы
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setRowHeight(25);
            table.getTableHeader().setReorderingAllowed(false);
            table.setAutoCreateRowSorter(true);
        } catch (SQLException e) {
            showError("Ошибка загрузки данных: " + e.getMessage());
        }
    }
    private void showDepartments() {
        currentTable = "Departments";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT DepartmentName, Location FROM Departments" )) {

            // Создаем модель таблицы
            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }
            };

            // Добавляем столбцы
            model.addColumn("Название департамента");
            model.addColumn("Расположение департамента");

            // Заполняем данными
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("DepartmentName"),
                        rs.getString("Location")
                });
            }

            table.setModel(model);

            // Настраиваем сортировку
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            // Устанавливаем компараторы для разных типов данных
            sorter.setComparator(0, String.CASE_INSENSITIVE_ORDER);
            sorter.setComparator(1, String.CASE_INSENSITIVE_ORDER);

            // Настраиваем ширину столбцов
            table.getColumnModel().getColumn(0).setPreferredWidth(300);
            table.getColumnModel().getColumn(0).setMinWidth(300);
            table.getColumnModel().getColumn(0).setMaxWidth(300);
            table.getColumnModel().getColumn(1).setPreferredWidth(300);
            table.getColumnModel().getColumn(1).setMinWidth(300);
            table.getColumnModel().getColumn(1).setMaxWidth(300);

            // Общие настройки таблицы
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setRowHeight(25);
            table.getTableHeader().setReorderingAllowed(false);
            table.setAutoCreateRowSorter(true);
        } catch (SQLException e) {
            showError("Ошибка загрузки данных: " + e.getMessage());
        }
    }
    private void showTeams() {
        currentTable = "Teams";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT TeamName FROM Teams" )) {

            // Создаем модель таблицы
            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }
            };

            // Добавляем столбцы
            model.addColumn("Название бригады");

            // Заполняем данными
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("TeamName")
                });
            }

            table.setModel(model);

            // Настраиваем сортировку
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            // Устанавливаем компараторы для разных типов данных
            sorter.setComparator(0, String.CASE_INSENSITIVE_ORDER);

            // Настраиваем ширину столбцов
            table.getColumnModel().getColumn(0).setPreferredWidth(300);
            table.getColumnModel().getColumn(0).setMinWidth(300);
            table.getColumnModel().getColumn(0).setMaxWidth(300);

            // Общие настройки таблицы
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setRowHeight(25);
            table.getTableHeader().setReorderingAllowed(false);
            table.setAutoCreateRowSorter(true);
        } catch (SQLException e) {
            showError("Ошибка загрузки данных: " + e.getMessage());
        }
    }

    private void showAirportStaff() {
        currentTable = "AirportStaff";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT a.FIO, " +
                             "d.DepartmentName AS Department, " +
                             "t.TypeName AS WorkerType, " +
                             "tm.TeamName AS Team, " +
                             "a.Salary, " +
                             "a.Employment, " +
                             "a.BirthDay, " +
                             "a.Gender " +
                             "FROM AirportStaff a " +
                             "LEFT JOIN Departments d ON a.DepartID = d.DepartmentID " +
                             "LEFT JOIN TypeOfAirportStaff t ON a.WorkerType = t.IdOfType " +
                             "LEFT JOIN Teams tm ON a.TeamID = tm.TeamID " +
                             "ORDER BY a.FIO")) {

            // Создаем модель таблицы
            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 4: return Integer.class; // Salary
                        case 5: case 6: return Date.class; // Employment, BirthDay
                        default: return String.class;
                    }
                }
            };

            // Добавляем столбцы
            model.addColumn("ФИО");
            model.addColumn("Департамент");
            model.addColumn("Тип сотрудника");
            model.addColumn("Команда");
            model.addColumn("Зарплата");
            model.addColumn("Дата приема");
            model.addColumn("Дата рождения");
            model.addColumn("Пол");

            // Заполняем данными
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("FIO"),
                        rs.getString("Department"),
                        rs.getString("WorkerType"),
                        rs.getString("Team"),
                        rs.getInt("Salary"),
                        rs.getDate("Employment"),
                        rs.getDate("BirthDay"),
                        rs.getString("Gender")
                });
            }

            table.setModel(model);

            // Настраиваем сортировку
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            // Устанавливаем компараторы для разных типов данных
            sorter.setComparator(0, String.CASE_INSENSITIVE_ORDER); // ФИО
            sorter.setComparator(1, String.CASE_INSENSITIVE_ORDER); // Департамент
            sorter.setComparator(2, String.CASE_INSENSITIVE_ORDER); // Тип сотрудника
            sorter.setComparator(3, String.CASE_INSENSITIVE_ORDER); // Команда
            sorter.setComparator(4, Comparator.naturalOrder());      // Зарплата
            sorter.setComparator(5, Comparator.naturalOrder());      // Дата приема
            sorter.setComparator(6, Comparator.naturalOrder());      // Дата рождения

            // Настраиваем ширину столбцов
            table.getColumnModel().getColumn(0).setPreferredWidth(300); // ФИО
            table.getColumnModel().getColumn(0).setMinWidth(300);
            table.getColumnModel().getColumn(0).setMaxWidth(300);
            table.getColumnModel().getColumn(1).setPreferredWidth(200); // Департамент
            table.getColumnModel().getColumn(1).setMinWidth(200);
            table.getColumnModel().getColumn(1).setMaxWidth(200);
            table.getColumnModel().getColumn(2).setPreferredWidth(200); // Тип сотрудника
            table.getColumnModel().getColumn(2).setMinWidth(200);
            table.getColumnModel().getColumn(2).setMaxWidth(200);
            table.getColumnModel().getColumn(3).setPreferredWidth(200); // Команда
            table.getColumnModel().getColumn(3).setMinWidth(200);
            table.getColumnModel().getColumn(3).setMaxWidth(200);
            table.getColumnModel().getColumn(4).setPreferredWidth(100);  // Зарплата
            table.getColumnModel().getColumn(4).setMinWidth(100);
            table.getColumnModel().getColumn(4).setMaxWidth(100);
            table.getColumnModel().getColumn(5).setPreferredWidth(150); // Дата приема
            table.getColumnModel().getColumn(5).setMinWidth(150);
            table.getColumnModel().getColumn(5).setMaxWidth(150);
            table.getColumnModel().getColumn(6).setPreferredWidth(150); // Дата рождения
            table.getColumnModel().getColumn(6).setMinWidth(150);
            table.getColumnModel().getColumn(6).setMaxWidth(150);
            table.getColumnModel().getColumn(7).setPreferredWidth(50);  // Пол
            table.getColumnModel().getColumn(7).setMinWidth(50);
            table.getColumnModel().getColumn(7).setMaxWidth(50);

            // Форматирование дат
            table.setDefaultRenderer(Date.class, new DefaultTableCellRenderer() {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int column) {
                    if (value instanceof Date) {
                        value = sdf.format((Date)value);
                    }
                    return super.getTableCellRendererComponent(table, value, isSelected,
                            hasFocus, row, column);
                }
            });

            // Общие настройки таблицы
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setRowHeight(25);
            table.getTableHeader().setReorderingAllowed(false);
            table.setAutoCreateRowSorter(true);

        } catch (SQLException e) {
            showError("Ошибка загрузки данных: " + e.getMessage());
        }
    }

    private void updateTableModel(ResultSet rs) throws SQLException {
        tableModel.setRowCount(0);
        ResultSetMetaData meta = rs.getMetaData();

        // Установка заголовков
        Vector<String> columns = new Vector<>();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            columns.add(meta.getColumnName(i));
        }
        tableModel.setColumnIdentifiers(columns);

        // Заполнение данных
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row.add(rs.getObject(i));
            }
            tableModel.addRow(row);
        }
    }

    private void adjustColumnWidths() {
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(120);
        }
    }

    private void refreshData() {
        showTable(currentTable);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    // Вспомогательный класс для диалогов ввода
    private static class InputDialog {
        private final JDialog dialog;
        private final JPanel panel;
        private final java.util.List<JTextField> fields = new java.util.ArrayList<>();

        public InputDialog(JFrame parent, String title) {
            dialog = new JDialog(parent, title, true);
            panel = new JPanel(new GridLayout(0, 2));

            // Добавляем кнопки OK/Cancel
            JButton okButton = new JButton("OK");
            JButton cancelButton = new JButton("Отмена");

            okButton.addActionListener(e -> {
                dialog.setVisible(false);
                dialog.dispose();
            });

            cancelButton.addActionListener(e -> {
                dialog.setVisible(false);
                dialog.dispose();
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);

            panel.add(buttonPanel, BorderLayout.SOUTH);
        }

        public InputDialog addField(String label, String type) {
            panel.add(new JLabel(label));
            JTextField field = new JTextField(20);
            panel.add(field);
            fields.add(field);
            return this;
        }

        public boolean showDialog() {
            dialog.setLayout(new BorderLayout());
            dialog.add(panel, BorderLayout.CENTER);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            return true;
        }

        public String getField(int index) {
            return fields.get(index).getText();
        }

        public void setStatementValues(PreparedStatement stmt) throws Exception {
            for (int i = 0; i < fields.size(); i++) {
                String text = fields.get(i).getText();
                switch (i) {
                    case 0: stmt.setInt(1, Integer.parseInt(text)); break;
                    case 1: stmt.setString(2, text); break;
                    // остальное
                }
            }
        }
    }
}