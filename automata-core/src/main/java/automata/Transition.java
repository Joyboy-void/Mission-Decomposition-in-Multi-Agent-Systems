package automata;

public record Transition<S, L>(
        S source,
        L label,
        S destination
)   implements Edge<S, L> {
}
