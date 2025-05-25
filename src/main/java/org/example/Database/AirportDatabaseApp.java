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

    public AirportDatabaseApp() {
        frame = new JFrame("Информация об аэропорте");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1200, 600);
        frame.setLocationRelativeTo(null);

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
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(group5);

        // Добавляем панель с кнопками и таблицу
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        frame.add(mainPanel);
    }

    // Новый метод для создания выровненных групп
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
        button.addActionListener(e -> showTable(tableName));
        return button;
    }

    private JButton createActionButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    private void initDatabase() {
        try {
            // Регистрация драйвера H2
            Class.forName("org.h2.Driver");

            // Подключение без авторизации
            connection = DriverManager.getConnection(
                    "jdbc:h2:./airportDB;DB_CLOSE_DELAY=-1",
                    "",
                    "");

            createTables();
            initializeData();

            showTable("AirportStaff");

        } catch (Exception e) {
            showError("Ошибка инициализации базы данных: " + e.getMessage());
            System.exit(1);
        }
    }



    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // 1. Создаем основные таблицы
            stmt.execute("CREATE TABLE IF NOT EXISTS TypeOfAirportStaff (" +
                    "IdOfType INT PRIMARY KEY, TypeName VARCHAR(50) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Departments (" +
                    "DepartmentID INT PRIMARY KEY, DepartmentName VARCHAR(100) NOT NULL, Location VARCHAR(100))");

            stmt.execute("CREATE TABLE IF NOT EXISTS Teams (" +
                    "TeamID INT PRIMARY KEY, TeamName VARCHAR(100) NOT NULL)");

            // персонал аэропорта
            stmt.execute("CREATE TABLE IF NOT EXISTS AirportStaff (" +
                    "WorkerID INT PRIMARY KEY, " +
                    "FIO VARCHAR(100) NOT NULL, " +
                    "DepartID INT REFERENCES Departments(DepartmentID), " +
                    "WorkerType INT REFERENCES TypeOfAirportStaff(IdOfType), " +
                    "TeamID INT REFERENCES Teams(TeamID), " +
                    "Employment DATE, " +
                    "Gender CHAR(1), " +
                    "BirthDay DATE, " +
                    "Salary INT)");

            // 2. Создаем справочные таблицы для специализаций
            stmt.execute("CREATE TABLE IF NOT EXISTS TypeOfQualificationLevel (" +
                    "id INT PRIMARY KEY, name VARCHAR(400) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS TypeOfPylotLicense (" +
                    "id INT PRIMARY KEY, name VARCHAR(400) NOT NULL UNIQUE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS ClearanceLevelsList (" +
                    "id INT PRIMARY KEY, nameOfLevel VARCHAR(200) NOT NULL UNIQUE)");

            // 3. Создаем таблицы для специализаций сотрудников

            // 3.1 Пилоты
            stmt.execute("CREATE TABLE IF NOT EXISTS Pylots (" +
                    "WorkerID INT PRIMARY KEY REFERENCES AirportStaff(WorkerID), " +
                    "MedCheckUp CHAR(1) DEFAULT 'N', " +
                    "PylotLicense INT REFERENCES TypeOfPylotLicense(id), " +
                    "FlightHours INT, " +
                    "Qualification_Level INT REFERENCES TypeOfQualificationLevel(id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS PylotsAndMedCheckUps (" +
                    "PylotID INT REFERENCES Pylots(WorkerID), " +
                    "CheckUpDate TIMESTAMP, " +
                    "Passed CHAR(1) DEFAULT 'N', " +
                    "PRIMARY KEY (PylotID, CheckUpDate))");

            // 3.2 Диспетчеры
            stmt.execute("CREATE TABLE IF NOT EXISTS Dispatchers (" +
                    "WorkerID INT PRIMARY KEY REFERENCES AirportStaff(WorkerID), " +
                    "ClearanceLevel INT REFERENCES ClearanceLevelsList(id))");

            // 3.3 Техники
            stmt.execute("CREATE TABLE IF NOT EXISTS Technics (" +
                    "WorkerID INT PRIMARY KEY REFERENCES AirportStaff(WorkerID), " +
                    "ClearanceLevel INT REFERENCES ClearanceLevelsList(id), " +
                    "Specialization VARCHAR(400))");

            // 3.4 Кассиры
            stmt.execute("CREATE TABLE IF NOT EXISTS Cashiers (" +
                    "WorkerID INT PRIMARY KEY REFERENCES AirportStaff(WorkerID), " +
                    "CustomerServiceExperience CHAR(1) DEFAULT 'N', " +
                    "PaymentProcessingSkills CHAR(1) DEFAULT 'N', " +
                    "BookingSystemKnowledge CHAR(1) DEFAULT 'N')");

            // 3.5 Служба безопасности
            stmt.execute("CREATE TABLE IF NOT EXISTS SecurityStaff (" +
                    "WorkerID INT PRIMARY KEY REFERENCES AirportStaff(WorkerID), " +
                    "ClearanceLevel INT REFERENCES ClearanceLevelsList(id), " +
                    "EmergencyResponseSkills CHAR(1) DEFAULT 'N', " +
                    "CCTVExperience CHAR(1) DEFAULT 'N', " +
                    "InspectionSkills CHAR(1) DEFAULT 'N')");

            // 3.6 Справочная служба
            stmt.execute("CREATE TABLE IF NOT EXISTS HelperDepartment (" +
                    "WorkerID INT PRIMARY KEY REFERENCES AirportStaff(WorkerID), " +
                    "SchedulingSkills CHAR(1) DEFAULT 'N', " +
                    "HRProcessesKnowledge CHAR(1) DEFAULT 'N')");

            // 4. Создаем таблицы для детей сотрудников
            stmt.execute("CREATE TABLE IF NOT EXISTS Childrens (" +
                    "ChildID INT PRIMARY KEY, ChildName VARCHAR(100) NOT NULL, ChildBirthDay DATE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS WorkersAndChildrens (" +
                    "WorkerID INT REFERENCES AirportStaff(WorkerID), " +
                    "ChildID INT REFERENCES Childrens(ChildID), " +
                    "PRIMARY KEY (WorkerID, ChildID))");
        }
    }


    private void initializeData() throws SQLException {
        if (isTableEmpty("AirportStaff")) {
            insertTestData();
        }
    }

    private boolean isTableEmpty(String tableName) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private void insertTestData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // 1. Заполняем справочники
            stmt.execute("INSERT INTO TypeOfAirportStaff VALUES " +
                    "(1,'Пилот'),(2,'Диспетчер'),(3,'Техник'),(4,'Кассир')," +
                    "(5,'Сотрудник безопасности'),(6,'Сотрудник справочной')");

            stmt.execute("INSERT INTO TypeOfQualificationLevel VALUES " +
                    "(1,'Junior'),(2,'Middle'),(3,'Senior'),(4,'Lead'),(5,'Expert')");

            stmt.execute("INSERT INTO TypeOfPylotLicense VALUES " +
                    "(1,'Частный пилот (PPL)'),(2,'Коммерческий пилот (CPL)')," +
                    "(3,'Линейный пилот (ATPL)'),(4,'Пилот-инструктор')");

            stmt.execute("INSERT INTO ClearanceLevelsList VALUES " +
                    "(1,'Базовый'),(2,'Средний'),(3,'Высокий'),(4,'Максимальный')");

            // 2. Заполняем отделы
            stmt.execute("INSERT INTO Departments VALUES " +
                    "(1,'Летный отдел','Терминал A, 2 этаж')," +
                    "(2,'Диспетчерская служба','Башня управления')," +
                    "(3,'Техническая служба','Ангар 1')," +
                    "(4,'Кассы и регистрация','Терминал B, 1 этаж')," +
                    "(5,'Служба безопасности','Терминал C, КПП')");

            // 3. Заполняем команды
            stmt.execute("INSERT INTO Teams VALUES " +
                    "(1,'Экипаж 101',1),(2,'Экипаж 202',3),(3,'Диспетчеры смены A',5)," +
                    "(4,'Техническая бригада 1',7),(5,'Кассиры утренней смены',9)," +
                    "(6,'Охрана терминала B',11)");

            // 4. Заполняем основных сотрудников
            stmt.execute("INSERT INTO AirportStaff VALUES " +
                    // Пилоты
                    "(1,'Иванов Иван Иванович',1,1,1,'2015-06-15','M','1980-05-10',250000)," +
                    "(2,'Петров Петр Петрович',1,1,1,'2016-03-10','M','1982-11-22',230000)," +
                    "(3,'Сидорова Мария Сергеевна',1,1,2,'2017-08-22','F','1985-07-15',220000)," +
                    // Диспетчеры
                    "(4,'Кузнецов Алексей Дмитриевич',2,2,3,'2018-05-14','M','1990-02-28',150000)," +
                    "(5,'Николаева Елена Викторовна',2,2,3,'2019-01-30','F','1992-09-17',140000)," +
                    // Техники
                    "(6,'Васильев Дмитрий Олегович',3,3,4,'2016-11-05','M','1988-04-25',120000)," +
                    "(7,'Григорьева Анна Павловна',3,3,4,'2017-09-18','F','1991-12-10',110000)," +
                    // Кассиры
                    "(8,'Смирнова Ольга Игоревна',4,4,5,'2018-07-22','F','1993-03-15',90000)," +
                    "(9,'Федоров Михаил Андреевич',4,4,5,'2019-04-10','M','1994-08-20',95000)," +
                    // Охрана
                    "(10,'Алексеев Сергей Владимирович',5,5,6,'2017-02-15','M','1989-06-12',100000)," +
                    "(11,'Дмитриева Татьяна Николаевна',5,5,6,'2018-11-28','F','1990-10-05',105000)," +
                    // Справочная
                    "(12,'Павлова Виктория Сергеевна',4,6,5,'2019-08-15','F','1995-01-30',85000)");

            // 5. Заполняем специализированные таблицы
            // Пилоты
            stmt.execute("INSERT INTO Pylots VALUES " +
                    "(1,'Y',3,4500,4)," +
                    "(2,'Y',2,3200,3)," +
                    "(3,'Y',1,1800,2)");

            // Медосмотры пилотов
            stmt.execute("INSERT INTO PylotsAndMedCheckUps VALUES " +
                    "(1,'2023-01-15','Y'),(1,'2023-07-15','Y')," +
                    "(2,'2023-02-20','Y'),(2,'2023-08-20','N')," +
                    "(3,'2023-03-10','Y')");

            // Диспетчеры
            stmt.execute("INSERT INTO Dispatchers VALUES " +
                    "(4,3),(5,2)");

            // Техники
            stmt.execute("INSERT INTO Technics VALUES " +
                    "(6,3,'Двигатели'),(7,2,'Электроника')");

            // Кассиры
            stmt.execute("INSERT INTO Cashiers VALUES " +
                    "(8,'Y','Y','Y'),(9,'Y','N','Y')");

            // Охрана
            stmt.execute("INSERT INTO SecurityStaff VALUES " +
                    "(10,4,'Y','Y','Y'),(11,3,'Y','N','Y')");

            // Справочная
            stmt.execute("INSERT INTO HelperDepartment VALUES " +
                    "(12,'Y','N')");

            // 6. Заполняем данные о детях
            stmt.execute("INSERT INTO Childrens VALUES " +
                    "(1,'Иванов Алексей Иванович','2010-05-15')," +
                    "(2,'Иванова София Ивановна','2015-08-22')," +
                    "(3,'Петрова Дарья Петровна','2018-03-10')," +
                    "(4,'Сидоров Максим Александрович','2016-11-28')," +
                    "(5,'Кузнецова Алиса Алексеевна','2019-07-03')");

            // 7. Связи сотрудник-ребенок
            stmt.execute("INSERT INTO WorkersAndChildrens VALUES " +
                    "(1,1),(1,2),(3,4),(4,5),(8,3)");
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
    }

    private void showAddClearanceDialog() {
    }

    private void showAddChildDialog() {
    }

    private void showAddQualificationDialog() {
    }

    private void showAddPylotDialog() {
    }

    private void showAddTypeDialog() {
    }

    private void showAddTeamDialog() {
    }

    private void showAddDepartmentDialog() {
    }

    private void showAddStaffDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField workerIdField = new JTextField();
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

        panel.add(new JLabel("WorkerID:"));
        panel.add(workerIdField);
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
                if (workerIdField.getText().isEmpty() || fioField.getText().isEmpty() ||
                        employmentField.getText().isEmpty() || birthDayField.getText().isEmpty() ||
                        salaryField.getText().isEmpty()) {
                    throw new Exception("Все обязательные поля должны быть заполнены");
                }

                String sql = "INSERT INTO AirportStaff VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(workerIdField.getText()));
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

    }

    private void showAddCashierDialog() {

    }

    private void showAddSecurityStaffDialog() {

    }

    private void showAddHelperDialog() {

    }

    private void showAddDispatcherDialog() {

    }

    private void showAddMedCheckupDialog() {

    }

    private void showAddWorkerChildDialog() {

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
                    return false;
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
                    return false;
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
            table.getColumnModel().getColumn(0).setPreferredWidth(200); // ФИО
            table.getColumnModel().getColumn(1).setPreferredWidth(120);  // Медосмотр
            table.getColumnModel().getColumn(2).setPreferredWidth(150);  // Лицензия
            table.getColumnModel().getColumn(3).setPreferredWidth(80);   // Часы
            table.getColumnModel().getColumn(4).setPreferredWidth(150);  // Квалификация

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
            model.addColumn("Уровень квалификации");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки уровней квалификации: " + e.getMessage());
        }
    }
    private void showPylotLicenses() {
        currentTable = "TypeOfPylotLicense";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM TypeOfPylotLicense ORDER BY id")) {

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
            model.addColumn("Тип лицензии");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки типов лицензий: " + e.getMessage());
        }
    }
    private void showClearanceLevels() {
        currentTable = "ClearanceLevelsList";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nameOfLevel FROM ClearanceLevelsList ORDER BY id")) {

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
            model.addColumn("Уровень допуска");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
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
                     "SELECT c.ChildID, c.ChildName, c.ChildBirthDay " +
                             "FROM Childrens c ORDER BY c.ChildName")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0: return Integer.class;
                        case 2: return Date.class;
                        default: return String.class;
                    }
                }
            };

            model.addColumn("ID ребенка");
            model.addColumn("Имя ребенка");
            model.addColumn("Дата рождения");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("ChildID"),
                        rs.getString("ChildName"),
                        rs.getDate("ChildBirthDay")
                });
            }

            table.setModel(model);
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
                    return false;
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
                    return false;
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
            table.getColumnModel().getColumn(1).setPreferredWidth(120); // Дата
            table.getColumnModel().getColumn(2).setPreferredWidth(100); // Статус

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
                    return false;
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
            table.getColumnModel().getColumn(0).setPreferredWidth(250); // ФИО
            table.getColumnModel().getColumn(1).setPreferredWidth(150); // Уровень допуска

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
                    return false;
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
            table.getColumnModel().getColumn(1).setPreferredWidth(150);
            table.getColumnModel().getColumn(2).setPreferredWidth(200);

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
                    return false;
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

            table.getColumnModel().getColumn(0).setPreferredWidth(250);
            table.getColumnModel().getColumn(1).setPreferredWidth(120);
            table.getColumnModel().getColumn(2).setPreferredWidth(120);
            table.getColumnModel().getColumn(3).setPreferredWidth(120);

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
                    return false;
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
            table.getColumnModel().getColumn(1).setPreferredWidth(150);
            table.getColumnModel().getColumn(2).setPreferredWidth(100);
            table.getColumnModel().getColumn(3).setPreferredWidth(100);
            table.getColumnModel().getColumn(4).setPreferredWidth(120);

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
                    return false;
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
            table.getColumnModel().getColumn(1).setPreferredWidth(150);
            table.getColumnModel().getColumn(2).setPreferredWidth(150);

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
                    return false;
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
                    return false;
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
            table.getColumnModel().getColumn(1).setPreferredWidth(300);

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
                    return false;
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
                    return false;
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
            table.getColumnModel().getColumn(1).setPreferredWidth(200); // Департамент
            table.getColumnModel().getColumn(2).setPreferredWidth(200); // Тип сотрудника
            table.getColumnModel().getColumn(3).setPreferredWidth(200); // Команда
            table.getColumnModel().getColumn(4).setPreferredWidth(100);  // Зарплата
            table.getColumnModel().getColumn(5).setPreferredWidth(150); // Дата приема
            table.getColumnModel().getColumn(6).setPreferredWidth(150); // Дата рождения
            table.getColumnModel().getColumn(7).setPreferredWidth(50);  // Пол

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

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AirportDatabaseApp().show());
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