package agent.model.application;

import agent.model.AgentModel;
import agent.model.io.AgentModelParser;

import java.nio.file.Path;
import java.util.Objects;

public final class AgentModelFileLoader
    implements AgentModelLoader{

    private final AgentModelParser parser;

    public AgentModelFileLoader(){
        this(new AgentModelParser());
    }

    public AgentModelFileLoader(AgentModelParser parser){
        this.parser = Objects.requireNonNull(parser, "parser");
    }

    @Override
    public AgentModel load(Path input){
        return parser.parse(input);
    }
}
