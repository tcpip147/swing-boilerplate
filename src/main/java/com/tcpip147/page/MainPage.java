package com.tcpip147.page;

import com.tcpip147.component.TCButton;
import com.tcpip147.component.TCDialog;
import com.tcpip147.component.TCFrame;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainPage extends JPanel {

    private MainPage self = this;

    public MainPage() {
        {
            TCButton button = new TCButton("Block UI");
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    TCFrame frame = (TCFrame) SwingUtilities.getWindowAncestor(self);
                    frame.execute(() -> {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                }
            });
            add(button);
        }

        {
            TCButton button = new TCButton("Open Dialog");
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    TCFrame frame = (TCFrame) SwingUtilities.getWindowAncestor(self);
                    frame.execute(() -> {
                        TCDialog dialog = new TCDialog(frame);
                        dialog.setTitle("Dialog");
                        dialog.setSize(600, 400);
                        dialog.setLocation(frame.getX() + 150, frame.getY() + 100);
                        dialog.setModal(true);
                        dialog.setVisible(true);
                    });
                }
            });
            add(button);
        }
    }
}
