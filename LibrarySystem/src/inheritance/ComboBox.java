package inheritance;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ComboBox<E> extends JComboBox<E> {

    public ComboBox(E[] items) {
        super(items);
        init();
    }

    private void init() {
        // Set basic styling
        setBackground(new Color(100, 149, 237));
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.PLAIN, 12));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setOpaque(true);
        setFocusable(false);

        // Smaller preferred size
        setPreferredSize(new Dimension(120, 25));

        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(65, 105, 225));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(new Color(100, 149, 237));
            }
        });
    }
}
