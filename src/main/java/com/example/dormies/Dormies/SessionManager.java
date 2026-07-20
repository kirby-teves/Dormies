package com.example.dormies.Dormies;

import java.io.*;

public class SessionManager {
    private static final String SESSION_FILE = "user.dat";

    public static void saveSession(Person person) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SESSION_FILE))) {
            oos.writeObject(person);
        } catch (IOException e) {
            System.err.println("Serialization error: " + e.getMessage());
        }
    }

    public static Person loadSession() {
        File file = new File(SESSION_FILE);
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SESSION_FILE))) {
            return (Person) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Deserialization error: " + e.getMessage());
            return null;
        }
    }

    public static void clearSession() {
        File file = new File(SESSION_FILE);
        if (file.exists()) {
            boolean deleted = file.delete();
        }
    }
}
