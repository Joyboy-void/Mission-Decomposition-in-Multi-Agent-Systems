package agent.model.graphviz;

import agent.model.AgentModel;
import agent.model.AgentState;
import agent.model.AgentTransition;
import ltlf.ast.AtomicProposition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AgentModelDotExporterTest{

    @Test
    void exportsStatesLabelsInitialStatesAndCosts(){
        AgentState s0 = new AgentState("s0");
        AgentState s1 = new AgentState("s1");

        AtomicProposition p = new AtomicProposition("p");
        AtomicProposition q = new AtomicProposition("q");

        AgentModel model = new AgentModel(
                List.of(s0, s1),
                List.of(s0),
                Map.of(
                        s0, Set.of(p, q),
                        s1, Set.of(q)
                ),
                List.of(new AgentTransition(s0, s1, 5))
        );

        String dot = new AgentModelDotExporter().export(model);

        assertTrue(dot.contains("digraph AgentModel"));
        assertTrue(dot.contains("s0"));
        assertTrue(dot.contains("s1"));
        assertTrue(dot.contains("p, q"));
        assertTrue(dot.contains("label=\"5\""));
        assertTrue(dot.contains("__initial -> s_s0"));
    }
}