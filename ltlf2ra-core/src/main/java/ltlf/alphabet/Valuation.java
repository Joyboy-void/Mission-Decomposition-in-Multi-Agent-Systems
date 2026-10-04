package ltlf.alphabet;

import java.util.BitSet;
import java.util.Objects;

import ltlf.ast.AtomicProposition;

public final class Valuation {

    private final AtomicPropositionSet apSet;
    private final BitSet bits;

    private Valuation(AtomicPropositionSet apSet, BitSet bits){
        this.apSet = Objects.requireNonNull(apSet);
        this.bits = (BitSet) bits.clone();
    }

    public static Valuation empty(AtomicPropositionSet apSet){

        return new Valuation(
                apSet,
                new BitSet(apSet.size())
        );
    }

    public static Valuation fromBits(AtomicPropositionSet apSet, BitSet bits){
        Objects.requireNonNull(bits);

        return new Valuation(apSet, bits);
    }

    public static Valuation of(AtomicPropositionSet apSet, AtomicProposition... propositions){

        BitSet bits = new BitSet(apSet.size());

        for(AtomicProposition ap : propositions){
            bits.set(apSet.indexOf(ap));
        }

        return new Valuation(apSet, bits);
    }

    public boolean contains(AtomicProposition proposition){
        int index = apSet.indexOf(proposition);

        return bits.get(index);
    }

    public BitSet asBitSet(){
        return (BitSet) bits.clone();
    }

    public AtomicPropositionSet getAtomicPropositionSet(){
        return apSet;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj)
            return true;

        if(!(obj instanceof Valuation other))
            return false;

        return apSet.equals(other.apSet) &&
                bits.equals(other.bits);
    }

    @Override
    public int hashCode(){
        return Objects.hash(apSet, bits);
    }

    @Override
    public String toString(){

        StringBuilder result = new StringBuilder("{");

        boolean isFirst = true;
        for(int i = 0; i < apSet.size(); i++){

            if(bits.get(i)){

                if(!isFirst)
                    result.append(", ");

                result.append(apSet.get(i));
                isFirst = false;
            }
        }

        result.append(" }");

        return result.toString();
    }
}
