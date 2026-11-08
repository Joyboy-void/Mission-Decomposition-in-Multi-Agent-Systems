package decomposition.application;

import automata.WeightedTransition;
import decomposition.DecompositionResult;
import ltlf.ast.AtomicProposition;
import ltlf.automaton.ResidualState;
import product.automaton.ProductAutomaton;
import product.automaton.ProductState;
import team.automaton.TeamAction;
import team.automaton.TeamState;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * formats the optimal decomposition as work assigned to individual agents.
 */
public final class DecompositionFormatter{

    private static final class AgentSegment{

        private final int agentId;
        private final ProductState startState;

        private ProductState endState;

        private final List<ProductState> visitedStates =
                new ArrayList<>();

        private final List<WeightedTransition<
                TeamState, TeamAction, Integer>> actions =
                new ArrayList<>();

        private long cost;

        private AgentSegment(TeamState initialState){
            this.agentId = initialState.copyId();
            this.startState = initialState.productState();
            this.endState = initialState.productState();
            this.visitedStates.add(initialState.productState());
        }
    }

    public String format(DecompositionPipelineResult pipeline){

        DecompositionResult result =
                pipeline.decompositionResult();

        if(!result.isDecomposable()){
            return "No valid decomposition exists for the given "
                    + pipeline.teamAutomaton().getNumberOfAgents()
                    + " agents.\n";
        }

        List<AgentSegment> segments =
                buildAgentSegments(result);

        StringBuilder out = new StringBuilder();

        out.append("========================================\n");
        out.append("       OPTIMAL DECOMPOSITION\n");
        out.append("========================================\n");

        for (AgentSegment segment : segments) {
            appendAgent(out, segment, pipeline);
        }

        out.append("----------------------------------------\n");
        out.append("Minimum total cost: ")
                .append(result.getTotalCost())
                .append("\n");
        out.append("========================================\n");

        return out.toString();
    }

    /*
     * splits the optimal Team Automaton path at epsilon transitions.
     * each resulting segment represents the work of one agent.
     */
    private List<AgentSegment> buildAgentSegments(
            DecompositionResult result) {

        List<TeamState> path = result.getPath();

        List<WeightedTransition<
                TeamState, TeamAction, Integer>> transitions =
                result.getTransitions();

        if(path.isEmpty()
                || path.size() != transitions.size() + 1){
            throw new IllegalStateException(
                    "Invalid decomposition path."
            );
        }

        List<AgentSegment> segments = new ArrayList<>();

        AgentSegment current = new AgentSegment(path.get(0));

        for(WeightedTransition<
                TeamState, TeamAction, Integer> edge : transitions){

            if(edge.label() == TeamAction.SwitchAction.EPSILON){

                // the current agent's work ends at the epsilon source.
                current.endState = edge.source().productState();
                segments.add(current);

                // the next agent starts at the epsilon destination.
                current = new AgentSegment(edge.destination());

            }else if(edge.label()
                    instanceof TeamAction.ValuationAction){

                current.actions.add(edge);
                current.endState = edge.destination().productState();

                current.cost = Math.addExact(
                        current.cost,
                        edge.weight().longValue()
                );

                ProductState destination =
                        edge.destination().productState();

                if(!current.visitedStates.getLast().equals(destination)){
                    current.visitedStates.add(destination);
                }

            } else {
                throw new IllegalStateException(
                        "Unknown Team Automaton action: " + edge.label()
                );
            }
        }

        segments.add(current);

        return segments;
    }

    private void appendAgent(
            StringBuilder out,
            AgentSegment segment,
            DecompositionPipelineResult pipeline){

        ProductAutomaton product = pipeline.productAutomaton();

        ResidualState startResidual =
                segment.startState.residualState();

        ResidualState endResidual =
                segment.endState.residualState();

        boolean accepting = pipeline.residualAutomaton()
                .isAccepting(endResidual);

        out.append("\nAgent ")
                .append(segment.agentId)
                .append(": residual state q")
                .append(startResidual.getId())
                .append(" -> q")
                .append(endResidual.getId());

        if (accepting) {
            out.append(" (ACCEPTING)");
        }

        out.append("\n");

        out.append("  States along the optimal path:\n");

        for (ProductState state : segment.visitedStates) {
            out.append("    ")
                    .append(formatState(state, product))
                    .append("\n");
        }

        out.append("  Actions:\n");

        if(segment.actions.isEmpty()){
            out.append("    No valuation transitions.\n");
        }

        for(WeightedTransition<
                TeamState, TeamAction, Integer> edge : segment.actions){

            TeamAction.ValuationAction action =
                    (TeamAction.ValuationAction) edge.label();

            out.append("    ")
                    .append(action.valuation())
                    .append("  [cost: ")
                    .append(edge.weight())
                    .append("]\n");
        }

        out.append("  Agent cost: ")
                .append(segment.cost)
                .append("\n");
    }

    /**
     * displays the agent state, residual state, and AP label of a
     * Product Automaton state.
     */
    private String formatState(
            ProductState state,
            ProductAutomaton product) {

        String propositions = product.getLabel(state)
                .stream()
                .map(AtomicProposition::getName)
                .sorted()
                .collect(Collectors.joining(", "));

        return "("
                + state.agentState().id()
                + ", q" + state.residualState().getId()
                + ")  AP = {" + propositions + "}";
    }
}
