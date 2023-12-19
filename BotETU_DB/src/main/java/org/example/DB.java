package org.example;

import com.pengrad.telegrambot.model.User;

import java.sql.*;

public class DB {
    String jdbcUrl = "jdbc:postgresql://localhost:5432/BotETU";
    Connection connection;
    public DB() {
        String password = System.getenv("BotETUPassword");
        if (password == null) {
            throw new RuntimeException("Пароль не найден в переменной среды 'BotETUPassword'");
        }
        try {
            connection = DriverManager.getConnection(
                    jdbcUrl,
                    "postgres",
                    password
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertUserIfNotExist(User user, String group) {
        int groupNumber = Integer.parseInt(group);
        String statement = String.format(
                "INSERT INTO users (tg_id, username, firstname, lastname, group_number, timestamp) " +
                        "VALUES (%d, '%s', '%s', '%s', %d, current_timestamp) " +
                        "ON CONFLICT (tg_id) DO UPDATE " +
                        "SET " +
                        "   username = EXCLUDED.username, " +
                        "   firstname = EXCLUDED.firstname, " +
                        "   lastname = EXCLUDED.lastname, " +
                        "   group_number = EXCLUDED.group_number, " +
                        "   timestamp = current_timestamp;",
                user.id(), user.username(), user.firstName(), user.lastName(), groupNumber
        );

        try {
            PreparedStatement query = connection.prepareStatement(statement);
            query.execute();
        } catch (SQLException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    public void setGroupForUser(User user, String newGroupNumber) {
        int GroupNumber = Integer.parseInt(newGroupNumber);
        String statement = "UPDATE users SET group_number = ? WHERE tg_id = ?";

        try (PreparedStatement query = connection.prepareStatement(statement)) {
            query.setInt(1, GroupNumber);
            query.setLong(2, user.id());
            int rowsUpdated = query.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Номер группы успешно обновлен.");
            } else {
                System.out.println("Данный пользователь не найден в базе.");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    public String getGroupForUser(User user) {
        String statement = "SELECT group_number FROM users WHERE tg_id = ?";
        String groupNumber = "0";

        try (PreparedStatement query = connection.prepareStatement(statement)) {
            query.setLong(1, user.id());
            try (ResultSet result = query.executeQuery()) {
                if (result.next()) {
                    groupNumber = result.getString("group_number");
                } else {
                    System.out.println("Данный пользователь не найден в базе.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        return groupNumber;
    }
}