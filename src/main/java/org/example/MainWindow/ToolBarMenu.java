package org.example.MainWindow;

import javax.swing.*;
import java.awt.*;

public class ToolBarMenu extends JToolBar {


    private FrameWork frameWork;
    private final int btnSize = 45;




    public ToolBarMenu(FrameWork frameWork) {
        this.frameWork = frameWork;

        this.setFloatable(false);
        this.setRollover(false);


    }


    private JButton setDimensionBtn(JButton btn, Dimension dim) {
        btn.setPreferredSize(dim);
        btn.setMinimumSize(dim);
        btn.setMaximumSize(dim);

        return btn;
    }

}
