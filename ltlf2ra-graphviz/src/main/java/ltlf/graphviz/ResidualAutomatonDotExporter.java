package ltlf.graphviz;

import ltlf.alphabet.Valuation;
import ltlf.automaton.ResidualAutomaton;
import ltlf.automaton.ResidualState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 *  Converts a residual Automaton into Graphviz DOT source
 *
 *  <p>this class only produces DOT text. It deliberately doesn't
 *  invoke the graphviz executable , keeping the graph construction independent from the
 *  local graphviz installation </p>
 */

public final class ResidualAutomatonDotExporter {

    public String export(ResidualAutomaton automaton){

        Objects.requireNonNull(automaton, "automaton");

        StringBuilder dot = new StringBuilder();

        dot.append("digraph ResidualAutomaton{\n");
        dot.append("    rankdir=LR;\n");
        dot.append("    graph [fontname=\"Helvetica\", pad=0.2, nodesep=0.35, ranksep=0.55];\n");
        dot.append("    node [shape=circle, fontname=\"DejaVu Sans\"];\n");
        dot.append("    edge [fontname=\"DejaVu Sans\"];\n\n");

        // Invisible source used to draw the initial-state arrow without
        // changing the automaton itself.
        dot.append("    __initial [shape=point, width=0, label=\"\"];\n");

        for(ResidualState state : automaton.getStates()){

            String shape = automaton.isAccepting(state) ? "doublecircle" : "circle";

            String formula = escapeHtml(state.getFormula().toString());

            String label =
                    "<<TABLE BORDER=\"0\" CELLBORDER=\"0\" CELLSPACING=\"0\">"
                            + "<TR><TD>"
                            + "<FONT FACE=\"DejaVu Sans\"><B>q"
                            + state.getId()
                            + "</B></FONT>"
                            + "</TD></TR>"

                            + "<TR><TD HEIGHT=\"5\"></TD></TR>"

                            + "<TR><TD>"
                            + "<FONT FACE=\"DejaVu Sans\" POINT-SIZE=\"10\">"
                            + formula
                            + "</FONT>"
                            + "</TD></TR>"

                            + "</TABLE>>";

            dot.append("    q")
                    .append(state.getId())
                    .append(" [shape=")
                    .append(shape)
                    .append(", label=")
                    .append(label)
                    .append("];\n");

        }

        dot.append("\n    __initial -> q")
                .append(automaton.getInitialState().getId())
                .append(";\n\n");

        ///  group valuations that lead from a same source to the same destination.
        /// this keeps the rendered automaton readable when alphabet contains many valuations

        for(ResidualState source : automaton.getStates()){

            Map<ResidualState, List<Valuation>> grouped = new LinkedHashMap<>();

            for(Valuation valuation : automaton.getAlphabet()){
                ResidualState destination =
                        automaton.getDestination(source, valuation);

                grouped.computeIfAbsent(destination, ignored -> new ArrayList<>())
                        .add(valuation);
            }

            for(Map.Entry<ResidualState, List<Valuation>> entry : grouped.entrySet()){
                StringJoiner labels = new StringJoiner(", ");

                for(Valuation valuation : entry.getValue()){
                    labels.add(valuation.toString());
                }

                dot.append("    q")
                        .append(source.getId())
                        .append(" -> q")
                        .append(entry.getKey().getId())
                        .append(" [label=\"")
                        .append(escape(labels.toString()))
                        .append("\"];\n");

            }
        }

        dot.append("}\n");

        return dot.toString();
    }

    private String escape(String value){

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n","\\n")
                .replace("\r","\\r");
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
