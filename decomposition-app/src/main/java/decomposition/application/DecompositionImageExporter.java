package decomposition.application;

import automata.WeightedTransition;
import decomposition.DecompositionResult;
import graphviz.core.GraphvizRenderer;
import ltlf.graphviz.ResidualAutomatonDotExporter;
import product.automaton.graphviz.ProductAutomatonDotExporter;
import team.automaton.TeamAction;
import team.automaton.TeamState;
import team.automaton.graphviz.TeamAutomatonDotExporter;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class DecompositionImageExporter {

    private final GraphvizRenderer renderer;
    private final ResidualAutomatonDotExporter raExporter;
    private final ProductAutomatonDotExporter productExporter;
    private final TeamAutomatonDotExporter teamExporter;

    public DecompositionImageExporter(
            GraphvizRenderer renderer,
            ResidualAutomatonDotExporter raExporter,
            ProductAutomatonDotExporter productExporter,
            TeamAutomatonDotExporter teamExporter
    ) {

        this.renderer = Objects.requireNonNull(renderer);
        this.raExporter = Objects.requireNonNull(raExporter);
        this.productExporter = Objects.requireNonNull(productExporter);
        this.teamExporter = Objects.requireNonNull(teamExporter);
    }

    /**
     * exports all intermediate automata in SVG and PNG formats.
     * retained for callers that do not require a suffix.
     */
    public void exportAll(
            DecompositionPipelineResult result,
            Path outputDirectory
    ){

        exportAll(result, outputDirectory, "");
    }

    /**
     * exports all intermediate automata in SVG and PNG formats,
     * appending the supplied suffix to each filename.
     */
    public void exportAll(
            DecompositionPipelineResult result,
            Path outputDirectory,
            String suffix
    ) {

        Objects.requireNonNull(result);
        Objects.requireNonNull(outputDirectory);
        Objects.requireNonNull(suffix);

        if (!suffix.isEmpty()
                && !suffix.matches("-[a-fA-F0-9]+")) {

            throw new IllegalArgumentException(
                    "Suffix must be empty or a hyphen followed by hexadecimal characters."
            );
        }

        exportResidualAutomaton(result, outputDirectory, suffix);
        exportProductAutomaton(result, outputDirectory, suffix);
        exportTeamAutomaton(result, outputDirectory, suffix);
    }

    /**
     * exports the Team Automaton with the exact minimum-cost
     * decomposition transitions highlighted.
     *
     * The existing unhighlighted exports are unaffected.
     */
    public void exportHighlightedDecomposition(
            DecompositionPipelineResult result,
            Path outputDirectory,
            String suffix
    ){

        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(outputDirectory, "outputDirectory");
        Objects.requireNonNull(suffix, "suffix");

        if(!suffix.isEmpty()
                && !suffix.matches("-[a-fA-F0-9]+")){

            throw new IllegalArgumentException(
                    "Suffix must be empty or a hyphen followed by hexadecimal characters."
            );
        }

        DecompositionResult decomposition =
                result.decompositionResult();

        if(!decomposition.isDecomposable()){
            throw new IllegalStateException(
                    "Cannot export a highlighted decomposition: "
                            + "no decomposition exists."
            );
        }

        Set<WeightedTransition<
                TeamState,
                TeamAction,
                Integer>> selectedTransitions =
                new LinkedHashSet<>(
                        decomposition.getTransitions()
                );

        String dot = teamExporter.export(
                result.teamAutomaton(),
                selectedTransitions
        );

        renderBothFormats(
                dot,
                "neato",
                outputDirectory.resolve(
                        "team-automaton-decomposition-highlighted"
                                + suffix
                )
        );
    }

    private void exportResidualAutomaton(
            DecompositionPipelineResult result,
            Path directory,
            String suffix
    ){

        String dot =
                raExporter.export(result.residualAutomaton());

        renderBothFormats(
                dot,
                "dot",
                directory.resolve("residual-automaton" + suffix)
        );
    }

    private void exportProductAutomaton(
            DecompositionPipelineResult result,
            Path directory,
            String suffix
    ){

        String dot =
                productExporter.export(result.productAutomaton());

        // The existing Product Automaton exporter uses fixed positions.
        renderBothFormats(
                dot,
                "neato",
                directory.resolve("product-automaton" + suffix)
        );
    }

    private void exportTeamAutomaton(
            DecompositionPipelineResult result,
            Path directory,
            String suffix
    ){

        String dot =
                teamExporter.export(result.teamAutomaton());

        renderBothFormats(
                dot,
                "neato",
                directory.resolve("team-automaton" + suffix)
        );
    }

    private void renderBothFormats(
            String dot,
            String engine,
            Path outputBase
    ){

        renderer.renderDotWithEngine(
                engine,
                dot,
                "svg",
                Path.of(outputBase + ".svg")
        );

        renderer.renderDotWithEngine(
                engine,
                dot,
                "png",
                Path.of(outputBase + ".png")
        );
    }
}