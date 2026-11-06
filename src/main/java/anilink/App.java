package anilink;

import anilink.home.GuestHomeFrame;
import anilink.util.UiUtil;
import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {
        UiUtil.applyLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            GuestHomeFrame frame = new GuestHomeFrame();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
