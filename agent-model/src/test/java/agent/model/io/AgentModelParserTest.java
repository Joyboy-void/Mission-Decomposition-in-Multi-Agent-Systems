package agent.model.io;

import agent.model.AgentModel;
import agent.model.AgentState;
import agent.model.AgentTransition;
import ltlf.ast.AtomicProposition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AgentModelParserTest{

    @TempDir
    Path temporaryDirectory;

    private final AgentModelParser parser = new AgentModelParser();

    @Test
    void parsesTheChosenFormatAndMultipleInitialStates() throws IOException{
        Path file = writeModel("""
                # A simple example model
                initial: A C

                states:
                  A: p q
                  B: q r
                  C: p
                  D: -

                edges:
                  A B 5
                  B D 2
                  C D 3
                """);

        AgentModel model = parser.parse(file);

        assertEquals(4, model.getStates().size());

        assertEquals(Set.of(new AgentState("A"), new AgentState("C")),
                Set.copyOf(model.getInitialStates()));

        assertEquals(Set.of(new AtomicProposition("p"), new AtomicProposition("q")),
                model.getLabel(new AgentState("A")));

        assertEquals(Set.of(new AtomicProposition("q"), new AtomicProposition("r")),
                model.getLabel(new AgentState("B")));

        assertEquals(Set.of(new AtomicProposition("p")),
                model.getLabel(new AgentState("C")));

        assertTrue(model.getLabel(new AgentState("D")).isEmpty());

        assertEquals(3, model.getTransitions().size());

        assertTrue(model.getTransitions().contains(
                new AgentTransition(new AgentState("A"), new AgentState("B"), 5)));
    }

    @Test
    void acceptsCommentsAndEmptyEdgesSection() throws IOException{
        Path file = writeModel("""
                initial: A # starting state
                states:
                  A: -
                edges:
                # no transitions
                """);

        AgentModel model = parser.parse(file);

        assertEquals(1, model.getStates().size());
        assertTrue(model.getTransitions().isEmpty());
    }

    @Test
    void rejectsUnknownInitialState() throws IOException{
        Path file = writeModel("""
                initial: X
                states:
                  A: p
                edges:
                """);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> parser.parse(file));
        assertTrue(error.getMessage().contains("Initial state is not declared"));
    }

    @Test
    void rejectsEdgeToUnknownState() throws IOException{
        Path file = writeModel("""
                initial: A
                states:
                  A: p
                edges:
                  A Z 2
                """);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> parser.parse(file));
        assertTrue(error.getMessage().contains("Edge destination is not declared"));
    }

    @Test
    void rejectsNegativeCosts() throws IOException{
        Path file = writeModel("""
                initial: A
                states:
                  A: p
                  B: q
                edges:
                  A B -1
                """);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> parser.parse(file));
        assertTrue(error.getMessage().contains("cannot be negative"));
    }

    @Test
    void acceptsAbsoluteAndNestedPaths() throws IOException{
        Path nestedDirectory = Files.createDirectories(temporaryDirectory.resolve("nested/models"));
        Path file = nestedDirectory.resolve("model.txt");
        Files.writeString(file, "initial: A\nstates:\n  A: p\nedges:\n", StandardCharsets.UTF_8);

        assertEquals(1, parser.parse(file.toAbsolutePath()).getStates().size());
    }

    private Path writeModel(String content) throws IOException{
        Path file = temporaryDirectory.resolve("model-" + System.nanoTime() + ".txt");
        return Files.writeString(file, content, StandardCharsets.UTF_8);
    }
}
