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
    
    /**
     * Constructs this node. The input ID must be unique throughout the graph.
     * 
     * @param id the node ID.
     */
    public DirectedGraphNode(int id) {
        this.id = id;
    }
    
    /**
     * Creates a directed arc {@code (this -> head)}.
     * 
     * @param head the head node of the arc.
     */
    public void connectTo(DirectedGraphNode head) {
        children.add(head);
        head.parents.add(this);
    }
    
    /**
     * Returns the unmodifiable view of all the child nodes of this node.
     * 
     * @return the set of child nodes of this node.
     */
    public Set<DirectedGraphNode> children() {
        return Collections.unmodifiableSet(children);
    }
    
    /**
     * Returns the unmodifiable view of all the parent nodes of this node.
     * 
     * @return the set of parent nodes of this node. 
     */
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
