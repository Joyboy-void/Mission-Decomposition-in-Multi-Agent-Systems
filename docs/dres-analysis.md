# DRes Analysis

## 1. Algorithm Intuition

The objective of DRes analysis is to identify pairs of residual automaton states that represent interchangeable task sequences.

Let:

- `Q` be the set of states of the residual automaton.
- `q₀` be its initial state.
- `Qf` be the set of final states.
- `Σ` be its alphabet.
- `δ` be its transition function.

We are interested in pairs of distinct states `q` and `q'` such that neither is initial nor final, and there exist two words `w₁, w₂ ∈ Σ*` satisfying:

```text
q₀ ──w₁──> q       and       q' ──w₁──> qf

q  ──w₂──> qf      and       q₀ ──w₂──> q'
```

where `qf ∈ Qf`.

In other words, the two words can be exchanged while still producing accepting executions:

```text
q₀ ──w₁──> q ──w₂──> qf

q₀ ──w₂──> q' ──w₁──> qf
```

### 1.1 Synchronized Product Reachability

To check these conditions efficiently, we use synchronized product-state exploration.

Consider two states `a, b ∈ Q`. We construct a conceptual product graph whose states are pairs:

```text
(a, b) ∈ Q × Q
```

For every valuation `v ∈ Σ`, there is a transition:

```text
(a, b) ──v──> (δ(a, v), δ(b, v))
```

The same valuation is applied to both automaton states. This lets us check whether a common word takes both components from their starting states to a desired pair of target states.

We denote this operation by `Reach((a, b), T)`: it determines whether a product state in target set `T` is reachable from `(a, b)`.

We do not need to explicitly construct or store the complete product graph. Instead, we perform Breadth-First Search (BFS) over pairs of states, generating successor pairs as needed.

## 2. Applying Synchronized Reachability to DRes

Let the eligible state set be:

```text
E = Q \ ({q₀} ∪ Qf)
```

Both candidate states must belong to `E`, and they must be distinct.

### 2.1 Condition 1

There must exist a word `w₁` such that:

```text
q₀ ──w₁──> q

q' ──w₁──> qf
```

where `qf ∈ Qf`.

Both executions consume the same word. Therefore, perform product reachability from:

```text
(q₀, q')
```

and search for a reachable product state of the form `(q, qf)`, where `q ∈ E` and `qf ∈ Qf`.

If such a product state is reachable, the ordered candidate pair `(q, q')` satisfies Condition 1. Denote this by `C₁(q, q')`.

### 2.2 Condition 2

There must exist a word `w₂` such that:

```text
q ──w₂──> qf

q₀ ──w₂──> q'
```

where `qf ∈ Qf`.

Perform product reachability from:

```text
(q, q₀)
```

and search for a reachable product state of the form `(qf, q')`, where `qf ∈ Qf` and `q' ∈ E`.

If such a product state is reachable, the ordered candidate pair `(q, q')` satisfies Condition 2. Denote this by `C₂(q, q')`.

### 2.3 Formal Characterization

A pair belongs to DRes if and only if both conditions hold for the same ordered pair:

```text
(q, q') ∈ DRes  iff  C₁(q, q') and C₂(q, q')
```

subject to:

```text
q, q' ∈ E  and  q ≠ q'
```

This characterization is equivalent to the original DRes condition, assuming the residual automaton has a deterministic transition function.

## 3. Optimizing the Number of BFS Executions

A straightforward implementation could perform a separate BFS for every possible candidate pair `(q, q')`. This is unnecessary: we compute the two conditions independently and reuse the results obtained during each BFS.

### 3.1 Computing Condition 1

For every noninitial, nonfinal state `q' ∈ E`, perform one BFS starting from:

```text
(q₀, q')
```

Whenever a reachable product state `(q, qf)` is encountered, where `q ∈ E`, `qf ∈ Qf`, and `q ≠ q'`, record the ordered candidate pair `(q, q')`.

A single BFS can discover multiple states `q` satisfying Condition 1 for the same fixed `q'`.

