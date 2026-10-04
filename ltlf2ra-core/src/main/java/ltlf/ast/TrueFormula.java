package ltlf.ast;

public final class TrueFormula implements Formula {

    @Override
    public String toString() {
        return "⊤";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TrueFormula;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor){
        return visitor.visitTrue(this);
    }
}
