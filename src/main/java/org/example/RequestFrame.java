package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class RequestFrame {
    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTable table;
    private Connection connection;
    private JTextArea queryTextArea;
    private JScrollPane tableScrollPane;

    public void show() {
        frame.setVisible(true);
    }

    public RequestFrame(Connection connection) {
        this.connection = connection;
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Запросы");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Query input area
        queryTextArea = new JTextArea(5, 80);
        queryTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane queryScrollPane = new JScrollPane(queryTextArea);

        // Execute button
        JButton executeButton = new JButton("Выполнить запрос");
        executeButton.addActionListener(e -> executeCustomQuery());

        // Query panel
        JPanel queryPanel = new JPanel(new BorderLayout());
        queryPanel.add(new JLabel("SQL запрос:"), BorderLayout.NORTH);
        queryPanel.add(queryScrollPane, BorderLayout.CENTER);
        queryPanel.add(executeButton, BorderLayout.SOUTH);

        // Table setup
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableScrollPane = new JScrollPane(table);

        // Predefined queries panel
        JPanel predefinedQueriesPanel = new JPanel();
        predefinedQueriesPanel.setLayout(new BoxLayout(predefinedQueriesPanel, BoxLayout.Y_AXIS));
        predefinedQueriesPanel.setBorder(BorderFactory.createTitledBorder("Предопределенные запросы"));

        // 1. Работники аэропорта по различным критериям
        predefinedQueriesPanel.add(createQueryButton(
                "1 - Работники по стажу, полу, возрасту, детям и зарплате",
                "SELECT s.FIO, " +
                        "TIMESTAMPDIFF(YEAR, s.Employment, CURDATE()) AS Experience, " +
                        "CASE s.Gender WHEN 'M' THEN 'Мужской' WHEN 'F' THEN 'Женский' ELSE '-' END AS Gender, " +
                        "TIMESTAMPDIFF(YEAR, s.BirthDay, CURDATE()) AS Age, " +
                        "(SELECT COUNT(*) FROM WorkersAndChildrens wc WHERE wc.WorkerID = s.WorkerID) AS ChildrenCount, " +
                        "s.Salary, d.DepartmentName " +
                        "FROM AirportStaff s " +
                        "JOIN Departments d ON s.DepartID = d.DepartmentID " +
                        "ORDER BY Experience DESC, Gender, Age"
        ));

        predefinedQueriesPanel.add(createQueryButton(
                "2 - Получить перечень и общее число работников, обслуживающих конкретный рейс, " +
                        "по возрасту, суммарной (средней) зарплате в бригаде.",
                "SELECT s.FIO, " +
                        "DATEDIFF('YEAR', s.BirthDay, CURRENT_DATE) AS Age, " +
                        "s.Salary, t.TeamName, " +
                        "(SELECT AVG(s2.Salary) FROM AirportStaff s2 WHERE s2.TeamID = s.TeamID) AS AvgTeamSalary, " +
                        "(SELECT SUM(s2.Salary) FROM AirportStaff s2 WHERE s2.TeamID = s.TeamID) AS TotalTeamSalary " +
                        "FROM AirportStaff s " +
                        "JOIN Teams t ON s.TeamID = t.TeamID " +
                        "JOIN Flights f ON (f.PylotTeam = s.TeamID OR f.TechnicTeam = s.TeamID) " +
                        "WHERE f.FlightID = 1 " + // Здесь можно заменить на параметр для конкретного рейса
                        "ORDER BY s.FIO"
        ));
// 3. Пилоты по медосмотрам (исправленная версия)
        predefinedQueriesPanel.add(createQueryButton(
                "3 - Пилоты по медосмотрам",
                "SELECT p.WorkerID, s.FIO, " +
                        "CASE s.Gender WHEN 'M' THEN 'Мужской' WHEN 'F' THEN 'Женский' ELSE '-' END AS Gender, " +
                        "TIMESTAMPDIFF(YEAR, s.BirthDay, CURDATE()) AS Age, " +
                        "s.Salary, " +
                        "CASE WHEN EXISTS (SELECT 1 FROM PylotsAndMedCheckUps pm " +
                        "WHERE pm.PylotID = p.WorkerID AND YEAR(pm.CheckUpDate) = YEAR(CURRENT_DATE) AND pm.Passed = 'Y') " +
                        "THEN 'Прошел' ELSE 'Не прошел' END AS MedicalCheck " +
                        "FROM Pylots p " +
                        "JOIN AirportStaff s ON p.WorkerID = s.WorkerID " +
                        "ORDER BY MedicalCheck, Gender, Age"
        ));

        predefinedQueriesPanel.add(createQueryButton(
                "4 - Получить перечень и общее число самолетов приписанных к аэропорту, находящихся в нем " +
                        "в указанное время, по количеству совершенных рейсов.",
                "SELECT p.PlanesID, pt.ModelName, " +
                        "(SELECT COUNT(*) FROM Flights f WHERE f.PlaneID = p.PlanesID) AS FlightCount, " +
                        "a.Name AS HomeAirport " +
                        "FROM Planes p " +
                        "JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID " +
                        "JOIN Airports a ON p.HomeAirportID = a.AirportID " +
                        "WHERE p.HomeAirportID = 1 " + // Здесь можно заменить на параметр для конкретного аэропорта
                        "AND EXISTS ( " +
                        "    SELECT 1 FROM Flights f " +
                        "    WHERE f.PlaneID = p.PlanesID " +
                        "    AND f.FromTime <= '2023-12-01 12:00:00' " + // Здесь можно заменить на параметр времени
                        "    AND f.ToTime >= '2023-12-01 12:00:00' " + // Здесь можно заменить на параметр времени
                        ") " +
                        "ORDER BY FlightCount DESC"
        ));

        predefinedQueriesPanel.add(createQueryButton(
                "5 - Получить перечень и общее число самолетов, прошедших техосмотр за определенный " +
                        "период времени, отправленных в ремонт в указанное время, ремонтированных заданное " +
                        "число раз, по количеству совершенных рейсов до ремонта.",
                "SELECT p.PlanesID, pt.ModelName, " +
                        "(SELECT COUNT(*) FROM Flights f WHERE f.PlaneID = p.PlanesID) AS FlightCount, " +
                        "(SELECT COUNT(*) FROM PlanesAndTechCheckUps tc WHERE tc.PlaneID = p.PlanesID AND tc.Passed = 'Y' " +
                        "    AND tc.TechCheckUpDate BETWEEN '2023-01-01' AND '2023-12-31') AS TechCheckCount, " + // Параметры дат
                        "(SELECT COUNT(*) FROM PlanesAndRepairCheckUps rc WHERE rc.PlaneID = p.PlanesID) AS RepairCount " +
                        "FROM Planes p " +
                        "JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID " +
                        "WHERE EXISTS ( " +
                        "    SELECT 1 FROM PlanesAndTechCheckUps tc " +
                        "    WHERE tc.PlaneID = p.PlanesID AND tc.Passed = 'Y' " +
                        "    AND tc.TechCheckUpDate BETWEEN '2023-01-01' AND '2023-12-31' " + // Параметры дат
                        ") OR EXISTS ( " +
                        "    SELECT 1 FROM PlanesAndRepairCheckUps rc " +
                        "    WHERE rc.PlaneID = p.PlanesID " +
                        "    AND rc.RepairCheckUpDate BETWEEN '2023-06-01' AND '2023-06-30' " + // Параметры дат
                        ") " +
                        "ORDER BY FlightCount DESC"
        ));

        predefinedQueriesPanel.add(createQueryButton(
                "6 - Получить перечень и общее число рейсов по указанному маршруту, по длительности " +
                        "перелета, по цене билета и по всем этим критериям сразу.",
                "SELECT f.FlightID, " +
                        "l1.LocationName AS FromLocation, " +
                        "l2.LocationName AS ToLocation, " +
                        "DATEDIFF(MINUTE, f.FromTime, f.ToTime) AS DurationMinutes, " +
                        "sc.TicketCost, " +
                        "pif.RealCapacity AS PassengersCount " +
                        "FROM Flights f " +
                        "JOIN Schedule sc ON f.JourneyID = sc.JourneyID " +
                        "JOIN Locations l1 ON f.FromLocation = l1.LocationID " +
                        "JOIN Locations l2 ON f.ToLocation = l2.LocationID " +
                        "JOIN PassengersInFlight pif ON f.FlightID = pif.FlightID " +
                        "WHERE (f.FromLocation = 1 AND f.ToLocation = 2) " + // Параметры маршрута
                        "AND DATEDIFF(MINUTE, f.FromTime, f.ToTime) BETWEEN 60 AND 180 " + // Параметры длительности
                        "AND sc.TicketCost BETWEEN 5000 AND 15000 " + // Параметры цены
                        "ORDER BY sc.TicketCost, DurationMinutes"
        ));

        predefinedQueriesPanel.add(createQueryButton(
                "7 - Получить перечень и общее число отмененных рейсов полностью, в указанном " +
                        "направлении, по указанному маршруту, по количеству невостребованных мест, по " +
                        "процентному соотношению невостребованных мест.",
                "SELECT f.FlightID, " +
                        "l1.LocationName AS FromLocation, " +
                        "l2.LocationName AS ToLocation, " +
                        "fs.StatusName, " +
                        "pif.BoughtTickets - pif.PassedTickets AS UnusedTickets, " +
                        "CAST((pif.BoughtTickets - pif.PassedTickets) AS FLOAT) / pif.BoughtTickets * 100 AS UnusedPercentage " +
                        "FROM Flights f " +
                        "JOIN FlightsStatusForHistory fs ON f.FlightStatus = fs.StatusID " +
                        "JOIN PassengersInFlight pif ON f.FlightID = pif.FlightID " +
                        "JOIN Locations l1 ON f.FromLocation = l1.LocationID " +
                        "JOIN Locations l2 ON f.ToLocation = l2.LocationID " +
                        "WHERE fs.StatusName = 'Cancelled' " + // Параметр статуса
                        "AND (f.FromLocation = 1 OR f.ToLocation = 2) " + // Параметры направления
                        "AND (pif.BoughtTickets - pif.PassedTickets) > 0 " + // Параметр невостребованных мест
                        "ORDER BY UnusedPercentage DESC"
        ));

        predefinedQueriesPanel.add(createQueryButton(
                "8 - Получить перечень и общее число задержанных рейсов полностью, по указанной причине, " +
                        "по указанному маршруту, и количество сданных билетов за время задержки.",
                "SELECT f.FlightID, " +
                        "l1.LocationName AS FromLocation, " +
                        "l2.LocationName AS ToLocation, " +
                        "fs.StatusName, " +
                        "DATEDIFF(MINUTE, f.ScheduleFromTime, f.FromTime) AS DelayMinutes, " +
                        "pif.PassengersNotCame AS CancelledTicketsDuringDelay " +
                        "FROM Flights f " +
                        "JOIN FlightsStatusForHistory fs ON f.FlightStatus = fs.StatusID " +
                        "JOIN PassengersInFlight pif ON f.FlightID = pif.FlightID " +
                        "JOIN Locations l1 ON f.FromLocation = l1.LocationID " +
                        "JOIN Locations l2 ON f.ToLocation = l2.LocationID " +
                        "WHERE fs.StatusName = 'Delayed' " + // Параметр статуса
                        "AND (f.FromLocation = 1 AND f.ToLocation = 2) " + // Параметры маршрута
                        "AND DATEDIFF(MINUTE, f.ScheduleFromTime, f.FromTime) > 0 " + // Параметр задержки
                        "ORDER BY DelayMinutes DESC"
        ));

        predefinedQueriesPanel.add(createQueryButton(
                "9 - Получить перечень и общее число рейсов, по которым летают самолеты заданного типа и " +
                        "среднее количество проданных билетов на определенные маршруты, по длительности " +
                        "перелета, по цене билета, времени вылета.",
                "SELECT f.FlightID, " +
                        "pt.ModelName AS PlaneType, " +
                        "l1.LocationName AS FromLocation, " +
                        "l2.LocationName AS ToLocation, " +
                        "AVG(pif.BoughtTickets) AS AvgTicketsSold, " +
                        "DATEDIFF(MINUTE, f.FromTime, f.ToTime) AS DurationMinutes, " +
                        "sc.TicketCost, " +
                        "CONVERT(VARCHAR, f.FromTime, 108) AS DepartureTime " +
                        "FROM Flights f " +
                        "JOIN Planes p ON f.PlaneID = p.PlanesID " +
                        "JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID " +
                        "JOIN Schedule sc ON f.JourneyID = sc.JourneyID " +
                        "JOIN PassengersInFlight pif ON f.FlightID = pif.FlightID " +
                        "JOIN Locations l1 ON f.FromLocation = l1.LocationID " +
                        "JOIN Locations l2 ON f.ToLocation = l2.LocationID " +
                        "WHERE p.PlaneTypeID = 1 " + // Параметр типа самолета
                        "GROUP BY f.FlightID, pt.ModelName, l1.LocationName, l2.LocationName, " +
                        "DATEDIFF(MINUTE, f.FromTime, f.ToTime), sc.TicketCost, f.FromTime " +
                        "ORDER BY AvgTicketsSold DESC"
        ));

        predefinedQueriesPanel.add(createQueryButton(
                "10 - Получить перечень и общее число авиарейсов указанной категории, в определенном " +
                        "направлении, с указанным типом самолета.",
                "SELECT f.FlightID, " +
                        "CASE " +
                        "    WHEN ifl.FlightID IS NOT NULL THEN 'Internal' " +
                        "    WHEN intfl.FlightID IS NOT NULL THEN 'International' " +
                        "    WHEN cf.FlightID IS NOT NULL THEN 'Cargo' " +
                        "    WHEN chf.FlightID IS NOT NULL THEN 'Charter' " +
                        "    WHEN sf.FlightID IS NOT NULL THEN 'Special' " +
                        "END AS FlightCategory, " +
                        "pt.ModelName AS PlaneType, " +
                        "l1.LocationName AS FromLocation, " +
                        "l2.LocationName AS ToLocation " +
                        "FROM Flights f " +
                        "LEFT JOIN InternalFlight ifl ON f.FlightID = ifl.FlightID " +
                        "LEFT JOIN InternationalFlight intfl ON f.FlightID = intfl.FlightID " +
                        "LEFT JOIN CargoFlight cf ON f.FlightID = cf.FlightID " +
                        "LEFT JOIN CharterFlight chf ON f.FlightID = chf.FlightID " +
                        "LEFT JOIN SpecialFlight sf ON f.FlightID = sf.FlightID " +
                        "JOIN Planes p ON f.PlaneID = p.PlanesID " +
                        "JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID " +
                        "JOIN Locations l1 ON f.FromLocation = l1.LocationID " +
                        "JOIN Locations l2 ON f.ToLocation = l2.LocationID " +
                        "WHERE (ifl.FlightID IS NOT NULL OR intfl.FlightID IS NOT NULL OR " +
                        "      cf.FlightID IS NOT NULL OR chf.FlightID IS NOT NULL OR sf.FlightID IS NOT NULL) " +
                        "AND (f.FromLocation = 1 OR f.ToLocation = 2) " + // Параметры направления
                        "AND p.PlaneTypeID = 1 " + // Параметр типа самолета
                        "ORDER BY FlightCategory"
        ));

        predefinedQueriesPanel.add(createQueryButton(
                "11 - Получить перечень и общее число пассажиров на данном рейсе, улетевших в указанный " +
                        "день, улетевших за границу в указанный день, по признаку сдачи вещей в багажное " +
                        "отделение, по половому признаку, по возрасту.",
                "SELECT p.PassengerID, p.FIO, " +
                        "DATEDIFF('YEAR', p.BirthDay, CURRENT_DATE) AS Age, " +
                        "p.Gender, " +
                        "CASE WHEN t.BaggageTypeID IS NOT NULL THEN 'Yes' ELSE 'No' END AS HasBaggage, " +
                        "CASE WHEN intf.FlightID IS NOT NULL THEN 'International' ELSE 'Domestic' END AS FlightType " +
                        "FROM Passengers p " +
                        "JOIN TicketsStory ts ON p.PassengerID = ts.PassengerID " +
                        "JOIN Tickets t ON ts.TicketID = t.TicketID " +
                        "JOIN Flights f ON t.FlightID = f.FlightID " +
                        "LEFT JOIN InternationalFlight intf ON f.FlightID = intf.FlightID " +
                        "WHERE f.FlightID = 1 " + // Параметр рейса
                        "AND CONVERT(DATE, f.FromTime) = '2023-12-01' " + // Параметр даты
                        "AND (p.Gender = 'M' OR p.Gender = 'F') " + // Параметр пола
                        "AND DATEDIFF('YEAR', p.BirthDay, CURRENT_DATE) BETWEEN 18 AND 65 " + // Параметры возраста
                        "ORDER BY p.FIO"
        ));

        predefinedQueriesPanel.add(createQueryButton(
                "12 - Получить перечень и общее число свободных и забронированных мест на указанном рейсе, " +
                        "на определенный день, по указанному маршруту, по цене, по времени вылета.",
                "SELECT " +
                        "s.SeatID, s.SeatName, " +
                        "CASE WHEN t.TicketID IS NOT NULL THEN 'Booked' ELSE 'Free' END AS Status, " +
                        "t.TicketCost " +
                        "FROM Seats s " +
                        "LEFT JOIN Tickets t ON s.SeatID = t.SeatID AND t.FlightID = 1 " + // Параметр рейса
                        "WHERE EXISTS ( " +
                        "    SELECT 1 FROM Flights f " +
                        "    WHERE f.FlightID = 1 " + // Параметр рейса
                        "    AND CONVERT(DATE, f.FromTime) = '2023-12-01' " + // Параметр даты
                        "    AND (f.FromLocation = 1 AND f.ToLocation = 2) " + // Параметры маршрута
                        ") " +
                        "ORDER BY s.SeatName"
        ));

        predefinedQueriesPanel.add(createQueryButton(
                "13 - Получить общее число сданных билетов на некоторый рейс, в указанный день, по " +
                        "определенному маршруту, по цене билета, по возрасту, полу.",
                "SELECT " +
                        "COUNT(*) AS CancelledTicketsCount, " +
                        "AVG(t.TicketCost) AS AvgTicketPrice, " +
                        "AVG(DATEDIFF('YEAR', p.BirthDay, CURRENT_DATE)) AS AvgPassengerAge " +
                        "FROM TicketsStory ts " +
                        "JOIN Tickets t ON ts.TicketID = t.TicketID " +
                        "JOIN Passengers p ON ts.PassengerID = p.PassengerID " +
                        "JOIN Flights f ON t.FlightID = f.FlightID " +
                        "WHERE ts.Status = 'Cancelled' " +
                        "AND f.FlightID = 1 " + // Параметр рейса
                        "AND CONVERT(DATE, f.FromTime) = '2023-12-01' " + // Параметр даты
                        "AND (f.FromLocation = 1 AND f.ToLocation = 2) " + // Параметры маршрута
                        "AND t.TicketCost BETWEEN 5000 AND 15000 " + // Параметры цены
                        "AND DATEDIFF('YEAR', p.BirthDay, CURRENT_DATE) BETWEEN 18 AND 65 " + // Параметры возраста
                        "AND p.Gender = 'M'" // Параметр пола
        ));

        // Add components to main panel
        mainPanel.add(queryPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(predefinedQueriesPanel),
                tableScrollPane);
        splitPane.setResizeWeight(0.5);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        frame.add(mainPanel);
    }

    private JButton createQueryButton(String text, String query) {
        JButton button = new JButton("<html>" + text.replaceAll("\n", "<br>") + "</html>");
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        button.addActionListener(e -> {
            queryTextArea.setText(query);
            executeQuery(query);
        });
        return button;
    }

    private void executeCustomQuery() {
        String query = queryTextArea.getText().trim();
        if (!query.isEmpty()) {
            executeQuery(query);
        } else {
            JOptionPane.showMessageDialog(frame, "Введите SQL запрос", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeQuery(String query) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Get metadata
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Create new table model
            DefaultTableModel newModel = new DefaultTableModel();

            // Add column names
            for (int i = 1; i <= columnCount; i++) {
                newModel.addColumn(metaData.getColumnName(i));
            }

            // Add data rows
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                newModel.addRow(row);
            }

            // Set the new model
            table.setModel(newModel);

            // Adjust column widths
            for (int i = 0; i < table.getColumnCount(); i++) {
                TableColumn column = table.getColumnModel().getColumn(i);
                column.setPreferredWidth(150);
            }

            // Show row count
            JOptionPane.showMessageDialog(frame,
                    "Найдено записей: " + newModel.getRowCount(),
                    "Результат",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame,
                    "Ошибка выполнения запроса: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}