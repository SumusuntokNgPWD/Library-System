import java.util.ArrayList;
import java.util.List;

public class WorkerNode {
    private final int nodeId;
    private final MultiHashTable table;
    private final List<LibraryBook> assignedBooks;
    private final int capacity;

    public WorkerNode(int nodeId, int tableSize, int capacity) {
        this.nodeId = nodeId;
        this.table = new MultiHashTable(tableSize);
        this.assignedBooks = new ArrayList<>();
        this.capacity = capacity;
    }

    // Called by master to add a book to this worker (before preprocessing)
    public void addBook(LibraryBook book) {
        if (assignedBooks.size() < capacity) {
            assignedBooks.add(book);
        } else {
            System.out.println("⚠️ Worker " + nodeId + " is full! Ignoring book id=" + book.getId());
        }
    }

    // Bulk load and preprocess (build hash tables & sort buckets)
    public void loadBooks(List<LibraryBook> books) {
        assignedBooks.addAll(books);
        preprocess();
    }

    // Build internal hash tables using assignedBooks
    public void preprocess() {
        table.preprocess(assignedBooks);
    }

    // Search wrapper: uses local MultiHashTable
    public List<Integer> search(String type, String query, int fromYear, int toYear) {
        switch (type) {
            case "Title": return table.searchByTitle(query);
            case "Author": return table.searchByAuthor(query);
            case "Genre": return table.searchByGenre(query);
            case "Year": return table.searchByYear(fromYear, toYear);
            default: return List.of();
        }
    }

    public int getNodeId() { return nodeId; }
    public int getBookCount() { return assignedBooks.size(); }
    public int getCapacity() { return capacity; }
}
