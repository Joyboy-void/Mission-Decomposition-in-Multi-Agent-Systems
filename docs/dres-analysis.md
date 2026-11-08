# DRes analysis

DRes analysis identifies pairs of residual states that permit two finite task sequences to be exchanged while preserving accepting executions. These states are used by the Team Automaton to decide where one agent may hand work to the next.

## 1. Definition

Let `Q` be the Residual Automaton states, `q₀` its initial state, `Qf` its final states, `Σ` its alphabet, and `δ` its transition function.

A candidate ordered pair `(q, q')` is eligible when:

```text
q, q' ∈ E,    q ≠ q'
E = Q \ ({q₀} ∪ Qf)
```

The pair is DRes when there are words `w₁, w₂ ∈ Σ*` and a final state `qf ∈ Qf` such that:

```text
q₀ ──w₁──> q       q' ──w₁──> qf
q  ──w₂──> qf      q₀ ──w₂──> q'
```

Equivalently, the two accepting executions can be arranged as:

```text
q₀ ──w₁──> q ──w₂──> qf
q₀ ──w₂──> q' ──w₁──> qf
```

The same word is consumed by both components in each condition.

## 2. Synchronized product reachability

For two RA states `a` and `b`, consider pairs `(a,b)`. For each alphabet valuation `v`, advance both components using the same valuation:

```text
(a,b) ──v──> (δ(a,v), δ(b,v))
```

A BFS over these pairs determines whether a target pair is reachable. The product graph is generated on demand; it does not need to be materialized in advance.

### Condition 1

For a fixed `q' ∈ E`, start BFS at `(q₀,q')`. A reachable pair `(q,qf)` establishes `C₁(q,q')` when `q ∈ E`, `qf ∈ Qf`, and `q ≠ q'`.

### Condition 2

For a fixed `q ∈ E`, start BFS at `(q,q₀)`. A reachable pair `(qf,q')` establishes `C₂(q,q')` when `qf ∈ Qf`, `q' ∈ E`, and `q ≠ q'`.

The final candidate set is:

```text
DRes = S₁ ∩ S₂
S₁ = {(q,q') | C₁(q,q')}
S₂ = {(q,q') | C₂(q,q')}
```

## 3. Why ordered pairs must be preserved

The two components have different roles in each condition. Therefore `(q,q')` and `(q',q)` are not interchangeable during the search.

For example, discovering `C₁(q₁,q₂)` and `C₂(q₂,q₁)` does not establish both conditions for either one ordered pair. Canonicalizing pairs before intersection could incorrectly merge these results.

The implementation must therefore:
1. record ordered pairs in `S₁` and `S₂`;
2. intersect the ordered sets;
3. canonicalize only afterward if the final result represents pairs as unordered.

## 4. Algorithm outline

```text
FIND-DRES(A):

    E = states excluding initial and final states
    if A has no final states or |E| < 2:
        return empty set

    S1 = empty set
    for each qPrime in E:
        R = BFS over synchronized pairs from (q0, qPrime)
        for each (q, qf) in R:
            if q in E and qf is final and q != qPrime:
                add (q, qPrime) to S1

    S2 = empty set
    for each q in E:
        R = BFS over synchronized pairs from (q, q0)
        for each (qf, qPrime) in R:
            if qf is final and qPrime in E and q != qPrime:
                add (q, qPrime) to S2

    return S1 intersect S2
```

Each BFS stores visited pairs and a queue. For each pair, it generates successors for the automaton's alphabet. If either component has no transition for a valuation, that successor is skipped.

## 5. Correctness intuition

A synchronized BFS path corresponds to a single word because both components consume the same sequence of valuations at every step.

- A target `(q,qf)` reached from `(q₀,q')` witnesses a word `w₁` for Condition 1.
- A target `(qf,q')` reached from `(q,q₀)` witnesses a word `w₂` for Condition 2.

Thus, membership in `S₁ ∩ S₂` means that the same ordered pair satisfies both required conditions. Conversely, if the two words exist, their synchronized runs form paths to the corresponding target pairs, so the BFS searches will discover them. This argument assumes the deterministic transition semantics used by the Residual Automaton.

## 6. Edge cases

- **No final states:** no accepting target exists, so DRes is empty.
- **Fewer than two eligible states:** no distinct candidate pair exists.
- **Empty valuation:** it is an ordinary alphabet symbol and consumes one trace position; it is not a structural epsilon transition.
- **Undefined transition:** skip the product successor if either component cannot advance.
- **Reversed pairs:** preserve order through both condition searches and their intersection.

## 7. Complexity

Let `n = |Q|` and `m = |Σ|`.

A synchronized product has at most `n²` pairs and at most `m` outgoing transitions per pair. One BFS therefore costs `O(n²m)` time and `O(n²)` auxiliary space.

At most `O(n)` BFS executions are performed for each condition, giving:

```text
Time:  O(n³m)
Space: O(n²)
```

The two ordered candidate sets require at most `O(n²)` additional storage. BFS runs are performed sequentially.

## 8. Role in decomposition

DRes is computed from the Residual Automaton before Team Automaton construction. Its states identify residual obligations at which the Team Automaton may switch from one agent copy to the next. DRes does not itself select an assignment or minimize cost; that is handled by shortest-path search over the Team Automaton.
