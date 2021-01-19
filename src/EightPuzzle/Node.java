package EightPuzzle;

import java.util.List;

import static EightPuzzle.EightPuzzle.*;
import static EightPuzzle.EightPuzzle.Heuristic.*;

public class Node implements Comparable {
    Node parent;
    int depth;
    State state;
    int heuristic;

    Node(Node parent_, int depth_, State state_) {
        parent = parent_;
        depth = depth_;
        state = new State(state_);
    }

    public void setHeuristics(State goal_state, List<Heuristic> types) {
        // it sets the heuristic value of the node which later is
        // used to determine its order within PriorityQueue
        // that stores nodes to be expanded, that is accomplished
        // thanks to overridden "compareTo" method

        heuristic = 0;

        int[][] goal_values = goal_state.getValues();
        int[][] values = state.getValues();

        for (int row = 0; row < state.getRowSize(); row++) {
            for (int col = 0; col < state.getColSize(); col++) {

                if (types.contains(MANHATTAN_DISTANCE)) {
                    // Manhattan distance is how many moves a value is from reaching
                    // its goal position (assuming that nothing is blocking it).
                    // In this scenario, added heuristic is a total sum of manhattan
                    // distances of each value to their corresponding goal position.
                    // For example
                    //       [5,2,3] - 5 is 2 moves away from it's goal position
                    //       [4,1,6] - 1 is 2 moves away from it's goal position
                    //       [7,8,0]   So the total heuristic value of this state would be 4
                    //                 (sum of all distances)

                    // get coordinates of a value within goal_state
                    int[]goal_coords = goal_state.valueCoordinates(values[row][col]);
                    // calculate the absolute difference between positions and add it to heuristic
                    // (absolute to avoid negative values)
                    heuristic += (Math.abs(goal_coords[0] - row) + Math.abs(goal_coords[1] - col));
                }

                if (types.contains(MISMATCH_COUNT)) {
                    // Added heuristic = how many values don't match their goal values.
                    // Using it was suggested on slide 18 of the following lecture slides
                    // found online: http://www.sci.brooklyn.cuny.edu/~chipp/cis32/lectures/Lecture6.pdf
                    heuristic += values[row][col] == goal_values[row][col] ? 0 : 1;

                }

                if (types.contains(DIRECT_REVERSE_PENALTY)) {
                    // Direct reverse penalty idea is from:
                    // http://web.mit.edu/6.034/wwwbob/EightPuzzle.pdf
                    //
                    // It is justified in the paper from link above in the following way:
                    //       "Additional improvements are made in informedness by adding a penalty
                    //       for directly reversed tiles. This is due to the fact that reversed
                    //       tiles are much more difficult to deal with because one must “go around” the other."
                    //
                    // It's worth to notice that "directness" means not only being positioned before the previous
                    // goal number, but also means that both numbers must be occupying on their corresponding
                    // goal positions. For example
                    //       [2,1,3] - penalty is given (1 is in place of 2, 2 is in place of 1)
                    //       [4,5,6]
                    //       [7,8,0]
                    //
                    //       [4,2,3] - penalty is given (1 is in place of 4, 4 is in place of 1)
                    //       [1,5,6]
                    //       [7,8,0]
                    //
                    //       [1,2,3]
                    //       [4,0,8]
                    //       [7,6,5] - penalty is NOT given despite 6 being before 5 because they're not
                    //                 on their corresponding goal positions. That is reasonable because
                    //                 this scenario doesn't require 6 and 5 to "go around" themselves,
                    //                 and the puzzle can be solved in few steps (0,8,6,5 have to be
                    //                 rotated 180 degrees).

                    if (state.isDirectlyReversed(row, col, goal_state))
                        heuristic += 1;
                }
            }
        }
    }
    public int getHeuristic() {
        return heuristic;
    }


    @Override
    public int compareTo(Object o) {
        Node other = (Node)o;
        return Integer.compare(depth + heuristic, other.depth + other.getHeuristic());
    }



    /* // it was useful for development, but not needed for the program
    @Override
    public String toString() {
        return "EightPuzzle.Node{" +
                //"parent=" + parent +
                " depth=" + depth +
                ", state=" + state +
                ", last_move=" + State.moveDirFlagToString(state.getLastMove()) +
                " }";
    }*/
}