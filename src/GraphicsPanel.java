import EightPuzzle.State;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static java.lang.Integer.max;
import static java.lang.Math.floor;

public class GraphicsPanel extends JPanel {
    /*  This panel can be in 3 states:
        - init
        - solving
        - solved  */
    private PanelState panel_state;
    public enum PanelState { INIT, SOLVING, SOLVED };

    java.util.List<State> solution_states;

    GraphicsPanel() {
        solution_states = new ArrayList<>();
        panel_state = PanelState.INIT;
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
        setBackground(Color.gray);
        g.setColor(Color.white);

        FontMetrics fm = g.getFontMetrics();

        synchronized (panel_state) {
            switch(panel_state) {
                case INIT: {
                    // probably should paint currently selected state here too
                    String str = "Press 'Solve'";
                    int half_width = (int) (fm.getStringBounds(str, g).getWidth() * 0.5);
                    g.drawString(str, 400 - half_width, 290);
                }
                break;
                case SOLVING: {
                    // probably should paint currently selected state here too
                    String str = "Solving...";
                    int half_width = (int) (fm.getStringBounds(str, g).getWidth() * 0.5);
                    g.drawString(str, 400 - half_width, 290);
                }
                break;
                case SOLVED:
                    State temp_state = solution_states.get(0);

                    int how_many_states_could_fit = -1;
                    double size_divisor = 1.0;
                    int max_row_col = 0, max_size = 0, width = 0, height = 0, max_w_h = 0, margin = 0, x = 0, y = 0;
                    while (how_many_states_could_fit < solution_states.size()) {
                        max_row_col = max(temp_state.getRowSize(), temp_state.getColSize());
                        max_size = (int)((20 + max_row_col * 20) / size_divisor);

                        width = max_size * temp_state.getColSize() / max_row_col;
                        height = max_size * temp_state.getRowSize() / max_row_col;
                        max_w_h = max(width, height);
                        margin = (int)(max_w_h * 0.1);
                        x = margin;
                        y = margin;

                        how_many_states_could_fit = (int)(floor(getWidth() / (width + margin)) * floor(getHeight() / (height + margin)));
                        size_divisor += 0.1;// (double)solution_states.size() / (double)how_many_states_could_fit;
                    }

                    synchronized (solution_states) {
                        for (int i = 0; i < solution_states.size(); i++) {
                            State state = solution_states.get(i);
                            state.drawAt(g, x, y, max_w_h);
                            x += width + margin;
                            if (x > getWidth() - width) {
                                x = margin;
                                y += height + margin;
                            }
                        }
                    }
                    break;

            }
        }
    }

    public void setPanelState(PanelState panel_state) {
        synchronized (this.panel_state) {
            this.panel_state = panel_state;
        }
    }

    public void setSolutionStates(java.util.List<State> states) {
        synchronized (solution_states) {
            solution_states = states;
        }
    }
}