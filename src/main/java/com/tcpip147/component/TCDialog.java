package com.tcpip147.component;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.BaseTSD.LONG_PTR;
import com.sun.jna.win32.W32APIOptions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static com.sun.jna.platform.win32.WinUser.*;

public class TCDialog extends JDialog {

    private final Window parent;

    private final int TITLE_HEIGHT = 30;
    private final int BUTTON_WIDTH = 47;
    private final int BORDER_THICKNESS = 6;

    private final TCDialog self = this;
    private final TCWndProc wndProc = new TCWndProc();
    private final TCBlockUI blockUI = new TCBlockUI();

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
    private boolean loaded;
    private double hidpiScale = 1;

    public TCDialog(Window parent) throws HeadlessException {
        super(parent);
        this.parent = parent;

        if (parent instanceof TCFrame) {
            TCFrame frame = (TCFrame) parent;
            titleBarBackground = frame.getTitleBarBackground();
            titleBarForeground = frame.getTitleBarForeground();
            buttonHoverBackground = frame.getButtonHoverBackground();
            closeButtonHoverBackground = frame.getCloseButtonHoverBackground();
            buttonPressedBackground = frame.getButtonPressedBackground();
            closeButtonPressedBackground = frame.getCloseButtonPressedBackground();
        } else if (parent instanceof TCDialog) {
            TCDialog dialog = (TCDialog) parent;
            titleBarBackground = dialog.getTitleBarBackground();
            titleBarForeground = dialog.getTitleBarForeground();
            buttonHoverBackground = dialog.getButtonHoverBackground();
            closeButtonHoverBackground = dialog.getCloseButtonHoverBackground();
            buttonPressedBackground = dialog.getButtonPressedBackground();
            closeButtonPressedBackground = dialog.getCloseButtonPressedBackground();
        }

        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setOpaque(false);
        setContentPane(contentPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

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

        /* wrap */
        JLayeredPane wrap = new JLayeredPane();
        wrap.setLayout(null);
        wrap.add(blockUI, JLayeredPane.MODAL_LAYER);
        contentPanel.add(wrap, BorderLayout.CENTER);

        /* container */
        containerPanel = new JPanel();
        containerPanel.setOpaque(false);
        wrap.add(containerPanel, JLayeredPane.DEFAULT_LAYER);

        wrap.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                containerPanel.setBounds(0, 0, wrap.getWidth(), wrap.getHeight());
                blockUI.setBounds(0, 0, wrap.getWidth(), wrap.getHeight());
                revalidate();
                repaint();
            }
        });

        pack();
    }

    @Override
    public void setLayout(LayoutManager manager) {
        if (!initialized) {
            initialized = true;
            super.setLayout(manager);
        } else {
            containerPanel.setLayout(manager);
        }
    }

    @Override
    public Component add(Component comp) {
        return containerPanel.add(comp);
    }

    @Override
    public void setVisible(boolean b) {
        if (!loaded) {
            loaded = true;
            lazySetup();
            HWND hWnd = new HWND();
            hWnd.setPointer(Native.getComponentPointer(this));
            wndProc.init(hWnd);
        }
        super.setVisible(b);
    }

    private void lazySetup() {
        if (parent.getIconImages().get(0) != null) {
            Image image = parent.getIconImages().get(0);
            JLabel titleBarIcon = new JLabel(new ImageIcon(image.getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
            titleBarIcon.setPreferredSize(new Dimension(16, 16));
            logoPanel.add(titleBarIcon, 0);
            setIconImage(image.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        }
        titleBarLabel.setFont(getFont());
        titleBarLabel.setText(getTitle());
        titleBarLabel.setForeground(titleBarForeground);
        titleBarPanel.setBackground(titleBarBackground);
        titleBarPanel.setForeground(titleBarForeground);
    }

    public Color getCloseButtonPressedBackground() {
        return closeButtonPressedBackground;
    }

    public Color getButtonPressedBackground() {
        return buttonPressedBackground;
    }

    public Color getCloseButtonHoverBackground() {
        return closeButtonHoverBackground;
    }

    public Color getButtonHoverBackground() {
        return buttonHoverBackground;
    }

    public Color getTitleBarForeground() {
        return titleBarForeground;
    }

    public Color getTitleBarBackground() {
        return titleBarBackground;
    }

    public void execute(Runnable runnable) {
        blockUI.execute(runnable);
    }

    private class ControlBox extends JPanel {

        private ControlBox box = this;
        private int inHover;
        private int inPressed;

        public ControlBox() {
            setPreferredSize(new Dimension(BUTTON_WIDTH, TITLE_HEIGHT));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);
                    if (e.getX() >= 0) {
                        inPressed = 3;
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    super.mouseReleased(e);
                    if (inHover == inPressed) {
                        if (inHover == 3) {
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

            /* close */
            if (inHover == 3) {
                if (inPressed == inHover) {
                    g2d.setColor(closeButtonPressedBackground);
                } else {
                    g2d.setColor(closeButtonHoverBackground);
                }
            } else {
                g2d.setColor(titleBarBackground);
            }
            g2d.fillRect(0, 0, BUTTON_WIDTH, TITLE_HEIGHT);
            g2d.setColor(titleBarForeground);
            g2d.drawLine(BUTTON_WIDTH / 2 - 5, TITLE_HEIGHT / 2 - 5, BUTTON_WIDTH / 2 + 5, TITLE_HEIGHT / 2 + 5);
            g2d.drawLine(BUTTON_WIDTH / 2 + 5, TITLE_HEIGHT / 2 - 5, BUTTON_WIDTH / 2 - 5, TITLE_HEIGHT / 2 + 5);

            g2d.dispose();
        }
    }

    private class TCWndProc implements WindowProc {

        private final int WM_NCCALCSIZE = 0x0083;
        private final int WM_NCHITTEST = 0x0084;
        private final int WM_NCMOUSELEAVE = 0x02A2;

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
                case WM_NCMOUSELEAVE:
                    controlBox.inHover = 0;
                    controlBox.repaint();
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
            hidpiScale = (double) realWidth / getWidth();

            int r = 1;
            int c = 1;
            boolean onResize = false;
            boolean onDrag = false;

            if (pos.y >= rect.top && pos.y < rect.top + TITLE_HEIGHT * hidpiScale) {
                onResize = pos.y < (rect.top + BORDER_THICKNESS);
                if (!onResize) {
                    onDrag = (pos.y <= rect.top + TITLE_HEIGHT * hidpiScale) && (pos.x < rect.right - (BUTTON_WIDTH * hidpiScale));
                }
                if (pos.x >= rect.right - BUTTON_WIDTH * hidpiScale) {
                    controlBox.inHover = 3;
                } else {
                    controlBox.inHover = 0;
                }
                controlBox.repaint();
                r = 0;
            } else if (pos.y < rect.bottom && pos.y >= rect.bottom - BORDER_THICKNESS * hidpiScale) {
                r = 2;
            }

            if (pos.x >= rect.left && pos.x < rect.left + BORDER_THICKNESS * hidpiScale) {
                c = 0;
            } else if (pos.x < rect.right && pos.x >= rect.right - BORDER_THICKNESS * hidpiScale) {
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
