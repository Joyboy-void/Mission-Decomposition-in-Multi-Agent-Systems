package ltlf.ast;

import java.util.Objects;

public final class AtomicProposition implements Formula {
    private final String name;

    public AtomicProposition(String name){
        this.name = Objects.requireNonNull(name);

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "An Atomic proposition's name cannot be empty"
            );
        }
    }

    public String getName(){
        return name;
    }

    @Override
    public String toString(){
        return name;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }

        if(!(obj instanceof AtomicProposition other)){
            return false;
        }

        return name.equals(other.name);
    }

    @Override
    public int hashCode(){
        return name.hashCode();
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor){
        return visitor.visitAtomicProposition(this);
    }
}
