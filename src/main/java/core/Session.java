package core;

// author: Felix D'Cruz
// saves a verified user so that their verified info can be accessed easily

import core.tables.User;

public class Session {
    private static User user = null;
    public static void setUser(User login) { user = login; }
    public static User getUser()        { return user; }
}
