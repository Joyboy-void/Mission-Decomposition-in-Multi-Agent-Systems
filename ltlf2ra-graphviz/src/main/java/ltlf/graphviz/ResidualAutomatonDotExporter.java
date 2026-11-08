package ltlf.graphviz;

import graphviz.core.DotExporter;
import ltlf.alphabet.Valuation;
import ltlf.analysis.dres.DResResult;
import ltlf.automaton.ResidualAutomaton;
import ltlf.automaton.ResidualState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Converts a Residual Automaton into Graphviz DOT source.
 */
public final class ResidualAutomatonDotExporter
        implements DotExporter<ResidualAutomaton> {

    private static final List<String> CERTIFICATE_COLORS = List.of(
            "#E67E22",
            "#8E44AD",
            "#16A085",
            "#D35400",
            "#2980B9",
            "#C0392B",
            "#7D3C98",
            "#117A65"
    );

    private record EdgeKey(
            ResidualState source,
            ResidualState destination,
            Valuation valuation
    ) { }

    @Override
    public String export(ResidualAutomaton automaton){
        return export(automaton, null);
    }

    /**
     * Exports the RA with DRes states and certificate transitions highlighted.
     */
    public String export(
            ResidualAutomaton automaton,
            DResResult dresResult
    ){

        Objects.requireNonNull(automaton, "automaton");

        Map<ResidualState, String> highlightedStates =
                new LinkedHashMap<>();

        Map<EdgeKey, String> highlightedEdges =
                new LinkedHashMap<>();

        if (dresResult != null) {

            int certificateIndex = 0;

            for (DResResult.Certificate certificate
                    : dresResult.getCertificates()) {

                String color = CERTIFICATE_COLORS.get(
                        certificateIndex % CERTIFICATE_COLORS.size()
                );

                certificateIndex++;

                highlightedStates.putIfAbsent(
                        certificate.q(), color
                );

                highlightedStates.putIfAbsent(
                        certificate.qPrime(), color
                );

                ResidualState initial =
                        automaton.getInitialState();

                // q0 --w1--> q
                addPathEdges(
                        automaton,
                        initial,
                        certificate.w1(),
                        certificate.q(),
                        false,
                        color,
                        highlightedEdges
                );

                // qPrime --w1--> some final state
                addPathEdges(
                        automaton,
                        certificate.qPrime(),
                        certificate.w1(),
                        null,
                        true,
                        color,
                        highlightedEdges
                );

                // q --w2--> some final state
                addPathEdges(
                        automaton,
                        certificate.q(),
                        certificate.w2(),
                        null,
                        true,
                        color,
                        highlightedEdges
                );

                // q0 --w2--> qPrime
                addPathEdges(
                        automaton,
                        initial,
                        certificate.w2(),
                        certificate.qPrime(),
                        false,
                        color,
                        highlightedEdges
                );
            }
        }

        StringBuilder dot = new StringBuilder();

        dot.append("digraph ResidualAutomaton{\n");
        dot.append("    rankdir=LR;\n");
        dot.append("    graph [fontname=\"Helvetica\", pad=0.2, nodesep=0.35, ranksep=0.55];\n");
        dot.append("    node [shape=circle, fontname=\"DejaVu Sans\"];\n");
        dot.append("    edge [fontname=\"DejaVu Sans\"];\n\n");

        // invisible source used for the initial-state arrow.
        dot.append("    __initial [shape=point, width=0, label=\"\"];\n");

        for (ResidualState state : automaton.getStates()){

            String shape =
                    automaton.isAccepting(state)
                            ? "doublecircle"
                            : "circle";

            String formula =
                    escapeHtml(state.getFormula().toString());

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
                    .append(label);

            String stateColor =
                    highlightedStates.get(state);

            if (stateColor != null){

                dot.append(", color=\"")
                        .append(stateColor)
                        .append("\"")
                        .append(", penwidth=3")
                        .append(", style=filled")
                        .append(", fillcolor=\"#FFF4E5\"");
            }

            dot.append("];\n");
        }

        dot.append("\n    __initial -> q")
                .append(automaton.getInitialState().getId())
                .append(";\n\n");

        /*
         * preserve the original source/destination grouping.
         *
         * HTML labels allow individual valuations to be coloured when
         * only some valuations in a grouped edge belong to certificates.
         */
        for(ResidualState source : automaton.getStates()){

            Map<ResidualState, List<Valuation>> grouped =
                    new LinkedHashMap<>();

            for(Valuation valuation : automaton.getAlphabet()){
                ResidualState destination =
                        automaton.getDestination(source, valuation);

                grouped.computeIfAbsent(
                        destination,
                        ignored -> new ArrayList<>()
                ).add(valuation);
            }

            for (Map.Entry<ResidualState, List<Valuation>> entry
                    : grouped.entrySet()) {

                boolean hasHighlightedValuation = false;

                String edgeColor = null;

                StringJoiner ordinaryLabels =
                        new StringJoiner(", ");

                StringBuilder htmlLabels =
                        new StringBuilder(
                                "<TABLE BORDER=\"0\" CELLBORDER=\"0\" CELLSPACING=\"0\"><TR><TD>"
                        );

                for(Valuation valuation : entry.getValue()){

                    String color = highlightedEdges.get(
                            new EdgeKey(
                                    source,
                                    entry.getKey(),
                                    valuation
                            )
                    );

                    if (color == null) {

                        ordinaryLabels.add(
                                valuation.toString()
                        );

                        htmlLabels.append(
                                "<FONT COLOR=\"black\">"
                        ).append(
                                escapeHtml(valuation.toString())
                        ).append("</FONT>");

                    } else {

                        hasHighlightedValuation = true;

                        htmlLabels.append(
                                "<FONT COLOR=\""
                        ).append(
                                color
                        ).append(
                                "\">"
                        ).append(
                                escapeHtml(valuation.toString())
                        ).append(
                                "</FONT>"
                        );

                        if (edgeColor == null){
                            edgeColor = color;
                        }
                    }

                    htmlLabels.append(", ");
                }

                if (!entry.getValue().isEmpty()) {
                    htmlLabels.setLength(
                            htmlLabels.length() - 2
                    );
                }

                htmlLabels.append("</TD></TR></TABLE>");

                dot.append("    q")
                        .append(source.getId())
                        .append(" -> q")
                        .append(entry.getKey().getId())
                        .append(" [label=");

                if(hasHighlightedValuation){

                    dot.append("<")
                            .append(htmlLabels)
                            .append(">");

                }else{

                    // Preserve the original plain-text edge label.
                    dot.append("\"")
                            .append(escape(ordinaryLabels.toString()))
                            .append("\"");
                }

                if(edgeColor != null){
                    dot.append(", color=\"")
                            .append(edgeColor)
                            .append("\"")
                            .append(", penwidth=3");
                }

                dot.append("];\n");
            }
        }

        if (dresResult != null && !dresResult.isEmpty()) {

            dot.append("\n    subgraph cluster_dres_legend {\n")
                    .append("        label=\"DRes certificate\";\n")
                    .append("        color=\"#D5D8DC\";\n")
                    .append("        fontname=\"Helvetica\";\n")
                    .append("        __dres_state_legend [shape=doublecircle, label=\"DRes state\", ")
                    .append("color=\"#E67E22\", penwidth=3, style=filled, fillcolor=\"#FFF4E5\"];\n")
                    .append("        __dres_edge_a [shape=point, label=\"\", width=0.01];\n")
                    .append("        __dres_edge_b [shape=point, label=\"\", width=0.01];\n")
                    .append("        __dres_edge_a -> __dres_edge_b [label=\"certificate transition\", ")
                    .append("color=\"#E67E22\", fontcolor=\"#E67E22\", penwidth=3];\n")
                    .append("    }\n");
        }

        dot.append("}\n");

        return dot.toString();
    }

    /**
     * Reconstructs a certificate path and registers its transitions.
     *
     * If expectedDestination is null, the resulting state must be final
     * when expectedFinal is true.
     */
    private void addPathEdges(
            ResidualAutomaton automaton,
            ResidualState start,
            List<Valuation> word,
            ResidualState expectedDestination,
            boolean expectedFinal,
            String color,
            Map<EdgeKey, String> highlightedEdges
    ) {

        ResidualState current = start;

        for (Valuation valuation : word) {

            ResidualState next =
                    automaton.getDestination(current, valuation);

            if (next == null) {

                throw new IllegalStateException(
                        "DRes certificate contains a missing transition "
                                + "from q" + current.getId()
                                + " for " + valuation
                );
            }

            highlightedEdges.putIfAbsent(
                    new EdgeKey(current, next, valuation),
                    color
            );

            current = next;
        }

        if (expectedDestination != null
                && current != expectedDestination) {

            throw new IllegalStateException(
                    "Invalid DRes certificate: witness ends at q"
                            + current.getId()
                            + " instead of q"
                            + expectedDestination.getId()
            );
        }

        if (expectedFinal && !automaton.isFinalState(current)) {

            throw new IllegalStateException(
                    "Invalid DRes certificate: witness does not end "
                            + "in a final state."
            );
        }
    }

    private String escape(String value){
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}