package com.jarhax.tcgatagacttagtga;

import java.util.Objects;

public class Pair<K, V> {
    
    private final K left;
    private final V right;
    
    public Pair(K left, V right) {
        this.left = left;
        this.right = right;
    }
    
    public K getLeft() {
        return left;
    }
    
    public V getRight() {
        return right;
    }
    
    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(getLeft(), pair.getLeft()) && Objects.equals(getRight(), pair.getRight());
    }
    
    @Override
    public int hashCode() {
        
        return Objects.hash(getLeft(), getRight());
    }
}
