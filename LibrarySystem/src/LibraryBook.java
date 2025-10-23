public class LibraryBook {
    private String title;
    private String author;
    private String genre;
    private int year;
    private int id;

    public LibraryBook(String title, String author, int year, String genre, int id) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.genre = genre;
        this.id = id;
    }

    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }
    public int getYear() {
        return year;
    }
    public String getGenre() {
        return genre;
    }

    @Override
    public String toString() {
        return String.format("%d | %s | %s | %s | %d", id, title, author, genre, year);
    }
}
