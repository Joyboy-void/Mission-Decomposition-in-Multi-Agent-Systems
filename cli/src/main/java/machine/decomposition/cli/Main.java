package machine.decomposition.cli;

import agent.model.application.AgentModelApplication;
import agent.model.application.AgentModelFileLoader;
import agent.model.application.AgentModelLoader;
import agent.model.graphviz.AgentModelDotExporter;

import decomposition.ShortestPathFinder;
import decomposition.application.DecompositionApplication;
import decomposition.application.DecompositionFormatter;
import decomposition.application.DecompositionImageExporter;

import graphviz.core.GraphvizRenderer;

import ltlf.analysis.dres.DResFinder;
import ltlf.application.Ltlf2RaApplication;
import ltlf.graphviz.ResidualAutomatonDotExporter;

import product.automaton.application.ProductAutomatonApplication;
import product.automaton.graphviz.ProductAutomatonDotExporter;

import team.automaton.graphviz.TeamAutomatonDotExporter;

public final class Main {

    private Main() { }

    public static void main(String[] args){

        // LTLf -> Residual Automaton
        Ltlf2RaApplication raApplication =
                new Ltlf2RaApplication();

        // Shared Graphviz renderer
        GraphvizRenderer renderer =
                new GraphvizRenderer();

        // Agent Model loader
        AgentModelLoader loader =
                new AgentModelFileLoader();

        // Agent Model application
        AgentModelApplication agentApplication =
                new AgentModelApplication(
                        loader,
                        new AgentModelDotExporter(),
                        renderer
                );

        // Graphviz exporters
        ResidualAutomatonDotExporter raExporter =
                new ResidualAutomatonDotExporter();

        ProductAutomatonDotExporter productExporter =
                new ProductAutomatonDotExporter();

        TeamAutomatonDotExporter teamExporter =
                new TeamAutomatonDotExporter();

        // Product Automaton application
        ProductAutomatonApplication productApplication =
                new ProductAutomatonApplication(
                        loader::load,
                        raApplication::build,
                        productExporter,
                        renderer
                );

        // decomposition application
        DecompositionApplication decompositionApplication =
                new DecompositionApplication(
                        loader::load,
                        raApplication::build,
                        new DResFinder(),
                        new ShortestPathFinder()
                );

        DecompositionFormatter decompositionFormatter =
                new DecompositionFormatter();

        // exports RA, Product Automaton, and Team Automaton
        // in SVG and PNG formats.
        DecompositionImageExporter decompositionImageExporter =
                new DecompositionImageExporter(
                        renderer,
                        raExporter,
                        productExporter,
                        teamExporter
                );

        // CLI
        CommandLineApplication cli =
                new CommandLineApplication(
                        raApplication,
                        agentApplication,
                        productApplication,
                        decompositionApplication,
                        decompositionFormatter,
                        renderer,
                        teamExporter,
                        decompositionImageExporter
                );

        int exitCode = cli.run(args, System.out, System.err);

        if (exitCode != 0){
            System.exit(exitCode);
        }
    }
}