```text
S₁ = {(q, q') | C₁(q, q')}
```

### 3.2 Computing Condition 2

For every noninitial, nonfinal state `q ∈ E`, perform one BFS starting from:

```text
(q, q₀)
```

Whenever a reachable product state `(qf, q')` is encountered, where `qf ∈ Qf`, `q' ∈ E`, and `q ≠ q'`, record the ordered candidate pair `(q, q')`.

A single BFS can discover multiple states `q'` satisfying Condition 2 for the same fixed `q`.

```text
S₂ = {(q, q') | C₂(q, q')}
```

### 3.3 Combining the Results

Take the intersection:

```text
S₁ ∩ S₂ = {(q, q') | C₁(q, q') and C₂(q, q')}
```

The resulting set contains precisely the ordered candidate pairs satisfying both DRes conditions. The intersection must preserve the order of the states. Convert the pairs to the final representation only after this intersection.

## 4. Complete DRes Detection Algorithm

### 4.1 Synchronized Product BFS

```text
REACHABLE-PAIRS(automaton, start):

    visited = {start}
    queue = [start]

    while queue is not empty:

        (a, b) = queue.removeFront()

        for each valuation v in automaton.alphabet:

            aNext = delta(a, v)
            bNext = delta(b, v)

            if aNext or bNext is undefined:
                continue

            nextPair = (aNext, bNext)

            if nextPair not in visited:
                visited.add(nextPair)
                queue.addBack(nextPair)

    return visited
```

The BFS explores the synchronized product graph without explicitly constructing it. Each product state represents the current states of the two executions after consuming the same sequence of valuations.

### 4.2 Computing Condition 1

```text
FIND-CONDITION-ONE(automaton):

    S1 = empty set
    q0 = automaton.initialState
    E  = noninitial, nonfinal states

    for each qPrime in E:

        reachable = REACHABLE-PAIRS(automaton, (q0, qPrime))

        for each (a, b) in reachable:

            if a in E and b is final and a != qPrime:
                S1.add((a, qPrime))

    return S1
```

### 4.3 Computing Condition 2

```text
FIND-CONDITION-TWO(automaton):

    S2 = empty set
    q0 = automaton.initialState
    E  = noninitial, nonfinal states

    for each q in E:

        reachable = REACHABLE-PAIRS(automaton, (q, q0))

        for each (a, b) in reachable:

            if a is final and b in E and q != b:
                S2.add((q, b))

    return S2
```

### 4.4 Complete Algorithm

```text
FIND-DRES(automaton):

    F = automaton.finalStates
    q0 = automaton.initialState

    if F is empty:
        return empty set

    E = Q excluding q0 and all final states

    if |E| < 2:
        return empty set

    S1 = FIND-CONDITION-ONE(automaton)
    S2 = FIND-CONDITION-TWO(automaton)

    validPairs = S1 intersection S2

    result = empty set

    for each ordered pair (q, qPrime) in validPairs:
        result.add(canonicalize(q, qPrime))

    return result
```

Canonicalization is performed only after the intersection. This ensures that both conditions have been verified for the same ordered pair.

## 5. Correctness Consideration: Ordered Pairs

Although the final DRes result may represent a pair of states as an unordered pair, the two conditions must be evaluated using ordered pairs.

### 5.1 Why Does the Order Matter?

Recall the two conditions:

```text
C₁(q, q'):
    q₀ ──w₁──> q  and  q' ──w₁──> qf

C₂(q, q'):
    q  ──w₂──> qf and  q₀ ──w₂──> q'
```

The roles of `q` and `q'` are different in each condition:

- In Condition 1, `q` is reached from the initial state, whereas `q'` is the starting state of the second execution.
- In Condition 2, `q` is the starting state of the first execution, whereas `q'` is reached from the initial state.

Therefore, `(q, q')` and `(q', q)` must remain distinct during the computation.

### 5.2 The Incorrect Approach

Suppose candidate pairs are treated as unordered during the computation of both conditions.

Consider this situation:

