package ltlf.application;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

import graphviz.core.GraphvizRenderer;
import ltlf.ast.AtomicProposition;
import ltlf.automaton.ResidualAutomaton;
import ltlf.graphviz.ResidualAutomatonDotExporter;

public class Ltlf2RaApplication {


    private final Ltlf2RaService service;
    private final ResidualAutomatonFormatter formatter;
    private final ResidualAutomatonDotExporter dotExporter;
    private final GraphvizRenderer graphvizRenderer;


    public Ltlf2RaApplication(){
        this(
                new Ltlf2RaService(),
                new ResidualAutomatonFormatter(),
                new ResidualAutomatonDotExporter(),
                new GraphvizRenderer()
        );
    }

    public Ltlf2RaApplication(
            Ltlf2RaService service,
            ResidualAutomatonFormatter formatter){
        this(
                service,
                formatter,
                new ResidualAutomatonDotExporter(),
                new GraphvizRenderer()
        );
    }

    public Ltlf2RaApplication(
            Ltlf2RaService service,
            ResidualAutomatonFormatter formatter,
            ResidualAutomatonDotExporter dotExporter,
            GraphvizRenderer graphvizRenderer ){
        this.service = Objects.requireNonNull(service);
        this.formatter = Objects.requireNonNull(formatter);
        this.dotExporter = Objects.requireNonNull(dotExporter);
        this.graphvizRenderer = Objects.requireNonNull(graphvizRenderer);
    }

    public ResidualAutomaton build(String formulaText){
        return service.build(formulaText);
    }

    /**
     * Builds an RA using the formula's propositions plus additional propositions
     * from a surrounding model.
     */
    public ResidualAutomaton build(
            String formulaText,
            Collection<AtomicProposition> additionalPropositions) {
        return service.build(formulaText, additionalPropositions);
    }

    public String execute(String formulaText){
        return formatter.format(build(formulaText));
    }

    public String exportDot(String formulaText){
        return dotExporter.export(build(formulaText));
    }

    public void render(String formulaText, String format, Path output){
        graphvizRenderer.render(
                build(formulaText),
                dotExporter,
                format,
                output
        );
    }
}
