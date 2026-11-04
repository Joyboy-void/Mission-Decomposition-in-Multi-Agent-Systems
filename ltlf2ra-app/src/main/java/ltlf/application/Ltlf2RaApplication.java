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
            ResidualAutomatonFormatter formatter){
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
            GraphvizRenderer graphvizRenderer) {

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
            DResFormatter dResFormatter) {

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

    /**
     * builds the RA and discovers its DRes states.
     */
    public DResResult findDRes(String formulaText){

        ResidualAutomaton automaton =
                build(formulaText);

        return dResFinder.find(automaton);
    }

    /**
     * builds the RA, discovers DRes, and formats the result.
     */
    public String executeDRes(String formulaText){

        DResResult result =
                findDRes(formulaText);

        return dResFormatter.format(result);
    }
}
