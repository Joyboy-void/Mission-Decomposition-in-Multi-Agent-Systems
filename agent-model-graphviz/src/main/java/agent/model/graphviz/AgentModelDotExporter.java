package agent.model.graphviz;

import agent.model.AgentModel;
import agent.model.AgentState;
import agent.model.AgentTransition;
import graphviz.core.DotExporter;
import ltlf.ast.AtomicProposition;

import java.util.Objects;
import java.util.stream.Collectors;

public final class AgentModelDotExporter
        implements DotExporter<AgentModel> {

    @Override
    public String export(AgentModel model){
        Objects.requireNonNull(model);

        StringBuilder dot = new StringBuilder();
        dot.append("digraph AgentModel {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  graph [fontname=\"Helvetica\", pad=0.2, ")
                .append("nodesep=0.5, ranksep=0.7];\n");
        dot.append("  node [shape=circle, fontname=\"DejaVu Sans\"];\n");
        dot.append("  edge [fontname=\"DejaVu Sans\"];\n\n");

        dot.append("  __initial [shape=point, width=0, label=\"\"];\n");

        for (AgentState state : model.getStates()) {
            String propositions = model.getLabel(state).stream()
                    .map(AtomicProposition::getName)
                    .sorted()
                    .collect(Collectors.joining(", "));

            dot.append("  ")
                    .append(id(state))
                    .append(" [label=\"")
                    .append(escape(state.id()))
                    .append("\\n{")
                    .append(escape(propositions))
                    .append("}\"];\n");
        }

        for (AgentState initial : model.getInitialStates()) {
            dot.append("  __initial -> ")
                    .append(id(initial))
                    .append(";\n");
        }

        for (AgentTransition transition : model.getTransitions()) {
            dot.append("  ")
                    .append(id(transition.source()))
                    .append(" -> ")
                    .append(id(transition.destination()))
                    .append(" [label=\"")
                    .append(transition.cost())
                    .append("\"];\n");
        }

        dot.append("}\n");
        return dot.toString();
    }

    private String id(AgentState state) {
        return "s_" + sanitize(state.id());
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

}

