package ltlf.external.spot;

import ltlf.ast.Formula;
import ltlf.parser.FormulaParser;

import java.util.Objects;

public class SpotFormulaParser
    implements FormulaParser {

    @Override
    public Formula parse(String input){

        Objects.requireNonNull(input, "Input must be non-Null");

        return SpotNativeBridge.parse(input);
    }
}
