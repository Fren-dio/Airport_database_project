package org.example.MainWindow;

import javax.swing.*;

public class BoxLayoutUtils {
    // Выравнивание компонентов по оси X
    public void setGroupAlignmentX(JComponent[] cs, float alignment) {
        for (int i = 0; i < cs.length; i++) {
            cs[i].setAlignmentX(alignment);
        }
    }
    // Выравнивание компонентов по оси Y
    public void setGroupAlignmentY(JComponent[] cs, float alignment) {
        for (int i = 0; i < cs.length; i++) {
            cs[i].setAlignmentY(alignment);
        }
    }
    // Создание панели с установленным вертикальным блочным расположением
    public JPanel createVerticalPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }
    // Создание панели с установленным горизонтальным блочным расположением
    public JPanel createHorizontalPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        return p;
    }
}
