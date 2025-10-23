import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryData {
    private Connection conn;

    public LibraryData(Connection conn) {
        this.conn = conn;
    }

    public List<LibraryBook> fetchAllBooks() throws SQLException {
        List<LibraryBook> books = new ArrayList<>();
        String query = "SELECT id, title, author, year, genre FROM books";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            String title = rs.getString("title");
            String author = rs.getString("author");
            int year = rs.getInt("year");
            String genre = rs.getString("genre");
            int id = rs.getInt("id");
            books.add(new LibraryBook(title, author, year, genre, id));
        }

        rs.close();
        stmt.close();
        return books;
    }

    public void close() throws SQLException {
        if (conn != null) conn.close();
    }
}
