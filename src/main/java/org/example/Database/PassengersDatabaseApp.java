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

@SuppressWarnings("ALL")
public class PassengersDatabaseApp {
    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTable table;
    private Connection connection;
    private String currentTable = "Passengers";

    public PassengersDatabaseApp(Connection connection) {
        this.connection = connection;
        frame = new JFrame("Информация о пассажирах");
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
                createTableButton("Пассажиры", "Passengers"),
                createTableButton("Дети", "Childrens"),
                createTableButton("Связи пассажиров", "WorkersAndChildrens")
        );

        JPanel group2 = createAlignedGroup("Билеты и рейсы:",
                createTableButton("Билеты", "Tickets"),
                createTableButton("История билетов", "TicketsStory"),
                createTableButton("Пассажиры в рейсе", "PassengersInFlight")
        );

        JPanel group3 = createAlignedGroup("Действия:",
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

        // Добавляем панель с кнопками и таблицу
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        frame.add(mainPanel);
    }

    private void showAddRecordDialog(ActionEvent actionEvent) {
        // Реализация добавления записи
        JOptionPane.showMessageDialog(frame, "Функция добавления записи будет реализована позже", "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void initDatabase() {
        try {
            showTable("Passengers");
        } catch (Exception e) {
            showError("Ошибка инициализации базы данных: " + e.getMessage());
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
                case "Passengers":
                    deletePassenger(selectedRow);
                    break;
                case "Childrens":
                    deleteChild(selectedRow);
                    break;
                case "WorkersAndChildrens":
                    deleteWorkerChildRelation(selectedRow);
                    break;
                case "Tickets":
                    deleteTicket(selectedRow);
                    break;
                case "TicketsStory":
                    deleteTicketHistory(selectedRow);
                    break;
                case "PassengersInFlight":
                    deletePassengerInFlight(selectedRow);
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

    private void deletePassenger(int selectedRow) throws SQLException {
        int passengerId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Passengers WHERE PassengerID = ?")) {
            stmt.setInt(1, passengerId);
            stmt.executeUpdate();
        }
    }

    private void deleteChild(int selectedRow) throws SQLException {
        int childId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Childrens WHERE ChildID = ?")) {
            stmt.setInt(1, childId);
            stmt.executeUpdate();
        }
    }

    private void deleteWorkerChildRelation(int selectedRow) throws SQLException {
        int workerId = (int) table.getValueAt(selectedRow, 0);
        int childId = (int) table.getValueAt(selectedRow, 1);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM WorkersAndChildrens WHERE WorkerID = ? AND ChildID = ?")) {
            stmt.setInt(1, workerId);
            stmt.setInt(2, childId);
            stmt.executeUpdate();
        }
    }

