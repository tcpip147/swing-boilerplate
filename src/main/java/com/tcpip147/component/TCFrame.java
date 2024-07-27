package com.tcpip147.component;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.BaseTSD.LONG_PTR;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.win32.W32APIOptions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static com.sun.jna.platform.win32.WinUser.*;

public class TCFrame extends JFrame {

    private final int TITLE_HEIGHT = 30;
    private final int BUTTON_WIDTH = 47;
    private final int BORDER_THICKNESS = 6;

    private final TCWndProc wndProc = new TCWndProc();

    private final JPanel contentPanel;
    private final JPanel titleBarPanel;
    private final JPanel logoPanel;
    private final JLabel titleBarLabel;
    private final JPanel containerPanel;
    private final ControlBox controlBox;

    private Color titleBarBackground = new Color(5, 80, 125);
    private Color titleBarForeground = new Color(255, 255, 255);
    private Color buttonHoverBackground = new Color(37, 118, 167);
    private Color closeButtonHoverBackground = new Color(232, 17, 35);
    private Color buttonPressedBackground = new Color(71, 132, 171);
    private Color closeButtonPressedBackground = new Color(241, 112, 122);

    private boolean initialized;
    private double dpiScale = 1;

    public TCFrame() throws HeadlessException {
        super();
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setOpaque(false);
        setContentPane(contentPanel);

        /* titleBar */
        titleBarPanel = new JPanel();
        titleBarPanel.setLayout(new BorderLayout());
        titleBarPanel.setPreferredSize(new Dimension(0, TITLE_HEIGHT));
        contentPanel.add(titleBarPanel, BorderLayout.NORTH);

        /* titleBarLabel */
        logoPanel = new JPanel();
        logoPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));
        logoPanel.setOpaque(false);
        titleBarLabel = new JLabel();
        titleBarLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        logoPanel.add(titleBarLabel);
        titleBarPanel.add(logoPanel, BorderLayout.CENTER);

        /* controlBox */
        controlBox = new ControlBox();
        titleBarPanel.add(controlBox, BorderLayout.EAST);

        /* container */
        containerPanel = new JPanel();
        containerPanel.setOpaque(false);
        contentPanel.add(containerPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                if (getExtendedState() == MAXIMIZED_BOTH) {
                    getRootPane().setBorder(BorderFactory.createEmptyBorder(BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS));
                } else {
                    getRootPane().setBorder(null);
                }
            }
        });

        pack();
    }

    @Override
    public void setVisible(boolean b) {
        if (!initialized) {
            initialized = true;
            lazySetup();
            HWND hWnd = new HWND();
            hWnd.setPointer(Native.getComponentPointer(this));
            wndProc.init(hWnd);
        }
        super.setVisible(b);
    }

    private void lazySetup() {
        if (getIconImage() != null) {
            Image image = getIconImage();
            JLabel titleBarIcon = new JLabel(new ImageIcon(image.getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
            titleBarIcon.setPreferredSize(new Dimension(16, 16));
            logoPanel.add(titleBarIcon, 0);
        }
        titleBarLabel.setFont(getFont());
        titleBarLabel.setText(getTitle());
        titleBarLabel.setForeground(titleBarForeground);
        titleBarPanel.setBackground(titleBarBackground);
        titleBarPanel.setForeground(titleBarForeground);
    }

    public void setTitleBarBackground(Color titleBarBackground) {
        this.titleBarBackground = titleBarBackground;
    }

    public void setTitleBarForeground(Color titleBarForeground) {
        this.titleBarForeground = titleBarForeground;
    }

    public void setButtonHoverBackground(Color buttonHoverBackground) {
        this.buttonHoverBackground = buttonHoverBackground;
    }

    public void setCloseButtonHoverBackground(Color closeButtonHoverBackground) {
        this.closeButtonHoverBackground = closeButtonHoverBackground;
    }

    public void setButtonPressedBackground(Color buttonPressedBackground) {
        this.buttonPressedBackground = buttonPressedBackground;
    }

    public void setCloseButtonPressedBackground(Color closeButtonPressedBackground) {
        this.closeButtonPressedBackground = closeButtonPressedBackground;
    }

    private class ControlBox extends JPanel {

        private ControlBox box = this;
        private int inHover;
        private int inPressed;

        public ControlBox() {
            setPreferredSize(new Dimension(BUTTON_WIDTH * 3, TITLE_HEIGHT));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);
                    if (e.getX() >= 0 && e.getX() < BUTTON_WIDTH) {
                        inPressed = 1;
                    } else if (e.getX() >= BUTTON_WIDTH && e.getX() < BUTTON_WIDTH * 2) {
                        inPressed = 2;
                    } else if (e.getX() >= BUTTON_WIDTH * 2) {
                        inPressed = 3;
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    super.mouseReleased(e);
                    if (inHover == inPressed) {
                        if (inHover == 1) {
                            setExtendedState(getExtendedState() + ICONIFIED);
                        } else if (inHover == 2) {
                            if (getExtendedState() == MAXIMIZED_BOTH) {
                                setExtendedState(NORMAL);
                            } else {
                                setExtendedState(MAXIMIZED_BOTH);
                            }
                        } else if (inHover == 3) {
                            Window window = SwingUtilities.getWindowAncestor(box);
                            processWindowEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
                        }
                    }
                    inPressed = 0;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    super.mouseExited(e);
                    inHover = 0;
                    repaint();
                }
            });
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            /* minimize */
            if (inHover == 1) {
                if (inPressed == inHover) {
                    g2d.setColor(buttonPressedBackground);
                } else {
                    g2d.setColor(buttonHoverBackground);
                }
            } else {
                g2d.setColor(titleBarBackground);
            }
            g2d.fillRect(0, 0, BUTTON_WIDTH, TITLE_HEIGHT);
            g2d.setColor(titleBarForeground);
            g2d.drawLine(BUTTON_WIDTH / 2 - 5, TITLE_HEIGHT / 2, BUTTON_WIDTH / 2 + 5, TITLE_HEIGHT / 2);

            /* maximize */
            int offset = BUTTON_WIDTH;
            if (inHover == 2) {
                if (inPressed == inHover) {
                    g2d.setColor(buttonPressedBackground);
                } else {
                    g2d.setColor(buttonHoverBackground);
                }
            } else {
                g2d.setColor(titleBarBackground);
            }
            g2d.fillRect(offset, 0, BUTTON_WIDTH, TITLE_HEIGHT);
            g2d.setColor(titleBarForeground);
            if (getExtendedState() == MAXIMIZED_BOTH) {
                Point point = new Point(BUTTON_WIDTH / 2 - 5 + offset, TITLE_HEIGHT / 2 - 3);
                g2d.drawRect(point.x, point.y, 8, 8);
                point = new Point(BUTTON_WIDTH / 2 + 5 + offset, TITLE_HEIGHT / 2 - 5);
                g2d.drawLine(point.x, point.y, point.x - 8, point.y);
                g2d.drawLine(point.x, point.y, point.x, point.y + 8);
                g2d.drawLine(point.x - 8, point.y, point.x - 8, point.y + 2);
                g2d.drawLine(point.x, point.y + 8, point.x - 2, point.y + 8);
            } else {
                g2d.drawRect(BUTTON_WIDTH / 2 - 5 + offset, TITLE_HEIGHT / 2 - 5, 9, 9);
            }

            /* close */
            offset = BUTTON_WIDTH * 2;
            if (inHover == 3) {
                if (inPressed == inHover) {
                    g2d.setColor(closeButtonPressedBackground);
                } else {
                    g2d.setColor(closeButtonHoverBackground);
                }
            } else {
                g2d.setColor(titleBarBackground);
            }
            g2d.fillRect(offset, 0, BUTTON_WIDTH, TITLE_HEIGHT);
            g2d.setColor(titleBarForeground);
            g2d.drawLine(BUTTON_WIDTH / 2 - 5 + offset, TITLE_HEIGHT / 2 - 5, BUTTON_WIDTH / 2 + 5 + offset, TITLE_HEIGHT / 2 + 5);
            g2d.drawLine(BUTTON_WIDTH / 2 + 5 + offset, TITLE_HEIGHT / 2 - 5, BUTTON_WIDTH / 2 - 5 + offset, TITLE_HEIGHT / 2 + 5);

            g2d.dispose();
        }
    }

    private class TCWndProc implements WindowProc {

        private final int WM_NCCALCSIZE = 0x0083;
        private final int WM_NCHITTEST = 0x0084;

        private final int HTTOPLEFT = 13;
        private final int HTTOP = 12;
        private final int HTCAPTION = 2;
        private final int HTTOPRIGHT = 14;
        private final int HTLEFT = 10;
        private final int HTNOWHERE = 0;
        private final int HTRIGHT = 11;
        private final int HTBOTTOMLEFT = 16;
        private final int HTBOTTOM = 15;
        private final int HTBOTTOMRIGHT = 17;

        private LONG_PTR oriWndProc;

        public void init(HWND hWnd) {
            oriWndProc = User32Ex.INSTANCE.SetWindowLongPtr(hWnd, GWL_WNDPROC, this);
            User32Ex.INSTANCE.SetWindowPos(hWnd, hWnd, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
            DWMApi.INSTANCE.DwmExtendFrameIntoClientArea(hWnd, new int[]{0, 0, 0, 1});
        }

        @Override
        public LRESULT callback(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam) {
            switch (uMsg) {
                case WM_NCCALCSIZE:
                    return new LRESULT(0);
                case WM_NCHITTEST:
                    LRESULT lResult = hitTest(hWnd);
                    if (lResult.intValue() == 0) {
                        return User32Ex.INSTANCE.CallWindowProc(oriWndProc, hWnd, uMsg, wParam, lParam);
                    }
                    return lResult;
                default:
                    return User32Ex.INSTANCE.CallWindowProc(oriWndProc, hWnd, uMsg, wParam, lParam);
            }
        }

        private LRESULT hitTest(HWND hWnd) {
            final POINT pos = new POINT();
            final RECT rect = new RECT();
            User32.INSTANCE.GetCursorPos(pos);
            User32.INSTANCE.GetWindowRect(hWnd, rect);

            int realWidth = rect.right - rect.left;
            dpiScale = (double) realWidth / getWidth();

            int r = 1;
            int c = 1;
            boolean onResize = false;
            boolean onDrag = false;

            if (pos.y >= rect.top && pos.y < rect.top + TITLE_HEIGHT * dpiScale) {
                onResize = pos.y < (rect.top + BORDER_THICKNESS);
                if (!onResize) {
                    onDrag = (pos.y <= rect.top + TITLE_HEIGHT * dpiScale) && (pos.x < rect.right - (BUTTON_WIDTH * 3 * dpiScale));
                }
                if (pos.x >= rect.right - BUTTON_WIDTH * 3 * dpiScale && pos.x < rect.right - BUTTON_WIDTH * 2 * dpiScale) {
                    controlBox.inHover = 1;
                } else if (pos.x >= rect.right - BUTTON_WIDTH * 2 * dpiScale && pos.x < rect.right - BUTTON_WIDTH * dpiScale) {
                    controlBox.inHover = 2;
                } else if (pos.x >= rect.right - BUTTON_WIDTH * dpiScale) {
                    controlBox.inHover = 3;
                } else {
                    controlBox.inHover = 0;
                }
                controlBox.repaint();
                r = 0;
            } else if (pos.y < rect.bottom && pos.y >= rect.bottom - BORDER_THICKNESS * dpiScale) {
                r = 2;
            }

            if (pos.x >= rect.left && pos.x < rect.left + BORDER_THICKNESS * dpiScale) {
                c = 0;
            } else if (pos.x < rect.right && pos.x >= rect.right - BORDER_THICKNESS * dpiScale) {
                c = 2;
            }

            int[][] hitTests = {
                    {HTTOPLEFT, onResize ? HTTOP : onDrag ? HTCAPTION : HTNOWHERE, HTTOPRIGHT},
                    {HTLEFT, HTNOWHERE, HTRIGHT},
                    {HTBOTTOMLEFT, HTBOTTOM, HTBOTTOMRIGHT}
            };

            return new LRESULT(hitTests[r][c]);
        }
    }

    private interface User32Ex extends User32 {

        User32Ex INSTANCE = Native.load("user32", User32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

        LONG_PTR SetWindowLongPtr(HWND hWnd, int nIndex, WindowProc wndProc);

        LRESULT CallWindowProc(LONG_PTR wndProc, HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam);
    }

    private interface DWMApi extends Library {

        DWMApi INSTANCE = Native.load("dwmapi", DWMApi.class);

        HRESULT DwmExtendFrameIntoClientArea(HWND hWnd, int[] pMarInset);
    }
}
