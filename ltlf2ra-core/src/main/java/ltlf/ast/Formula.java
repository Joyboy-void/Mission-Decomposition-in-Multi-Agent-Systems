package ltlf.ast;

public interface Formula {
    public <T> T accept(FormulaVisitor<T> visitor);
}
