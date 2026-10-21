package product.automaton.graphviz;

import automata.WeightedTransition;
import graphviz.core.DotExporter;
import ltlf.alphabet.Valuation;
import ltlf.ast.AtomicProposition;
import product.automaton.ProductAutomaton;
import product.automaton.ProductState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class ProductAutomatonDotExporter
        implements DotExporter<ProductAutomaton>{

    private static final double COLUMN_SPACING = 150.0;
    private static final double ROW_SPACING = 150.0;

    @Override
    public String export(ProductAutomaton automaton){
        Objects.requireNonNull(automaton, "automaton");

        List<ProductState> states = new ArrayList<>(automaton.getStates());

        // preserve first-seen agent-state order as the grid's column order.
        Map<String, Integer> agentColumns = new LinkedHashMap<>();
        for(ProductState state : states){
            agentColumns.computeIfAbsent(
                    state.agentState().id(),
                    ignored -> agentColumns.size()
            );
        }

        // residual-state IDs determine row order: q0, q1, q2, ...
        Map<Integer, List<ProductState>> rows = new TreeMap<>();
        for(ProductState state : states){
            rows.computeIfAbsent(
                    state.residualState().getId(),
                    ignored -> new ArrayList<>()
            ).add(state);
        }

        Map<Integer, Integer> residualRows = new LinkedHashMap<>();
        int rowIndex = 0;
        for(Integer residualId : rows.keySet()){
            residualRows.put(residualId, rowIndex++);
        }

        StringBuilder dot = new StringBuilder();
        dot.append("digraph ProductAutomaton {\n");

        dot.append("  graph [layout=neato, splines=polyline, overlap=false, ")
                .append("outputorder=edgesfirst, pad=0.25, margin=0.1, ")
                .append("fontname=\"Helvetica\"];\n");

        dot.append("  node [shape=circle, fixedsize=true, width=1.05, ")
                .append("height=1.05, fontname=\"DejaVu Sans\"];\n");

        dot.append("  edge [fontname=\"DejaVu Sans\"];\n\n");

        // place the initial marker to the left of the first row.
        dot.append("  __initial [shape=point, width=0.08, label=\"\", ")
                .append("pos=\"-150,0!\"];\n");

        // every product node receives an explicit fixed position.
        for(ProductState state : states){
            int column = agentColumns.get(state.agentState().id());
            int row = residualRows.get(state.residualState().getId());

            double x = column * COLUMN_SPACING;
            double y = -row * ROW_SPACING;

            String stateLabel = "q" + state.residualState().getId()
                    + "," + state.agentState().id();

            String propositions = automaton.getLabel(state).stream()
                    .map(AtomicProposition::getName)
                    .sorted()
                    .collect(Collectors.joining(", "));

            String shape = automaton.isFinalState(state)
                    ? "doublecircle" : "circle";

            dot.append("  ").append(id(state))
                    .append(" [shape=").append(shape)
                    .append(", label=\"").append(escape(stateLabel))
                    .append("\\n{").append(escape(propositions)).append("}\"")
                    .append(", pos=\"").append(x).append(",").append(y).append("!\"];\n");
        }

        // initial edges are real automaton edges; the marker itself is fixed.
        for (ProductState initial : automaton.getInitialStates()) {
            dot.append("  __initial -> ").append(id(initial)).append(";\n");
        }

        // emit each stored product transition exactly once.
        for (ProductState source : states) {
            for (WeightedTransition<ProductState, Valuation, Integer> edge
                    : automaton.getOutgoingEdges(source)) {

                String label = String.valueOf(edge.label()) + " / " + edge.weight();
                dot.append("  ").append(id(edge.source()))
                        .append(" -> ").append(id(edge.destination()))
                        .append(" [label=\"").append(escape(label)).append("\"];\n");
            }
        }

        dot.append("}\n");
        return dot.toString();
    }

    private String id(ProductState state){
        return "p_q" + state.residualState().getId()
                + "_s" + sanitize(state.agentState().id());
    }

    private String sanitize(String value){
        return value.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private String escape(String value){
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
