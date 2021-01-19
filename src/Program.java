import EightPuzzle.EightPuzzle;

import java.io.FileNotFoundException;
import java.util.*;

import static EightPuzzle.EightPuzzle.Heuristic;
import static EightPuzzle.EightPuzzle.Heuristic.*;
import static java.lang.System.exit;

public class Program {
    private final static boolean SELECTABLE_HEURISTICS = false;

    private final static String menu_main =
            "Options:\n" +
                    "0. Exit\n" +
                    "1. Uniform cost search\n" +
                    "2. A* search\n";

    private final static int menu_main_limit = 2;

    /*  I abandoned this part after asking the tutor about multiple optional selectable heuristics.
        I decided to use 7th option as default (manhattan dist mismatch count and direct reversal penalty)
        Because it produces the best result for this particular example.
        However, I kept the results of all other combinations and included them in the report (in a table).
*/
    private final static String menu_astar =
            "Heuristics:\n" +
                    "1. Manhattan distance\n" +
                    "2. Mismatch count\n" +
                    "3. Direct reversal penalty\n" +
                    "4. 1 and 2\n" +
                    "5. 1 and 3\n" +
                    "6. 2 and 3\n" +
                    "7. All 3\n";

    private final static int menu_astar_limit = 7;

    private static int getMenuOption(String menu, int limit) {
        System.out.println("\n" + menu);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Select option > ");
            try {
                int value = scanner.nextInt();
                if (value < 0 || value > limit) {
                    System.out.print("\nOption does not exist. ");
                    continue;
                }
                System.out.println();
                return value;
            } catch (InputMismatchException e) {
                System.out.print("\nInvalid input. ");
                // ignore invalid input
                scanner.next();
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {

        int[][] initial_state = new int[][]{
                {8,7,6},
                {5,4,3},
                {2,1,0}
        };

        int[][] goal_state = new int[][]{
                {1,2,3},
                {4,5,6},
                {7,8,0}
        };

        int[][] initial_state_2 = new int[][]{
                {5,4,3},
                {2,1,0}
        };

        int[][] goal_state_2 = new int[][]{
                {1,2,3},
                {4,5,0}
        };

        // goal_state may be changed later using "puzzle.setGoalState" method
        EightPuzzle puzzle = new EightPuzzle(goal_state_2);

        int option = -1;
        while ((option = getMenuOption(menu_main, menu_main_limit)) != 0) {
            switch(option) {

                // If uniform cost search is selected
                case 1:
                    // don't use any heuristics
                    puzzle.setHeuristics(new ArrayList<>());

                    // solve method is overloaded, so PrintWriter also could be supplied (instead of a file name)
                    puzzle.solve(initial_state_2, "outputUniCost.txt");
                    break;

                // If A* is selected
                case 2:
                    List<Heuristic> heuristics = new ArrayList<>();

                    // SELECTABLE_HEURISTICS allowed me to do some
                    // research about different heuristic methods
                    if (SELECTABLE_HEURISTICS) {
                        // Get type of heuristic to use
                        int option_h = getMenuOption(menu_astar, menu_astar_limit);
                        // if the user selected option that combines multiple methods
                        // then 3 conditionals below will combine all heuristics in one list
                        if (Arrays.asList(new Integer[]{1, 4, 5, 7}).contains(option_h))
                            heuristics.add(MANHATTAN_DISTANCE);
                        if (Arrays.asList(new Integer[]{2, 4, 6, 7}).contains(option_h))
                            heuristics.add(MISMATCH_COUNT);
                        if (Arrays.asList(new Integer[]{3, 5, 6, 7}).contains(option_h))
                            heuristics.add(DIRECT_REVERSE_PENALTY);
                    } else {
                        // use default (optimal)
                        heuristics = new ArrayList<>(){{
                            add(MANHATTAN_DISTANCE);
                            add(DIRECT_REVERSE_PENALTY);
                        }};
                    }
                    puzzle.setHeuristics(heuristics);
                    puzzle.solve(initial_state_2, "outputAstar.txt");
                    break;
            }
        }
    }
}
