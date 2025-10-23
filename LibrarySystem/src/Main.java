import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class Main extends JFrame {
    private MasterNode master;
    private List<String> allTitles;
    private List<String> allAuthors;
    private List<String> allGenres;
    private JLabel fromLabel;
    private JLabel toLabel;
    private JPanel inputPanel;
    private JComponent inputComponent1;
    private JComponent inputComponent2;
    private JTextArea resultArea;
    private JComboBox<String> searchTypeCombo;
    private Map<Integer, LibraryBook> bookMap;

    public Main(MasterNode master, Map<Integer, LibraryBook> bookMap,
                           List<String> allTitles, List<String> allAuthors, List<String> allGenres) {
        this.master = master;
        this.bookMap = bookMap;
        this.allTitles = allTitles;
        this.allAuthors = allAuthors;
        this.allGenres = allGenres;
        createGUI();
    }

    private void createGUI() {
        setTitle("Distributed Library Search (simulated)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 450);
        getContentPane().setLayout(new BorderLayout());

        inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        searchTypeCombo = new JComboBox<>(new String[]{"Title", "Author", "Year", "Genre"});
        inputComponent1 = new JTextField(18);
        inputComponent2 = null;
        fromLabel = new JLabel("From:");
        toLabel = new JLabel("To:");
        JButton searchButton = new JButton("Search");

        inputPanel.add(new JLabel("Search by:"));
        inputPanel.add(searchTypeCombo);
        inputPanel.add(inputComponent1);
        inputPanel.add(searchButton);

        getContentPane().add(inputPanel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        getContentPane().add(new JScrollPane(resultArea), BorderLayout.CENTER);

        searchTypeCombo.addActionListener(e -> switchInputComponent());
        searchButton.addActionListener(e -> performSearch());

        // default autocomplete for title
        setupAutocomplete((JTextField) inputComponent1, allTitles);

        setVisible(true);
        setResizable(true);
        setLocationRelativeTo(null);
    }

    private void switchInputComponent() {
        inputPanel.removeAll();

        String type = (String) searchTypeCombo.getSelectedItem();
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch());

        inputPanel.add(new JLabel("Search by:"));
        inputPanel.add(searchTypeCombo);

        if ("Year".equals(type)) {
            inputComponent1 = new JSpinner(new SpinnerNumberModel(2000, 1900, 2100, 1));
            inputComponent2 = new JSpinner(new SpinnerNumberModel(2025, 1900, 2100, 1));
            inputPanel.add(fromLabel);
            inputPanel.add(inputComponent1);
            inputPanel.add(toLabel);
            inputPanel.add(inputComponent2);
        } else {
            inputComponent1 = new JTextField(18);
            inputComponent2 = null;
            inputPanel.add(inputComponent1);

            if ("Title".equals(type)) setupAutocomplete((JTextField) inputComponent1, allTitles);
            else if ("Author".equals(type)) setupAutocomplete((JTextField) inputComponent1, allAuthors);
            else if ("Genre".equals(type)) setupAutocomplete((JTextField) inputComponent1, allGenres);
        }

        inputPanel.add(searchButton);
        inputPanel.revalidate();
        inputPanel.repaint();
    }

    private void setupAutocomplete(JTextField textField, List<String> dataList) {
        JWindow suggestionWindow = new JWindow(this);
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> suggestionList = new JList<>(listModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFocusable(false);

        suggestionList.setBackground(new Color(230, 230, 250));
        suggestionList.setForeground(Color.BLACK);
        suggestionList.setSelectionBackground(new Color(100, 149, 237));
        suggestionList.setSelectionForeground(Color.WHITE);

        suggestionWindow.getContentPane().add(new JScrollPane(suggestionList));

        Runnable updateSuggestions = () -> {
            String text = textField.getText().trim().toLowerCase();
            listModel.clear();
            if (text.isEmpty()) {
                suggestionWindow.setVisible(false);
                return;
            }
            int count = 0;
            for (String item : dataList) {
                if (item.toLowerCase().contains(text)) {
                    listModel.addElement(item);
                    count++;
                    if (count >= 10) break;
                }
            }
            if (listModel.isEmpty()) {
                suggestionWindow.setVisible(false);
                return;
            }
            suggestionList.setSelectedIndex(0);
            try {
                Point p = textField.getLocationOnScreen();
                suggestionWindow.setSize(textField.getWidth(), Math.min(listModel.size() * 25, 10 * 25));
                suggestionWindow.setLocation(p.x, p.y + textField.getHeight());
                suggestionWindow.setVisible(true);
            } catch (IllegalComponentStateException ex) {
                // ignore if not on screen yet
            }
        };

        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { SwingUtilities.invokeLater(updateSuggestions); }
            public void removeUpdate(DocumentEvent e) { SwingUtilities.invokeLater(updateSuggestions); }
            public void changedUpdate(DocumentEvent e) { SwingUtilities.invokeLater(updateSuggestions); }
        });

        textField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!suggestionWindow.isVisible()) return;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN -> {
                        int next = Math.min(suggestionList.getSelectedIndex() + 1, listModel.size() - 1);
                        suggestionList.setSelectedIndex(next);
                        suggestionList.ensureIndexIsVisible(next);
                    }
                    case KeyEvent.VK_UP -> {
                        int prev = Math.max(suggestionList.getSelectedIndex() - 1, 0);
                        suggestionList.setSelectedIndex(prev);
                        suggestionList.ensureIndexIsVisible(prev);
                    }
                    case KeyEvent.VK_ENTER -> {
                        String selected = suggestionList.getSelectedValue();
                        if (selected != null) textField.setText(selected);
                        suggestionWindow.setVisible(false);
                    }
                    case KeyEvent.VK_ESCAPE -> suggestionWindow.setVisible(false);
                }
            }
        });

        suggestionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                String selected = suggestionList.getSelectedValue();
                if (selected != null) {
                    textField.setText(selected);
                    suggestionWindow.setVisible(false);
                }
            }
        });

        textField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                Component o = e.getOppositeComponent();
                if (o == null || !SwingUtilities.isDescendingFrom(o, suggestionWindow)) {
                    SwingUtilities.invokeLater(() -> suggestionWindow.setVisible(false));
                }
            }
        });
    }

    private static long measureTimeMs(Runnable r) {
        long s = System.nanoTime();
        r.run();
        long e = System.nanoTime();	
        return (e - s) / 1_000_000;
    }

    private void performSearch() {
        String type = (String) searchTypeCombo.getSelectedItem();
        resultArea.setText("");

        Runtime rt = Runtime.getRuntime();
        long beforeMem = rt.totalMemory() - rt.freeMemory();

        List<LibraryBook> results = new ArrayList<>();
        long startTime = System.nanoTime();

        try {
            switch (type) {
                case "Title" -> {
                    if (!(inputComponent1 instanceof JTextField)) return;
                    String title = ((JTextField) inputComponent1).getText().trim();
                    if (title.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Please enter a title.");
                        return;
                    }
                    results.addAll(master.search("Title", title.toLowerCase(), 0, 0));
                }
                case "Author" -> {
                    if (!(inputComponent1 instanceof JTextField)) return;
                    String authorInput = ((JTextField) inputComponent1).getText().trim();
                    if (authorInput.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Please enter an author.");
                        return;
                    }
                    results.addAll(master.search("Author", authorInput.toLowerCase(), 0, 0));
                }
                case "Genre" -> {
                    if (!(inputComponent1 instanceof JTextField)) return;
                    String genre = ((JTextField) inputComponent1).getText().trim();
                    if (genre.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Please enter a genre.");
                        return;
                    }
                    results.addAll(master.search("Genre", genre.toLowerCase(), 0, 0));
                }
                case "Year" -> {
                    if (!(inputComponent1 instanceof JSpinner) || !(inputComponent2 instanceof JSpinner)) return;
                    int fromY = (int) ((JSpinner) inputComponent1).getValue();
                    int toY = (int) ((JSpinner) inputComponent2).getValue();
                    if (fromY > toY) {
                        JOptionPane.showMessageDialog(this, "'From' cannot be > 'To'");
                        return;
                    }
                    results.addAll(master.search("Year", null, fromY, toY));
                }
            }

            // Already on EDT, just call directly
            displayBookResults(results);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        long endTime = System.nanoTime();
        long afterMem = rt.totalMemory() - rt.freeMemory();

        long elapsedMs = (endTime - startTime) / 1_000_000;
        double memUsedMB = (afterMem - beforeMem) / 1024.0 / 1024.0;

        resultArea.append(String.format("\n(Search took %d ms)", elapsedMs));
        resultArea.append(String.format("\nMemory used (approx): %.2f MB", memUsedMB));
    }
    

    // Normalize input to match MultiHashTable
    private String normalizeInput(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+", " ").trim().toLowerCase();
    }


    private void displayBookResults(List<LibraryBook> books) {
        if (books == null || books.isEmpty()) {
            resultArea.setText("No books found.");
            return;
        }
        resultArea.setText("");
        for (LibraryBook b : books) {
            resultArea.append(b.toString() + "\n");
        }
    }

    // ---------- entry point ----------
    public static void main(String[] args) throws SQLException {
        DbConnection dbConn = new DbConnection();
        Connection conn = dbConn.Connect();
        LibraryData libraryData = new LibraryData(conn);

        final List<LibraryBook>[] holder = new List[1];
        long retrievalTime = measureTimeMs(() -> {
            try {
                holder[0] = libraryData.fetchAllBooks();
            } catch (SQLException ex) {
                ex.printStackTrace();
                holder[0] = new ArrayList<>();
            }
        });
        List<LibraryBook> books = holder[0];
        System.out.println("Data retrieval took: " + retrievalTime + " ms. Loaded: " + books.size());

        Map<Integer, LibraryBook> bookMap = new HashMap<>();
        for (LibraryBook b : books) bookMap.put(b.getId(), b);

        // create master + workers
        int numWorkers = 10;         // changeable
        int perWorkerTableSize = 100;
        int capacityPerWorker = 20000; // large capacity for simulation
        MasterNode master = new MasterNode(numWorkers, perWorkerTableSize, capacityPerWorker);

        // distribute & preprocess
        master.distributeBooks(books);
        master.printWorkerStats();

        // prepare autocomplete lists (global)
        List<String> allTitles = new ArrayList<>();
        List<String> allAuthors = new ArrayList<>();
        Set<String> genres = new HashSet<>();
        for (LibraryBook b : books) {
            allTitles.add(b.getTitle());
            allAuthors.add(b.getAuthor());
            genres.add(b.getGenre());
        }
        List<String> allGenres = new ArrayList<>(genres);
        Collections.sort(allGenres);

        SwingUtilities.invokeLater(() ->
                new Main(master, bookMap, allTitles, allAuthors, allGenres)
        );
    }
}
