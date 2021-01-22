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

    // top panel with selection of puzzle and "Solve" button
    JPanel top_panel;
    JComboBox comboBox_puzzle_type;
    JButton button_solve;

    // checkboxes for selectable heuristics
    JCheckBox checkbox_manhattan_distance;
    JCheckBox checkbox_direct_reversal_penalty;
    JCheckBox checkbox_mismatch_count;
    JPanel panel_heuristics;

    // performance statistics elements
    JLabel label_moves;
    JLabel label_completion_time;
    JLabel label_expanded_nodes;
    JPanel panel_performance;

    // graphics_panel is where the squares and numbers representing states are displayed after solution
    GraphicsPanel graphics_panel;

    public static void main(String[] args) throws FileNotFoundException {
        new Program();
    }

    Program() {
        initWindow();

        // create single puzzle object that is reused
        EightPuzzle puzzle = new EightPuzzle();

        button_solve.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread thread = new Thread() {
                    public void run(){
                        graphics_panel.setPanelState(GraphicsPanel.PanelState.SOLVING);

                        // set heuristics supplied to solver based on the state of the checkboxes
                        List<Heuristic> heuristics = new ArrayList<>(){{
                            if (checkbox_manhattan_distance.isSelected()) { add(MANHATTAN_DISTANCE); }
                            if (checkbox_direct_reversal_penalty.isSelected()) { add(DIRECT_REVERSE_PENALTY); }
                            if (checkbox_mismatch_count.isSelected()) { add(MISMATCH_COUNT); }
                        }};
                        puzzle.setHeuristics(heuristics);

                        // clear performance statistics of the previous solution because the new one is about to be done
                        label_moves.setText("");
                        label_completion_time.setText("");
                        label_expanded_nodes.setText("");

                        // this is where the puzzle gets solved
                        // (it may take some time but it doesn't block GUI because it's in a separate thread)
                        puzzle.solve(
                                lineToInputState(comboBox_puzzle_type.getSelectedItem().toString())
                        );

                        // at this point the puzzle is solved, so performance statistics are displayed
                        label_moves.setText("   Moves: " + (puzzle.getSolutionStates().size()-1));
                        label_completion_time.setText("   Time taken: " + puzzle.getCompletionTime() + "ms");
                        label_expanded_nodes.setText("   Nodes expanded: " + puzzle.getExpandedNodesCount());

                        // states (number values) of moves that lead to solution must be passed from solver to GUI
                        graphics_panel.setSolutionStates(puzzle.getSolutionStates());

                        graphics_panel.setPanelState(GraphicsPanel.PanelState.SOLVED);
                    }
                };
                // setting as daemon will terminate thread when the program exits
                thread.setDaemon(true);

                thread.start();
            }
        });
    }

    private void initWindow() {
        // create panels that later get added to BoxLayout of the JFrame
        // Flow layout of these panels makes their items positioned next to each other.
        // As shown here: https://www.javatpoint.com/FlowLayout
        top_panel = new JPanel(new FlowLayout());
        panel_heuristics = new JPanel(new FlowLayout());
        panel_performance = new JPanel(new FlowLayout());
        graphics_panel = new GraphicsPanel();

        // top_panel elements
        comboBox_puzzle_type = new JComboBox();
        button_solve = new JButton("Solve");
        top_panel.add(comboBox_puzzle_type);
        top_panel.add(button_solve);

        // selectable heuristic elements
        checkbox_manhattan_distance = new JCheckBox("Manhattan distance", true);
        checkbox_direct_reversal_penalty = new JCheckBox("Direct reversal penalty", true);
        checkbox_mismatch_count = new JCheckBox("Mismatch count", false);
        panel_heuristics.add(new JLabel("Heuristics for A* algorithm:"));
        panel_heuristics.add(checkbox_manhattan_distance);
        panel_heuristics.add(checkbox_direct_reversal_penalty);
        panel_heuristics.add(checkbox_mismatch_count);

        // performance statistics elements
        label_moves = new JLabel();
        label_completion_time = new JLabel();
        label_expanded_nodes = new JLabel();
        panel_performance.add(label_moves);
        panel_performance.add(label_completion_time);
        panel_performance.add(label_expanded_nodes);

        // Create BoxLayout. It is equivalent to "vertical layout" thanks to
        // "Y_AXIS" parameter which positions each new panel under previous one.
        // As seen here: https://www.javatpoint.com/BoxLayout
        Container cp = getContentPane();
        cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));
        add(top_panel);
        add(panel_heuristics);
        add(panel_performance);
        add(graphics_panel);

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
                comboBox_puzzle_type.addItem(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // it converts the line from input file into 2D array containing initial state of the puzzle
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
