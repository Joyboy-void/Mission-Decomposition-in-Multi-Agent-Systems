package ltlf.transform;

import java.util.Comparator;

import ltlf.ast.*;

/*
*   FormulaComparator only establishes a deterministic ordering for operands inside canonical Boolean
*   expressions.it does not claim anything about LTLf precedence or semantic ordering.
*
*   This Comparator provides implementation for Canonical ordering of Operators as below :
*   (Not Operator precedence)
*     True < False < Atomic < Not < Next < Eventually < Always < And < Or < Until
*
*
*
*/
public final class FormulaComparator
    implements Comparator<Formula>{

    public static final FormulaComparator INSTANCE =
                                                new FormulaComparator();

    private FormulaComparator(){

    }

    @Override
    public int compare(Formula f1, Formula f2){

        if(f1 == f2)
            return 0;

        int rank1 = rank(f1);
        int rank2 = rank(f2);

        if(rank1 != rank2){
            return Integer.compare(rank1, rank2);
        }

        // if f1, f2 belong to same formula category

        if(f1 instanceof TrueFormula)
            return 0;

        if(f1 instanceof FalseFormula)
            return 0;

        if(f1 instanceof AtomicProposition p1 && f2 instanceof AtomicProposition p2){
            return p1.getName().compareTo(
                p2.getName()
            );
        }

        if(f1 instanceof UnaryFormula u1 && f2 instanceof UnaryFormula u2){
            return compare(
                    u1.getOperand(),
                    u2.getOperand()
            );
        }

        if(f1 instanceof BinaryFormula b1 && f2 instanceof BinaryFormula b2){

            int leftComparison = compare(
                    b1.getLeft(),
                    b2.getLeft()
            );

            if(leftComparison != 0)
                return leftComparison;

            return compare(
                    b1.getRight(),
                    b2.getRight()
            );
        }

        // this should never be reached,
        throw new IllegalStateException(
                "Undefined Formula implementation : " + f1.getClass()
        );
    }

    private int rank(Formula formula){

        if(formula instanceof TrueFormula)
            return 0;

        if(formula instanceof FalseFormula)
            return 1;

        if(formula instanceof AtomicProposition)
            return 2;

        if(formula instanceof Not)
            return 3;

        if(formula instanceof Next)
            return 4;

        if(formula instanceof Eventually)
            return 5;

        if(formula instanceof Always)
            return 6;

        if(formula instanceof And)
            return 7;

        if(formula instanceof Or)
            return 8;

        if(formula instanceof Until)
            return 9;

        throw new IllegalStateException(
                "Unknown formula Implementation" + formula.getClass()
        );
    }
}
