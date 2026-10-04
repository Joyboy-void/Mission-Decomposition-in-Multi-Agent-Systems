package ltlf.external.spot;

import ltlf.ast.Formula;
import ltlf.semantics.FormulaEquivalenceChecker;

import java.util.Objects;

public final class SpotEquivalenceChecker
        implements FormulaEquivalenceChecker{

    private final SpotFormulaPrinter printer;

    public SpotEquivalenceChecker(){
        this.printer = new SpotFormulaPrinter();
    }

    @Override
    public boolean equivalent(Formula left, Formula right){

        Objects.requireNonNull(left);
        Objects.requireNonNull(right);

        String leftSpot = printer.print(left);

        String rightSpot = printer.print(right);

        return SpotNativeBridge.equivalent(
                leftSpot,
                rightSpot
        );
    }
}