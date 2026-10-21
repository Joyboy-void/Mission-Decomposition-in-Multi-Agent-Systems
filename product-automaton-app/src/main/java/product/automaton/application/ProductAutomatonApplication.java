package product.automaton.application;

import agent.model.AgentModel;
import graphviz.core.GraphvizRenderer;
import ltlf.automaton.ResidualAutomaton;
import product.automaton.ProductAutomaton;
import product.automaton.ProductAutomatonBuilder;
import product.automaton.graphviz.ProductAutomatonDotExporter;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;

/** Coordinates agent loading, RA construction, product construction and rendering. */
public final class ProductAutomatonApplication {

    private final Function<Path, AgentModel> agentModelLoader;
    private final Function<String, ResidualAutomaton> raBuilder;
    private final ProductAutomatonDotExporter dotExporter;
    private final GraphvizRenderer renderer;

    public ProductAutomatonApplication(
            Function<Path, AgentModel> agentModelLoader,
            Function<String, ResidualAutomaton> raBuilder,
            ProductAutomatonDotExporter dotExporter,
            GraphvizRenderer renderer) {

        this.agentModelLoader = Objects.requireNonNull(agentModelLoader, "agentModelLoader");
        this.raBuilder = Objects.requireNonNull(raBuilder, "raBuilder");
        this.dotExporter = Objects.requireNonNull(dotExporter, "dotExporter");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
    }

    public ProductAutomaton build(Path agentModelFile, String formula){
        Objects.requireNonNull(agentModelFile, "agentModelFile");

        if (formula == null || formula.isBlank()){
            throw new IllegalArgumentException("LTLf formula must not be blank");
        }

        AgentModel model = agentModelLoader.apply(agentModelFile);
        ResidualAutomaton ra = raBuilder.apply(formula);
        return ProductAutomatonBuilder.build(ra, model);
    }

    public String exportDot(Path agentModelFile, String formula){
        return dotExporter.export(build(agentModelFile, formula));
    }

    public void render(
            Path agentModelFile,
            String formula,
            String format,
            Path output) {

        ProductAutomaton product = build(agentModelFile, formula);
        renderer.renderDotWithEngine(
                "neato",
                dotExporter.export(product),
                format,
                output
        );
    }
}