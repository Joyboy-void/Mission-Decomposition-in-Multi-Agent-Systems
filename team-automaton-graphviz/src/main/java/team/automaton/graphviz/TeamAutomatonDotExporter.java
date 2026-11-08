package team.automaton.graphviz;

import automata.WeightedTransition;
import graphviz.core.DotExporter;
import ltlf.ast.AtomicProposition;
import product.automaton.ProductAutomaton;
import product.automaton.ProductState;
import team.automaton.TeamAction;
import team.automaton.TeamAutomaton;
import team.automaton.TeamState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * exports a Team Automaton as a fixed-position Graphviz graph.
 *
 * each TeamAutomaton copy is represented as a repetition
 * of the underlying Product Automaton grid.
 *
 * Layout:
 * - Rows represent Residual Automaton states.
 * - Columns represent Agent Model states.
 * - Each Team Automaton copy occupies its own grid.
 * - Copies are arranged horizontally.
 *
 * Rendering must use Graphviz neato with -n2.
 */
public final class TeamAutomatonDotExporter
        implements DotExporter<TeamAutomaton> {

    private static final double COLUMN_SPACING = 150.0;
    private static final double ROW_SPACING = 150.0;
    private static final double COPY_GAP = 180.0;
    private static final double INITIAL_OFFSET = 150.0;
    private static final double NODE_SIZE = 1.05;

    @Override
    public String export(TeamAutomaton automaton) {
        return export(automaton, Set.of());
    }

    /**
     * exports the Team Automaton while highlighting exactly the supplied
     * transitions.
     *
     * highlighted transitions are red and have increased pen width.
     * Selected epsilon transitions retain their dashed style.
     */
    public String export(
            TeamAutomaton automaton,
            Set<WeightedTransition<
                    TeamState,
                    TeamAction,
                    Integer>> highlightedTransitions
    ) {

        Objects.requireNonNull(automaton, "automaton");

        Objects.requireNonNull(
                highlightedTransitions,
                "highlightedTransitions"
        );

        ProductAutomaton product =
                automaton.getProductAutomaton();

        /*
         * get the underlying Product Automaton states.
         */
        List<ProductState> productStates =
                new ArrayList<>(product.getStates());

        /*
         * preserve first-seen Agent Model state order.
         */
        Map<String, Integer> agentColumns =
                new LinkedHashMap<>();

        for(ProductState state : productStates){
            agentColumns.computeIfAbsent(
                    state.agentState().id(),
                    ignored -> agentColumns.size()
            );
        }

        /*
         * residual state IDs determine row order:
         * q0, q1, q2, ...
         */
        Map<Integer, List<ProductState>> rows =
                    new TreeMap<>();

        for(ProductState state : productStates){
            rows.computeIfAbsent(
                    state.residualState().getId(),
                    ignored -> new ArrayList<>()
            ).add(state);
        }

        // map residual-state IDs to row indices.
        Map<Integer, Integer> residualRows =
                new LinkedHashMap<>();

        int rowIndex = 0;

        for (Integer residualId : rows.keySet()) {

            residualRows.put(
                    residualId,
                    rowIndex++
            );
        }

        // create the DOT document.
        StringBuilder dot = new StringBuilder();

        dot.append("digraph TeamAutomaton {\n");

        dot.append("  graph [")
                .append("layout=neato, ")
                .append("splines=polyline, ")
                .append("overlap=false, ")
                .append("outputorder=edgesfirst, ")
                .append("pad=0.35, ")
                .append("margin=0.1, ")
                .append("fontname=\"Helvetica\"")
                .append("];\n");

        dot.append("  node [")
                .append("shape=circle, ")
                .append("fixedsize=true, ")
                .append("width=").append(NODE_SIZE).append(", ")
                .append("height=").append(NODE_SIZE).append(", ")
                .append("fontname=\"DejaVu Sans\", ")
                .append("fontsize=10")
                .append("];\n");

        dot.append("  edge [")
                .append("fontname=\"DejaVu Sans\", ")
                .append("fontsize=9")
                .append("];\n\n");

        // position the initial marker to the left of the first product Automaton grid.

        dot.append("  __initial [")
                .append("shape=point, ")
                .append("width=0.08, ")
                .append("label=\"\", ")
                .append("pos=\"")
                .append(-INITIAL_OFFSET)
                .append(",0!\"];\n\n");

        /*
         * declare every Team Automaton state.
         *
         * Position formula:
         *
         * x = copyOffset + agentColumn * COLUMN_SPACING
         * y = -residualRow * ROW_SPACING
         */
        for(int copyId = 1;
             copyId <= automaton.getNumberOfAgents();
             copyId++){

            double copyOffset =
                    (copyId - 1)
                            * (
                            agentColumns.size()
                                    * COLUMN_SPACING
                                    + COPY_GAP
                    );

            for(ProductState productState : productStates){

                int column =
                        agentColumns.get(
                                productState.agentState().id()
                        );

                int row =
                        residualRows.get(
                                productState.residualState().getId()
                        );

                double x =
                        copyOffset + column * COLUMN_SPACING;

                double y =
                        -row * ROW_SPACING;

                TeamState teamState =
                        new TeamState(copyId, productState);

                String stateLabel =
                        "C" + copyId
                                + " / q"
                                + productState.residualState().getId()
                                + ","
                                + productState.agentState().id();

                String propositions =
                        product.getLabel(productState)
                                .stream()
                                .map(AtomicProposition::getName)
                                .sorted()
                                .collect(Collectors.joining(", "));

                String shape =
                        automaton.isFinalState(teamState)
                                ? "doublecircle"
                                : "circle";

                dot.append("  ")
                        .append(id(teamState))
                        .append(" [")
                        .append("shape=")
                        .append(shape)
                        .append(", label=\"")
                        .append(escape(stateLabel))
                        .append("\\n{")
                        .append(escape(propositions))
                        .append("}\"")
                        .append(", pos=\"")
                        .append(x)
                        .append(",")
                        .append(y)
                        .append("!\"];\n");
            }

            dot.append("\n");
        }

        // initial states belong exclusively to copy 1.
        for(TeamState initial : automaton.getInitialStates()){
            dot.append("  __initial -> ")
                    .append(id(initial))
                    .append(";\n");
        }

        dot.append("\n");

        /*
         * export actual Team Automaton transitions.
         *
         * Ordinary transitions:
         * - remain inside the same copy.
         * - use valuation / weight labels.
         *
         * Epsilon transitions:
         * - connect consecutive copies.
         * - are displayed as dashed edges.
         */
        for(int copyId = 1;
             copyId <= automaton.getNumberOfAgents();
             copyId++){

            for(ProductState productState : productStates){

                TeamState source =
                        new TeamState(copyId, productState);

                for(WeightedTransition<
                        TeamState,
                        TeamAction,
                        Integer> edge
                        : automaton.getOutgoingEdges(source)){

                    String label;
                    String style = "solid";
                    String color = "black";

                    boolean highlighted =
                            highlightedTransitions.contains(edge);

                    if (edge.label()
                            instanceof TeamAction.ValuationAction action){

                        label = action.valuation()
                                + " / " + edge.weight();

                    }else if (edge.label()
                            == TeamAction.SwitchAction.EPSILON){

                        label = "ε / " + edge.weight();

                        style = "dashed";
                        color = "gray45";

                    }else{

                        label =
                                edge.label() + " / " + edge.weight();
                    }

                    /*
                     * highlighting changes colour and width only.
                     * selected epsilon edges remain dashed.
                     */
                    if (highlighted) {
                        color = "#E53935";
                    }

                    dot.append("  ")
                            .append(id(edge.source()))
                            .append(" -> ")
                            .append(id(edge.destination()))
                            .append(" [")
                            .append("label=\"")
                            .append(escape(label))
                            .append("\", ")
                            .append("style=")
                            .append(style)
                            .append(", ")
                            .append("color=\"")
                            .append(color)
                            .append("\"");

                    if (highlighted) {

                        dot.append(", fontcolor=\"#E53935\"")
                                .append(", penwidth=3.5");
                    }

                    dot.append("];\n");
                }
            }
        }

        dot.append("}\n");

        return dot.toString();
    }

    // Generates a unique DOT identifier for a Team Automaton state.

    private String id(TeamState state) {

        ProductState productState =
                state.productState();

        return "c" + state.copyId()
                + "_q" + productState.residualState().getId()
                + "_s" + sanitize(productState.agentState().id());
    }

    // makes Agent Model state IDs safe for DOT identifiers.

    private String sanitize(String value) {

        return value.replaceAll(
                "[^a-zA-Z0-9_]",
                "_"
        );
    }

    // escapes special characters in Graphviz labels.
    private String escape(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}