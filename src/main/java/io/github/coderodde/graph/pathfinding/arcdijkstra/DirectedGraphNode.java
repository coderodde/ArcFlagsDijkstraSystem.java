package io.github.coderodde.graph.pathfinding.arcdijkstra;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class implements the directed graph node.
 */
public final class DirectedGraphNode {
    
    private final int id;
    private final Set<DirectedGraphNode> children = new HashSet<>();
    private final Set<DirectedGraphNode> parents  = new HashSet<>();
    
    public DirectedGraphNode(int id) {
        this.id = id;
    }
    
    public void connectTo(DirectedGraphNode tail) {
        children.add(tail);
        tail.parents.add(this);
    }
    
    public Set<DirectedGraphNode> children() {
        return Collections.unmodifiableSet(children);
    }
    
    public Set<DirectedGraphNode> parents() {
        return Collections.unmodifiableSet(parents);
    }
    
    /**
     * Returns the hash code of this node.
     * 
     * @return the hash code of this node.
     */
    @Override
    public int hashCode() {
        return id;
    }

    /**
     * Returns {@code true} if and only if this node equals the argument node.
     * 
     * @param obj the object to test against.
     * 
     * @return a {@code boolean} flag indicating wether this and the argument 
     *         nodes are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        DirectedGraphNode other = (DirectedGraphNode) obj;
        return this.id == other.id;
    }
    
    @Override
    public String toString() {
        return "[Node %d]".formatted(id);
    }
}
