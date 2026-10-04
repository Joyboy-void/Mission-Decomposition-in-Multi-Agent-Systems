package ltlf.transform;

import java.util.List;
import java.util.Objects;

import ltlf.ast.Formula;

// configurable formula transformation pipeline
public final class Normalizer {

    private final List<FormulaTransformer> transformers;

    public Normalizer(List<FormulaTransformer> transformers){

        this.transformers = List.copyOf(
                Objects.requireNonNull(transformers)
        );
    }

    public Formula normalize(Formula formula){

        Formula result = formula;

        for(FormulaTransformer transformer : transformers){
            result = transformer.transform(result);
        }

        return result;
    }
}
