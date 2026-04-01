package sickbay.pokenamon.controller;

import sickbay.pokenamon.model.User;

public class UserManager {
    private static UserManager instance;
    private User currentUser;

    private UserManager() {}

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public User getUser() { return currentUser; }
    public void setUser(User user) { this.currentUser = user; }
}
