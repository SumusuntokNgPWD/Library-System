import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MultiHashTable {

    private HashTable titleTable;
    private HashTable authorTable;
    private HashTable yearTable;
    private HashTable genreTable;
    private int tableSize;

    public MultiHashTable(int tableSize) {
        // Use a prime number to reduce collisions
        this.tableSize = tableSize;
        titleTable = new HashTable(tableSize);
        authorTable = new HashTable(tableSize);
        yearTable = new HashTable(tableSize);
        genreTable = new HashTable(tableSize);
    }

    // Preprocess phase: insert all books
    public void preprocess(List<LibraryBook> books) {
        for (LibraryBook book : books) {
            int id = book.getId();
            titleTable.insert(book.getTitle(), id);
            authorTable.insert(book.getAuthor(), id);
            yearTable.insert(String.valueOf(book.getYear()), id);
            genreTable.insert(book.getGenre(), id);
        }

        titleTable.preprocess();
        authorTable.preprocess();
        yearTable.preprocess();
        genreTable.preprocess();
    }

    // ------------------ Data access ------------------
    public List<Integer> searchByTitle(String title) {
        return titleTable.search(title);
    }

    public List<Integer> searchByAuthor(String author) {
        return authorTable.search(author);
    }

    public List<Integer> searchByYear(int fromYear, int toYear) {
        List<Integer> result = new ArrayList<>();
        for (int year = fromYear; year <= toYear; year++) {
            result.addAll(yearTable.search(String.valueOf(year)));
        }
        return result;
    }

    public List<Integer> searchByGenre(String genre) {
        return genreTable.search(genre);
    }

    // ---------------------- Inner HashTable -----------------------
    private static class HashTable {
        private List<Entry>[] table;
        private int size;

        @SuppressWarnings("unchecked")
        public HashTable(int size) {
            this.size = size;
            table = new ArrayList[size];
            for (int i = 0; i < size; i++) {
                table[i] = new ArrayList<>();
            }
        }

        private int hash(String key) {
            return Math.abs(normalizeKey(key).hashCode() % size);
        }

        // Entry stores normalized key and ID
        private static class Entry {
            String key;
            int id;

            Entry(String key, int id) {
                this.key = key;
                this.id = id;
            }
        }

        public void insert(String key, int id) {
            String normalized = normalizeKey(key);
            int index = hash(normalized);
            table[index].add(new Entry(normalized, id));
        }

        // Preprocess: sort each bucket by normalized key
        public void preprocess() {
            for (List<Entry> bucket : table) {
                bucket.sort(Comparator.comparing(e -> e.key)); // already lowercase
            }
        }

        private String normalizeKey(String key) {
            if (key == null) return "";
            // Replace any whitespace (space, tab, line break) with a single space, trim, and lowercase
            return key.replaceAll("\\s+", " ").trim().toLowerCase();
        }

        // Hash + Binary search inside the bucket
        public List<Integer> search(String key) {
            String normalized = normalizeKey(key);
            int index = hash(normalized);
            List<Entry> bucket = table[index];
            List<Integer> result = new ArrayList<>();

            int low = 0, high = bucket.size() - 1;

            while (low <= high) {
                int mid = (low + high) / 2;
                Entry midEntry = bucket.get(mid);
                int cmp = midEntry.key.compareTo(normalized); // already normalized

                if (cmp == 0) {
                    // Found match, collect duplicates on both sides
                    int left = mid;
                    while (left >= 0 && bucket.get(left).key.equals(normalized)) {
                        result.add(bucket.get(left).id);
                        left--;
                    }
                    int right = mid + 1;
                    while (right < bucket.size() && bucket.get(right).key.equals(normalized)) {
                        result.add(bucket.get(right).id);
                        right++;
                    }
                    break;
                } else if (cmp < 0) {
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }

            return result;
        }

        // Optional: display bucket contents
        public void display() {
            for (int i = 0; i < size; i++) {
                System.out.print("Bucket " + i + ": ");
                for (Entry e : table[i]) {
                    System.out.print("(" + e.key + ", " + e.id + ") ");
                }
                System.out.println();
            }
        }
    }
}
