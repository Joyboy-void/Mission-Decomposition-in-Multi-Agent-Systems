package agent.model.application;

import agent.model.AgentModel;
import agent.model.graphviz.AgentModelDotExporter;
import graphviz.core.GraphvizRenderer;


import java.nio.file.Path;
import java.util.Objects;

/** Application use cases for loading and visualizing an agent model. */

public final class AgentModelApplication {

    private final AgentModelLoader loader;
    private final AgentModelDotExporter dotExporter;
    private final GraphvizRenderer renderer;

    public AgentModelApplication(
            AgentModelLoader loader,
            AgentModelDotExporter dotExporter,
            GraphvizRenderer renderer
    ){
        this.loader = Objects.requireNonNull(loader, "loader");
        this.dotExporter = Objects.requireNonNull(dotExporter, "dotExporter");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
    }

    public AgentModel load(Path input){
        Objects.requireNonNull(input, "input");
        return loader.load(input);
    }

    public String exportDot(Path input){
        return dotExporter.export(load(input));
    }

    public void render(Path input, String format, Path output){
        renderer.render(load(input), dotExporter, format, output);
    }

}
