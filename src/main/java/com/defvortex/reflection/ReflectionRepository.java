package com.defvortex.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class ReflectionRepository<T> {

    private static Connection connection;
    private static Statement stmt;

    private Class<T> myClass;

    public ReflectionRepository(Class<T> myClass) {
        this.myClass = myClass;
    }

    public void save(T object) throws IllegalAccessException, ClassNotFoundException, SQLException {
        if (object == null) {
            throw new RuntimeException("Illegal object, null");
        }
        List<String> keys = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        StringBuilder stringBuilder = saveQuery(object, keys, values);
        try {
            connect();
            connection.setAutoCommit(false);
            stmt.executeUpdate(stringBuilder.toString());
            connection.commit();
            connection.setAutoCommit(true);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Can't connect to DB");
        } catch (SQLException e) {
            throw new SQLException("SQL query inadequate");
        } finally {
            disconnect();
        }
    }

    public T findById(Long id) {
        Constructor<?> mostArgsConstructor = getMostArgsConstructor();
        T returningObject = null;
        try {
            connect();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM Students WHERE id = ?");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                returningObject = (T) mostArgsConstructor.newInstance(id, rs.getString(2),
                        rs.getString(3), rs.getInt(4), rs.getInt(5));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return returningObject;
    }


    public List<T> findAllObjects() throws Exception {
        Constructor<?> mostArgsConstructor = getMostArgsConstructor();
        String sqlQuery = "SELECT * FROM " + myClass.getAnnotation(DbTable.class).name() + ";";
        Object returningObject;
        List<T> objectsList = new ArrayList<>();
        try {
            connect();
            ResultSet rs = stmt.executeQuery(sqlQuery);
            while (rs.next()) {
                assert mostArgsConstructor != null;
                returningObject = mostArgsConstructor.newInstance(rs.getLong(1), rs.getString(2),
                        rs.getString(3), rs.getInt(4), rs.getInt(5));
                objectsList.add((T) returningObject);
            }
            rs.close();
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Can't connect to DB");
        } catch (Exception e) {
            throw new Exception("Something wrong with this method");
        } finally {
            disconnect();
        }
        return objectsList;
    }

    public void deleteById(Long id) throws SQLException, ClassNotFoundException {
        try {
            connect();
            String stringBuilder = "DELETE FROM " +
                    myClass.getAnnotation(DbTable.class).name() + " WHERE ID = " + id + ";";
            stmt.executeUpdate(stringBuilder);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Can't connect to DB");
        } catch (SQLException e) {
            throw new SQLException("SQL query inadequate");
        } finally {
            disconnect();
        }
    }

    public void deleteAllFields() throws ClassNotFoundException, SQLException {
        try {
            connect();
            connection.setAutoCommit(false);
            String stringBuilder = "DELETE FROM " + myClass.getAnnotation(DbTable.class).name() + ";";
            stmt.executeUpdate(stringBuilder);
            connection.setAutoCommit(true);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Can't connect to DB");
        } catch (SQLException e) {
            throw new SQLException("SQL query inadequate");
        } finally {
            disconnect();
        }
    }

    public T saveAndGet(T object) throws Exception {
        if (object == null) {
            throw new RuntimeException("Illegal object null");
        }
        List<String> keys = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        StringBuilder stringBuilder = saveQuery(object, keys, values);
        T returningObject = null;
        try {
            connect();
            connection.setAutoCommit(false);
            stmt.executeUpdate(stringBuilder.toString());
            connection.commit();

            stringBuilder.delete(0, stringBuilder.length());

            stringBuilder.append("SELECT * FROM ");
            stringBuilder.append(object.getClass().getAnnotation(DbTable.class).name());
            stringBuilder.append(" WHERE ");
            for (int i = 0; i < keys.size(); i++) {
                stringBuilder.append(keys.get(i)).append(" = ").append(values.get(i)).append(" AND ");
            }
            stringBuilder.delete(stringBuilder.length() - 5, stringBuilder.length());
            stringBuilder.append(";");

            ResultSet rs = stmt.executeQuery(stringBuilder.toString());

            Constructor<?> mostArgsConstructor = getMostArgsConstructor();
            while (rs.next()) {
                returningObject = (T) mostArgsConstructor.newInstance(rs.getLong(1), rs.getString(2),
                        rs.getString(3), rs.getInt(4), rs.getInt(5));
            }
            connection.setAutoCommit(true);
            rs.close();
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Can't connect to DB");
        } catch (Exception e) {
            throw new Exception("Something wrong with this method");
        } finally {
            disconnect();
        }
        return returningObject;
    }

    private StringBuilder saveQuery(T object, List<String> keys, List<Object> values) throws IllegalAccessException {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.getAnnotation(DbColumn.class) != null) {
                keys.add(f.getName());
                if (f.getAnnotation(DbText.class) != null) {
                    values.add("\"" + f.get(object) + "\"");
                } else values.add(f.get(object));
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO ");
        stringBuilder.append(object.getClass().getAnnotation(DbTable.class).name());
        stringBuilder.append(" (");
        for (String key : keys) {
            stringBuilder.append(key).append(", ");
        }
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        stringBuilder.append(") VALUES (");
        for (Object value : values) {
            stringBuilder.append(value).append(", ");
        }
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        stringBuilder.append(");");
        return stringBuilder;
    }

    private Constructor<?> getMostArgsConstructor() {
        Constructor<?>[] constructors = myClass.getConstructors();
        Constructor<?> mostArgsConstructor = null;

        int numInMostArgsConstructor = Integer.MIN_VALUE;
        for (Constructor<?> c : constructors) {
            if (c.getParameterCount() > numInMostArgsConstructor) {
                mostArgsConstructor = c;
                numInMostArgsConstructor = c.getParameterCount();
            }
        }
        return mostArgsConstructor;
    }

    public static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Саша\\java projects\\Reflection-API-ORM\\src\\main\\java\\com\\defvortex\\reflection\\data.db");
        stmt = connection.createStatement();
    }

    public static void disconnect() {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}