package org.example.MainWindow;

import org.example.Database.*;
import org.example.RequestFrame;

import java.awt.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.*;

@SuppressWarnings("ALL")
public class FrameWork extends JFrame {
    private ToolBarMenu toolBarMenu;
    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private Connection connection;

    public FrameWork() throws SQLException {
        super("База данных аэропорта");

        this.connection = DatabaseManager.getConnection();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int minWindowWidth = 640;
        int minWindowHeight = 480;
        int windowWidth = (int) screenSize.getWidth();
        int windowHeight = (int) screenSize.getHeight();

        setPreferredSize(new Dimension(windowWidth, windowHeight));
        setSize(new Dimension(windowWidth, windowHeight));
        setMinimumSize(new Dimension(minWindowWidth, minWindowHeight));
        setResizable(true);

        MainWindowSettings mainWindowSettings = new MainWindowSettings();
        mainWindowSettings.setSize(new Dimension(windowWidth-30, windowHeight-115));

        setLocation(
                (int)((screenSize.getWidth() - windowWidth) / 2),
                (int)((screenSize.getHeight() - windowHeight) / 2)
        );
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        addMainMenu();

        BoxLayoutUtils blUtils = new BoxLayoutUtils();
        JPanel utilsPanel = blUtils.createHorizontalPanel();
        toolBarMenu = new ToolBarMenu(this);
        utilsPanel.add(toolBarMenu);
        getContentPane().add(utilsPanel, "North");

        // Create main panel with BorderLayout
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create the 2x2 grid panel for the first four buttons
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        createTables();
        initializeData();

        addConnectToAirportBtn(gridPanel);
        addConnectToAirplaneBtn(gridPanel);
        addConnectToScheduleBtn(gridPanel);
        addConnectToPassengersBtn(gridPanel);

        // Add the grid panel to the center of main panel
        mainPanel.add(gridPanel, BorderLayout.CENTER);

        // Add the request button at the bottom
        addRequestBtn(mainPanel);

        this.add(mainPanel, BorderLayout.CENTER);

        pack();
        setVisible(true);
    }

