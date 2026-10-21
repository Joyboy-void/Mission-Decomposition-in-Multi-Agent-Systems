package machine.decomposition.cli;

import agent.model.application.AgentModelApplication;
import agent.model.application.AgentModelLoader;
import agent.model.application.AgentModelFileLoader;
import agent.model.graphviz.AgentModelDotExporter;

import graphviz.core.GraphvizRenderer;

import ltlf.application.Ltlf2RaApplication;

import product.automaton.application.ProductAutomatonApplication;
import product.automaton.graphviz.ProductAutomatonDotExporter;

public final class Main {

    private Main() { }

    public static void main(String[] args){

        Ltlf2RaApplication raApplication = new Ltlf2RaApplication();
        GraphvizRenderer renderer = new GraphvizRenderer();
        AgentModelLoader loader = new AgentModelFileLoader();

        AgentModelApplication agentApplication = new AgentModelApplication(
                loader,
                new AgentModelDotExporter(),
                renderer
        );

        ProductAutomatonApplication productApplication = new ProductAutomatonApplication(
                loader::load,
                raApplication::build,
                new ProductAutomatonDotExporter(),
                renderer
        );

        CommandLineApplication cli = new CommandLineApplication(
                raApplication,
                agentApplication,
                productApplication
        );

        int exitCode = cli.run(args, System.out, System.err);

        if (exitCode != 0){
            System.exit(exitCode);
        }
    }
}
