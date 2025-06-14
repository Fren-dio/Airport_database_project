package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;
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
                "1 - Получить список и общее число всех pаботников аэpопоpта по стажу pаботы в аэpопоpту,\n" +
                        "половому пpизнаку, возpасту, количеству детей, по pазмеpу заpаботной платы.",
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
                "3 - Получить перечень и общее число пилотов, пpошедших медосмотp либо не пpошедших его\n" +
                        "в указанный год, по половому пpизнаку, возpасту, pазмеpу заpаботной платы.",
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
                "SELECT f.FlightID, \n" +
                        "       l1.LocationName AS FromLocation, \n" +
                        "       l2.LocationName AS ToLocation, \n" +
                        "       fs.StatucName AS StatusName,\n" +
                        "       pif.BoughtTickets - pif.PassedTickets AS UnusedTickets, \n" +
                        "       CASE \n" +
                        "           WHEN pif.BoughtTickets > 0 THEN \n" +
                        "               CAST((pif.BoughtTickets - pif.PassedTickets) AS FLOAT) / pif.BoughtTickets * 100 \n" +
                        "           ELSE 0 \n" +
                        "       END AS UnusedPercentage \n" +
                        "FROM Flights f \n" +
                        "JOIN FlightsStatusForHistory fs ON f.FlightStatus = fs.StatusID \n" +
                        "JOIN PassengersInFlight pif ON f.FlightID = pif.FlightID \n" +
                        "JOIN Locations l1 ON f.FromLocation = l1.LocationID \n" +
                        "JOIN Locations l2 ON f.ToLocation = l2.LocationID \n" +
                        "WHERE fs.StatucName = 'Cancelled'\n" +
                        "ORDER BY UnusedPercentage DESC"
        ));

        predefinedQueriesPanel.add(createQueryButton(
                "8 - Получить перечень и общее число задержанных рейсов полностью, по указанной причине, " +
                        "по указанному маршруту, и количество сданных билетов за время задержки.",
                "SELECT \n" +
                        "    f.FlightID,\n" +
                        "    loc_from.LocationName AS DepartureAirport,\n" +
                        "    loc_to.LocationName AS ArrivalAirport,\n" +
                        "    f.ScheduleFromTime AS ScheduledDeparture,\n" +
                        "    f.FromTime AS ActualDeparture,\n" +
                        "    DATEDIFF(MINUTE, f.ScheduleFromTime, f.FromTime) AS DelayMinutes,\n" +
                        "    fs.StatucName AS FlightStatus,\n" +
                        "    wc.NameOfCondition AS WeatherCondition,\n" +
                        "    CASE \n" +
                        "        WHEN wc.NeedWait = 'Y' THEN 'Weather Conditions'\n" +
                        "        WHEN fs.StatucName = 'Technical Issues' THEN 'Technical Issues'\n" +
                        "        WHEN fs.StatucName = 'Crew Issues' THEN 'Crew Issues'\n" +
                        "        WHEN fs.StatucName = 'Air Traffic Control' THEN 'Air Traffic Control'\n" +
                        "        ELSE 'Other Reasons'\n" +
                        "    END AS DelayReason,\n" +
                        "    (\n" +
                        "        SELECT COUNT(*) \n" +
                        "        FROM Tickets t\n" +
                        "        JOIN TicketsStory ts ON t.TicketID = ts.TicketID\n" +
                        "        WHERE t.FlightID = f.FlightID\n" +
                        "        AND ts.Status = 'Returned'\n" +
                        "        AND ts.TimeStatusUpdate BETWEEN f.ScheduleFromTime AND f.FromTime\n" +
                        "    ) AS ReturnedTicketsDuringDelay\n" +
                        "FROM \n" +
                        "    Flights f\n" +
                        "    JOIN Locations loc_from ON f.FromLocation = loc_from.LocationID\n" +
                        "    JOIN Locations loc_to ON f.ToLocation = loc_to.LocationID\n" +
                        "    LEFT JOIN WeatherConditions wc ON f.WeatherFromLocation = wc.weatherID\n" +
                        "    LEFT JOIN FlightsStatusForHistory fs ON f.FlightStatus = fs.StatusID\n" +
                        "WHERE \n" +
                        "    f.FromTime > f.ScheduleFromTime\n" +
                        "ORDER BY \n" +
                        "    f.FromTime DESC;\n"
        ));

        predefinedQueriesPanel.add(createQueryButton(
                "9 - Получить перечень и общее число рейсов, по которым летают самолеты заданного типа и " +
                        "среднее количество проданных билетов на определенные маршруты, по длительности " +
                        "перелета, по цене билета, времени вылета.",
                "SELECT f.FlightID, \n" +
                        "       pt.ModelName AS PlaneType, \n" +
                        "       l1.LocationName AS FromLocation, \n" +
                        "       l2.LocationName AS ToLocation, \n" +
                        "       AVG(pif.BoughtTickets) AS AvgTicketsSold, \n" +
                        "       DATEDIFF('MINUTE', f.FromTime, f.ToTime) AS DurationMinutes, \n" +
                        "       sc.TicketCost, \n" +
                        "       FORMATDATETIME(f.FromTime, 'HH:mm:ss') AS DepartureTime \n" +
                        "FROM Flights f\n" +
                        "JOIN Planes p ON f.PlaneID = p.PlanesID \n" +
                        "JOIN PlaneTypeCharacteristics pt ON p.PlaneTypeID = pt.TypeID \n" +
                        "JOIN Schedule sc ON f.JourneyID = sc.JourneyID \n" +
                        "JOIN PassengersInFlight pif ON f.FlightID = pif.FlightID \n" +
                        "JOIN Locations l1 ON f.FromLocation = l1.LocationID \n" +
                        "JOIN Locations l2 ON f.ToLocation = l2.LocationID \n" +
                        "GROUP BY f.FlightID, pt.ModelName, l1.LocationName, l2.LocationName, \n" +
                        "         DATEDIFF('MINUTE', f.FromTime, f.ToTime), sc.TicketCost, f.FromTime \n" +
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
                "SELECT p.PassengerID, \n" +
                        "       p.FIO, \n" +
                        "       CASE WHEN t.BaggageTypeID IS NOT NULL THEN 'Yes' ELSE 'No' END AS HasBaggage, \n" +
                        "       CASE WHEN intf.FlightID IS NOT NULL THEN 'International' ELSE 'Domestic' END AS FlightType \n" +
                        "FROM Passengers p \n" +
                        "JOIN TicketsStory ts ON p.PassengerID = ts.PassengerID \n" +
                        "JOIN Tickets t ON ts.TicketID = t.TicketID \n" +
                        "JOIN Flights f ON t.FlightID = f.FlightID \n" +
                        "LEFT JOIN InternationalFlight intf ON f.FlightID = intf.FlightID \n" +
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
                        "LEFT JOIN Tickets t ON s.SeatID = t.Seat AND t.FlightID = 1 " + // Параметр рейса
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
                "SELECT \n" +
                        "    s.SeatID, \n" +
                        "    s.SeatName, \n" +
                        "    CASE \n" +
                        "        WHEN t.TicketID IS NOT NULL AND t.TciketStatus IN ('BOOKED', 'SOLD') THEN 'Occupied' \n" +
                        "        ELSE 'Free' \n" +
                        "    END AS Status,\n" +
                        "    COALESCE(t.TicketCost, sc.TicketCost) AS Price,\n" +
                        "    f.FromTime AS DepartureTime\n" +
                        "FROM Seats s\n" +
                        "LEFT JOIN Tickets t ON t.Seat = s.SeatID AND t.FlightID = f.FlightID\n" +
                        "LEFT JOIN Schedule sc ON f.JourneyID = sc.JourneyID\n" +
                        "ORDER BY s.SeatName"
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
            String finalQuery = query;
            boolean cancelled = false;

            // Для запроса 1 (работники по критериям)
            if (text.startsWith("1 -")) {
                // Этот запрос не требует параметров
            }
            // Для запроса 2 (работники обслуживающие рейс)
            else if (text.startsWith("2 -")) {
                String flightId = JOptionPane.showInputDialog(frame,
                        "Введите ID рейса (оставьте пустым для всех рейсов):", "");
                if (flightId == null) {
                    cancelled = true;
                } else if (!flightId.isEmpty()) {
                    finalQuery = finalQuery.replace("WHERE f.FlightID = 1",
                            "WHERE f.FlightID = " + flightId);
                }
            }
            // Для запроса 3 (пилоты по медосмотрам)
            else if (text.startsWith("3 -")) {
                String year = JOptionPane.showInputDialog(frame,
                        "Введите год для проверки медосмотров (оставьте пустым для текущего года):",
                        "");
                if (year == null) {
                    cancelled = true;
                } else if (!year.isEmpty()) {
                    finalQuery = finalQuery.replace("YEAR(CURRENT_DATE)", year);
                }
            }
            // Для запроса 4 (самолеты приписанные к аэропорту)
            else if (text.startsWith("4 -")) {
                String airportId = JOptionPane.showInputDialog(frame,
                        "Введите ID аэропорта (оставьте пустым для всех аэропортов):", "");
                String time = JOptionPane.showInputDialog(frame,
                        "Введите время проверки (гггг-мм-дд чч:мм:сс, оставьте пустым для текущего времени):",
                        "");

                if (airportId == null || time == null) {
                    cancelled = true;
                } else {
                    if (!airportId.isEmpty()) {
                        finalQuery = finalQuery.replace("WHERE p.HomeAirportID = 1",
                                "WHERE p.HomeAirportID = " + airportId);
                    }
                    if (!time.isEmpty()) {
                        finalQuery = finalQuery
                                .replace("f.FromTime <= '2023-12-01 12:00:00'", "f.FromTime <= '" + time + "'")
                                .replace("f.ToTime >= '2023-12-01 12:00:00'", "f.ToTime >= '" + time + "'");
                    }
                }
            }
            // Для запроса 5 (самолеты по техосмотрам)
            else if (text.startsWith("5 -")) {
                String startDate = JOptionPane.showInputDialog(frame,
                        "Введите начальную дату периода (гггг-мм-дд, оставьте пустым без ограничения):", "");
                String endDate = JOptionPane.showInputDialog(frame,
                        "Введите конечную дату периода (гггг-мм-дд, оставьте пустым без ограничения):", "");

                if (startDate == null || endDate == null) {
                    cancelled = true;
                } else {
                    if (!startDate.isEmpty() && !endDate.isEmpty()) {
                        finalQuery = finalQuery
                                .replace("tc.TechCheckUpDate BETWEEN '2023-01-01' AND '2023-12-31'",
                                        "tc.TechCheckUpDate BETWEEN '" + startDate + "' AND '" + endDate + "'")
                                .replace("rc.RepairCheckUpDate BETWEEN '2023-06-01' AND '2023-06-30'",
                                        "rc.RepairCheckUpDate BETWEEN '" + startDate + "' AND '" + endDate + "'");
                    }
                }
            }
            // Для запроса 6 (рейсы по маршруту)
            else if (text.startsWith("6 -")) {
                JPanel panel = new JPanel(new GridLayout(0, 2));

                JTextField fromLocField = new JTextField();
                JTextField toLocField = new JTextField();
                JTextField minDurationField = new JTextField();
                JTextField maxDurationField = new JTextField();
                JTextField minPriceField = new JTextField();
                JTextField maxPriceField = new JTextField();

                panel.add(new JLabel("ID начального пункта:"));
                panel.add(fromLocField);
                panel.add(new JLabel("ID конечного пункта:"));
                panel.add(toLocField);
                panel.add(new JLabel("Мин. длительность (мин):"));
                panel.add(minDurationField);
                panel.add(new JLabel("Макс. длительность (мин):"));
                panel.add(maxDurationField);
                panel.add(new JLabel("Мин. цена билета:"));
                panel.add(minPriceField);
                panel.add(new JLabel("Макс. цена билета:"));
                panel.add(maxPriceField);

                int result = JOptionPane.showConfirmDialog(frame, panel,
                        "Параметры поиска рейсов", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    StringBuilder whereClause = new StringBuilder("WHERE 1=1");

                    if (!fromLocField.getText().isEmpty()) {
                        whereClause.append(" AND f.FromLocation = ").append(fromLocField.getText());
                    }
                    if (!toLocField.getText().isEmpty()) {
                        whereClause.append(" AND f.ToLocation = ").append(toLocField.getText());
                    }
                    if (!minDurationField.getText().isEmpty()) {
                        whereClause.append(" AND DATEDIFF(MINUTE, f.FromTime, f.ToTime) >= ")
                                .append(minDurationField.getText());
                    }
                    if (!maxDurationField.getText().isEmpty()) {
                        whereClause.append(" AND DATEDIFF(MINUTE, f.FromTime, f.ToTime) <= ")
                                .append(maxDurationField.getText());
                    }
                    if (!minPriceField.getText().isEmpty()) {
                        whereClause.append(" AND sc.TicketCost >= ").append(minPriceField.getText());
                    }
                    if (!maxPriceField.getText().isEmpty()) {
                        whereClause.append(" AND sc.TicketCost <= ").append(maxPriceField.getText());
                    }

                    finalQuery = finalQuery.replace(
                            "WHERE (f.FromLocation = 1 AND f.ToLocation = 2) " +
                                    "AND DATEDIFF(MINUTE, f.FromTime, f.ToTime) BETWEEN 60 AND 180 " +
                                    "AND sc.TicketCost BETWEEN 5000 AND 15000",
                            whereClause.toString()
                    );
                } else {
                    cancelled = true;
                }
            }
            // Для запроса 7 (отмененные рейсы)
            else if (text.startsWith("7 -")) {
                JPanel panel = new JPanel(new GridLayout(0, 2));

                JTextField fromLocField = new JTextField();
                JTextField toLocField = new JTextField();
                JTextField minUnusedField = new JTextField();

                panel.add(new JLabel("ID начального пункта:"));
                panel.add(fromLocField);
                panel.add(new JLabel("ID конечного пункта:"));
                panel.add(toLocField);
                panel.add(new JLabel("Мин. невостребованных мест:"));
                panel.add(minUnusedField);

                int result = JOptionPane.showConfirmDialog(frame, panel,
                        "Параметры отмененных рейсов", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    StringBuilder whereClause = new StringBuilder("WHERE fs.StatusName = 'Cancelled'");

                    if (!fromLocField.getText().isEmpty()) {
                        whereClause.append(" AND f.FromLocation = ").append(fromLocField.getText());
                    }
                    if (!toLocField.getText().isEmpty()) {
                        whereClause.append(" OR f.ToLocation = ").append(toLocField.getText());
                    }
                    if (!minUnusedField.getText().isEmpty()) {
                        whereClause.append(" AND (pif.BoughtTickets - pif.PassedTickets) >= ")
                                .append(minUnusedField.getText());
                    }

                    finalQuery = finalQuery.replace(
                            "WHERE fs.StatusName = 'Cancelled' " +
                                    "AND (f.FromLocation = 1 OR f.ToLocation = 2) " +
                                    "AND (pif.BoughtTickets - pif.PassedTickets) > 0",
                            whereClause.toString()
                    );
                } else {
                    cancelled = true;
                }
            }
            // Для запроса 8 (задержанные рейсы)
            else if (text.startsWith("8 -")) {
                JPanel panel = new JPanel(new GridLayout(0, 2));

                JTextField fromLocField = new JTextField();
                JTextField toLocField = new JTextField();
                JTextField minDelayField = new JTextField();

                panel.add(new JLabel("ID начального пункта:"));
                panel.add(fromLocField);
                panel.add(new JLabel("ID конечного пункта:"));
                panel.add(toLocField);
                panel.add(new JLabel("Мин. задержка (мин):"));
                panel.add(minDelayField);

                int result = JOptionPane.showConfirmDialog(frame, panel,
                        "Параметры задержанных рейсов", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    StringBuilder whereClause = new StringBuilder("WHERE fs.StatusName = 'Delayed'");

                    if (!fromLocField.getText().isEmpty()) {
                        whereClause.append(" AND f.FromLocation = ").append(fromLocField.getText());
                    }
                    if (!toLocField.getText().isEmpty()) {
                        whereClause.append(" AND f.ToLocation = ").append(toLocField.getText());
                    }
                    if (!minDelayField.getText().isEmpty()) {
                        whereClause.append(" AND DATEDIFF(MINUTE, f.ScheduleFromTime, f.FromTime) >= ")
                                .append(minDelayField.getText());
                    }

                    finalQuery = finalQuery.replace(
                            "WHERE fs.StatusName = 'Delayed' " +
                                    "AND (f.FromLocation = 1 AND f.ToLocation = 2) " +
                                    "AND DATEDIFF(MINUTE, f.ScheduleFromTime, f.FromTime) > 0",
                            whereClause.toString()
                    );
                } else {
                    cancelled = true;
                }
            }
            // Для запроса 9 (рейсы по типу самолета)
            else if (text.startsWith("9 -")) {
                String planeType = JOptionPane.showInputDialog(frame,
                        "Введите ID типа самолета (оставьте пустым для всех типов):", "");

                if (planeType == null) {
                    cancelled = true;
                } else if (!planeType.isEmpty()) {
                    finalQuery = finalQuery.replace("WHERE p.PlaneTypeID = 1",
                            "WHERE p.PlaneTypeID = " + planeType);
                }
            }
            // Для запроса 10 (рейсы по категории)
            else if (text.startsWith("10 -")) {
                JPanel panel = new JPanel(new GridLayout(0, 2));

                JComboBox<String> categoryCombo = new JComboBox<>(new String[] {
                        "Все", "Internal", "International", "Cargo", "Charter", "Special"
                });
                JTextField fromLocField = new JTextField();
                JTextField toLocField = new JTextField();
                JTextField planeTypeField = new JTextField();

                panel.add(new JLabel("Категория рейса:"));
                panel.add(categoryCombo);
                panel.add(new JLabel("ID начального пункта:"));
                panel.add(fromLocField);
                panel.add(new JLabel("ID конечного пункта:"));
                panel.add(toLocField);
                panel.add(new JLabel("ID типа самолета:"));
                panel.add(planeTypeField);

                int result = JOptionPane.showConfirmDialog(frame, panel,
                        "Параметры поиска рейсов", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    StringBuilder whereClause = new StringBuilder("WHERE 1=1");

                    if (categoryCombo.getSelectedIndex() > 0) {
                        String category = (String)categoryCombo.getSelectedItem();
                        whereClause.append(" AND ")
                                .append(category.toLowerCase())
                                .append("f.FlightID IS NOT NULL");
                    }
                    if (!fromLocField.getText().isEmpty()) {
                        whereClause.append(" AND f.FromLocation = ").append(fromLocField.getText());
                    }
                    if (!toLocField.getText().isEmpty()) {
                        whereClause.append(" OR f.ToLocation = ").append(toLocField.getText());
                    }
                    if (!planeTypeField.getText().isEmpty()) {
                        whereClause.append(" AND p.PlaneTypeID = ").append(planeTypeField.getText());
                    }

                    finalQuery = finalQuery.replace(
                            "WHERE (ifl.FlightID IS NOT NULL OR intfl.FlightID IS NOT NULL OR " +
                                    "cf.FlightID IS NOT NULL OR chf.FlightID IS NOT NULL OR sf.FlightID IS NOT NULL) " +
                                    "AND (f.FromLocation = 1 OR f.ToLocation = 2) " +
                                    "AND p.PlaneTypeID = 1",
                            whereClause.toString()
                    );
                } else {
                    cancelled = true;
                }
            }
            // Для запроса 11 (пассажиры на рейсе)
            else if (text.startsWith("11 -")) {
                JPanel panel = new JPanel(new GridLayout(0, 2));

                JTextField flightIdField = new JTextField();
                JTextField dateField = new JTextField();
                JComboBox<String> genderCombo = new JComboBox<>(new String[] {
                        "Все", "Мужской", "Женский"
                });
                JTextField minAgeField = new JTextField();
                JTextField maxAgeField = new JTextField();
                JCheckBox baggageCheck = new JCheckBox("Только с багажом");
                JCheckBox intlCheck = new JCheckBox("Только международные");

                panel.add(new JLabel("ID рейса:"));
                panel.add(flightIdField);
                panel.add(new JLabel("Дата (гггг-мм-дд):"));
                panel.add(dateField);
                panel.add(new JLabel("Пол:"));
                panel.add(genderCombo);
                panel.add(new JLabel("Мин. возраст:"));
                panel.add(minAgeField);
                panel.add(new JLabel("Макс. возраст:"));
                panel.add(maxAgeField);
                panel.add(baggageCheck);
                panel.add(intlCheck);

                int result = JOptionPane.showConfirmDialog(frame, panel,
                        "Параметры поиска пассажиров", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    StringBuilder whereClause = new StringBuilder("WHERE 1=1");

                    if (!flightIdField.getText().isEmpty()) {
                        whereClause.append(" AND f.FlightID = ").append(flightIdField.getText());
                    }
                    if (!dateField.getText().isEmpty()) {
                        whereClause.append(" AND CONVERT(DATE, f.FromTime) = '")
                                .append(dateField.getText()).append("'");
                    }
                    if (genderCombo.getSelectedIndex() > 0) {
                        whereClause.append(" AND p.Gender = '")
                                .append(genderCombo.getSelectedIndex() == 1 ? "M" : "F")
                                .append("'");
                    }
                    if (!minAgeField.getText().isEmpty()) {
                        whereClause.append(" AND DATEDIFF(YEAR, p.BirthDay, CURRENT_DATE) >= ")
                                .append(minAgeField.getText());
                    }
                    if (!maxAgeField.getText().isEmpty()) {
                        whereClause.append(" AND DATEDIFF(YEAR, p.BirthDay, CURRENT_DATE) <= ")
                                .append(maxAgeField.getText());
                    }
                    if (baggageCheck.isSelected()) {
                        whereClause.append(" AND t.BaggageTypeID IS NOT NULL");
                    }
                    if (intlCheck.isSelected()) {
                        whereClause.append(" AND intf.FlightID IS NOT NULL");
                    }

                    finalQuery = finalQuery.replace(
                            "WHERE f.FlightID = 1 " +
                                    "AND CONVERT(DATE, f.FromTime) = '2023-12-01' " +
                                    "AND (p.Gender = 'M' OR p.Gender = 'F') " +
                                    "AND DATEDIFF('YEAR', p.BirthDay, CURRENT_DATE) BETWEEN 18 AND 65",
                            whereClause.toString()
                    );
                } else {
                    cancelled = true;
                }
            }
            // Для запроса 12 (свободные места)
            else if (text.startsWith("12 -")) {
                JPanel panel = new JPanel(new GridLayout(0, 2));

                JTextField flightIdField = new JTextField();
                JTextField dateField = new JTextField();
                JTextField fromLocField = new JTextField();
                JTextField toLocField = new JTextField();
                JTextField minPriceField = new JTextField();
                JTextField maxPriceField = new JTextField();

                panel.add(new JLabel("ID рейса:"));
                panel.add(flightIdField);
                panel.add(new JLabel("Дата (гггг-мм-дд):"));
                panel.add(dateField);
                panel.add(new JLabel("ID начального пункта:"));
                panel.add(fromLocField);
                panel.add(new JLabel("ID конечного пункта:"));
                panel.add(toLocField);
                panel.add(new JLabel("Мин. цена билета:"));
                panel.add(minPriceField);
                panel.add(new JLabel("Макс. цена билета:"));
                panel.add(maxPriceField);

                int result = JOptionPane.showConfirmDialog(frame, panel,
                        "Параметры поиска мест", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    StringBuilder whereClause = new StringBuilder("WHERE 1=1");

                    if (!flightIdField.getText().isEmpty()) {
                        whereClause.append(" AND f.FlightID = ").append(flightIdField.getText());
                    }
                    if (!dateField.getText().isEmpty()) {
                        whereClause.append(" AND CONVERT(DATE, f.FromTime) = '")
                                .append(dateField.getText()).append("'");
                    }
                    if (!fromLocField.getText().isEmpty()) {
                        whereClause.append(" AND f.FromLocation = ").append(fromLocField.getText());
                    }
                    if (!toLocField.getText().isEmpty()) {
                        whereClause.append(" AND f.ToLocation = ").append(toLocField.getText());
                    }
                    if (!minPriceField.getText().isEmpty()) {
                        whereClause.append(" AND t.TicketCost >= ").append(minPriceField.getText());
                    }
                    if (!maxPriceField.getText().isEmpty()) {
                        whereClause.append(" AND t.TicketCost <= ").append(maxPriceField.getText());
                    }

                    finalQuery = finalQuery.replace(
                            "WHERE EXISTS ( " +
                                    "    SELECT 1 FROM Flights f " +
                                    "    WHERE f.FlightID = 1 " +
                                    "    AND CONVERT(DATE, f.FromTime) = '2023-12-01' " +
                                    "    AND (f.FromLocation = 1 AND f.ToLocation = 2) " +
                                    ")",
                            whereClause.toString()
                    );
                } else {
                    cancelled = true;
                }
            }
            // Для запроса 13 (сданные билеты)
            else if (text.startsWith("13 -")) {
                JPanel panel = new JPanel(new GridLayout(0, 2));

                JTextField flightIdField = new JTextField();
                JTextField dateField = new JTextField();
                JTextField fromLocField = new JTextField();
                JTextField toLocField = new JTextField();
                JTextField minPriceField = new JTextField();
                JTextField maxPriceField = new JTextField();
                JTextField minAgeField = new JTextField();
                JTextField maxAgeField = new JTextField();
                JComboBox<String> genderCombo = new JComboBox<>(new String[] {
                        "Все", "Мужской", "Женский"
                });

                panel.add(new JLabel("ID рейса:"));
                panel.add(flightIdField);
                panel.add(new JLabel("Дата (гггг-мм-дд):"));
                panel.add(dateField);
                panel.add(new JLabel("ID начального пункта:"));
                panel.add(fromLocField);
                panel.add(new JLabel("ID конечного пункта:"));
                panel.add(toLocField);
                panel.add(new JLabel("Мин. цена билета:"));
                panel.add(minPriceField);
                panel.add(new JLabel("Макс. цена билета:"));
                panel.add(maxPriceField);
                panel.add(new JLabel("Мин. возраст:"));
                panel.add(minAgeField);
                panel.add(new JLabel("Макс. возраст:"));
                panel.add(maxAgeField);
                panel.add(new JLabel("Пол:"));
                panel.add(genderCombo);

                int result = JOptionPane.showConfirmDialog(frame, panel,
                        "Параметры сданных билетов", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    StringBuilder whereClause = new StringBuilder("WHERE ts.Status = 'Cancelled'");

                    if (!flightIdField.getText().isEmpty()) {
                        whereClause.append(" AND f.FlightID = ").append(flightIdField.getText());
                    }
                    if (!dateField.getText().isEmpty()) {
                        whereClause.append(" AND CONVERT(DATE, f.FromTime) = '")
                                .append(dateField.getText()).append("'");
                    }
                    if (!fromLocField.getText().isEmpty()) {
                        whereClause.append(" AND f.FromLocation = ").append(fromLocField.getText());
                    }
                    if (!toLocField.getText().isEmpty()) {
                        whereClause.append(" AND f.ToLocation = ").append(toLocField.getText());
                    }
                    if (!minPriceField.getText().isEmpty()) {
                        whereClause.append(" AND t.TicketCost >= ").append(minPriceField.getText());
                    }
                    if (!maxPriceField.getText().isEmpty()) {
                        whereClause.append(" AND t.TicketCost <= ").append(maxPriceField.getText());
                    }
                    if (!minAgeField.getText().isEmpty()) {
                        whereClause.append(" AND DATEDIFF(YEAR, p.BirthDay, CURRENT_DATE) >= ")
                                .append(minAgeField.getText());
                    }
                    if (!maxAgeField.getText().isEmpty()) {
                        whereClause.append(" AND DATEDIFF(YEAR, p.BirthDay, CURRENT_DATE) <= ")
                                .append(maxAgeField.getText());
                    }
                    if (genderCombo.getSelectedIndex() > 0) {
                        whereClause.append(" AND p.Gender = '")
                                .append(genderCombo.getSelectedIndex() == 1 ? "M" : "F")
                                .append("'");
                    }

                    finalQuery = finalQuery.replace(
                            "WHERE ts.Status = 'Cancelled' " +
                                    "AND f.FlightID = 1 " +
                                    "AND CONVERT(DATE, f.FromTime) = '2023-12-01' " +
                                    "AND (f.FromLocation = 1 AND f.ToLocation = 2) " +
                                    "AND t.TicketCost BETWEEN 5000 AND 15000 " +
                                    "AND DATEDIFF('YEAR', p.BirthDay, CURRENT_DATE) BETWEEN 18 AND 65 " +
                                    "AND p.Gender = 'M'",
                            whereClause.toString()
                    );
                } else {
                    cancelled = true;
                }
            }

            if (!cancelled) {
                queryTextArea.setText(finalQuery);
                executeQuery(finalQuery);
            }
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