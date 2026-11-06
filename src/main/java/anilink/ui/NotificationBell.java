package anilink.ui;

import anilink.data.NotificationDAO;
import anilink.data.RiderDAO;
import anilink.model.Rider;
import anilink.model.User;
import anilink.session.SessionContext;
import anilink.util.UiUtil;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

/**
 * Header bell that shows the signed-in user's unread notification count and
 * opens a tray listing recent order and payment activity.
 *
 * <p>Lives once in {@code MainFrame}'s shared top bar rather than being
 * rebuilt per page, and picks its data source by role each time it refreshes:
 * buyer, farmer (every order with one of their products) and rider (every
 * order assigned to them) each see their own feed; admin and guests see
 * nothing, since admin already has dedicated queue pages that serve the same
 * purpose a feed would.
 *
 * <p>A {@link JPopupMenu} is used rather than a dialog so the tray dismisses
 * on focus loss like a real notification menu, and never blocks the EDT.
 */
public class NotificationBell extends JButton {

    /** Invoked when a notification is clicked, with its order id. */
    public interface OrderOpener {
        void open(int orderId);
    }

    private final OrderOpener opener;
    private final NotificationDAO dao = new NotificationDAO();
    private List<NotificationDAO.Note> notes = new ArrayList<>();
    private int unread;

    public NotificationBell(OrderOpener opener) {
        this.opener = opener;
        setIcon(UiUtil.bellIcon(20));
        setToolTipText("Notifications");
        setHorizontalAlignment(SwingConstants.CENTER);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 12));
        setPreferredSize(new Dimension(46, 40));
        addActionListener(e -> showTray());
    }

    /** Reloads the feed and repaints the badge. Safe to call often. */
    public final void refresh() {
        try {
            notes = loadForCurrentRole();
        } catch (SQLException e) {
            // A header badge is not worth interrupting the user for.
            notes = new ArrayList<>();
        }
        setVisible(!notes.isEmpty() || hasAudience());
        int count = 0;
        for (NotificationDAO.Note note : notes) {
            if (note.isUnread()) {
                count++;
            }
        }
        unread = count;
        setToolTipText(unread == 0
                ? "Notifications"
                : unread + " new notification" + (unread == 1 ? "" : "s"));
        repaint();
    }

    /** True for a signed-in buyer, farmer or rider - the roles with a feed. */
    private boolean hasAudience() {
        return SessionContext.isRole("buyer")
                || SessionContext.isRole("farmer")
                || SessionContext.isRole("rider");
    }

    /**
     * Picks the right feed for whoever is signed in. Riders have no id
     * cached on {@link SessionContext} the way buyer/farmer do, so it is
     * resolved per call - the same thing every rider panel already does.
     */
    private List<NotificationDAO.Note> loadForCurrentRole() throws SQLException {
        Date seenAt = SessionContext.getNotificationsSeenAt();
        if (SessionContext.isRole("buyer")) {
            return dao.findForBuyer(SessionContext.getBuyerId(), 12, seenAt);
        }
        if (SessionContext.isRole("farmer")) {
            return dao.findForFarmer(SessionContext.getFarmerId(), 12, seenAt);
        }
        if (SessionContext.isRole("rider")) {
            User user = SessionContext.getCurrentUser();
            if (user == null) {
                return new ArrayList<>();
            }
            Rider rider = new RiderDAO().findByUserId(user.getId()).orElse(null);
            if (rider == null) {
                return new ArrayList<>();
            }
            return dao.findForRider(rider.getId(), 12, seenAt);
        }
        return new ArrayList<>();
    }

    private void showTray() {
        JPopupMenu tray = new JPopupMenu();
        tray.setBorder(BorderFactory.createLineBorder(UiUtil.BORDER));

        JMenuItem heading = new JMenuItem("Notifications");
        heading.setFont(UiUtil.bold(13));
        heading.setForeground(UiUtil.DARK);
        heading.setEnabled(false);
        tray.add(heading);
        tray.addSeparator();

        if (notes.isEmpty()) {
            JMenuItem empty = new JMenuItem("No new notifications");
            empty.setFont(UiUtil.plain(12));
            empty.setEnabled(false);
            tray.add(empty);
        } else {
            for (NotificationDAO.Note note : notes) {
                String when = UiUtil.formatDateTime(note.getWhen());
                JMenuItem item = new JMenuItem("<html><div style='width:280px'>"
                        + (note.isUnread() ? "<b>" + note.getText() + "</b>" : note.getText())
                        + "<br><font color='#607D6B' size='2'>" + when + "</font></div></html>");
                item.setFont(UiUtil.plain(12));
                item.setBackground(note.isUnread() ? UiUtil.TINT : UiUtil.WHITE);
                final int orderId = note.getOrderId();
                item.addActionListener(e -> {
                    if (opener != null && orderId > 0) {
                        opener.open(orderId);
                    }
                });
                tray.add(item);
            }
        }

        // Opening the tray is the "read" action.
        SessionContext.markNotificationsSeen();
        unread = 0;
        repaint();

        tray.show(this, -260 + getWidth(), getHeight() + 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (unread <= 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String label = unread > 9 ? "9+" : String.valueOf(unread);
        g2.setFont(UiUtil.bold(10));
        int textWidth = g2.getFontMetrics().stringWidth(label);
        int diameter = Math.max(16, textWidth + 8);
        int x = getWidth() - diameter - 4;
        int y = 2;

        g2.setColor(UiUtil.DANGER);
        g2.fillOval(x, y, diameter, diameter);
        g2.setColor(Color.WHITE);
        g2.drawString(label,
                x + (diameter - textWidth) / 2,
                y + diameter / 2 + g2.getFontMetrics().getAscent() / 2 - 1);
        g2.dispose();
    }
}