    void addRequestBtn(JPanel parentPanel) {
        JButton btn = new JButton("Запросы");
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 60)); // Высота 60px, ширина максимальная

        // Чтобы кнопка растягивалась по всей ширине
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        btn.setToolTipText("Выполнить запросы");
        btn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                RequestFrame app = null;
                app = new RequestFrame(connection);
                app.show();
            });
        });

        // Панель для кнопки (чтобы контролировать отступы)
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(btn, BorderLayout.CENTER);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); // Отступ сверху 10px

        parentPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    void addConnectToAirportBtn(JPanel panel) {
        JButton btn = createIconButton("Аэропорт", "/airport_icon.jpg",
                "Получить информацию, связанную с аэропортом",
                () -> new AirportDatabaseApp(connection));
        panel.add(btn);
    }

    void addConnectToAirplaneBtn(JPanel panel) {
        JButton btn = createIconButton("Самолеты", "/airplane_icon.jpg",
                "Получить информацию, связанную с самолетами",
                () -> {
                    new AirplaneDatabaseApp(connection);
                });
        panel.add(btn);
    }

    void addConnectToScheduleBtn(JPanel panel) {
        JButton btn = createIconButton("Расписание", "/schedule_icon.jpg",
                "Получить информацию, связанную с расписанием полетов",
                () -> new ScheduleDatabaseApp(connection));
        panel.add(btn);
    }

    void addConnectToPassengersBtn(JPanel panel) {
        JButton btn = createIconButton("Пассажиры", "/passengers_icon.jpg",
                "Получить информацию, связанную с пассажирами",
                () -> new PassengersDatabaseApp(connection));
        panel.add(btn);
    }


    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // 1. Сначала создаем независимые таблицы (без внешних ключей)

            // Справочные таблицы
            stmt.execute("CREATE TABLE IF NOT EXISTS TypeOfAirportStaff (" +
                    "IdOfType INT PRIMARY KEY AUTO_INCREMENT, " +
                    "TypeName VARCHAR(50) NOT NULL UNIQUE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS TypeOfQualificationLevel (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(400) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS TypeOfPylotLicense (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(400) NOT NULL UNIQUE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS ClearanceLevelsList (" +
                    "id INT PRIMARY KEY, " +
                    "nameOfLevel VARCHAR(200) NOT NULL UNIQUE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Locations (" +
                    "LocationID INT PRIMARY KEY, " +
                    "LocationName VARCHAR(100) NOT NULL UNIQUE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS WeekDays (" +
                    "DayID INT PRIMARY KEY, " +
                    "DayName VARCHAR(20) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS ReadyStatuses (" +
                    "StatusID INT PRIMARY KEY, " +
                    "StatusName VARCHAR(40) NOT NULL UNIQUE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS FlightStatuses (" +
                    "StatusID INT PRIMARY KEY, " +
                    "StatusName VARCHAR(40) DEFAULT 'Parked' NOT NULL UNIQUE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS FlightsStatusForHistory (" +
                    "StatusID INT PRIMARY KEY, " +
                    "StatucName VARCHAR(100) NOT NULL UNIQUE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS WeatherConditions (" +
                    "weatherID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "NameOfCondition VARCHAR(100), " +
                    "CanFlight CHAR(1) DEFAULT 'N', " +
                    "NeedWait CHAR(1) DEFAULT 'N', " +
                    "NeedCancel CHAR(1) DEFAULT 'N')");

            stmt.execute("CREATE TABLE IF NOT EXISTS PlaneTypeCharacteristics (" +
                    "TypeID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "ModelName VARCHAR(100), " +
                    "VendorID VARCHAR(10), " +
                    "MaxSpeed FLOAT DEFAULT 0, " +
                    "MediumSpeed FLOAT DEFAULT 0, " +
                    "MaxFlightDistance FLOAT DEFAULT 0, " +
                    "FuelTankCapacity FLOAT DEFAULT 0, " +
                    "CanRefuelingInAir CHAR(1) DEFAULT 'N', " +
                    "MinRanwaylenght FLOAT DEFAULT 0, " +
                    "NormalRanwaylenght FLOAT DEFAULT 0, " +
                    "MaxFlightAltitude FLOAT DEFAULT 0, " +
                    "StandartFlightAltitude FLOAT DEFAULT 0, " +
                    "fuelConsumption FLOAT DEFAULT 0)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Agency (" +
                    "AgencyID INT PRIMARY KEY, " +
                    "AgencyName VARCHAR(200) UNIQUE, " +
                    "Country VARCHAR(50), " +
                    "AgencyType VARCHAR(50))");

            stmt.execute("CREATE TABLE IF NOT EXISTS TypesOfBaggages (" +
                    "BaggageTypeID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "MaxWeight FLOAT DEFAULT 0, " +
                    "MaxItems FLOAT DEFAULT 0, " +
                    "MaxHeight FLOAT DEFAULT 0, " +
                    "MaxWidth FLOAT DEFAULT 0, " +
                    "MaxLength FLOAT DEFAULT 0)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Seats (" +
                    "SeatID INT PRIMARY KEY, " +
                    "SeatName VARCHAR(5) UNIQUE)");

            // 2. Затем таблицы, которые зависят от уже созданных

            stmt.execute("CREATE TABLE IF NOT EXISTS Departments (" +
                    "DepartmentID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "DepartmentName VARCHAR(100) NOT NULL UNIQUE, " +
                    "Location VARCHAR(100))");

            stmt.execute("CREATE TABLE IF NOT EXISTS Teams (" +
                    "TeamID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "TeamName VARCHAR(100) NOT NULL UNIQUE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Airports (" +
                    "AirportID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "Name VARCHAR(200), " +
                    "Location INT, " +
                    "FOREIGN KEY (Location) REFERENCES Locations(LocationID) ON DELETE SET NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Childrens (" +
                    "ChildID INT PRIMARY KEY, " +
                    "ChildName VARCHAR(100) NOT NULL, " +
                    "ChildBirthDay DATE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Passengers (" +
                    "PassengerID INT PRIMARY KEY, " +
                    "FIO VARCHAR(100), " +
                    "Country VARCHAR(50), " +
                    "PassportData VARCHAR(200), " +
                    "InternationalPassportData VARCHAR(200))");

            // 3. Основные таблицы с зависимостями

            stmt.execute("CREATE TABLE IF NOT EXISTS AirportStaff (" +
                    "WorkerID INT PRIMARY KEY, " +
                    "FIO VARCHAR(100) NOT NULL, " +
                    "DepartID INT, " +
                    "WorkerType INT, " +
                    "TeamID INT, " +
                    "Employment DATE, " +
                    "Gender CHAR(1), " +
                    "BirthDay DATE, " +
                    "Salary INT, " +
                    "FOREIGN KEY (DepartID) REFERENCES Departments(DepartmentID) ON DELETE SET NULL, " +
                    "FOREIGN KEY (WorkerType) REFERENCES TypeOfAirportStaff(IdOfType) ON DELETE SET NULL, " +
                    "FOREIGN KEY (TeamID) REFERENCES Teams(TeamID) ON DELETE SET NULL)");

            try {
                // Проверяем, существует ли уже столбец
                stmt.execute("SELECT DepartmentLeader FROM Departments LIMIT 0");
            } catch (SQLException e) {
                // Если столбца нет - добавляем его
                stmt.execute("ALTER TABLE Departments ADD COLUMN DepartmentLeader INT");
            }

            // И только теперь добавляем внешний ключ
            stmt.execute("ALTER TABLE Departments ADD FOREIGN KEY (DepartmentLeader) " +
                    "REFERENCES AirportStaff(WorkerID) ON DELETE SET NULL");

            // Обновляем Departments для добавления связи с DepartmentLeader
            stmt.execute("ALTER TABLE Departments ADD FOREIGN KEY (DepartmentLeader) " +
                    "REFERENCES AirportStaff(WorkerID) ON DELETE SET NULL");

            stmt.execute("CREATE TABLE IF NOT EXISTS Planes (" +
                    "PlanesID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "HomeAirportID INT, " +
                    "PlaneTypeID INT, " +
                    "FOREIGN KEY (HomeAirportID) REFERENCES Airports(AirportID) ON DELETE SET NULL, " +
                    "FOREIGN KEY (PlaneTypeID) REFERENCES PlaneTypeCharacteristics(TypeID) ON DELETE SET NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Schedule (" +
                    "JourneyID INT PRIMARY KEY, " +
                    "TrailStartPoint INT, " +
                    "TrailTransferPoint INT, " +
                    "TrailFinishPoint INT, " +
                    "TicketCost INT DEFAULT 0, " +
                    "FOREIGN KEY (TrailStartPoint) REFERENCES Locations(LocationID) ON DELETE SET NULL, " +
                    "FOREIGN KEY (TrailTransferPoint) REFERENCES Locations(LocationID) ON DELETE SET NULL, " +
                    "FOREIGN KEY (TrailFinishPoint) REFERENCES Locations(LocationID) ON DELETE SET NULL)");

            // 4. Таблицы специализаций сотрудников

            stmt.execute("CREATE TABLE IF NOT EXISTS Pylots (" +
                    "WorkerID INT PRIMARY KEY, " +
                    "MedCheckUp CHAR(1) DEFAULT 'N', " +
                    "PylotLicense INT, " +
                    "FlightHours INT, " +
                    "Qualification_Level INT, " +
                    "FOREIGN KEY (WorkerID) REFERENCES AirportStaff(WorkerID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (PylotLicense) REFERENCES TypeOfPylotLicense(id) ON DELETE SET NULL, " +
                    "FOREIGN KEY (Qualification_Level) REFERENCES TypeOfQualificationLevel(id) ON DELETE SET NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Dispatchers (" +
                    "WorkerID INT PRIMARY KEY, " +
                    "ClearanceLevel INT, " +
                    "FOREIGN KEY (WorkerID) REFERENCES AirportStaff(WorkerID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (ClearanceLevel) REFERENCES ClearanceLevelsList(id) ON DELETE SET NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Technics (" +
                    "WorkerID INT PRIMARY KEY, " +
                    "ClearanceLevel INT, " +
                    "Specialization VARCHAR(400), " +
                    "FOREIGN KEY (WorkerID) REFERENCES AirportStaff(WorkerID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (ClearanceLevel) REFERENCES ClearanceLevelsList(id) ON DELETE SET NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Cashiers (" +
                    "WorkerID INT PRIMARY KEY, " +
                    "CustomerServiceExperience CHAR(1) DEFAULT 'N', " +
                    "PaymentProcessingSkills CHAR(1) DEFAULT 'N', " +
                    "BookingSystemKnowledge CHAR(1) DEFAULT 'N', " +
                    "FOREIGN KEY (WorkerID) REFERENCES AirportStaff(WorkerID) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS SecurityStaff (" +
                    "WorkerID INT PRIMARY KEY, " +
                    "ClearanceLevel INT, " +
                    "EmergencyResponseSkills CHAR(1) DEFAULT 'N', " +
                    "CCTVExperience CHAR(1) DEFAULT 'N', " +
                    "InspectionSkills CHAR(1) DEFAULT 'N', " +
                    "FOREIGN KEY (WorkerID) REFERENCES AirportStaff(WorkerID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (ClearanceLevel) REFERENCES ClearanceLevelsList(id) ON DELETE SET NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS HelperDepartment (" +
                    "WorkerID INT PRIMARY KEY, " +
                    "SchedulingSkills CHAR(1) DEFAULT 'N', " +
                    "HRProcessesKnowledge CHAR(1) DEFAULT 'N', " +
                    "FOREIGN KEY (WorkerID) REFERENCES AirportStaff(WorkerID) ON DELETE CASCADE)");

            // 5. Таблицы связей и дополнительных данных

            stmt.execute("CREATE TABLE IF NOT EXISTS WorkersAndChildrens (" +
                    "WorkerID INT, " +
                    "ChildID INT, " +
                    "PRIMARY KEY (WorkerID, ChildID), " +
                    "FOREIGN KEY (WorkerID) REFERENCES AirportStaff(WorkerID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (ChildID) REFERENCES Childrens(ChildID) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS PylotsAndMedCheckUps (" +
                    "PylotID INT, " +
                    "CheckUpDate TIMESTAMP, " +
                    "Passed CHAR(1) DEFAULT 'N', " +
                    "PRIMARY KEY (PylotID, CheckUpDate), " +
                    "FOREIGN KEY (PylotID) REFERENCES Pylots(WorkerID) ON DELETE CASCADE)");


            // Таблицы для самолетов и их обслуживания
            stmt.execute("CREATE TABLE IF NOT EXISTS PlanesAndTechCheckUps (" +
                    "PlaneID INT, " +
                    "TechCheckUpDate TIMESTAMP, " +
                    "Passed CHAR(1) DEFAULT 'N', " +
                    "PRIMARY KEY (PlaneID, TechCheckUpDate), " +
                    "FOREIGN KEY (PlaneID) REFERENCES Planes(PlanesID) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS PlanesAndRepairCheckUps (" +
                    "PlaneID INT, " +
                    "RepairCheckUpDate TIMESTAMP, " +
                    "Passed CHAR(1) DEFAULT 'N', " +
                    "PRIMARY KEY (PlaneID, RepairCheckUpDate), " +
                    "FOREIGN KEY (PlaneID) REFERENCES Planes(PlanesID) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS PlanesAndReadyStatuses (" +
                    "PlaneID INT, " +
                    "Status INT, " +
                    "UpdateTime TIMESTAMP, " +
                    "PRIMARY KEY (PlaneID, Status, UpdateTime), " +
                    "FOREIGN KEY (PlaneID) REFERENCES Planes(PlanesID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (Status) REFERENCES ReadyStatuses(StatusID) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS PlanesAndFlightStatuses (" +
                    "PlaneID INT, " +
                    "Status INT, " +
                    "UpdateTime TIMESTAMP, " +
                    "PRIMARY KEY (PlaneID, Status, UpdateTime), " +
                    "FOREIGN KEY (PlaneID) REFERENCES Planes(PlanesID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (Status) REFERENCES FlightStatuses(StatusID) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS PlanesAndOilFillings (" +
                    "PlaneID INT, " +
                    "Date TIMESTAMP, " +
                    "TypeOfOil INT, " +
                    "CapcityOfOil INT DEFAULT 0, " +
                    "PRIMARY KEY (PlaneID, Date), " +
                    "FOREIGN KEY (PlaneID) REFERENCES Planes(PlanesID) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS PlanesAndCabinCleanUps (" +
                    "PlaneID INT, " +
                    "CabinCleanUpID INT, " +
                    "TypeOfCleaning INT, " +
                    "Date TIMESTAMP, " +
                    "PRIMARY KEY (PlaneID, CabinCleanUpID), " +
                    "FOREIGN KEY (PlaneID) REFERENCES Planes(PlanesID) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS PlanesAndFoodSupplies (" +
                    "PlaneID INT, " +
                    "Date TIMESTAMP, " +
                    "TypeOfSuppling VARCHAR(100) DEFAULT 'Ordinary', " +
                    "Amount INT DEFAULT 0, " +
                    "PRIMARY KEY (PlaneID, Date), " +
                    "FOREIGN KEY (PlaneID) REFERENCES Planes(PlanesID) ON DELETE CASCADE)");

            // Таблицы рейсов
            stmt.execute("CREATE TABLE IF NOT EXISTS Flights (" +
                    "FlightID INT PRIMARY KEY, " +
                    "JourneyID INT, " +
                    "PlaneID INT, " +
                    "MaxPassengers INT, " +
                    "MaxLoad INT, " +
                    "PylotTeam INT, " +
                    "TechnicTeam INT, " +
                    "FromLocation INT, " +
                    "FromTime DATETIME, " +
                    "ScheduleFromTime DATETIME, " +
                    "ToLocation INT, " +
                    "ToTime DATETIME, " +
                    "ScheduleToTime DATETIME, " +
                    "WeatherFromLocation INT, " +
                    "FlightStatus INT, " +
                    "FOREIGN KEY (JourneyID) REFERENCES Schedule(JourneyID) ON DELETE SET NULL, " +
                    "FOREIGN KEY (PlaneID) REFERENCES Planes(PlanesID) ON DELETE SET NULL, " +
                    "FOREIGN KEY (PylotTeam) REFERENCES Teams(TeamID) ON DELETE SET NULL, " +
                    "FOREIGN KEY (TechnicTeam) REFERENCES Teams(TeamID) ON DELETE SET NULL, " +
                    "FOREIGN KEY (FromLocation) REFERENCES Locations(LocationID) ON DELETE SET NULL, " +
                    "FOREIGN KEY (ToLocation) REFERENCES Locations(LocationID) ON DELETE SET NULL, " +
                    "FOREIGN KEY (WeatherFromLocation) REFERENCES WeatherConditions(weatherID) ON DELETE SET NULL, " +
                    "FOREIGN KEY (FlightStatus) REFERENCES FlightsStatusForHistory(StatusID) ON DELETE SET NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS JourneyAndDaysTime (" +
                    "JourneyID INT, " +
                    "DayIDFrom INT, " +
                    "TimeFrom TIME, " +
                    "DayIDTo INT, " +
                    "TimeTo TIME, " +
                    "PRIMARY KEY (JourneyID, DayIDFrom), " +
                    "FOREIGN KEY (JourneyID) REFERENCES Schedule(JourneyID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (DayIDFrom) REFERENCES WeekDays(DayID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (DayIDTo) REFERENCES WeekDays(DayID) ON DELETE SET NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS PassengersInFlight (" +
                    "FlightID INT PRIMARY KEY, " +
                    "MaxCapcity INT DEFAULT 0, " +
                    "RealCapacity INT DEFAULT 0, " +
                    "WithBaggage INT DEFAULT 0, " +
                    "BaggageItems INT DEFAULT 0, " +
                    "BaggageWeight INT DEFAULT 0, " +
                    "BoughtTickets INT DEFAULT 0, " +
                    "PassedTickets INT DEFAULT 0, " +
                    "PassengersNotCame INT DEFAULT 0, " +
                    "HaveNotDocumets INT DEFAULT 0, " +
                    "HaveNotPermission INT DEFAULT 0, " +
                    "DeportedPassengers INT DEFAULT 0, " +
                    "FOREIGN KEY (FlightID) REFERENCES Flights(FlightID) ON DELETE CASCADE)");

            // Специализированные таблицы рейсов
            stmt.execute("CREATE TABLE IF NOT EXISTS InternalFlight (" +
                    "FlightID INT PRIMARY KEY, " +
                    "MinimumTickets INT DEFAULT 0, " +
                    "PassengersCapacity INT, " +
                    "FOREIGN KEY (FlightID) REFERENCES Flights(FlightID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (PassengersCapacity) REFERENCES PassengersInFlight(FlightID) ON DELETE SET NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS InternationalFlight (" +
                    "FlightID INT PRIMARY KEY, " +
                    "MinimumTickets INT DEFAULT 0, " +
                    "PassengersCapcity INT, " +
                    "FOREIGN KEY (FlightID) REFERENCES Flights(FlightID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (PassengersCapcity) REFERENCES PassengersInFlight(FlightID) ON DELETE SET NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS CharterFlight (" +
                    "FlightID INT PRIMARY KEY, " +
                    "AgencyID INT, " +
                    "MinimumTickets INT DEFAULT 0, " +
                    "PassengersCapcity INT, " +
                    "FOREIGN KEY (FlightID) REFERENCES Flights(FlightID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (AgencyID) REFERENCES Agency(AgencyID) ON DELETE SET NULL, " +
                    "FOREIGN KEY (PassengersCapcity) REFERENCES PassengersInFlight(FlightID) ON DELETE SET NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS CargoFlight (" +
                    "FlightID INT PRIMARY KEY, " +
                    "MaxCapacity INT DEFAULT 0, " +
                    "RealCapacity INT DEFAULT 0, " +
                    "FOREIGN KEY (FlightID) REFERENCES Flights(FlightID) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS SpecialFlight (" +
                    "FlightID INT PRIMARY KEY, " +
                    "FlightGoal VARCHAR(200), " +
                    "About VARCHAR(500), " +
                    "FOREIGN KEY (FlightID) REFERENCES Flights(FlightID) ON DELETE CASCADE)");

            // Специализированные таблицы самолетов
            stmt.execute("CREATE TABLE IF NOT EXISTS PassengerPlanes (" +
                    "PlaneID INT PRIMARY KEY, " +
                    "MaxPassangers INT DEFAULT 0, " +
                    "AllLoadCapacity FLOAT DEFAULT 0, " +
                    "BaggageLoadCapacity FLOAT DEFAULT 0, " +
                    "FOREIGN KEY (PlaneID) REFERENCES Planes(PlanesID) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS CargoPlanes (" +
                    "PlaneID INT PRIMARY KEY, " +
                    "MaxLoad FLOAT DEFAULT 0, " +
                    "MaxVolume FLOAT DEFAULT 0, " +
                    "CompartmentLength FLOAT DEFAULT 0, " +
                    "CompartmentWeight FLOAT DEFAULT 0, " +
                    "CompartmentHeight FLOAT DEFAULT 0, " +
                    "TrapdoorLenght FLOAT DEFAULT 0, " +
                    "TrapdoorWeight FLOAT DEFAULT 0, " +
                    "TrapdoorHeight FLOAT DEFAULT 0, " +
                    "FOREIGN KEY (PlaneID) REFERENCES Planes(PlanesID) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS SpecialPlanes (" +
                    "PlaneID INT PRIMARY KEY, " +
                    "SpecialFlightType VARCHAR(100), " +
                    "FlightClient VARCHAR(200), " +
                    "CrewSpecialRequirements VARCHAR(400), " +
                    "SpecialFuelRequired CHAR(1) DEFAULT 'N', " +
                    "AdditionalEquipment CHAR(1) DEFAULT 'N', " +
                    "FOREIGN KEY (PlaneID) REFERENCES Planes(PlanesID) ON DELETE CASCADE)");

            // Таблицы билетов и пассажиров
            stmt.execute("CREATE TABLE IF NOT EXISTS Tickets (" +
                    "TicketID INT PRIMARY KEY, " +
                    "TicketCost FLOAT, " +
                    "TciketStatus VARCHAR(20), " +
                    "FlightID INT, " +
                    "Seat INT, " +
                    "BaggageTypeID INT, " +
                    "FOREIGN KEY (FlightID) REFERENCES Flights(FlightID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (Seat) REFERENCES Seats(SeatID) ON DELETE SET NULL, " +
                    "FOREIGN KEY (BaggageTypeID) REFERENCES TypesOfBaggages(BaggageTypeID) ON DELETE SET NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS TicketsStory (" +
                    "TicketID INT, " +
                    "PassengerID INT, " +
                    "Status VARCHAR(100), " +
                    "TimeStatusUpdate TIMESTAMP, " +
                    "PRIMARY KEY (TicketID, PassengerID, TimeStatusUpdate), " +
                    "FOREIGN KEY (TicketID) REFERENCES Tickets(TicketID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (PassengerID) REFERENCES Passengers(PassengerID) ON DELETE CASCADE)");

        } catch (SQLException e) {
            System.err.println("Ошибка при создании таблиц: " + e.getMessage());
            throw e;
        }
    }

    private void initializeData() throws SQLException {
        //insertTestData();
    }


    private boolean isTableEmpty(String tableName) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private void insertTestData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // 1. Type reference tables
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

            stmt.execute("INSERT INTO Locations VALUES " +
                    "(1, 'Москва (Шереметьево)'), (2, 'Санкт-Петербург (Пулково)'), " +
                    "(3, 'Новосибирск (Толмачево)'), (4, 'Екатеринбург (Кольцово)'), " +
                    "(5, 'Сочи (Адлер)'), (6, 'Казань'), (7, 'Владивосток')");

            stmt.execute("INSERT INTO WeekDays VALUES " +
                    "(1, 'Monday'), (2, 'Tuesday'), (3, 'Wednesday'), (4, 'Thursday'), " +
                    "(5, 'Friday'), (6, 'Saturday'), (7, 'Sunday')");

            stmt.execute("INSERT INTO ReadyStatuses VALUES " +
                    "(1, 'Ready'), (2, 'Maintenance Required'), (3, 'Out of Service'), " +
                    "(4, 'Scheduled Checkup')");

            stmt.execute("INSERT INTO FlightStatuses VALUES " +
                    "(1, 'Parked'), (2, 'Boarding'), (3, 'Departing'), " +
                    "(4, 'In Air'), (5, 'Landed'), (6, 'Delayed')");

            stmt.execute("INSERT INTO FlightsStatusForHistory VALUES " +
                    "(1, 'Completed'), (2, 'Cancelled'), (3, 'Diverted'), " +
                    "(4, 'Emergency Landing')");

            stmt.execute("INSERT INTO WeatherConditions VALUES " +
                    "(1, 'Clear', 'Y', 'N', 'N'), (2, 'Cloudy', 'Y', 'N', 'N'), " +
                    "(3, 'Rain', 'Y', 'Y', 'N'), (4, 'Thunderstorm', 'N', 'Y', 'Y'), " +
                    "(5, 'Fog', 'N', 'Y', 'Y'), (6, 'Snow', 'Y', 'Y', 'N')");

            stmt.execute("INSERT INTO PlaneTypeCharacteristics VALUES " +
                    "(1, 'Boeing 737-800', 'BOEING', 876, 828, 5765, 26020, 'N', 1500, 2500, 12500, 11000, 2500), " +
                    "(2, 'Airbus A320', 'AIRBUS', 890, 840, 6150, 23800, 'N', 1600, 2300, 12000, 11500, 2400), " +
                    "(3, 'Boeing 777-300ER', 'BOEING', 905, 892, 13650, 181280, 'Y', 2500, 3500, 13140, 12500, 6800), " +
                    "(4, 'Airbus A350', 'AIRBUS', 913, 900, 15000, 156000, 'Y', 2700, 3300, 13100, 12500, 5600), " +
                    "(5, 'Bombardier CRJ-200', 'BOMBARDIER', 860, 790, 3045, 5300, 'N', 1400, 1800, 12400, 11000, 1200)");

            stmt.execute("INSERT INTO Agency VALUES " +
                    "(1, 'Aeroflot', 'Russia', 'Airline'), " +
                    "(2, 'S7 Airlines', 'Russia', 'Airline'), " +
                    "(3, 'UTair', 'Russia', 'Airline'), " +
                    "(4, 'Nordwind Airlines', 'Russia', 'Charter'), " +
                    "(5, 'Pobeda', 'Russia', 'Low-cost')");

            stmt.execute("INSERT INTO TypesOfBaggages VALUES " +
                    "(1, 23, 1, 55, 40, 20), (2, 32, 2, 55, 40, 20), " +
                    "(3, 10, 1, 40, 30, 15), (4, 20, 1, 50, 35, 20)");

            stmt.execute("INSERT INTO Seats VALUES " +
                    "(1, '1A'), (2, '1B'), (3, '1C'), (4, '2A'), (5, '2B'), " +
                    "(6, '2C'), (7, '3A'), (8, '3B'), (9, '3C'), (10, '4A')");

            // 2. Departments and Teams
            stmt.execute("INSERT INTO Departments VALUES " +
                    "(1,'Летный отдел','Терминал A, 2 этаж', NULL)," +
                    "(2,'Диспетчерская служба','Башня управления', NULL)," +
                    "(3,'Техническая служба','Ангар 1', NULL)," +
                    "(4,'Кассы и регистрация','Терминал B, 1 этаж', NULL)," +
                    "(5,'Служба безопасности','Терминал C, КПП', NULL)," +
                    "(6,'Справочная служба','Терминал A, 1 этаж', NULL)");

            stmt.execute("INSERT INTO Teams VALUES " +
                    "(1,'Экипаж 101'),(2,'Экипаж 202'),(3,'Диспетчеры смены A')," +
                    "(4,'Техническая бригада 1'),(5,'Кассиры утренней смены')," +
                    "(6,'Охрана терминала B'),(7,'Справочная смена 1')");

            // 3. Airports
            stmt.execute("INSERT INTO Airports VALUES " +
                    "(1, 'Шереметьево', 1), (2, 'Пулково', 2), " +
                    "(3, 'Толмачево', 3), (4, 'Кольцово', 4), " +
                    "(5, 'Адлер', 5)");

            // 4. Children and Passengers
            stmt.execute("INSERT INTO Childrens VALUES " +
                    "(1,'Иванов Алексей Иванович','2010-05-15')," +
                    "(2,'Иванова София Ивановна','2015-08-22')," +
                    "(3,'Петрова Дарья Петровна','2018-03-10')," +
                    "(4,'Сидоров Максим Александрович','2016-11-28')," +
                    "(5,'Кузнецова Алиса Алексеевна','2019-07-03')," +
                    "(6,'Павлов Денис Викторович','2017-05-18')");

            stmt.execute("INSERT INTO Passengers VALUES " +
                    "(1, 'Смирнов Александр Петрович', 'Russia', '1234 567890', 'AB1234567'), " +
                    "(2, 'Ковалева Елена Викторовна', 'Russia', '2345 678901', 'BC2345678'), " +
                    "(3, 'Попов Дмитрий Сергеевич', 'Russia', '3456 789012', 'CD3456789'), " +
                    "(4, 'Васильева Ольга Ивановна', 'Russia', '4567 890123', 'DE4567890'), " +
                    "(5, 'Николаев Андрей Михайлович', 'Russia', '5678 901234', 'EF5678901')");

            // 5. Airport Staff
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
                    "(12,'Павлова Виктория Сергеевна',6,6,7,'2019-08-15','F','1995-01-30',85000)," +
                    "(13,'Козлов Артем Игоревич',6,6,7,'2020-03-10','M','1996-07-22',88000)");

            // Update department leaders
            stmt.execute("UPDATE Departments SET DepartmentLeader = 1 WHERE DepartmentID = 1");
            stmt.execute("UPDATE Departments SET DepartmentLeader = 4 WHERE DepartmentID = 2");
            stmt.execute("UPDATE Departments SET DepartmentLeader = 6 WHERE DepartmentID = 3");
            stmt.execute("UPDATE Departments SET DepartmentLeader = 8 WHERE DepartmentID = 4");
            stmt.execute("UPDATE Departments SET DepartmentLeader = 10 WHERE DepartmentID = 5");
            stmt.execute("UPDATE Departments SET DepartmentLeader = 12 WHERE DepartmentID = 6");

            // 6. Specialized staff tables
            stmt.execute("INSERT INTO Pylots VALUES " +
                    "(1,'Y',3,4500,4)," +
                    "(2,'Y',2,3200,3)," +
                    "(3,'Y',1,1800,2)");

            stmt.execute("INSERT INTO Dispatchers VALUES " +
                    "(4,3),(5,2)");

            stmt.execute("INSERT INTO Technics VALUES " +
                    "(6,3,'Двигатели'),(7,2,'Электроника')");

            stmt.execute("INSERT INTO Cashiers VALUES " +
                    "(8,'Y','Y','Y'),(9,'Y','N','Y')");

            stmt.execute("INSERT INTO SecurityStaff VALUES " +
                    "(10,4,'Y','Y','Y'),(11,3,'Y','N','Y')");

            stmt.execute("INSERT INTO HelperDepartment VALUES " +
                    "(12,'Y','N'),(13,'N','Y')");

            // 7. Worker-children relationships
            stmt.execute("INSERT INTO WorkersAndChildrens VALUES " +
                    "(1,1),(1,2),(3,4),(4,5),(8,3),(12,6)");

            // 8. Pilot medical checkups
            stmt.execute("INSERT INTO PylotsAndMedCheckUps VALUES " +
                    "(1,'2023-01-15','Y'),(1,'2023-07-15','Y')," +
                    "(2,'2023-02-20','Y'),(2,'2023-08-20','N')," +
                    "(3,'2023-03-10','Y'),(3,'2023-09-10','Y')");

            // 9. Planes
            stmt.execute("INSERT INTO Planes VALUES " +
                    "(1, 1, 1), (2, 1, 2), (3, 2, 3), (4, 3, 4), (5, 4, 5)");

            // Plane statuses and checkups
            stmt.execute("INSERT INTO PlanesAndTechCheckUps VALUES " +
                    "(1, '2023-01-10', 'Y'), (1, '2023-07-10', 'Y'), " +
                    "(2, '2023-02-15', 'Y'), (2, '2023-08-15', 'N'), " +
                    "(3, '2023-03-20', 'Y')");

            stmt.execute("INSERT INTO PlanesAndRepairCheckUps VALUES " +
                    "(1, '2023-01-05', 'Y'), (2, '2023-02-10', 'Y'), " +
                    "(3, '2023-03-15', 'Y'), (4, '2023-04-20', 'Y')");

            stmt.execute("INSERT INTO PlanesAndReadyStatuses VALUES " +
                    "(1, 1, '2023-10-01'), (2, 1, '2023-10-01'), " +
                    "(3, 2, '2023-10-01'), (4, 1, '2023-10-01'), " +
                    "(5, 1, '2023-10-01')");

            stmt.execute("INSERT INTO PlanesAndFlightStatuses VALUES " +
                    "(1, 1, '2023-10-01'), (2, 1, '2023-10-01'), " +
                    "(3, 1, '2023-10-01'), (4, 1, '2023-10-01'), " +
                    "(5, 1, '2023-10-01')");

            stmt.execute("INSERT INTO PlanesAndOilFillings VALUES " +
                    "(1, '2023-09-01', 1, 5000), (2, '2023-09-05', 1, 4500), " +
                    "(3, '2023-09-10', 2, 6000), (4, '2023-09-15', 2, 5500)");

            stmt.execute("INSERT INTO PlanesAndCabinCleanUps VALUES " +
                    "(1, 1, 1, '2023-09-01'), (1, 2, 2, '2023-09-15'), " +
                    "(2, 1, 1, '2023-09-05'), (3, 1, 1, '2023-09-10')");

            stmt.execute("INSERT INTO PlanesAndFoodSupplies VALUES " +
                    "(1, '2023-09-01', 'Full', 150), (2, '2023-09-05', 'Basic', 120), " +
                    "(3, '2023-09-10', 'Full', 200), (4, '2023-09-15', 'Basic', 100)");

            // 10. Plane specialization tables
            stmt.execute("INSERT INTO PassengerPlanes VALUES " +
                    "(1, 189, 20000, 5000), (2, 180, 19000, 4800), " +
                    "(3, 440, 45000, 10000)");

            stmt.execute("INSERT INTO CargoPlanes VALUES " +
                    "(4, 102000, 650, 30, 102000, 5, 5, 5000, 3)");

            stmt.execute("INSERT INTO SpecialPlanes VALUES " +
                    "(5, 'Government', 'State Transport', 'Extra security clearance', 'N', 'Y')");

            // 11. Schedule and Flights
            stmt.execute("INSERT INTO Schedule VALUES " +
                    "(1, 1, NULL, 2, 5000), (2, 1, 3, 5, 8000), " +
                    "(3, 2, NULL, 1, 5000), (4, 3, 1, 7, 15000)");

            stmt.execute("INSERT INTO JourneyAndDaysTime VALUES " +
                    "(1, 1, '08:00:00', 1, '10:00:00'), " +
                    "(1, 3, '14:00:00', 3, '16:00:00'), " +
                    "(2, 2, '09:00:00', 2, '14:00:00'), " +
                    "(3, 5, '18:00:00', 5, '20:00:00')");

            stmt.execute("INSERT INTO Flights VALUES " +
                    "(1, 1, 1, 189, 20000, 1, 4, 1, '2023-10-02 08:00:00', '2023-10-02 08:00:00', 2, '2023-10-02 10:00:00', '2023-10-02 10:00:00', 1, 1), " +
                    "(2, 2, 3, 440, 45000, 2, 4, 1, '2023-10-03 09:00:00', '2023-10-03 09:00:00', 5, '2023-10-03 14:00:00', '2023-10-03 14:00:00', 1, 1), " +
                    "(3, 3, 2, 180, 19000, 1, 4, 2, '2023-10-06 18:00:00', '2023-10-06 18:00:00', 1, '2023-10-06 20:00:00', '2023-10-06 20:00:00', 2, 1), " +
                    "(4, 4, 4, NULL, 102000, 2, 4, 3, '2023-10-04 12:00:00', '2023-10-04 12:00:00', 7, '2023-10-05 08:00:00', '2023-10-05 08:00:00', 3, 1)");

            stmt.execute("INSERT INTO PassengersInFlight VALUES " +
                    "(1, 189, 150, 120, 180, 2800, 160, 150, 10, 2, 1, 0), " +
                    "(2, 440, 420, 400, 800, 9000, 430, 420, 10, 5, 3, 2), " +
                    "(3, 180, 120, 100, 150, 2200, 130, 120, 10, 3, 2, 1)");

            // 12. Flight specialization tables
            stmt.execute("INSERT INTO InternalFlight VALUES " +
                    "(1, 100, 1), (3, 100, 3)");

            stmt.execute("INSERT INTO InternationalFlight VALUES " +
                    "(2, 200, 2)");

            stmt.execute("INSERT INTO CharterFlight VALUES " +
                    "(4, 4, 0, NULL)");

            // 13. Tickets and passengers
            stmt.execute("INSERT INTO Tickets VALUES " +
                    "(1, 5000, 'Sold', 1, 1, 1), (2, 5000, 'Sold', 1, 2, 1), " +
                    "(3, 8000, 'Sold', 2, 3, 2), (4, 8000, 'Sold', 2, 4, 2), " +
                    "(5, 5000, 'Sold', 3, 5, 3), (6, 15000, 'Sold', 4, NULL, NULL)");

            stmt.execute("INSERT INTO TicketsStory VALUES " +
                    "(1, 1, 'Booked', '2023-09-20'), (1, 1, 'Paid', '2023-09-21'), " +
                    "(2, 2, 'Booked', '2023-09-20'), (2, 2, 'Paid', '2023-09-21'), " +
                    "(3, 3, 'Booked', '2023-09-18'), (3, 3, 'Paid', '2023-09-19'), " +
                    "(4, 4, 'Booked', '2023-09-18'), (4, 4, 'Paid', '2023-09-19'), " +
                    "(5, 5, 'Booked', '2023-09-25'), (5, 5, 'Paid', '2023-09-26')");
        }
    }

    private JButton createIconButton(String text, String iconPath, String tooltip, Runnable action) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(200, 200));
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);

        try {
            URL imageUrl = getClass().getResource(iconPath);
            if (imageUrl == null) {
                imageUrl = getClass().getResource(iconPath.substring(1));
            }

            if (imageUrl != null) {
                btn.setIcon(new ImageIcon(imageUrl));
            } else {
                System.err.println("Изображение не найдено: " + iconPath);
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображения: " + e.getMessage());
        }

        btn.setToolTipText(tooltip);
        btn.addActionListener(e -> action.run());

        return btn;
    }

    private JButton setDimensionBtn(JButton btn, Dimension dim) {
        btn.setPreferredSize(dim);
        btn.setMinimumSize(dim);
        btn.setMaximumSize(dim);

        return btn;
    }

    private void showAboutDialog() {
        String aboutMessage = "Airport database - проект по курсу БД\n" +
                "\n" +
                "Автор: Кулишова Анастасия\n" +
                "\n" +
                "Функциональные возможности:\n" +
                "- Работа с базой данных аэропорта\n" +
                "- Работа с базой данных самолетов\n" +
                "- Работа с базой данных касс\n" +
                "- Работа с базой данных пассажиров\n";
        JOptionPane.showMessageDialog(this, aboutMessage, "О программе", JOptionPane.INFORMATION_MESSAGE);
    }

    public void addMainMenu() {
        JMenuBar menuBar = new JMenuBar();

        MainMenuPanel aboutMenu = new MainMenuPanel("Help");
        aboutMenu.addMenuItem("About", this::showAboutDialog);

        MainMenuPanel fileMenu = new MainMenuPanel("File");
        fileMenu.addMenuItem("Exit", () -> System.exit(0));

        menuBar.add(aboutMenu);
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
    }
}