package machine.decomposition.cli;

import agent.model.application.AgentModelApplication;
import decomposition.application.DecompositionApplication;
import decomposition.application.DecompositionFormatter;
import decomposition.application.DecompositionImageExporter;
import decomposition.application.DecompositionPipelineResult;
import graphviz.core.GraphvizRenderer;
import ltlf.application.Ltlf2RaApplication;
import product.automaton.application.ProductAutomatonApplication;
import team.automaton.TeamAutomaton;
import team.automaton.graphviz.TeamAutomatonDotExporter;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class CommandLineApplication {

    private enum Mode {
        TEXT,
        DOT,
        DRES,
        RA_RENDER,
        AGENT_RENDER,
        PRODUCT_RENDER,
        TEAM_RENDER,
        DECOMPOSE,
        DECOMPOSE_IMAGES
    }

    private record ParsedCommand(
            Mode mode,
            String formula,
            String format,
            String output,
            String agentFile,
            int numberOfAgents
    ) {}

    private static final Path INTERMEDIATE_OUTPUT_DIRECTORY =
            Path.of("output", "intermediate");

    private final Ltlf2RaApplication raApplication;
    private final AgentModelApplication agentApplication;
    private final ProductAutomatonApplication productApplication;
    private final DecompositionApplication decompositionApplication;
    private final DecompositionFormatter decompositionFormatter;
    private final GraphvizRenderer graphvizRenderer;
    private final TeamAutomatonDotExporter teamAutomatonExporter;
    private final DecompositionImageExporter decompositionImageExporter;

    public CommandLineApplication(
            Ltlf2RaApplication raApplication,
            AgentModelApplication agentApplication,
            ProductAutomatonApplication productApplication,
            DecompositionApplication decompositionApplication,
            DecompositionFormatter decompositionFormatter,
            GraphvizRenderer graphvizRenderer,
            TeamAutomatonDotExporter teamAutomatonExporter,
            DecompositionImageExporter decompositionImageExporter
    ) {
        this.raApplication = Objects.requireNonNull(raApplication);
        this.agentApplication = Objects.requireNonNull(agentApplication);
        this.productApplication = Objects.requireNonNull(productApplication);
        this.decompositionApplication =
                Objects.requireNonNull(decompositionApplication);
        this.decompositionFormatter =
                Objects.requireNonNull(decompositionFormatter);
        this.graphvizRenderer = Objects.requireNonNull(graphvizRenderer);
        this.teamAutomatonExporter =
                Objects.requireNonNull(teamAutomatonExporter);
        this.decompositionImageExporter =
                Objects.requireNonNull(decompositionImageExporter);
    }

    public int run(String[] args, PrintStream out, PrintStream err) {
        Objects.requireNonNull(args);
        Objects.requireNonNull(out);
        Objects.requireNonNull(err);

        if (args.length == 0) {
            printUsage(err);
            return 1;
        }
        if (args.length == 1 && "--help".equals(args[0])) {
            printUsage(out);
            return 0;
        }

        try{
            ParsedCommand command = parse(args);
            switch (command.mode()){

                case TEXT ->
                        out.println(
                                raApplication.execute(command.formula())
                        );

                case DOT ->
                        out.println(
                                raApplication.exportDot(command.formula())
                        );

                case DRES ->
                        out.println(
                                raApplication.executeDRes(command.formula())
                        );

                case RA_RENDER -> {
                    Path output = Path.of(command.output());

                    raApplication.render(
                            command.formula(),
                            command.format(),
                            output
                    );

                    out.println(
                            "Graph written to: " + output.toAbsolutePath()
                    );
                }

                case AGENT_RENDER -> {
                    Path input = validateAgentFile(command.agentFile());
                    Path output = Path.of(command.output());

                    agentApplication.render(
                            input,
                            command.format(),
                            output
                    );

                    out.println(
                            "Graph written to: " + output.toAbsolutePath()
                    );
                }

                case PRODUCT_RENDER -> {
                    Path input = validateAgentFile(command.agentFile());
                    Path output = Path.of(command.output());

                    productApplication.render(
                            input,
                            command.formula(),
                            command.format(),
                            output
                    );

                    out.println(
                            "Graph written to: " + output.toAbsolutePath()
                    );
                }

                case TEAM_RENDER -> {
                    Path input = validateAgentFile(command.agentFile());
                    Path output = Path.of(command.output());

                    TeamAutomaton team =
                            decompositionApplication.buildTeamAutomaton(
                                    input,
                                    command.formula(),
                                    command.numberOfAgents()
                            );

                    graphvizRenderer.renderDotWithEngine(
                            "neato",
                            teamAutomatonExporter.export(team),
                            command.format(),
                            output
                    );

                    out.println(
                            "Team Automaton graph written to: "
                                    + output.toAbsolutePath()
                    );
                }

                case DECOMPOSE -> {
                    Path input = validateAgentFile(command.agentFile());

                    DecompositionPipelineResult result =
                            decompositionApplication.execute(
                                    input,
                                    command.formula(),
                                    command.numberOfAgents()
                            );

                    out.println(
                            decompositionFormatter.format(result)
                    );
                }

                case DECOMPOSE_IMAGES -> {
                    Path input = validateAgentFile(command.agentFile());

                    DecompositionPipelineResult result =
                            decompositionApplication.execute(
                                    input,
                                    command.formula(),
                                    command.numberOfAgents()
                            );

                    String runHash = createRunHash(
                            input,
                            command.formula(),
                            command.numberOfAgents()
                    );

                    String suffix = "-" + runHash;

                    decompositionImageExporter.exportAll(
                            result,
                            INTERMEDIATE_OUTPUT_DIRECTORY,
                            suffix
                    );

                    out.println(decompositionFormatter.format(result));

                    out.println();
                    out.println(
                            "Intermediate automata images exported to: "
                                    + INTERMEDIATE_OUTPUT_DIRECTORY.toAbsolutePath()
                    );
                    out.println("Run identifier: " + runHash);
                }
            }

            return 0;

        } catch (IllegalArgumentException e){
            err.println("Error: " + e.getMessage());
            return 1;

        } catch (UnsupportedOperationException e){
            err.println("Not configured: " + e.getMessage());
            return 2;

        } catch (RuntimeException e){
            err.println("Error: " + e.getMessage());
            return 2;
        }
    }

    /**
     * Generates a deterministic identifier for a decomposition run.
     *
     * The hash depends on:
     * 1. Agent model file contents.
     * 2. LTLf formula.
     * 3. Number of agents.
     *
     * Only the first 12 hexadecimal characters are used.
     */
    private String createRunHash(
            Path agentModelFile,
            String formula,
            int numberOfAgents
    ){
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            //include the complete Agent Model file contents.
            byte[] modelContents = Files.readAllBytes(agentModelFile);

            digest.update(ByteBuffer.allocate(Integer.BYTES)
                    .putInt(modelContents.length)
                    .array());
            digest.update(modelContents);

            // include the formula.
            byte[] formulaBytes =
                    formula.trim().getBytes(StandardCharsets.UTF_8);

            digest.update(ByteBuffer.allocate(Integer.BYTES)
                    .putInt(formulaBytes.length)
                    .array());
            digest.update(formulaBytes);

            // include the number of agents.
            digest.update(ByteBuffer.allocate(Integer.BYTES)
                    .putInt(numberOfAgents)
                    .array());

            return HexFormat.of()
                    .formatHex(digest.digest())
                    .substring(0, 12);

        }catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException(
                    "Could not generate decomposition run identifier.",
                    e
            );
        }
    }

    private ParsedCommand parse(String[] args){
        return switch (args[0]){
            case "--ra" ->
                    new ParsedCommand(
                            Mode.TEXT,
                            joinFormula(args, 1),
                            null, null, null, 0
                    );

            case "--ra-dot" ->
                    new ParsedCommand(
                            Mode.DOT,
                            joinFormula(args, 1),
                            null, null, null, 0
                    );

            case "--dres" ->
                    new ParsedCommand(
                            Mode.DRES,
                            joinFormula(args, 1),
                            null, null, null, 0
                    );

            case "--ra-svg" ->
                    parseRaRender(args, "svg");

            case "--ra-png" ->
                    parseRaRender(args, "png");

            case "--agent-svg" ->
                    parseAgentRender(args, "svg");

            case "--agent-png" ->
                    parseAgentRender(args, "png");

            case "--product-svg" ->
                    parseProductRender(args, "svg");

            case "--product-png" ->
                    parseProductRender(args, "png");

            case "--team-svg" ->
                    parseTeamRender(args, "svg");

            case "--team-png" ->
                    parseTeamRender(args, "png");

            case "--decom" ->
                    parseDecomposition(args, Mode.DECOMPOSE);

            case "--decom-img" ->
                    parseDecomposition(args, Mode.DECOMPOSE_IMAGES);

            default ->
                    throw new IllegalArgumentException(
                            "Unknown command: " + args[0]
                                    + "\nUse --help to see available commands."
                    );
        };
    }

    /**
     * parses:
     *
     * --decom <agent-model-file> "<LTLf formula>" <number-of-agents>
     *
     * --decom-img <agent-model-file> "<LTLf formula>" <number-of-agents>
     */
    private ParsedCommand parseDecomposition(
            String[] args,
            Mode mode
    ) {
        if (args.length != 4) {
            throw new IllegalArgumentException(
                    "Usage: " + args[0]
                            + " <agent-model-file> "
                            + "\"<LTLf formula>\" <number-of-agents>"
            );
        }

        String agentFile =
                requireNonBlank(args[1], "Agent-model file");

        String formula =
                requireNonBlank(args[2], "Formula");

        int numberOfAgents =
                parseNumberOfAgents(args[3]);

        return new ParsedCommand(
                mode,
                formula,
                null,
                null,
                agentFile,
                numberOfAgents
        );
    }

    /**
     * parses:
     *
     * --ra-svg <output-file> "<LTLf formula>"
     * --ra-png <output-file> "<LTLf formula>"
     */
    private ParsedCommand parseRaRender(
            String[] args,
            String format
    ) {
        if (args.length < 3){
            throw new IllegalArgumentException(
                    "Usage: " + args[0]
                            + " <output-file> \"<LTLf formula>\""
            );
        }

        return new ParsedCommand(
                Mode.RA_RENDER,
                joinFormula(args, 2),
                format,
                requireNonBlank(args[1], "Output file"),
                null,
                0
        );
    }

    /**
     * parses:
     *
     * --agent-svg <output-file> <agent-model-file>
     * --agent-png <output-file> <agent-model-file>
     */
    private ParsedCommand parseAgentRender(
            String[] args,
            String format
    ) {
        if (args.length != 3) {
            throw new IllegalArgumentException(
                    "Usage: " + args[0]
                            + " <output-file> <agent-model-file>"
            );
        }

        return new ParsedCommand(
                Mode.AGENT_RENDER,
                null,
                format,
                requireNonBlank(args[1], "Output file"),
                requireNonBlank(args[2], "Agent-model file"),
                0
        );
    }

    /**
     * parses:
     *
     * --product-svg <output-file> <agent-model-file> "<LTLf formula>"
     * --product-png <output-file> <agent-model-file> "<LTLf formula>"
     */
    private ParsedCommand parseProductRender(
            String[] args,
            String format
    ) {
        if (args.length < 4) {
            throw new IllegalArgumentException(
                    "Usage: " + args[0]
                            + " <output-file> <agent-model-file> "
                            + "\"<LTLf formula>\""
            );
        }

        return new ParsedCommand(
                Mode.PRODUCT_RENDER,
                joinFormula(args, 3),
                format,
                requireNonBlank(args[1], "Output file"),
                requireNonBlank(args[2], "Agent-model file"),
                0
        );
    }

    /**
     * parses:
     *
     * --team-svg <output-file> <agent-model-file>
     *            "<LTLf formula>" <number-of-agents>
     *
     * --team-png <output-file> <agent-model-file>
     *            "<LTLf formula>" <number-of-agents>
     */
    private ParsedCommand parseTeamRender(
            String[] args,
            String format
    ) {
        if (args.length != 5){
            throw new IllegalArgumentException(
                    "Usage: " + args[0]
                            + " <output-file> <agent-model-file> "
                            + "\"<LTLf formula>\" <number-of-agents>"
            );
        }

        return new ParsedCommand(
                Mode.TEAM_RENDER,
                requireNonBlank(args[3], "Formula"),
                format,
                requireNonBlank(args[1], "Output file"),
                requireNonBlank(args[2], "Agent-model file"),
                parseNumberOfAgents(args[4])
        );
    }

    private int parseNumberOfAgents(String value) {
        int numberOfAgents;

        try{
            numberOfAgents = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Number of agents must be a valid integer: " + value
            );
        }

        if (numberOfAgents < 1){
            throw new IllegalArgumentException(
                    "Number of agents must be at least 1."
            );
        }

        return numberOfAgents;
    }

    private Path validateAgentFile(String file){
        Path path = Path.of(file);
        if (!Files.isRegularFile(path)){
            throw new IllegalArgumentException(
                    "Agent-model file does not exist or is not a regular file: "
                            + path
            );
        }
        if (!Files.isReadable(path)){
            throw new IllegalArgumentException(
                    "Agent-model file is not readable: " + path
            );
        }

        return path;
    }

    /**
     * joins remaining arguments into an LTLf formula.
     * supports quoted and unquoted formulas.
     */
    private String joinFormula(String[] args, int start){
        if (start >= args.length){
            throw new IllegalArgumentException(
                    "Formula must not be blank"
            );
        }

        String formula = String.join(
                " ",
                Arrays.copyOfRange(args, start, args.length)
        ).trim();

        return requireNonBlank(formula, "Formula");
    }

    private String requireNonBlank(
            String value,
            String description
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    description + " must not be blank"
            );
        }
        return value;
    }

    private void printUsage(PrintStream out) {
        out.println("Usage:");

        out.println();
        out.println("Residual Automaton:");
        out.println("  run-cli --ra \"<LTLf formula>\"");
        out.println("  run-cli --ra-dot \"<LTLf formula>\"");
        out.println(
                "  run-cli --ra-svg <output.svg> \"<LTLf formula>\""
        );
        out.println(
                "  run-cli --ra-png <output.png> \"<LTLf formula>\""
        );

        out.println();
        out.println("DRes:");
        out.println("  run-cli --dres \"<LTLf formula>\"");

        out.println();
        out.println("Agent Model:");
        out.println(
                "  run-cli --agent-svg <output.svg> <agent-model-file>"
        );
        out.println(
                "  run-cli --agent-png <output.png> <agent-model-file>"
        );

        out.println();
        out.println("Product Automaton:");
        out.println(
                "  run-cli --product-svg <output.svg> "
                        + "<agent-model-file> \"<LTLf formula>\""
        );
        out.println(
                "  run-cli --product-png <output.png> "
                        + "<agent-model-file> \"<LTLf formula>\""
        );

        out.println();
        out.println("Team Automaton:");
        out.println(
                "  run-cli --team-svg <output.svg> "
                        + "<agent-model-file> \"<LTLf formula>\" "
                        + "<number-of-agents>"
        );
        out.println(
                "  run-cli --team-png <output.png> "
                        + "<agent-model-file> \"<LTLf formula>\" "
                        + "<number-of-agents>"
        );

        out.println();
        out.println("Decomposition:");
        out.println(
                "  run-cli --decom <agent-model-file> "
                        + "\"<LTLf formula>\" <number-of-agents>"
        );
        out.println(
                "  run-cli --decom-img <agent-model-file> "
                        + "\"<LTLf formula>\" <number-of-agents>"
        );

        out.println();
        out.println("Other:");
        out.println("  run-cli --help");
    }
}
