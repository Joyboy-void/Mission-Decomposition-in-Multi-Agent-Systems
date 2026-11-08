package ltlf.application;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

import graphviz.core.GraphvizRenderer;
import ltlf.analysis.dres.DResFinder;
import ltlf.analysis.dres.DResResult;
import ltlf.ast.AtomicProposition;
import ltlf.automaton.ResidualAutomaton;
import ltlf.graphviz.ResidualAutomatonDotExporter;

public class Ltlf2RaApplication {

    private final Ltlf2RaService service;
    private final ResidualAutomatonFormatter formatter;
    private final ResidualAutomatonDotExporter dotExporter;
    private final GraphvizRenderer graphvizRenderer;
    private final DResFinder dResFinder;
    private final DResFormatter dResFormatter;

    public Ltlf2RaApplication(){
        this(
                new Ltlf2RaService(),
                new ResidualAutomatonFormatter(),
                new ResidualAutomatonDotExporter(),
                new GraphvizRenderer(),
                new DResFinder(),
                new DResFormatter()
        );
    }

    public Ltlf2RaApplication(
            Ltlf2RaService service,
            ResidualAutomatonFormatter formatter
    ){
        this(
                service,
                formatter,
                new ResidualAutomatonDotExporter(),
                new GraphvizRenderer(),
                new DResFinder(),
                new DResFormatter()
        );
    }

    public Ltlf2RaApplication(
            Ltlf2RaService service,
            ResidualAutomatonFormatter formatter,
            ResidualAutomatonDotExporter dotExporter,
            GraphvizRenderer graphvizRenderer
    ) {
        this(
                service,
                formatter,
                dotExporter,
                graphvizRenderer,
                new DResFinder(),
                new DResFormatter()
        );
    }

    public Ltlf2RaApplication(
            Ltlf2RaService service,
            ResidualAutomatonFormatter formatter,
            ResidualAutomatonDotExporter dotExporter,
            GraphvizRenderer graphvizRenderer,
            DResFinder dResFinder,
            DResFormatter dResFormatter
    ) {

        this.service = Objects.requireNonNull(service);
        this.formatter = Objects.requireNonNull(formatter);
        this.dotExporter = Objects.requireNonNull(dotExporter);
        this.graphvizRenderer = Objects.requireNonNull(graphvizRenderer);
        this.dResFinder = Objects.requireNonNull(dResFinder);
        this.dResFormatter = Objects.requireNonNull(dResFormatter);
    }

    public ResidualAutomaton build(String formulaText){
        return service.build(formulaText);
    }

    /**
     * builds an RA using the formula's propositions plus additional
     * propositions from a surrounding model.
     */
    public ResidualAutomaton build(
            String formulaText,
            Collection<AtomicProposition> additionalPropositions
    ){
        return service.build(
                formulaText,
                additionalPropositions
        );
    }

    public String execute(String formulaText){
        return formatter.format(build(formulaText));
    }

    public String exportDot(String formulaText){
        return dotExporter.export(build(formulaText));
    }

    public void render(
            String formulaText,
            String format,
            Path output
    ) {
        graphvizRenderer.render(
                build(formulaText),
                dotExporter,
                format,
                output
        );
    }

    /**
     * Builds the RA and discovers its DRes states.
     */
    public DResResult findDRes(String formulaText){

        ResidualAutomaton automaton =
                build(formulaText);

        return dResFinder.find(automaton);
    }

    /**
     * Builds the RA, discovers DRes, and formats the result.
     */
    public String executeDRes(String formulaText){

        DResResult result =
                findDRes(formulaText);

        return dResFormatter.format(result);
    }

    /**
     * Builds the Residual Automaton once, computes DRes certificates,
     * and renders the highlighted automaton in SVG and PNG formats.
     *
     * The supplied path is an output base; extensions are added here.
     */
    public void renderDResHighlighted(
            String formulaText,
            Path outputBase
    ) {

        Objects.requireNonNull(outputBase, "outputBase");

        ResidualAutomaton automaton =
                build(formulaText);

        DResResult result =
                dResFinder.find(automaton);

        String dot =
                dotExporter.export(automaton, result);

        graphvizRenderer.renderDot(
                dot,
                "svg",
                Path.of(outputBase + ".svg")
        );

        graphvizRenderer.renderDot(
                dot,
                "png",
                Path.of(outputBase + ".png")
        );
    }
}