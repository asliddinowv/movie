package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private final String URL = "jdbc:postgresql://localhost:5432";
    private final String DATABASE = "movie_bot";
    private final String USER = "postgres";
    private final String PASSWORD = "root";


    public Connection getConnection() {
        try{
            Connection connection = DriverManager
                    .getConnection(URL + "/" + DATABASE, USER, PASSWORD);

            System.out.println("Connected");
            return connection;

        } catch (SQLException e) {

            System.out.println("Failed");
            throw new RuntimeException(e);
        }
    }
}
