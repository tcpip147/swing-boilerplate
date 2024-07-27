package com.tcpip147;

import com.tcpip147.component.TCFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TCFrame frame = new TCFrame();
            frame.setTitle("Java Swing Boilerplate");
            frame.setLocation(100, 100);
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setIconImage(new ImageIcon(TCFrame.class.getResource("/favicon.png")).getImage());
            frame.setVisible(true);
        });
    }
}