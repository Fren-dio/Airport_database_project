package org.example.MainWindow;

import org.example.Database.AirportDatabaseApp;

import java.awt.*;
import java.net.URL;
import javax.swing.*;

public class FrameWork extends JFrame {
    private ToolBarMenu toolBarMenu;
    private JScrollPane scrollPane;
    private JPanel buttonPanel;

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

        // Создаем панель с сеткой 2x2
        buttonPanel = new JPanel(new GridLayout(2, 2));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addConnectToAirportBtn();
        addConnectToAirplaneBtn();
        addConnectToScheduleBtn();
        addConnectToPassengersBtn();

        this.add(buttonPanel, BorderLayout.CENTER); // Размещаем по центру

        pack();
        setVisible(true);
    }

    private ImageIcon createScaledIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        Image image = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    void addConnectToAirportBtn() {
        JButton btn = new JButton("Аэропорт");
        btn.setPreferredSize(new Dimension(200, 200));
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);

        // Установка изображения (замените путь на свой)
        try {
            URL imageUrl = getClass().getResource("/airport_icon.jpg");
            if (imageUrl == null) {
                imageUrl = getClass().getResource("airport_icon.jpg");
            }

            if (imageUrl != null) {
                btn.setIcon(new ImageIcon(imageUrl));
            } else {
                System.err.println("Изображение не найдено. Используется только текст.");
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображения: " + e.getMessage());
        }

        btn.setToolTipText("Получить информацию, связанную с аэропортом");
        btn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                AirportDatabaseApp app = new AirportDatabaseApp();
                app.show();
            });
        });

        buttonPanel.add(btn);
    }

    void addConnectToAirplaneBtn() {
        JButton btn = new JButton("Самолеты");
        btn.setPreferredSize(new Dimension(200, 200));
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);

        try {
            URL imageUrl = getClass().getResource("/airplane_icon.jpg");
            if (imageUrl == null) {
                imageUrl = getClass().getResource("airplane_icon.jpg");
            }

            if (imageUrl != null) {
                btn.setIcon(new ImageIcon(imageUrl));
            } else {
                System.err.println("Изображение не найдено. Используется только текст.");
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображения: " + e.getMessage());
        }

        btn.setToolTipText("Получить информацию, связанную с самолетами");
        btn.addActionListener(e -> {
            // Действие для кнопки
        });

        buttonPanel.add(btn);
    }

    void addConnectToScheduleBtn() {
        JButton btn = new JButton("Расписание");
        btn.setPreferredSize(new Dimension(200, 200));
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);

        try {
            URL imageUrl = getClass().getResource("/schedule_icon.jpg");
            if (imageUrl == null) {
                imageUrl = getClass().getResource("schedule_icon.jpg");
            }

            if (imageUrl != null) {
                btn.setIcon(new ImageIcon(imageUrl));
            } else {
                System.err.println("Изображение не найдено. Используется только текст.");
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображения: " + e.getMessage());
        }

        btn.setToolTipText("Получить информацию, связанную с расписанием полетов");
        btn.addActionListener(e -> {
            // Действие для кнопки
        });

        buttonPanel.add(btn);
    }

    void addConnectToPassengersBtn() {
        JButton btn = new JButton("Пассажиры");
        btn.setPreferredSize(new Dimension(200, 200));
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);

        try {
            URL imageUrl = getClass().getResource("/passengers_icon.jpg");
            if (imageUrl == null) {
                imageUrl = getClass().getResource("passengers_icon.jpg");
            }

            if (imageUrl != null) {
                btn.setIcon(new ImageIcon(imageUrl));
            } else {
                System.err.println("Изображение не найдено. Используется только текст.");
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображения: " + e.getMessage());
        }

        btn.setToolTipText("Получить информацию, связанную с пассажирами");
        btn.addActionListener(e -> {
            // Действие для кнопки
        });

        buttonPanel.add(btn);
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