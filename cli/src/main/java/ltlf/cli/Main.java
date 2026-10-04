package ltlf.cli;

import ltlf.application.Ltlf2RaApplication;

public final class Main{

    private Main(){}

    public static void main(String[] args) {

        CommandLineApplication cli =
                new CommandLineApplication(
                    new Ltlf2RaApplication()
                );

        int exitCode = cli.run(
                args,
                System.out,
                System.err
        );

        if(exitCode != 0){
            System.exit(exitCode);
        }
    }
}
