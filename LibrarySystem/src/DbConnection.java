import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JOptionPane;

public class DbConnection {
    String url = "jdbc:mysql://localhost:3306/book";
    String username = "root";
    String password = "";

    public Connection Connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Successfully Connected to Database!");
            return conn;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Connection Error: Please Connect to Database First!", "Error!", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            return null;
        }
    }
}


