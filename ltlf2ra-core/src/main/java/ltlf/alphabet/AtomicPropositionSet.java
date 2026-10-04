package ltlf.alphabet;


import java.util.*;

import ltlf.ast.AtomicProposition;

public final class AtomicPropositionSet {

    private final List<AtomicProposition> propositions;
    private final Map<AtomicProposition, Integer> indices;

    public AtomicPropositionSet(Collection<AtomicProposition> propositions){
        Objects.requireNonNull(propositions);

        // for deterministic indices for Atomic propositions

        List<AtomicProposition> ordered = new ArrayList<>(propositions);
        ordered.sort(
                Comparator.comparing(AtomicProposition::getName)
        );

        // check for duplicates in input

        for(int i = 1; i < ordered.size(); i++){
            if(ordered.get(i).equals(ordered.get(i - 1))){
                throw new IllegalArgumentException(
                        "Duplicate Atomic Proposition : " + ordered.get(i)
                );
            }
        }

        this.propositions = List.copyOf(ordered);

        Map<AtomicProposition, Integer> map = new HashMap<>();

        for(int i = 0; i < this.propositions.size(); i++)
            map.put(this.propositions.get(i), i);

        this.indices = Map.copyOf(map);
    }

    public int size(){
        return propositions.size();
    }

    public boolean contains(AtomicProposition proposition){
        return indices.containsKey(proposition);
    }

    public int indexOf(AtomicProposition proposition){

        Integer index = indices.get(proposition);

        if(index == null){
            throw new IllegalArgumentException(
                    "Unknown Atomic Proposition : " + proposition
            );
        }

        return index;
    }

    public AtomicProposition get(int index){
        if(index < 0 || index >= propositions.size()){
            throw new IllegalArgumentException(
                    "Invalid Atomic Proposition Index : " + index
            );
        }

        return propositions.get(index);
    }

    public List<AtomicProposition> asList(){
        return propositions;
    }

    @Override
    public String toString(){
        return propositions.toString();
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj)
            return true;

        if(!(obj instanceof AtomicPropositionSet other))
            return false;

        return propositions.equals(other.propositions);
    }

    @Override
    public int hashCode(){
        return propositions.hashCode();
    }
}
