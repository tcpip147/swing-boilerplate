package com.tcpip147.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

public class TCButton extends JButton {

    private TCButton self = this;

    public TCButton() {
        initialize();
    }

    public TCButton(Icon icon) {
        super(icon);
        initialize();
    }

    public TCButton(String text) {
        super(text);
        initialize();
    }

    public TCButton(Action a) {
        super(a);
        initialize();
    }

    public TCButton(String text, Icon icon) {
        super(text, icon);
        initialize();
    }

    private void initialize() {
        addMouseListener(new MouseAdapter() {
            private boolean entered;

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                entered = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                entered = false;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (entered) {
                    dispatchEvent(new MouseEvent(self, MouseEvent.MOUSE_CLICKED, new Date().getTime(), 0, e.getX(), e.getY(), 1, false));
                }
            }
        });
    }
}