    private void deleteTicket(int selectedRow) throws SQLException {
        int ticketId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Tickets WHERE TicketID = ?")) {
            stmt.setInt(1, ticketId);
            stmt.executeUpdate();
        }
    }

    private void deleteTicketHistory(int selectedRow) throws SQLException {
        int ticketId = (int) table.getValueAt(selectedRow, 0);
        int passengerId = (int) table.getValueAt(selectedRow, 1);
        Timestamp time = (Timestamp) table.getValueAt(selectedRow, 3);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM TicketsStory WHERE TicketID = ? AND PassengerID = ? AND TimeStatusUpdate = ?")) {
            stmt.setInt(1, ticketId);
            stmt.setInt(2, passengerId);
            stmt.setTimestamp(3, time);
            stmt.executeUpdate();
        }
    }

    private void deletePassengerInFlight(int selectedRow) throws SQLException {
        int flightId = (int) table.getValueAt(selectedRow, 0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM PassengersInFlight WHERE FlightID = ?")) {
            stmt.setInt(1, flightId);
            stmt.executeUpdate();
        }
    }

    private void refreshData() {
        showTable(currentTable);
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
            case "Passengers":
                showPassengers();
                break;
            case "Childrens":
                showChildren();
                break;
            case "WorkersAndChildrens":
                showWorkersAndChildren();
                break;
            case "Tickets":
                showTickets();
                break;
            case "TicketsStory":
                showTicketsStory();
                break;
            case "PassengersInFlight":
                showPassengersInFlight();
                break;
            default:
                showGenericTable(tableName);
        }
    }

    private void showPassengers() {
        currentTable = "Passengers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Passengers")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            model.addColumn("ФИО");
            model.addColumn("Страна");
            model.addColumn("Паспортные данные");
            model.addColumn("Данные загранпаспорта");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("FIO"),
                        rs.getString("Country"),
                        rs.getString("PassportData"),
                        rs.getString("InternationalPassportData")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки данных о пассажирах: " + e.getMessage());
        }
    }

    private void showChildren() {
        currentTable = "Childrens";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Childrens")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            model.addColumn("ФИО ребенка");
            model.addColumn("Дата рождения");

            while (rs.next()) {
                model.addRow(new Object[]{
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

    private void showWorkersAndChildren() {
        currentTable = "WorkersAndChildrens";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT wc.WorkerID, s.FIO AS WorkerName, wc.ChildID, c.ChildName " +
                             "FROM WorkersAndChildrens wc " +
                             "JOIN AirportStaff s ON wc.WorkerID = s.WorkerID " +
                             "JOIN Childrens c ON wc.ChildID = c.ChildID")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            model.addColumn("ФИО сотрудника");
            model.addColumn("Имя ребенка");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("WorkerName"),
                        rs.getString("ChildName")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки связей сотрудников и детей: " + e.getMessage());
        }
    }

    private void showTickets() {
        currentTable = "Tickets";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT t.TicketID, t.TicketCost, t.TciketStatus, f.FlightID, s.SeatName, b.MaxWeight " +
                             "FROM Tickets t " +
                             "LEFT JOIN Flights f ON t.FlightID = f.FlightID " +
                             "LEFT JOIN Seats s ON t.Seat = s.SeatID " +
                             "LEFT JOIN TypesOfBaggages b ON t.BaggageTypeID = b.BaggageTypeID")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            model.addColumn("Номер билета");
            model.addColumn("Стоимость");
            model.addColumn("Статус");
            model.addColumn("Номер рейса");
            model.addColumn("Место");
            model.addColumn("Макс. вес багажа");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("TicketID"),
                        rs.getFloat("TicketCost"),
                        rs.getString("TciketStatus"),
                        rs.getObject("FlightID"),
                        rs.getString("SeatName"),
                        rs.getObject("MaxWeight")
                });
            }

            table.setModel(model);
            configureTable();
        } catch (SQLException e) {
            showError("Ошибка загрузки данных о билетах: " + e.getMessage());
        }
    }

    private void showTicketsStory() {
        currentTable = "TicketsStory";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT ts.TicketID, p.FIO, ts.Status, ts.TimeStatusUpdate " +
                             "FROM TicketsStory ts " +
                             "JOIN Passengers p ON ts.PassengerID = p.PassengerID " +
                             "ORDER BY ts.TimeStatusUpdate DESC")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            model.addColumn("Номер билета");
            model.addColumn("Пассажир");
            model.addColumn("Статус");
            model.addColumn("Время изменения");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("TicketID"),
                        rs.getString("FIO"),
                        rs.getString("Status"),
                        rs.getTimestamp("TimeStatusUpdate")
                });
            }

            table.setModel(model);
            configureTableWithDates();
        } catch (SQLException e) {
            showError("Ошибка загрузки истории билетов: " + e.getMessage());
        }
    }

    private void showPassengersInFlight() {
        currentTable = "PassengersInFlight";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM PassengersInFlight")) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            model.addColumn("Номер рейса");
            model.addColumn("Макс. вместимость");
            model.addColumn("Факт. пассажиров");
            model.addColumn("С багажом");
            model.addColumn("Кол-во багажа");
            model.addColumn("Вес багажа");
            model.addColumn("Куплено билетов");
            model.addColumn("Прошло по билетам");
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
            showError("Ошибка загрузки данных о пассажирах в рейсе: " + e.getMessage());
        }
    }

    private void showGenericTable(String tableName) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }

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

    public void show() {
        frame.setVisible(true);
    }
}