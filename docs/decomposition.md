# Minimum-cost decomposition

The `decomposition` module searches the Team Automaton for a minimum-cost path from an initial state to any final state. The selected path represents the assignment of work to the agents.

## 1. Optimization problem

Given a Team Automaton with non-negative transition costs, find:

```text
minimum cost of a path from any initial state to any final state
```

Ordinary valuation transitions carry the costs inherited from the agent model. Epsilon switches have cost zero. If no final state is reachable, no valid decomposition exists for the supplied inputs and agent count.

## 2. Algorithm: Dijkstra

`ShortestPathFinder` uses Dijkstra's algorithm because all edge weights are non-negative.

1. Initialize every Team Automaton initial state with distance zero.
2. Insert the initial states into a min-priority queue.
3. Repeatedly remove the state with the smallest tentative distance.
4. Ignore stale queue entries and states already settled.
5. If the removed state is final, reconstruct and return the path. The first final state removed has minimum distance.
6. Otherwise, generate its outgoing transitions and relax each destination.
7. If the queue becomes empty without reaching a final state, return failure.

The algorithm supports zero-cost epsilon switches. Negative weights are rejected because Dijkstra is not correct for negative edge weights.

## 3. Result representation

`DecompositionResult` records:

- whether a valid decomposition exists;
- the total path cost;
- the ordered Team Automaton state path;
- the ordered weighted transitions on that path.

A successful path contains exactly one more state than transitions. A failure result contains no path and reports `-1` as its total-cost sentinel.

## 4. Converting a path into agent assignments

`DecompositionFormatter` splits the selected path at epsilon transitions.

- The path segment before a switch is the current agent's work.
- The switch destination begins the next agent's segment.
- Valuation transitions are listed as actions and contribute to that agent's cost.
- Epsilon transitions separate segments and contribute zero cost.

For each agent, the formatter reports the start and end residual states, Product Automaton states visited, proposition labels, valuation actions, and segment cost. It then reports the minimum total cost.

An agent may receive an empty segment if the selected path switches without taking an ordinary valuation transition for that copy.

## 5. Pipeline orchestration

`DecompositionApplication` builds the intermediate objects in order:

```text
AgentModel
   + formula
   → ResidualAutomaton
   → DRes
   → ProductAutomaton
   → TeamAutomaton
   → ShortestPathFinder
   → DecompositionResult
```

The same intermediate construction is reused by the standalone Team Automaton rendering workflow.

## 6. Complexity

Let `V` and `E` denote the number of Team Automaton states and transitions actually explored by Dijkstra. With a binary heap, the usual bound is:

```text
O((V + E) log V)
```

The Team Automaton generates states and outgoing edges lazily, so these quantities refer to the explored portion of the implicit graph, not necessarily every logical state and edge.
