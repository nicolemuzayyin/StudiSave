package core.tables;

// authors: Nicole Muzayyin

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class User {
    private final String userID;
    private final String email;
    private final String name;

    public User(String userID, String name, String email) {
        this.userID = userID;
        this.name = name;
        this.email = email;
    }

    // UserManager: verify the retrieved password (result[3]) and the passwordAttempt
    public static User validate(String[] result, String passwordAttempt){
        String password = result[3];
        Argon2 argon2 = Argon2Factory.create();
        if (argon2.verify(password, passwordAttempt.toCharArray())) {
            return new User(result[0], result[1], result[2]);
        }
        return null;
    }

    public String getUserID() { return userID; }
    public String getEmail() {
        return email;
    }
    public String getName() {
        return name;
    }
}