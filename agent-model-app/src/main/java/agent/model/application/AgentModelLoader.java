package agent.model.application;

import agent.model.AgentModel;

import java.nio.file.Path;

/** Loads an agent model from a file. The concrete format/parser can be added later. */
@FunctionalInterface
public interface AgentModelLoader {
    AgentModel load(Path input);
}
