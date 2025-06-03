package org.example.MainWindow;

import org.example.Database.AirplaneDatabaseApp;
import org.example.Database.AirportDatabaseApp;
import org.example.Database.PassengersDatabaseApp;
import org.example.Database.ScheduleDatabaseApp;
import org.example.RequestFrame;

import java.awt.*;
import java.net.URL;
import javax.swing.*;

public class FrameWork extends JFrame {
    private ToolBarMenu toolBarMenu;
    private JScrollPane scrollPane;
    private JPanel mainPanel;

    public FrameWork() {
        super("База данных аэропорта");

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
                RequestFrame app = new RequestFrame();
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
                () -> new AirportDatabaseApp().show());
        panel.add(btn);
    }

    void addConnectToAirplaneBtn(JPanel panel) {
        JButton btn = createIconButton("Самолеты", "/airplane_icon.jpg",
                "Получить информацию, связанную с самолетами",
                () -> new AirplaneDatabaseApp().show());
        panel.add(btn);
    }

    void addConnectToScheduleBtn(JPanel panel) {
        JButton btn = createIconButton("Расписание", "/schedule_icon.jpg",
                "Получить информацию, связанную с расписанием полетов",
                () -> new ScheduleDatabaseApp().show());
        panel.add(btn);
    }

    void addConnectToPassengersBtn(JPanel panel) {
        JButton btn = createIconButton("Пассажиры", "/passengers_icon.jpg",
                "Получить информацию, связанную с пассажирами",
                () -> new PassengersDatabaseApp().show());
        panel.add(btn);
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