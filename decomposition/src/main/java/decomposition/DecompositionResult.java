package decomposition;

import automata.WeightedTransition;
import team.automaton.TeamAction;
import team.automaton.TeamState;

import java.util.List;
import java.util.Objects;

/**
 * result of the optimal decomposition search.
 *
 * if decomposable is false, no path from an initial
 * state to any final state exists.
 */
public final class DecompositionResult {

    private final boolean decomposable;

    private final long totalCost;

    private final List<TeamState> path;

    private final List<WeightedTransition<
            TeamState,
            TeamAction,
            Integer>> transitions;

    private DecompositionResult(
            boolean decomposable,
            long totalCost,
            List<TeamState> path,
            List<WeightedTransition<
                    TeamState,
                    TeamAction,
                    Integer>> transitions
    ){

        this.decomposable = decomposable;
        this.totalCost = totalCost;
        this.path = List.copyOf(path);
        this.transitions = List.copyOf(transitions);
    }

    public static DecompositionResult success(
            long totalCost,
            List<TeamState> path,
            List<WeightedTransition<
                    TeamState,
                    TeamAction,
                    Integer>> transitions
    ){

        if(totalCost < 0){
            throw new IllegalArgumentException(
                    "Total cost cannot be negative"
            );
        }

        Objects.requireNonNull(path);
        Objects.requireNonNull(transitions);

        if(path.isEmpty()){
            throw new IllegalArgumentException(
                    "Successful path cannot be empty"
            );
        }

        if(path.size() != transitions.size() + 1){
            throw new IllegalArgumentException(
                    "Path and transition counts are inconsistent"
            );
        }

        return new DecompositionResult(
                true,
                totalCost,
                path,
                transitions
        );
    }

    public static DecompositionResult failure(){

        return new DecompositionResult(
                false,
                -1,
                List.of(),
                List.of()
        );
    }

    public boolean isDecomposable(){
        return decomposable;
    }

    // returns -1 if no decomposition exists.
    public long getTotalCost(){
        return totalCost;
    }

    public List<TeamState> getPath(){
        return path;
    }

    public List<WeightedTransition<
            TeamState,
            TeamAction,
            Integer>> getTransitions(){
        return transitions;
    }
}
