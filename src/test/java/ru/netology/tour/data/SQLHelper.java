package ru.netology.tour.data;

import lombok.SneakyThrows;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class SQLHelper {

    private static final QueryRunner runner = new QueryRunner();
    private static final Properties properties = loadProperties();

    private SQLHelper() {}

    @SneakyThrows
    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(
                Paths.get("application.properties"))) {
            properties.load(input);
        }
        return properties;
    }

    @SneakyThrows
    private static Connection getConn() {
        return DriverManager.getConnection(
                properties.getProperty("spring.datasource.url"),
                properties.getProperty("spring.datasource.username"),
                properties.getProperty("spring.datasource.password")
        );
    }

    @SneakyThrows
    public static <T> T getValue(String query, Object... params) {
        try (var conn = getConn()) {
            return runner.query(
                    conn,
                    query,
                    new ScalarHandler<>(),
                    params
            );
        }
    }

    @SneakyThrows
    public static void cleanDatabase() {
        try (var conn = getConn()) {
            runner.update(conn, "DELETE FROM order_entity");
            runner.update(conn, "DELETE FROM payment_entity");
            runner.update(conn, "DELETE FROM credit_request_entity");
        }
    }

    private static int getCount(String query, Object... params) {
        Number count = getValue(query, params);
        return count.intValue();
    }

    public static String getPaymentStatus(String transactionId) {
        return getValue(
                "SELECT status FROM payment_entity WHERE transaction_id = ?",
                transactionId
        );
    }

    public static int getPaymentAmount(String transactionId) {
        return getValue(
                "SELECT amount FROM payment_entity WHERE transaction_id = ?",
                transactionId
        );
    }

    public static int getOrderCount(String transactionId) {
        return getCount(
                "SELECT COUNT(*) FROM order_entity WHERE payment_id = ?",
                transactionId
        );
    }

    public static String getLatestTransactionId() {
        return getValue(
                "SELECT transaction_id " +
                        "FROM payment_entity " +
                        "ORDER BY created DESC LIMIT 1"
        );
    }

    public static int getPaymentCount() {
        return getCount(
                "SELECT COUNT(*) FROM payment_entity"
        );
    }

    public static int getOrderCount() {
        return getCount(
                "SELECT COUNT(*) FROM order_entity"
        );
    }

    public static int getCreditRequestCount() {
        return getCount(
                "SELECT COUNT(*) FROM credit_request_entity"
        );
    }
}