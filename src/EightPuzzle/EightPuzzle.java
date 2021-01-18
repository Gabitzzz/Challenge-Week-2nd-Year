package EightPuzzle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class EightPuzzle {

    // Detailed comments about each of these heuristics can be found in
    // Node.java file (setHeuristics method)
    public enum Heuristic {
        MANHATTAN_DISTANCE, MISMATCH_COUNT, DIRECT_REVERSE_PENALTY
    }

    private State goal_state;
    private List<Heuristic> heuristics = new ArrayList<>();

    private int nodes_expanded = 0;

    public int last_solution_path_size;

    // This will prevent expanding previously expanded nodes,
    // without it the code could "run around in circles" indefinitely
    // It is equivalent to "visited" list from this video: https://www.youtube.com/watch?v=dRMvK76xQJI
    Set<State> visited_states = new HashSet<>();

    public EightPuzzle(int[][] goal_state_values) {
        setGoalState(goal_state_values);
    }

    public void setHeuristics(List<Heuristic> heuristics_) {
        // The goal of heuristics is to prioritize expansion
        // of nodes that have promising states by penalizing
        // nodes that have unfavourable states so they're expanded
        // later (if goal was not reached).

        // The use of heuristics is what differentiates uniform
        // cost search (not used) from A* search (used).
        heuristics = heuristics_;
    }

    public boolean solve(int[][] initial_state_, String file_name) throws FileNotFoundException {
        PrintWriter output = new PrintWriter(new File(file_name));
        solve(initial_state_, output);
        output.close();
        return true;
    }
    public boolean solve(int[][] initial_state_, PrintWriter output) {
        long start_time = System.currentTimeMillis();

        // nodes is ordered starting with the most likely to result in solution (after expanding them)
        // PriorityQueue automatically sorts Nodes when inserted, the same queue is used for
        // uniform cost search and A* with different heuristics, so how does it know which Node should be first?
        // It knows it thanks to the fact that "Node" class implements "Comparable" interface and overrides
        // "compareTo" method which determines order of nodes in the PriorityQueue. It does that by adding
        // depth of a node to heuristic value (in case of uniform cost search the heuristic value is 0)
        PriorityQueue<Node> nodes = new PriorityQueue<Node>();
        List<String> path = new ArrayList<>();
        visited_states.clear();
        nodes_expanded = 0;

        // create first node with initial state
        Node node = new Node(null, 0, new State(initial_state_));

        // avoid visiting initial node
        visited_states.add(node.state);

        while (!node.state.equals(goal_state)) {
            // get list of possible nodes/states and
            // - add them to the "front-line" nodes
            // - add their states to previously visited states
            expand(node).forEach(n -> { nodes.add(n); visited_states.add(n.state); });

            // get the node that is the most promising (from PriorityQueue)
            //      - in case of uniform case search it's the lowest depth node
            //      - in case of A* it's the lowest value of combined depth + chosen heuristics
            node = nodes.poll();
        }

        // backtrack from the node that was found having goal state
        // all the way back to the first node (having initial state)
        while (node.parent != null) {
            path.add(0, node.state.toString());
            node = node.parent;
        }

        last_solution_path_size = path.size();

        System.out.printf("Solution took %dms.\n", System.currentTimeMillis() - start_time);

        for (String state_str : path)
            output.println(state_str);

        output.printf("%d Moves\n", path.size());
        output.printf("%d Nodes expanded\n", nodes_expanded);
        output.printf("%d Nodes unexpanded\n", nodes.size());

        System.out.printf("%d Moves\n", path.size());
        System.out.printf("%d Nodes expanded\n", nodes_expanded);
        System.out.printf("%d Nodes unexpanded\n", nodes.size());

        return true;
    }

    List<Node> expand(Node node) {
        // generates and returns all possible new nodes based
        // on all possible movements, it uses "visited_states"
        // set to prevent creating Nodes that were previously seen
        List<Node> new_nodes = new ArrayList<>();

        // "Set<State>visited_states" is supplied to prevent "going around in circles"
        // Nodes with states already visited are not created
        for (State new_state : node.state.possibleNewStates(visited_states)) {
            new_nodes.add(new Node(node, node.depth + 1, new_state));
            visited_states.add(new_state);
        }

        for (Node n : new_nodes)
            n.setHeuristics(goal_state, heuristics);

        nodes_expanded++;
        return new_nodes;
    }

    public void setGoalState(int[][] goal_state_values) {
        // It allows the user/developer to change
        // goal state after creation of EightPuzzle class.
        goal_state = new State(goal_state_values);
    }
}
