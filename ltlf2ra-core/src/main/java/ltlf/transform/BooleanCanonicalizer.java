package ltlf.transform;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import ltlf.ast.*;

/*
*   canonicalizer has a defined equivalence coverage. it does not provide semantic equivalence.
*   canonicalization itself guarantees semantic equivalence for formulas that collapse to the
*   same canonical representation.For formulas that canonicalize differently, though, we can't use
*   the difference as proof of non-equivalence. properties this canonicalizer deals with are given
*   below :
*
*
*/
public final class BooleanCanonicalizer
    implements FormulaTransformer, FormulaVisitor<Formula>{

    @Override
    public Formula transform(Formula formula) {
        return formula.accept(this);
    }

    @Override
    public Formula visitTrue(TrueFormula formula) {
        return formula;
    }

    @Override
    public Formula visitFalse(FalseFormula formula) {
        return formula;
    }

    @Override
    public Formula visitAtomicProposition(AtomicProposition formula) {
        return formula;
    }

    @Override
    public Formula visitNot(Not formula) {
        Formula operand =
                formula.getOperand().accept(this);

        return new Not(operand);
    }

    @Override
    public Formula visitNext(Next formula) {
        Formula operand =
                formula.getOperand().accept(this);

        return new Next(operand);
    }

    @Override
    public Formula visitEventually(Eventually formula) {
        Formula operand =
                formula.getOperand().accept(this);

        return new Eventually(operand);
    }

    @Override
    public Formula visitAlways(Always formula) {
        Formula operand =
                formula.getOperand().accept(this);

        return new Always(operand);
    }

    @Override
    public Formula visitAnd(And formula) {
        List<Formula> operands = new ArrayList<>();

        collectAndOperands(
                formula.getLeft(),
                operands
        );

        collectAndOperands(
                formula.getRight(),
                operands
        );

        return buildCanonicalAnd(operands);
    }

    @Override
    public Formula visitOr(Or formula) {

        List<Formula> operands = new ArrayList<>();

        collectOrOperands(
                formula.getLeft(),
                operands
        );

        collectOrOperands(
                formula.getRight(),
                operands
        );

        return buildCanonicalOr(operands);
    }

    @Override
    public Formula visitUntil(Until formula) {
        Formula left =
                formula.getLeft().accept(this);
        Formula right =
                formula.getRight().accept(this);

        return new Until(left, right);
    }

    private void collectAndOperands(Formula formula, List<Formula> operands){

        Formula canonical = formula.accept(this);

        if(canonical instanceof And and){

            collectAndOperands(
                    and.getLeft(),
                    operands
            );

            collectAndOperands(
                    and.getRight(),
                    operands
            );
        }else{
            operands.add(canonical);
        }

    }

    private Formula buildCanonicalAnd(List<Formula> operands){

        // remove duplicates
        Set<Formula> unique =
                new HashSet<>(operands);

        List<Formula> sorted =
                new ArrayList<>(unique);

        sorted.sort(FormulaComparator.INSTANCE);

        if(sorted.isEmpty()){
            throw new IllegalStateException(
                    "AND must contain atleast one operand"
            );
        }

        if(sorted.size() == 1)
            return sorted.getFirst();


        // build a deterministic left associative tree
        Formula result = sorted.getFirst();

        for(int i = 1; i < sorted.size(); i++){
            result = new And(
                    result ,
                    sorted.get(i)
            );
        }

        return result;
    }

    private void collectOrOperands(Formula formula, List<Formula> operands){

        Formula canonical =
                formula.accept(this);

        if(canonical instanceof Or or){
            collectOrOperands(
                    or.getLeft(),
                    operands
            );

            collectOrOperands(
                    or.getRight(),
                    operands
            );

        }else{
            operands.add(canonical);
        }
    }

    private Formula buildCanonicalOr(List<Formula> operands){

        //remove dup's

        Set<Formula> unique =
                new HashSet<>(operands);

        List<Formula> sorted =
                new ArrayList<>(unique);

        sorted.sort(FormulaComparator.INSTANCE);


        if(sorted.isEmpty()){
            throw new IllegalStateException(
                    "OR must contain at least one operand"
            );
        }

        if(sorted.size() == 1)
            return sorted.getFirst();

        Formula result = sorted.getFirst();

        for(int i = 1; i < sorted.size(); i++){
            result = new Or(
                    result,
                    sorted.get(i)
            );
        }

        return result;
    }
}
