import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterNode {
    private final List<WorkerNode> workers;
    private final Map<Integer, LibraryBook> bookMap; // id -> book
    private int numWorkers;

    /**
     * @param numWorkers number of worker nodes
     * @param tableSize per-worker hash table size
     * @param capacity maximum number of books per worker (simulation)
     */
    public MasterNode(int numWorkers, int tableSize, int capacity) {
        this.numWorkers = Math.max(1, numWorkers);
        workers = new ArrayList<>();
        bookMap = new HashMap<>();
        for (int i = 0; i < this.numWorkers; i++) {
            workers.add(new WorkerNode(i, tableSize, capacity));
        }
    }

    // Distribution hash: using title
    private int hashNodeByTitle(String title) {
        return Math.abs(title.toLowerCase().hashCode()) % numWorkers;
    }

    private int hashNodeById(int id) {
        return Math.abs(id) % numWorkers;
    }

    /**
     * Distribute books to workers (by title hash). Also stores master bookMap.
     */
    public void distributeBooks(List<LibraryBook> books) {
        Map<Integer, List<LibraryBook>> partitions = new HashMap<>();
        for (int i = 0; i < numWorkers; i++) partitions.put(i, new ArrayList<>());

        for (LibraryBook book : books) {
            int nodeIndex = hashNodeByTitle(book.getTitle()); // title-based distribution
            partitions.get(nodeIndex).add(book);
            bookMap.put(book.getId(), book);
        }

        for (int i = 0; i < numWorkers; i++) {
            List<LibraryBook> p = partitions.get(i);
            if (!p.isEmpty()) {
                workers.get(i).loadBooks(p);
            }
            System.out.println("Master: Worker " + i + " received " + p.size() + " books");
        }
    }

    /**
     * Search entry point. Returns full LibraryBook objects.
     * Title uses hash-based lookup.
     * Author, Genre, Year broadcast to all workers.
     */
    public List<LibraryBook> search(String type, String key, int fromYear, int toYear) {
        List<LibraryBook> result = new ArrayList<>();

        switch (type) {
            case "Title" -> {
                if (key == null || key.isEmpty()) return result;
                int nodeIndex = hashNodeByTitle(key); // single worker
                WorkerNode target = workers.get(nodeIndex);
                List<Integer> ids = target.search("Title", key, fromYear, toYear);
                for (int id : ids) {
                    LibraryBook b = bookMap.get(id);
                    if (b != null) result.add(b);
                }
            }
            case "Author", "Genre", "Year" -> {
                // broadcast search
                for (WorkerNode w : workers) {
                    List<Integer> ids = w.search(type, key, fromYear, toYear);
                    for (int id : ids) {
                        LibraryBook b = bookMap.get(id);
                        if (b != null) result.add(b);
                    }
                }
            }
        }

        return result;
    }
}


/*
    // For debugging / info
    public void printWorkerStats() {
        for (WorkerNode w : workers) {
            System.out.println("Worker " + w.getNodeId() + ": books=" + w.getBookCount() + " cap=" + w.getCapacity());
        }
    }
    */

