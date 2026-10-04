package ltlf.cli;

import ltlf.application.Ltlf2RaApplication;

import java.util.Objects;
import java.io.PrintStream;

public final class CommandLineApplication {

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

        String formulaText = String.join(" ", args).trim();

        if(formulaText.isEmpty()){
            err.println("Formula must not be blank");
            return 1;
        }

        try{
            String result = application.execute(formulaText);

            out.println(result);
            return 0;
        }catch(RuntimeException e){
            err.println("error : " + e.getMessage());
            return 2;
        }

    }
    private void printUsage(PrintStream out){
        out.println(
                "Usage: java -jar cli.jar \"<LTLf formula>\""
        );

        out.println();

        out.println("Examples : ");

        out.println(
                "  java -jar cli.jar \"F(p)\""
        );

        out.println(
                "  java -jar cli.jar \"F G p\""
        );

        out.println(
                "  java -jar cli.jar \"p | X(q)\""
        );
    }

}
