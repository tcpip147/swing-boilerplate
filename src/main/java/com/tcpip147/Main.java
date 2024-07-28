package com.tcpip147;

import com.tcpip147.component.TCFrame;
import com.tcpip147.page.MainPage;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TCFrame frame = new TCFrame();
            frame.setTitle("Java Swing Boilerplate");
            frame.setLocation(100, 100);
            frame.setSize(600, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setIconImage(new ImageIcon(TCFrame.class.getResource("/favicon.png")).getImage());
            frame.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
            frame.add(new MainPage());
            frame.setVisible(true);
        });
    }
}