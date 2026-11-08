package decomposition;

import automata.WeightedTransition;
import team.automaton.TeamAction;
import team.automaton.TeamAutomaton;
import team.automaton.TeamState;

import java.util.*;

/**
 * finds a minimum-cost path from any initial Team Automaton
 * state to any final Team Automaton state.
 *
 * uses Dijkstra's algorithm.
 *
 * all edge weights must be non-negative.
 * zero-cost epsilon transitions are supported.
 */
public final class ShortestPathFinder {

    private record QueueEntry(
            TeamState state,
            long distance
    ){}

    private final Comparator<QueueEntry> queueComparator =
            Comparator.comparingLong(QueueEntry::distance);

    public DecompositionResult find(TeamAutomaton automaton){

        Objects.requireNonNull(
                automaton,
                "Team Automaton must not be null"
        );

        if(automaton.getInitialStates().isEmpty()){
            return DecompositionResult.failure();
        }

        PriorityQueue<QueueEntry> queue =
                new PriorityQueue<>(queueComparator);

        Map<TeamState, Long> distance =
                new HashMap<>();

        Map<TeamState, WeightedTransition<
                TeamState,
                TeamAction,
                Integer>> predecessor =
                new HashMap<>();

        Set<TeamState> settled =
                new HashSet<>();

        // initialize all initial states with distance zero.
        for(TeamState initial : automaton.getInitialStates()){

            distance.put(initial, 0L);

            queue.add(
                    new QueueEntry(initial, 0L)
            );
        }

        while(!queue.isEmpty()){

            QueueEntry entry = queue.poll();

            TeamState current = entry.state();

            long currentDistance = entry.distance();

            // ignore stale priority queue entries.
            if(currentDistance != distance.getOrDefault(
                    current,
                    Long.MAX_VALUE
            )){
                continue;
            }

            if(!settled.add(current)){
                continue;
            }

            /*
             * dijkstra guarantees that the first final state
             * removed from the queue has minimum distance.
             */
            if(automaton.isFinalState(current)){

                return reconstruct(
                        current,
                        currentDistance,
                        predecessor
                );
            }

            for(WeightedTransition<
                    TeamState,
                    TeamAction,
                    Integer> edge :
                    automaton.getOutgoingEdges(current)){

                int weight = edge.weight();

                if(weight < 0){
                    throw new IllegalArgumentException(
                            "Dijkstra cannot process negative edge weights"
                    );
                }

                TeamState next = edge.destination();

                if(settled.contains(next)){
                    continue;
                }

                long newDistance =
                        Math.addExact(currentDistance, weight);

                long oldDistance =
                        distance.getOrDefault(
                                next,
                                Long.MAX_VALUE
                        );

                if(newDistance < oldDistance){

                    distance.put(next, newDistance);

                    predecessor.put(next, edge);

                    queue.add(
                            new QueueEntry(next, newDistance)
                    );
                }
            }
        }

        // No final state was reachable.
        return DecompositionResult.failure();
    }

    // reconstructs the optimal path using predecessor edges.

    private DecompositionResult reconstruct(
            TeamState finalState,
            long totalCost,
            Map<TeamState, WeightedTransition<
                    TeamState,
                    TeamAction,
                    Integer>> predecessor
    ){

        List<TeamState> states = new ArrayList<>();

        List<WeightedTransition<
                TeamState,
                TeamAction,
                Integer>> transitions =
                new ArrayList<>();

        TeamState current = finalState;

        states.add(current);

        while(predecessor.containsKey(current)){

            WeightedTransition<
                    TeamState,
                    TeamAction,
                    Integer> edge =
                    predecessor.get(current);

            transitions.add(edge);

            current = edge.source();

            states.add(current);
        }

        Collections.reverse(states);
        Collections.reverse(transitions);

        return DecompositionResult.success(
                totalCost,
                states,
                transitions
        );
    }
}
