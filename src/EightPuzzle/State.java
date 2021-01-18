package EightPuzzle;

import java.util.*;

import static java.lang.System.exit;

public class State {

    private int[][] values = new int[3][3];
    private Direction last_move = Direction.NONE;

    private enum Direction {
        NONE, RIGHT, LEFT, UP, DOWN
    }

    State(int[] arr) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                values[row][col] = arr[3 * row + col];
    }

    State(int[][] arr) {
        setValues(arr);
    }

    // copy constructor
    State(State other) {
        setValues(other.values);
        last_move = other.last_move;
    }

    private void setValues(int arr[][]) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                values[row][col] = arr[row][col];
    }

    public int[][] getValues() {
        return values;
    }

    public Direction getLastMove() { return last_move; }

    public void move(Direction dir) {
        last_move = dir;
        // moves and returns resulting state
        int[] coords = valueCoordinates(0);
        int empty_space_row = coords[0];
        int empty_space_col = coords[1];

        int h_offset = 0;
        int v_offset = 0;
        switch(dir) {
            case RIGHT: h_offset =  1; break;
            case LEFT:  h_offset = -1; break;
            case UP:    v_offset = -1; break;
            case DOWN:  v_offset =  1; break;
            default:
                System.out.printf("Invalid direction %d supplied to doMove() method.\n", dir);
                exit(-1);
        }
        // put moved value into empty space
        values[empty_space_row][empty_space_col] = values[empty_space_row - v_offset][empty_space_col - h_offset];

        // put empty space at previous position of value
        values[empty_space_row - v_offset][empty_space_col - h_offset] = 0;
    }

    private Direction oppositeMove(Direction dir) {
        switch(dir) {
            case DOWN:  return Direction.UP;
            case UP:    return Direction.DOWN;
            case LEFT:  return Direction.RIGHT;
            case RIGHT: return Direction.LEFT;
        }
        return null;
    }

    private Set<Direction> possibleMoveDirections() {
        Set<Direction> dirs = new HashSet<>();
        // position of empty space
        int[] empty_pos = valueCoordinates(0);

        // if empty space is not rightmost, allow moving left
        if (empty_pos[1] != 2)
            dirs.add(Direction.LEFT);

        // if empty space is not leftmost, allow moving right
        if (empty_pos[1] != 0)
            dirs.add(Direction.RIGHT);

        // if empty space is not down, allow moving upwards
        if (empty_pos[0] != 2)
            dirs.add(Direction.UP);

        // if empty space is not up, allow moving downwards
        if (empty_pos[0] != 0)
            dirs.add(Direction.DOWN);

        // (the initial node state has last_move equal to Direction.NONE, so )
        if (last_move != Direction.NONE) {
            // don't allow moving back
            // This is prevented by keeping "visited_states" set
            // but this dirs.remove is just for the sake of efficiency
            // (so additional useless State won't be created and destroyed)
            dirs.remove(oppositeMove(last_move));
        }
        return dirs;
    }

    public Set<State> possibleNewStates(Set<State> visited_states) {
        // excluded states will be supplied to avoid repeatedly
        // expanding previously expanded nodes
        Set<State> possible_states = new HashSet<State>();

        for (Direction dir : possibleMoveDirections()) {
            State new_state = new State(values);
            new_state.move(dir);
            // Set<State>.contains calls "equals" method of this class
            if (!visited_states.contains(new_state))
                possible_states.add(new_state);
        }
        return possible_states;
    }

    public int[] valueCoordinates(int value_to_find) {
        // returns position of a value in supplied state
        // can be useful for :
        //      - calculating manhattan distance (getting value position within the goal state)
        //      - determining possible moves (getting empty space position)

        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                if (values[row][col] == value_to_find)
                    return new int[] {row, col};
        return null;
    }

    public List<Integer> neighborValues(int row, int col) {
        // useful for calculating direct reverse penalty
        List<Integer> neighbor_values = new ArrayList<>();
        if (row != 0)
            neighbor_values.add(values[row - 1][col]);
        if (row != 2)
            neighbor_values.add(values[row + 1][col]);
        if (col != 0)
            neighbor_values.add(values[row][col - 1]);
        if (col != 2)
            neighbor_values.add(values[row][col + 1]);
        return neighbor_values;
    }

    public boolean isDirectlyReversed(int row, int col, State goal_state) {
        // Based on the heuristic from:
        // http://web.mit.edu/6.034/wwwbob/EightPuzzle.pdf
        // See Node.java file (setHeuristics method) comments for more information

        int goal = goal_state.values[row][col];
        int actual = values[row][col];

        List<Integer> neighbor_goal = goal_state.neighborValues(row, col);
        List<Integer> neighbor_actual = neighborValues(row, col);

        for (int i = 0; i < neighbor_actual.size(); i++)
            if (actual == neighbor_goal.get(i) && neighbor_actual.get(i) == goal && goal != 0 && actual != 0)
                return true;

        return false;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < 9; i++) {
            String value = Integer.toString(values[i / 3][i % 3]);
            // empty space if 0
            // add spaces between values (but not after thanks to "i==8" check)
            s += (value.equals("0") ? " " : value) + (i == 8 ? "" : " ");
        }
        return s;
    }

    @Override
    public boolean equals(Object o) {
        // This method will let the "Set<State> expanded_nodes_states" know that
        // the state was already seen (so there's no point in expanding Node
        // that has it).

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return Arrays.deepEquals(values, state.values);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(values);
    }

    /* // it was useful for development, but not needed for the program
    public static String moveDirFlagToString(int dir_flag) {
        switch(dir_flag) {
            case MOVE_RIGHT: return "right";
            case MOVE_LEFT:  return "left";
            case MOVE_UP:    return "up";
            case MOVE_DOWN:  return "down";
            case 0: return "none";
            default:
                System.out.printf("Invalid direction %d supplied to moveDirFlagToString() method.\n", dir_flag);
                exit(-1);
        }
        return "compiler";
    }*/
}