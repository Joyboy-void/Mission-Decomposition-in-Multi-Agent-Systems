package ltlf.graphviz;

import ltlf.alphabet.Alphabet;
import ltlf.alphabet.AtomicPropositionSet;
import ltlf.alphabet.Valuation;
import ltlf.ast.AtomicProposition;
import ltlf.ast.TrueFormula;
import ltlf.automaton.ResidualAutomaton;
import ltlf.automaton.ResidualState;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResidualAutomatonDotExporterTest {

    @Test
    void exportsStatesInitialStateFinalStateAndTransitions() {
        AtomicProposition p = new AtomicProposition("p");
        AtomicPropositionSet aps = new AtomicPropositionSet(List.of(p));
        Alphabet alphabet = new Alphabet(aps);

        ResidualState q0 = new ResidualState(0, p);
        ResidualState q1 = new ResidualState(1, new TrueFormula());

        Map<ResidualState, Map<Valuation, ResidualState>> transitions =
                new LinkedHashMap<>();

        for (Valuation valuation : alphabet) {
            transitions.computeIfAbsent(q0, ignored -> new LinkedHashMap<>())
                    .put(valuation, valuation.contains(p) ? q1 : q0);
            transitions.computeIfAbsent(q1, ignored -> new LinkedHashMap<>())
                    .put(valuation, q1);
        }

        ResidualAutomaton automaton =
                new ResidualAutomaton(alphabet, List.of(q0, q1), q0, transitions);

        String dot = new ResidualAutomatonDotExporter().export(automaton);

        assertTrue(dot.contains("digraph ResidualAutomaton"));
        assertTrue(dot.contains("q0"));
        assertTrue(dot.contains("q1"));
        assertTrue(dot.contains("doublecircle"));
        assertTrue(dot.contains("__initial -> q0"));
        assertTrue(dot.contains("-> q1"));
    }
}
