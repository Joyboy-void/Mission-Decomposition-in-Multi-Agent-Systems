package machine.decomposition.cli;

import agent.model.application.AgentModelApplication;
import ltlf.application.Ltlf2RaApplication;
import product.automaton.application.ProductAutomatonApplication;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

/** Parses command-line arguments and delegates to the appropriate application. */
public final class CommandLineApplication {

    private enum Mode {
        TEXT,
        DOT,
        DRES,
        RA_RENDER,
        AGENT_RENDER,
        PRODUCT_RENDER
    }

    private record ParsedCommand(
            Mode mode,
            String formula,
            String format,
            String output,
            String agentFile)
    { }

    private final Ltlf2RaApplication raApplication;
    private final AgentModelApplication agentApplication;
    private final ProductAutomatonApplication productApplication;

    public CommandLineApplication(
            Ltlf2RaApplication raApplication,
            AgentModelApplication agentApplication,
            ProductAutomatonApplication productApplication){

        this.raApplication = Objects.requireNonNull(raApplication);
        this.agentApplication = Objects.requireNonNull(agentApplication);
        this.productApplication = Objects.requireNonNull(productApplication);
    }

    public int run(String[] args, PrintStream out, PrintStream err){
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

                case TEXT -> out.println(raApplication.execute(command.formula()));

                case DOT -> out.println(raApplication.exportDot(command.formula()));

                case DRES -> out.println(raApplication.executeDRes(command.formula()));

                case RA_RENDER -> {
                    Path output = Path.of(command.output());
                    raApplication.render(command.formula(), command.format(), output);
                    out.println("Graph written to: " + output.toAbsolutePath());
                }

                case AGENT_RENDER -> {
                    Path input = validateAgentFile(command.agentFile());
                    Path output = Path.of(command.output());
                    agentApplication.render(input, command.format(), output);
                    out.println("Graph written to: " + output.toAbsolutePath());
                }

                case PRODUCT_RENDER -> {
                    Path input = validateAgentFile(command.agentFile());
                    Path output = Path.of(command.output());
                    productApplication.render(input, command.formula(), command.format(), output);
                    out.println("Graph written to: " + output.toAbsolutePath());
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

    private ParsedCommand parse(String[] args){
        return switch (args[0]){
            case "--ra" ->
                    new ParsedCommand(
                            Mode.TEXT, joinFormula(args, 1), null, null, null);

            case "--ra-dot" ->
                    new ParsedCommand(
                            Mode.DOT, joinFormula(args, 1), null, null, null);

            case "--dres" ->
                    new ParsedCommand(
                            Mode.DRES,
                            joinFormula(args, 1),
                            null,
                            null,
                            null
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

            default ->
                    throw new IllegalArgumentException(
                            "Unknown command: " + args[0]
                                    + "\nUse --help to see available commands."
                    );
        };
    }

    private ParsedCommand parseRaRender(String[] args, String format){
        if (args.length < 3){
            throw new IllegalArgumentException(
                    "Usage: " + args[0] + " <output-file> <LTLf formula>");
        }
        return new ParsedCommand(Mode.RA_RENDER, joinFormula(args, 2), format,
                requireNonBlank(args[1], "Output file"), null);
    }

    private ParsedCommand parseAgentRender(String[] args, String format){
        if (args.length != 3){
            throw new IllegalArgumentException(
                    "Usage: " + args[0] + " <output-file> <agent-model-file>");
        }
        return new ParsedCommand(Mode.AGENT_RENDER, null, format,
                requireNonBlank(args[1], "Output file"),
                requireNonBlank(args[2], "Agent-model file"));
    }

    private ParsedCommand parseProductRender(String[] args, String format){
        if (args.length < 4){
            throw new IllegalArgumentException(
                    "Usage: " + args[0]
                            + " <output-file> <agent-model-file> <LTLf formula>");
        }
        return new ParsedCommand(Mode.PRODUCT_RENDER, joinFormula(args, 3), format,
                requireNonBlank(args[1], "Output file"),
                requireNonBlank(args[2], "Agent-model file"));
    }

    private Path validateAgentFile(String file){
        Path path = Path.of(file);
        if (!Files.isRegularFile(path)){
            throw new IllegalArgumentException(
                    "Agent-model file does not exist or is not a regular file: " + path);
        }
        if (!Files.isReadable(path)){
            throw new IllegalArgumentException("Agent-model file is not readable: " + path);
        }
        return path;
    }

    private String joinFormula(String[] args, int start){
        if (start >= args.length){
            throw new IllegalArgumentException("Formula must not be blank");
        }
        String formula = String.join(" ", Arrays.copyOfRange(args, start, args.length)).trim();
        return requireNonBlank(formula, "Formula");
    }

    private String requireNonBlank(String value, String description){
        if (value == null || value.isBlank()){
            throw new IllegalArgumentException(description + " must not be blank");
        }
        return value;
    }

    private void printUsage(PrintStream out) {
        out.println("Usage:");

        out.println("  run-cli --ra \"<LTLf formula>\"");
        out.println("  run-cli --ra-dot \"<LTLf formula>\"");
        out.println("  run-cli --ra-svg <output.svg> \"<LTLf formula>\"");
        out.println("  run-cli --ra-png <output.png> \"<LTLf formula>\"");

        out.println("  run-cli --dres \"<LTLf formula>\"");

        out.println("  run-cli --agent-svg <output.svg> <agent-model-file>");
        out.println("  run-cli --agent-png <output.png> <agent-model-file>");

        out.println("  run-cli --product-svg <output.svg> "
                + "<agent-model-file> \"<LTLf formula>\"");
        out.println("  run-cli --product-png <output.png> "
                + "<agent-model-file> \"<LTLf formula>\"");
    }
}
