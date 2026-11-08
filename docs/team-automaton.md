# Team Automaton

The Team Automaton represents a fixed number of homogeneous agents executing work in sequence. It is built from the Product Automaton and the DRes result.

## 1. Intuition

For `n` agents, the conceptual Team Automaton contains `n` logical copies of the Product Automaton:

```text
copy 1  →  copy 2  →  ...  →  copy n
```

Each copy represents the current agent's progress through the same agent model and residual task specification. An agent may hand over the remaining task to the next agent only at a Product Automaton state whose residual component belongs to DRes.

## 2. State representation

A `TeamState` contains:

```text
(copyId, productState)
```

where `copyId` is in the range `1..n` and `productState` belongs to the underlying Product Automaton.

The logical state count is:

```text
n × |ProductAutomaton.states|
```

The implementation does not eagerly allocate all these states. It enumerates them lazily and generates outgoing transitions on demand.

## 3. Transition types

The Team Automaton has two kinds of weighted transitions.

### Ordinary valuation transition

An ordinary Product Automaton transition is lifted into the current copy:

```text
(c, p) ──valuation / cost──> (c, p')
```

The copy identifier remains unchanged and the original product-transition cost is preserved.

### Epsilon switch

If the current residual state is in DRes and another copy exists, the automaton can switch to the next copy:

```text
(c, (agentState, q))
       ── EPSILON / 0 ──>
(c+1, (any compatible agentState', q))
```

The destination may be any Product Automaton state with the same residual state. The switch advances the copy identifier, preserves the residual obligation, consumes no valuation, and costs zero.

No switch is generated from the last copy. A switch is not the same as an empty valuation: epsilon consumes no trace position, whereas an empty valuation is an ordinary symbol in the alphabet.

## 4. Initial and final states

All initial Team Automaton states are in copy 1 and correspond to the Product Automaton's initial states.

Only accepting Product Automaton states in copy `n` are final in the Team Automaton. This ensures that a complete team execution reaches an accepting residual state after the last agent's segment.

## 5. Why the representation is implicit

Copying every Product Automaton state and edge `n` times would multiply the stored graph size by the number of agents. Instead, `TeamAutomaton` stores the underlying product, DRes set, and copy count.

- `getStates()` lazily enumerates logical states.
- `getOutgoingEdges(state)` lifts product transitions and generates allowed switches when requested.
- An index groups Product Automaton states by residual state to find switch destinations efficiently.

This reduces eager construction and avoids duplicating the underlying Product Automaton graph.

## 6. Main classes

| Class | Responsibility |
|---|---|
| `TeamState` | Identifies an agent copy and a Product Automaton state |
| `TeamAction` | Distinguishes valuation actions from epsilon switches |
| `TeamAutomaton` | Implicit weighted automaton and transition generation |
| `TeamAutomatonBuilder` | Validates inputs and constructs the Team Automaton |
| `TeamAutomatonDotExporter` | Converts the Team Automaton into DOT |

## 7. Invariants

- Copy IDs are positive and do not exceed `n`.
- Every Team state refers to a state in the underlying Product Automaton.
- Ordinary transitions stay within the same copy.
- Epsilon switches advance exactly one copy.
- Switches preserve the residual state and have cost zero.
- Switches are permitted only from DRes residual states.
- Only final-copy accepting states are accepting.