- Condition 1 discovers `(q1, q2)`.
- Condition 2 discovers `(q2, q1)`.

If both pairs are canonicalized before intersection, they become identical. The intersection would incorrectly identify the pair as satisfying both conditions.

However, what was actually established is:

```text
C₁(q1, q2)

C₂(q2, q1)
```

This does not establish:

```text
C₁(q1, q2) and C₂(q1, q2)
```

The incorrect approach checks:

```text
C₁(q, q') and C₂(q', q)
```

instead of:

```text
C₁(q, q') and C₂(q, q')
```

These expressions are not equivalent in general and can introduce invalid candidate pairs.

### 5.3 Correct Approach

The implementation therefore uses two different representations:

1. **Ordered candidate pairs:** During the computation of Condition 1 and Condition 2, pairs preserve their original order. For example, `(q1, q2)` and `(q2, q1)` remain distinct.
2. **Unordered final pairs:** After intersecting the two ordered candidate sets, the resulting pairs can be canonicalized if the final result considers a pair equivalent under swapping its states.

The correct sequence is:

```text
Compute S1 using ordered pairs
Compute S2 using ordered pairs
Intersect S1 and S2
Canonicalize the resulting pairs, if required
```

This preserves the mathematical meaning of the two conditions while allowing the final result to use an unordered representation.

## 6. Edge Cases

### 6.1 Empty Valuation

An empty valuation `{}` is an ordinary alphabet symbol. It consumes one trace position, just like any other valuation.

It must not be confused with a structural epsilon transition, which consumes no valuation and does not advance the trace.

The synchronized BFS iterates over the automaton's alphabet, so an empty valuation is processed as an ordinary symbol. There are no structural epsilon transitions involved in the product reachability procedure.

### 6.2 No Final States

If the automaton has no final states, no accepting execution can be found. Therefore, the DRes result is empty.

### 6.3 Insufficient Eligible States

If fewer than two eligible states exist, no pair of distinct noninitial, nonfinal states can be formed. Therefore, the DRes result is empty.

### 6.4 Ordered Candidate Pairs

Reversed candidate pairs must remain distinct while computing and intersecting the two condition sets. Canonicalization is performed only after the intersection. This prevents pairs satisfying opposite orientations of the two conditions from being incorrectly accepted.

### 6.5 Undefined Transitions

If a transition is undefined for either component of a product state, that successor product state is skipped. This prevents the BFS from exploring invalid product transitions.

## 7. Time and Space Complexity

Let:

```text
n = |Q|
m = |Σ|
```

The synchronized product graph contains at most:

```text
|Q × Q| = n²
```

product states.

Each product state has at most `m` outgoing transitions, one for each alphabet valuation. Therefore, the time complexity of one BFS is:

```text
O(n²m)
```

We perform one BFS for every eligible state while computing Condition 1 and one BFS for every eligible state while computing Condition 2. Since the number of eligible states is at most `n`, the total number of BFS executions is `O(n)`.

Consequently, the overall time complexity is:

```text
O(n³m)
```

The auxiliary space required by each BFS is:

```text
O(n²)
```

This includes the visited set and BFS queue over the product state space.

The two candidate sets can each contain at most `O(n²)` ordered pairs. Therefore, storing both sets also requires `O(n²)` space.

Hence, the overall auxiliary space complexity remains:

```text
O(n²)
```

assuming BFS executions are performed sequentially.

## 8. Advantages of This Approach

The synchronized product reachability approach has several advantages:

1. We do not need an automaton concatenation operation.
2. We do not need a general language-intersection implementation.
3. We directly identify both states `q` and `q'` satisfying the DRes conditions.
4. We can reuse the existing residual automaton transition function and alphabet.
5. We avoid performing BFS independently for every candidate pair. Instead, we perform one BFS per fixed state, reducing the number of BFS executions from potentially `O(|Q|²)` to `O(|Q|)`.
6. We preserve correctness by maintaining ordered candidate pairs during the computation and intersection of the two conditions.
7. We can still represent the final results as unordered pairs, provided canonicalization is performed only after the intersection.
