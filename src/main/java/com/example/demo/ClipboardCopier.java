package com.example.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class ClipboardCopier extends JFrame {

    private Point initialClick;
    private final Map<String, Integer> pressCountMap = new HashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ClipboardCopier(Properties props) {
        setTitle("Clipboard Copier");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setUndecorated(true);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Drag support
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;
                setLocation(thisX + xMoved, thisY + yMoved);
            }
        });

        // Collect button definitions
        Set<Integer> buttonNumbers = new HashSet<>();
        for (String key : props.stringPropertyNames()) {
            if (key.matches("button\\d+\\.(label|text)")) {
                int num = Integer.parseInt(key.replaceAll("\\D+", ""));
                buttonNumbers.add(num);
            }
        }

        List<Integer> sortedButtons = new ArrayList<>(buttonNumbers);
        Collections.sort(sortedButtons);

        for (int i : sortedButtons) {
            String label = props.getProperty("button" + i + ".label");
            String text = props.getProperty("button" + i + ".text");

            if (label != null && text != null) {
                JButton button = new JButton(label);
                button.setAlignmentX(Component.CENTER_ALIGNMENT);
                button.setFocusable(false);
                pressCountMap.put(label, 0);

                button.addActionListener(e -> {
                    copyToClipboard(text);
                    int newCount = pressCountMap.merge(label, 1, Integer::sum);
                    String time = LocalDateTime.now().format(formatter);
                    System.out.println(time + " | " + label + " pressed | count: " + newCount);
                });

                panel.add(button);
                panel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        add(panel);
        pack();
    }

    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java com.example.demo.ClipboardCopier <path_to_properties_file>");
            System.exit(1);
        }

        String configPath = args[0];
        File configFile = new File(configPath);

        if (!configFile.exists() || !configFile.isFile()) {
            System.err.println("Error: Config file not found at path: " + configPath);
            System.exit(1);
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Error reading properties file: " + e.getMessage());
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> new ClipboardCopier(props).setVisible(true));
    }
}
