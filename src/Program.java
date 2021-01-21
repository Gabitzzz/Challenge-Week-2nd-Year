import EightPuzzle.EightPuzzle;

import javax.swing.*;
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
        initWindow();

        EightPuzzle puzzle = new EightPuzzle();
        List<Heuristic> heuristics = new ArrayList<>(){{
            add(MANHATTAN_DISTANCE);
            add(DIRECT_REVERSE_PENALTY);
        }};
        puzzle.setHeuristics(heuristics);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread thread = new Thread() {
                    public void run(){
                        graphics_panel.setPanelState(GraphicsPanel.PanelState.SOLVING);
                        puzzle.solve(
                                lineToInputState(comboBox.getSelectedItem().toString())
                        );
                        graphics_panel.setSolutionStates(puzzle.getSolutionStates());
                        graphics_panel.setPanelState(GraphicsPanel.PanelState.SOLVED);
                    }
                };

                thread.start();
            }
        });
    }

    private void initWindow() {
        panel = new JPanel(new FlowLayout());
        comboBox = new JComboBox();
        button = new JButton("Solve");
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
}
