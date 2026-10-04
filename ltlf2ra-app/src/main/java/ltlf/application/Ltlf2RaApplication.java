package ltlf.application;

import java.util.Objects;

public class Ltlf2RaApplication {


    private final Ltlf2RaService service;
    private final ResidualAutomatonFormatter formatter;

    public Ltlf2RaApplication(){
        this(
                new Ltlf2RaService(),
                new ResidualAutomatonFormatter()
        );
    }

    public Ltlf2RaApplication(Ltlf2RaService service, ResidualAutomatonFormatter formatter){
        this.service = Objects.requireNonNull(service);
        this.formatter = Objects.requireNonNull(formatter);
    }

    public String execute(String formulaText){
        return formatter.format(
            service.build(formulaText)
        );
    }
}
