package roomescape.user;

public class User {
    private final Long id;
    private final String name;
    private final String username;
    private final String password;
    private final Role role;
    private final Long storeId;

    public User(Long id, String name, String username, String password, Role role, Long storeId) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.storeId = storeId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public Long getStoreId() {
        return storeId;
    }

    public boolean isManager() {
        return role == Role.MANAGER;
    }
}
