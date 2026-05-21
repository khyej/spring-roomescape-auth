package roomescape.auth;

import roomescape.user.Role;

public record LoginUser(Long id, String name, Role role, Long storeId) {
    public boolean isManager() {
        return role == Role.MANAGER;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isManagerOrAdmin() {
        return role == Role.MANAGER || role == Role.ADMIN;
    }
}
