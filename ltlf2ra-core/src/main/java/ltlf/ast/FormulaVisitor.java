package ltlf.ast;

public interface FormulaVisitor<T> {

    T visitTrue(TrueFormula formula);

    T visitFalse(FalseFormula formula);

    T visitAtomicProposition(AtomicProposition formula);

    T visitNot(Not formula);

    T visitNext(Next formula);

    T visitEventually(Eventually formula);

    T visitAlways(Always formula);

    T visitAnd(And formula);

    T visitOr(Or formula);

    T visitUntil(Until formula);
}
