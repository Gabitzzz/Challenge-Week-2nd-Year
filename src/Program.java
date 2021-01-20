import EightPuzzle.EightPuzzle;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import static EightPuzzle.EightPuzzle.Heuristic;
import static EightPuzzle.EightPuzzle.Heuristic.*;
import static java.lang.System.exit;

public class Program extends JFrame {
    // https://www3.ntu.edu.sg/home/ehchua/programming/java/J4b_CustomGraphics.html
    JPanel panel;
    JComboBox comboBox;
    JButton button;
    GraphicsPanel graphics_panel;

    public static void main(String[] args) throws FileNotFoundException {
        new Program();
    }

    Program() {
        panel = new JPanel(new FlowLayout());
        comboBox = new JComboBox();
        button = new JButton("Start");
        graphics_panel = new GraphicsPanel();

        panel.add(comboBox);
        panel.add(button);

        //setContentPane(panel);

        // Add both panels to this JFrame's content-pane
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(panel, BorderLayout.CENTER);
        cp.add(graphics_panel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(comboBox.getSelectedItem().toString());
            }
        });

        loadInputFile();
    }

    private void loadInputFile() {
        int[][] initial_state_from_file;
        try {
            List<String> allLines = Files.readAllLines(Paths.get("input.txt"));
            for (String line : allLines) {
                System.out.println(line);
                initial_state_from_file = lineToInputState(line);
                comboBox.addItem(line);

//                // goal_state may be changed later using "puzzle.setGoalState" method
//                EightPuzzle puzzle = new EightPuzzle();
//
//                List<EightPuzzle.EightPuzzle.Heuristic> heuristics = new ArrayList<>(){{
//                    add(MANHATTAN_DISTANCE);
//                    add(DIRECT_REVERSE_PENALTY);
//                }};
//
//                puzzle.setHeuristics(heuristics);
//                puzzle.solve(initial_state_from_file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static private int[][] lineToInputState(String line) {
        int [][] arr;
        int rows = line.charAt(1) - '0';
        int cols = line.charAt(3) - '0';
        arr = new int[rows][cols];
        String[] nums = line.substring(6, line.length() - 1).split(" ");

        int num_i = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                arr[i][j] = Integer.parseInt(nums[num_i]);
                num_i += 1;
            }
        }
        return arr;
    }

    class GraphicsPanel extends JPanel {
        GraphicsPanel() {
            setPreferredSize(new Dimension(800, 600));

            Timer update_timer = new Timer(20, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                /* This is where action upon every frame is performed
                  (State is an abstract class, "current()" is a static method) */
                    //State.current().advance();

                    // repaint calls "paintComponent" where "draw" of current state is called
                    repaint();
                }
            });
            update_timer.start();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.black);
            g.setColor(Color.white);
            int x1 = 100, x2 = 200;
            int y1 = 100, y2= 100;
            g.drawLine(x1, y1, x2, y2); // Draw the line
        }
    }
}
