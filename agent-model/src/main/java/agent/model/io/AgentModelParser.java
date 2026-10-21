package agent.model.io;

import agent.model.AgentModel;
import agent.model.AgentState;
import agent.model.AgentTransition;
import ltlf.ast.AtomicProposition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the plain-text agent-model format used by the CLI.
 *
 * <pre>
 * initial: A C
 *
 * states:
 *   A: p q
 *   B: q r
 *   C: p
 *   D: -
 *
 * edges:
 *   A B 5
 *   B D 2
 *   C D 3
 * </pre>
 *
 * Blank lines and comments beginning with {@code #} are ignored. State IDs
 * and atomic propositions must be identifiers beginning with a letter or
 * underscore and containing only letters, digits, underscores, or hyphens
 * (hyphens are allowed in state IDs, but not proposition names).
 */

public final class AgentModelParser {

    private static final Pattern STATE_ID =
            Pattern.compile("[A-Za-z_][A-Za-z0-9_-]*");

    private static final Pattern PROPOSITION =
            Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private static final Pattern STATE_LINE =
            Pattern.compile("^([A-Za-z_][A-Za-z0-9_-]*):\\s*(.*?)\\s*$");

    private enum Section { BEFORE_SECTIONS, STATES, EDGES }

    private record EdgeSpec(String source, String destination, int cost, int line){}

    /**
     * parses an agent model from the supplied path. Relative paths are resolved
     * by Java against the process working directory, not against the script's
     * directory.
     */

    public AgentModel parse(Path input){
        Objects.requireNonNull(input, "Agent model input-path must be non null");

        Path path = input.toAbsolutePath().normalize();

        if(!Files.isRegularFile(path)){
            throw new IllegalArgumentException(
                    "Agent-model file does not exist or is not a regular file: " + path
            );
        }

        if(!Files.isReadable(path)){
            throw new IllegalArgumentException(
                    "Agent-model file is not readable: " + path
            );
        }

        final List<String> lines;
        try{
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        }catch(IOException e){
            throw new IllegalArgumentException(
                    "Could not read agent-model file '" + path + "': " + e.getMessage(), e
            );
        }

        return parseLines(path, lines);
    }

    private AgentModel parseLines(Path path, List<String> lines){
        Section section = Section.BEFORE_SECTIONS;
        boolean initialSeen = false;
        boolean statesSeen = false;
        boolean edgesSeen = false;

        Set<String> initialIds = new LinkedHashSet<>();
        Map<String, Set<String>> labelsById = new LinkedHashMap<>();
        Map<String, Integer> stateLines = new LinkedHashMap<>();
        List<EdgeSpec> edgeSpecs = new ArrayList<>();

        for(int index = 0 ; index < lines.size(); index++){
            int lineNumber = index + 1;

            String line = stripComment(lines.get(index)).trim();
            if(line.isEmpty())
                continue;

            if(line.startsWith("initial:")){
                if(initialSeen || statesSeen || edgesSeen){
                    throw error(path, lineNumber,
                            "'initial:' must appear exactly once before 'states:'");
                }

                initialSeen = true;
                String value = line.substring("initial:".length()).trim();

                if(value.isEmpty()){
                    throw error(path, lineNumber,
                            "Declare at least one initial state after 'initial:'");
                }

                for(String id : value.split("\\s+")){
                    requireStateId(path, lineNumber, id);

                    if (!initialIds.add(id)) {
                        throw error(path, lineNumber,
                                "Initial state is listed more than once: " + id);
                    }
                }
                continue;
            }

            if(line.equals("states:")){

                if(!initialSeen){
                    throw error(path, lineNumber,
                            "'initial:' must appear before 'states:'");
                }

                if(statesSeen || edgesSeen){
                    throw error(path, lineNumber,
                            "Duplicate or misplaced 'states:' section");
                }

                statesSeen = true;
                section = Section.STATES;
                continue;
            }

            if(line.equals("edges:")){
                if(!statesSeen || edgesSeen) {
                    throw error(path, lineNumber,
                            "'edges:' must appear once, after the 'states:' section");
                }
                edgesSeen = true;
                section = Section.EDGES;
                continue;
            }

            if(section == Section.BEFORE_SECTIONS){
                throw error(path, lineNumber,
                        "Expected 'initial:' followed by the 'states:' section");
            }


            if(section == Section.STATES){
                Matcher matcher = STATE_LINE.matcher(line);
                if(!matcher.matches()){
                    throw error(path, lineNumber,
                            "Expected a state declaration like 'A: p q' or 'D: -'");
                }

                String id = matcher.group(1);
                String propositionText = matcher.group(2).trim();
                if(labelsById.containsKey(id)){
                    throw error(path, lineNumber,
                            "State is declared more than once: " + id);
                }
                stateLines.put(id, lineNumber);

                Set<String> propositions = new LinkedHashSet<>();
                if(propositionText.equals("-")){
                    // The dash is the format's explicit empty-label marker.
                }else if (propositionText.isEmpty()){
                    throw error(path, lineNumber,
                            "Use '-' to declare a state with no atomic propositions");
                }else{
                    for (String proposition : propositionText.split("\\s+")) {
                        if(!PROPOSITION.matcher(proposition).matches()){
                            throw error(path, lineNumber,
                                    "Invalid atomic proposition name: " + proposition);
                        }
                        if(!propositions.add(proposition)){
                            throw error(path, lineNumber,
                                    "Atomic proposition is repeated for state " + id + ": "
                                            + proposition);
                        }
                    }
                }
                labelsById.put(id, propositions);
                continue;
            }

            String[] fields = line.split("\\s+");
            if (fields.length != 3) {
                throw error(path, lineNumber,
                        "Expected an edge in the form '<source> <destination> <cost>'");
            }
            requireStateId(path, lineNumber, fields[0]);
            requireStateId(path, lineNumber, fields[1]);

            final int cost;
            try{
                cost = Integer.parseInt(fields[2]);
            }catch (NumberFormatException e){
                throw error(path, lineNumber,
                        "Edge cost must be a non-negative integer: "
                        + fields[2]);
            }
            if (cost < 0){
                throw error(path, lineNumber, "Edge cost cannot be negative: " + cost);
            }
            edgeSpecs.add(new EdgeSpec(fields[0], fields[1], cost, lineNumber));

        }

        if(!initialSeen)
            throw error(path, 0, "Missing required 'initial:' declaration");
        if(!statesSeen)
            throw error(path, 0, "Missing required 'states:' section");
        if (!edgesSeen)
            throw error(path, 0, "Missing required 'edges:' section");
        if (labelsById.isEmpty())
            throw error(path, 0, "The 'states:' section must declare at least one state");


        Map<String, AgentState> statesById = new LinkedHashMap<>();
        labelsById.keySet().forEach(id -> statesById.put(id, new AgentState(id)));

        for(String initialId : initialIds){
            if (!statesById.containsKey(initialId)){
                throw error(path, 0,
                        "Initial state is not declared in the 'states:' section: " + initialId);
            }
        }

        Map<AgentState, Collection<AtomicProposition>> labels = new LinkedHashMap<>();

        for (Map.Entry<String, Set<String>> entry : labelsById.entrySet()){

            AgentState state = statesById.get(entry.getKey());
            Set<AtomicProposition> propositions = new LinkedHashSet<>();

            for(String proposition : entry.getValue()){
                propositions.add(new AtomicProposition(proposition));
            }
            labels.put(state, propositions);
        }

        List<AgentTransition> transitions = new ArrayList<>();
        for(EdgeSpec edge : edgeSpecs){
            AgentState source = statesById.get(edge.source());
            AgentState destination = statesById.get(edge.destination());

            if(source == null){
                throw error(path, edge.line(),
                        "Edge source is not declared in 'states:': " + edge.source());
            }

            if(destination == null){
                throw error(path, edge.line(),
                        "Edge destination is not declared in 'states:': " + edge.destination());
            }
            transitions.add(new AgentTransition(source, destination, edge.cost()));
        }

        List<AgentState> states = new ArrayList<>(statesById.values());
        List<AgentState> initialStates = initialIds.stream().map(statesById::get).toList();
        return new AgentModel(states, initialStates, labels, transitions);
    }

    private static String stripComment(String line){
        int commentStart = line.indexOf('#');
        return commentStart >= 0 ? line.substring(0, commentStart) : line;
    }

    private static void requireStateId(Path path, int lineNumber, String id){
        if(!STATE_ID.matcher(id).matches()){
            throw error(path, lineNumber,
                    "Invalid state identifier: " + id);
        }
    }

    private static IllegalArgumentException error(Path path, int lineNumber, String message){

        String location = lineNumber > 0 ? "line " + lineNumber : "model validation";

        return new IllegalArgumentException(
                "Invalid agent model '" + path + "' (" + location + "): " + message);
    }


}
