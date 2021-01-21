package EightPuzzle;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;

import static java.lang.Integer.max;
import static java.lang.System.exit;

public class State {
    static private int MAX_SIZE = 5;

    private int[][] values;
    private Direction last_move = Direction.NONE;

    /*  The challenge week instruction specified that the final output should
        include values of puzzle fields that moved (as opposed to the direction
        of moves used in AI assignment). That's why this variable is added
        to State class. */
    private int value_that_moved;

    private int row_size;
    private int col_size;

    private enum Direction {
        NONE, RIGHT, LEFT, UP, DOWN
    }

    State(int[] arr, int n_of_rows) {
        row_size = n_of_rows;
        col_size = arr.length/n_of_rows;
        for (int row = 0; row < row_size; row++)
            for (int col = 0; col < col_size; col++)
                values[row][col] = arr[col_size * row + col];
    }

    State(int[][] arr) {
        row_size = arr.length;
        col_size = arr[0].length;
        setValues(arr);
    }

    // copy constructor
    State(State other) {
        row_size = other.row_size;
        col_size = other.col_size;
        setValues(other.values);
        last_move = other.last_move;
    }

    public int getValueThatMoved() {
        return value_that_moved;
    }

    public int getRowSize() {
        return row_size;
    }

    public int getColSize() {
        return col_size;
    }

    public void setRowSize(int size) {
        this.row_size = size;
    }

    public void setColSize(int size) {
        this.col_size = size;
    }

    private void setValues(int arr[][]) {
        if (values == null)
            values = new int[row_size][col_size];

        for (int row = 0; row < row_size; row++)
            for (int col = 0; col < col_size; col++)
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
        value_that_moved = values[empty_space_row - v_offset][empty_space_col - h_offset];
        values[empty_space_row][empty_space_col] = value_that_moved;

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
        if (empty_pos[1] != col_size - 1)
            dirs.add(Direction.LEFT);

        // if empty space is not leftmost, allow moving right
        if (empty_pos[1] != 0)
            dirs.add(Direction.RIGHT);

        // if empty space is not down, allow moving upwards
        if (empty_pos[0] != row_size - 1)
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

        for (int row = 0; row < row_size; row++)
            for (int col = 0; col < col_size; col++)
                if (values[row][col] == value_to_find)
                    return new int[] {row, col};
        return null;
    }

    public List<Integer> neighborValues(int row, int col) {
        // useful for calculating direct reverse penalty
        List<Integer> neighbor_values = new ArrayList<>();
        if (row != 0)
            neighbor_values.add(values[row - 1][col]);
        if (row != row_size - 1)
            neighbor_values.add(values[row + 1][col]);
        if (col != 0)
            neighbor_values.add(values[row][col - 1]);
        if (col != col_size - 1)
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
//        int all_values_size = row_size*col_size;
//        for (int i = 0; i < all_values_size; i++) {
//            String value = Integer.toString(values[i / row_size][i % col_size]);
//            // empty space if 0
//            // add spaces between values (but not after thanks to "i==8" check)
//            s += (value.equals("0") ? " " : value) + (i == all_values_size-1 ? "" : " ");
//        }
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

    public void drawAt(Graphics g, int x, int y, int size) {
        int separating_dist = (int)(size * 0.05);
        int max_dimension = max(col_size,row_size);
        int field_size = (size - separating_dist * (max_dimension-1)) / max_dimension;

        // draw background
        //g.setColor(new Color(51,153,255));
        //g.fillRect(x,y,size,size * row_size/col_size);

        g.setColor(Color.green);

        int arrow_x1 = 0, arrow_x2 = 0, arrow_y1 = 0, arrow_y2 = 0;

        for (int row = 0; row < row_size; row++) {
            for (int col = 0; col < col_size; col++) {

                // draw single field rect background
                int field_y = y + row * (field_size + separating_dist);
                int field_x = x + col * (field_size + separating_dist);
                //g.drawRect(field_x, field_y, field_size, field_size);
                g.setColor(Color.lightGray);
                g.fillRect(field_x, field_y, field_size, field_size);

                g.setColor(Color.black);

                int field_center_y = (int) (field_y + field_size * 0.5);
                int field_center_x = (int) (field_x + field_size * 0.5);


                String val = Integer.toString(values[row][col]);
                FontMetrics fm = g.getFontMetrics();
                int half_label_width = (int)(fm.getStringBounds(val, g).getWidth()*0.5);
                int half_label_height = (int)(fm.getStringBounds(val, g).getHeight()*0.5);

                if (val.equals("0")) {
                    // keep reference to current color
                    Color preserved_color = g.getColor();

                    g.setColor(Color.orange);
                    g.fillRect(field_x+1, field_y+1, field_size-1, field_size-1);
                    if (last_move != Direction.NONE) {
                        int last_move_x = field_center_x, last_move_y = field_center_y;
                        switch (last_move) {
                            case UP:   last_move_y -= field_size * 0.9 + separating_dist - half_label_height; break;
                            case DOWN: last_move_y += field_size * 0.9 + separating_dist - half_label_height; break;
                            case RIGHT: last_move_x += field_size * 0.9 + separating_dist - half_label_width; break;
                            case LEFT: last_move_x -= field_size * 0.9 + separating_dist - half_label_width; break;
                        }
                        arrow_x1 = field_center_x; arrow_x2 = last_move_x;
                        arrow_y1 = field_center_y; arrow_y2 = last_move_y;

                        //drawArrowLine(g, field_center_x, field_center_y, last_move_x, last_move_y, (int)(field_size *0.15), (int)(field_size *0.1));
                        //g.drawLine(field_center_x, field_center_y, last_move_x, last_move_y);
                    }
                    // restore color
                    g.setColor(preserved_color);

                } else {
                    g.drawString(val, field_center_x - half_label_width, field_center_y + half_label_height);
                }
            }
        }

        if (last_move != Direction.NONE) {
            g.setColor(Color.black);
            drawArrowLine(g, arrow_x1, arrow_y1, arrow_x2, arrow_y2, (int) (field_size * 0.20), (int) (field_size * 0.15));
        }

        g.setColor(Color.orange);

        int boundary_width = field_size * col_size + (separating_dist * (col_size-1));
        int boundary_height = field_size * row_size + (separating_dist * (row_size-1));
        // draw boundary
        g.drawRect(x,y, boundary_width, boundary_height);
    }

    /**
     * "drawArrowLine" function was copied from: https://stackoverflow.com/a/27461352/4620679
     * Author: phibao37; last edited by: RubenLaguna
     *
     * Draw an arrow line between two points.
     * @param g the graphics component.
     * @param x1 x-position of first point.
     * @param y1 y-position of first point.
     * @param x2 x-position of second point.
     * @param y2 y-position of second point.
     * @param d  the width of the arrow.
     * @param h  the height of the arrow.
     */
    private void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h) {
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx*dx + dy*dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;

        x = xm*cos - ym*sin + x1;
        ym = xm*sin + ym*cos + y1;
        xm = x;

        x = xn*cos - yn*sin + x1;
        yn = xn*sin + yn*cos + y1;
        xn = x;

        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};

        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint
                (RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

        // ugly fix to hide line being visible after arrow head
        // (due to increased stroke of the line)
        x2 += (x1 - x2) / 10;
        y2 += (y1 - y2) / 10;

        Stroke preserved_stroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2));
        g2.draw(new Line2D.Double(x1, y1, x2, y2));
        g2.setStroke(preserved_stroke);

        //g.drawLine(x1, y1, x2, y2);
        g.fillPolygon(xpoints, ypoints, 3);
    }
}