package inheritance;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SimpleButton extends JButton {

    public SimpleButton(String text) {
        super(text);
        init();
    }

    private void init() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(true);
        setBackground(new Color(100, 149, 237));
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.PLAIN, 12)); // smaller font
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Smaller preferred size
        setPreferredSize(new Dimension(80, 25)); // width=80px, height=25px

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
