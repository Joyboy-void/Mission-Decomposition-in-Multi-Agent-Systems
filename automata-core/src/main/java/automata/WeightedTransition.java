package automata;

public record WeightedTransition<S, L, W>(
        S source,
        L label,
        S destination,
        W weight
) implements Edge<S, L> {
}
