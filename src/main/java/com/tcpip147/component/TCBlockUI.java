package com.tcpip147.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TCBlockUI extends JPanel {

    private final Color DISABLED_BACKGROUND = new Color(0, 0, 0, 60);
    private final Color LOADING_BAR_COLOR = new Color(0, 146, 230);
    private final Color TEXT_COLOR = new Color(12, 16, 21);
    private final String text = "Loading...";

    private int arc = 0;
    private int cw = 50;
    private int ch = 50;
    private int width = 160;
    private int height = 120;

    public TCBlockUI() {
        setOpaque(false);
        setVisible(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // stopPropagation
            }
        });
    }

    public void execute(Runnable runnable) {
        setVisible(true);
        Timer interval = new Timer(10, e -> {
            repaint();
        });
        interval.start();

        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                try {
                    runnable.run();
                } catch (Exception e) {
                    throw e;
                }
                return null;
            }

            @Override
            protected void done() {
                super.done();
                setVisible(false);
                interval.stop();
            }
        };
        worker.execute();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int x = (getWidth() - width) / 2;
        int y = (getHeight() - height) / 2;
        g2d.setColor(DISABLED_BACKGROUND);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(x, y, width, height, 20, 20);
        g2d.setColor(Color.GRAY);
        g2d.drawRoundRect(x, y, width, height, 20, 20);

        int tw = g.getFontMetrics().stringWidth(text);

        int cx = x + width / 2 - cw / 2;
        int cy = y + height / 2 - ch / 2 - 13;

        g2d.setColor(LOADING_BAR_COLOR);
        g2d.fillArc(cx, cy, cw, ch, arc, 270);
        g2d.setColor(Color.GRAY);
        g2d.fillArc(cx, cy, cw, ch, arc, -90);
        g2d.setColor(Color.WHITE);
        int rad = 14;
        g2d.fillArc(cx + rad, cy + rad, cw - rad * 2, ch - rad * 2, 0, 360);
        arc += 5;

        g2d.setColor(TEXT_COLOR);
        g2d.setFont(getFont());
        g2d.drawString(text, x + width / 2 - tw / 2, cy + 75);

        g2d.dispose();
    }
}
