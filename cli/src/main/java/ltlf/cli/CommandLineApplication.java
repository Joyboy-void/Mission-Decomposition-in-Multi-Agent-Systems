package ltlf.cli;

import ltlf.application.Ltlf2RaApplication;

import java.util.Objects;
import java.io.PrintStream;
import java.nio.file.Path;

public final class CommandLineApplication {

    private enum Mode {
        TEXT,
        DOT,
        RENDER
    }

    private record ParsedCommand(
            Mode mode,
            String formula,
            String format,
            String output) {
    }

    private final Ltlf2RaApplication application;

    public CommandLineApplication(Ltlf2RaApplication application){
        this.application = Objects.requireNonNull(application);
    }

    public int run(
            String[] args,
            PrintStream out,
            PrintStream err){

        Objects.requireNonNull(args);
        Objects.requireNonNull(out);
        Objects.requireNonNull(err);

        if(args.length == 0){
            printUsage(err);
            return 1;
        }

        if(args.length == 1
                && "--help".equals(args[0])){
            printUsage(out);
            return 0;
        }

        try{
            ParsedCommand command = parse(args);

            switch(command.mode()){
                case TEXT -> out.println(application.execute(command.formula()));
                case DOT -> out.println(application.exportDot(command.formula()));
                case RENDER -> {
                    application.render(
                            command.formula(),
                            command.format(),
                            Path.of(command.output())
                    );

                    out.println("Graph Written to : " + Path.of(command.output()).toAbsolutePath());
                }
            }

            return 0;
        }catch(IllegalArgumentException e){
            err.println("error : " + e.getMessage());
            return 1;
        }catch(RuntimeException e){
            err.println("error : " + e.getMessage());
            return 2;
        }

    }
    private ParsedCommand parse(String[] args){

        if("--dot".equals(args[0])){
            return new ParsedCommand(Mode.DOT, joinFormula(args, 1), null, null);
        }

        if("--svg".equals(args[0])){
            return parseRender(args, "svg");
        }

        if("--png".equals(args[0])){
            return parseRender(args, "png");
        }

        return new ParsedCommand(Mode.TEXT, joinFormula(args, 0), null, null);
    }

    private ParsedCommand parseRender(String[] args, String format){

        if(args.length < 3){
            throw new IllegalArgumentException(
                    "Usage: "+ args[0] + " <output-file> \"<LTLf formula>\"");
        }

        String output = args[1];
        if(output.isBlank()){
            throw new IllegalArgumentException("Output file mustn't be Blank");
        }

        String formula = joinFormula(args, 2);
        return new ParsedCommand(Mode.RENDER, formula, format, output);
    }

    private String joinFormula(String[] args, int start){
        if(start >= args.length){
            throw new IllegalArgumentException("Formula Mustn't be blank");
        }

        String formula = String.join(
                " ",
                java.util.Arrays.copyOfRange(args, start, args.length)).trim();

        if(formula.isEmpty()){
            throw new IllegalArgumentException("Formula mustn't be Blank");
        }

        return formula;
    }

    private void printUsage(PrintStream out){
        out.println("Usage:");
        out.println("  java -jar cli.jar \"<LTLf formula>\"");
        out.println("  java -jar cli.jar --dot \"<LTLf formula>\"");
        out.println("  java -jar cli.jar --svg <output.svg> \"<LTLf formula>\"");
        out.println("  java -jar cli.jar --png <output.png> \"<LTLf formula>\"");

        out.println();
        out.println("Examples:");
        out.println("  java -jar cli.jar \"F(p)\"");
        out.println("  java -jar cli.jar \"F G p\"");
        out.println("  java -jar cli.jar --dot \"p | X(q & p)\"");
        out.println("  java -jar cli.jar --svg automaton.svg \"p | X(q & p)\"");
    }

}
