package id.ac.ui.cs.advprog.udehnihauth.model;


public class UserRoleManager {

    private UserRoleManager() {}

    public static boolean addRoleToUser(User user, Role role) {
        boolean roleExists = user.getRoles().stream()
                .anyMatch(r -> r.getId() != null && r.getId().equals(role.getId()));

        if (roleExists) {
            return false;
        }

        user.getRoles().add(role);
        role.getUsers().add(user);
        return true;
    }

    public static boolean removeRoleFromUser(User user, Role role) {
        Role roleToRemove = user.getRoles().stream()
                .filter(r -> r.getId() != null && r.getId().equals(role.getId()))
                .findFirst()
                .orElse(null);

        if (roleToRemove == null) {
            return false;
        }

        user.getRoles().remove(roleToRemove);
        role.getUsers().remove(user);
        return true;
    }

    public static boolean hasRole(User user, RoleType roleType) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == roleType);
    }
}