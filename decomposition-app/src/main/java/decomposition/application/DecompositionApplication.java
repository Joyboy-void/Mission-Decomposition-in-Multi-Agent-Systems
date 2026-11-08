package decomposition.application;

import agent.model.AgentModel;

import decomposition.DecompositionResult;
import decomposition.ShortestPathFinder;

import ltlf.analysis.dres.DResFinder;
import ltlf.analysis.dres.DResResult;
import ltlf.automaton.ResidualAutomaton;

import product.automaton.ProductAutomaton;
import product.automaton.ProductAutomatonBuilder;

import team.automaton.TeamAutomaton;
import team.automaton.TeamAutomatonBuilder;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;

/**
 * coordinates the machine decomposition pipeline.
 *
 * Pipeline:
 *
 * 1. Load Agent Model.
 * 2. Construct Residual Automaton.
 * 3. Compute DRes.
 * 4. Construct Product Automaton.
 * 5. Construct Team Automaton.
 * 6. Find minimum-cost decomposition.
 */
public final class DecompositionApplication {

    private final Function<Path, AgentModel> agentModelLoader;

    private final Function<String, ResidualAutomaton>
            residualAutomatonBuilder;

    private final DResFinder dresFinder;

    private final ShortestPathFinder shortestPathFinder;

    private record IntermediateAutomata(
            ResidualAutomaton residualAutomaton,
            ProductAutomaton productAutomaton,
            DResResult dresResult,
            TeamAutomaton teamAutomaton
    ) {}

    public DecompositionApplication(
            Function<Path, AgentModel> agentModelLoader,
            Function<String, ResidualAutomaton> residualAutomatonBuilder
    ){
        this(
                agentModelLoader,
                residualAutomatonBuilder,
                new DResFinder(),
                new ShortestPathFinder()
        );
    }

    public DecompositionApplication(
            Function<Path, AgentModel> agentModelLoader,
            Function<String, ResidualAutomaton> residualAutomatonBuilder,
            DResFinder dresFinder,
            ShortestPathFinder shortestPathFinder
    ){
        this.agentModelLoader =
                Objects.requireNonNull(agentModelLoader);

        this.residualAutomatonBuilder =
                Objects.requireNonNull(residualAutomatonBuilder);

        this.dresFinder =
                Objects.requireNonNull(dresFinder);

        this.shortestPathFinder =
                Objects.requireNonNull(shortestPathFinder);
    }

    /**
     * builds the Team Automaton without executing the shortest-path
     * decomposition algorithm.
     *
     * used by standalone Team Automaton rendering commands.
     */
    public TeamAutomaton buildTeamAutomaton(
            Path agentModelFile,
            String formula,
            int numberOfAgents
    ) {
        return buildIntermediateAutomata(
                agentModelFile,
                formula,
                numberOfAgents
        ).teamAutomaton();
    }

    /**
     * executes the complete decomposition pipeline.
     */
    public DecompositionPipelineResult execute(
            Path agentModelFile,
            String formula,
            int numberOfAgents
    ) {
        IntermediateAutomata intermediate =
                buildIntermediateAutomata(
                        agentModelFile,
                        formula,
                        numberOfAgents
                );

        // find the minimum-cost decomposition.
        DecompositionResult result =
                shortestPathFinder.find(
                        intermediate.teamAutomaton()
                );

        return new DecompositionPipelineResult(
                intermediate.residualAutomaton(),
                intermediate.productAutomaton(),
                intermediate.dresResult(),
                intermediate.teamAutomaton(),
                result
        );
    }

    /**
     * constructs all automata required before shortest-path search.
     *
     * shared by execute() and buildTeamAutomaton().
     */
    private IntermediateAutomata buildIntermediateAutomata(
            Path agentModelFile,
            String formula,
            int numberOfAgents
    ) {
        Objects.requireNonNull(
                agentModelFile,
                "Agent model file must not be null"
        );

        if(formula == null || formula.isBlank()){
            throw new IllegalArgumentException(
                    "LTLf formula must not be blank"
            );
        }

        if(numberOfAgents < 1){
            throw new IllegalArgumentException(
                    "Number of agents must be >= 1"
            );
        }

        // Step 1: Load Agent Model.
        AgentModel agentModel =
                agentModelLoader.apply(agentModelFile);

        // Step 2: Construct Residual Automaton.
        ResidualAutomaton ra =
                residualAutomatonBuilder.apply(formula);

        // Step 3: Compute DRes.
        DResResult dres =
                dresFinder.find(ra);

        // Step 4: Construct Product Automaton.
        ProductAutomaton product =
                ProductAutomatonBuilder.build(
                        ra,
                        agentModel
                );

        // Step 5: Construct Team Automaton.
        TeamAutomaton team =
                TeamAutomatonBuilder.build(
                        product,
                        dres,
                        numberOfAgents
                );

        return new IntermediateAutomata(
                ra,
                product,
                dres,
                team
        );
    }
}
