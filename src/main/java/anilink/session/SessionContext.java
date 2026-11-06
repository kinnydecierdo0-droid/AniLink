package anilink.session;

import anilink.data.CartDAO;
import anilink.model.Buyer;
import anilink.model.Farmer;
import anilink.model.Product;
import anilink.model.User;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SessionContext {

    private static User currentUser;
    private static Farmer farmerProfile;
    private static Buyer buyerProfile;
    private static final List<CartItem> cart = new ArrayList<>();
    private static java.util.Date notificationsSeenAt;

    private SessionContext() {
    }

    public static void login(User user) {
        currentUser = user;
        farmerProfile = null;
        buyerProfile = null;
        notificationsSeenAt = null;
        cart.clear();
    }

    public static void logout() {
        currentUser = null;
        farmerProfile = null;
        buyerProfile = null;
        notificationsSeenAt = null;
        cart.clear();
    }

    /**
     * When the buyer last opened the notification tray. Anything updated after
     * this counts as unread. Session scoped, so a fresh login starts clean.
     */
    public static java.util.Date getNotificationsSeenAt() {
        return notificationsSeenAt;
    }

    public static void markNotificationsSeen() {
        notificationsSeenAt = new java.util.Date();
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static String getRole() {
        return currentUser == null ? null : currentUser.getRole();
    }

    public static boolean isRole(String role) {
        return currentUser != null && role != null && role.equals(currentUser.getRole());
    }

    public static Farmer getFarmerProfile() {
        return farmerProfile;
    }

    public static void setFarmerProfile(Farmer farmerProfile) {
        SessionContext.farmerProfile = farmerProfile;
    }

    public static Buyer getBuyerProfile() {
        return buyerProfile;
    }

    public static void setBuyerProfile(Buyer buyerProfile) {
        SessionContext.buyerProfile = buyerProfile;
    }

    public static int getFarmerId() {
        return farmerProfile == null ? -1 : farmerProfile.getId();
    }

    public static int getBuyerId() {
        return buyerProfile == null ? -1 : buyerProfile.getId();
    }

    public static List<CartItem> getCart() {
        return cart;
    }

    public static int cartCount() {
        int total = 0;
        for (CartItem item : cart) {
            total += item.getQuantity();
        }
        return total;
    }

    public static double cartTotal() {
        double total = 0;
        for (CartItem item : cart) {
            total += item.getSubtotal();
        }
        return total;
    }

    public static void clearCart() {
        cart.clear();
        int buyerId = getBuyerId();
        if (buyerId > 0) {
            try {
                new CartDAO().clear(buyerId);
            } catch (SQLException e) {
                System.err.println("[cart] clear failed: " + e.getMessage());
            }
        }
    }

    public static Optional<CartItem> findCartItem(int productId) {
        for (CartItem item : cart) {
            if (item.getProduct().getId() == productId) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    public static void addToCart(Product product, int quantity) {
        Optional<CartItem> existing = findCartItem(product.getId());
        int finalQty;
        if (existing.isPresent()) {
            finalQty = existing.get().getQuantity() + quantity;
            existing.get().setQuantity(finalQty);
        } else {
            cart.add(new CartItem(product, quantity));
            finalQty = quantity;
        }
        persistUpsert(product.getId(), finalQty);
    }

    public static void setCartQuantity(int productId, int quantity) {
        Optional<CartItem> existing = findCartItem(productId);
        if (quantity <= 0) {
            removeFromCart(productId);
            return;
        }
        if (existing.isPresent()) {
            existing.get().setQuantity(quantity);
            persistUpsert(productId, quantity);
        }
    }

    public static void removeFromCart(int productId) {
        cart.removeIf(item -> item.getProduct().getId() == productId);
        int buyerId = getBuyerId();
        if (buyerId > 0) {
            try {
                new CartDAO().remove(buyerId, productId);
            } catch (SQLException e) {
                System.err.println("[cart] remove failed: " + e.getMessage());
            }
        }
    }

    public static void replaceCart(List<CartItem> items) {
        cart.clear();
        cart.addAll(items);
    }

    public static void loadPersistedCart() {
        int buyerId = getBuyerId();
        if (buyerId <= 0) {
            return;
        }
        try {
            replaceCart(new CartDAO().load(buyerId));
        } catch (SQLException e) {
            System.err.println("[cart] load failed: " + e.getMessage());
        }
    }

    private static void persistUpsert(int productId, int quantity) {
        int buyerId = getBuyerId();
        if (buyerId > 0) {
            try {
                new CartDAO().upsert(buyerId, productId, quantity);
            } catch (SQLException e) {
                System.err.println("[cart] persist failed: " + e.getMessage());
            }
        }
    }
}
