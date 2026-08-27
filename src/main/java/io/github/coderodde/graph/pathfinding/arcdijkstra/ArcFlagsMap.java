package io.github.coderodde.graph.pathfinding.arcdijkstra;

import java.util.HashMap;
import java.util.Map;

/**
 * This class maps directed graph arcs to arc-flags objects.
 */
public final class ArcFlagsMap {
    
    private final Map<DirectedGraphNode, Map<DirectedGraphNode, ArcFlags>> map =
        new HashMap<>();
    
    /**
     * Map the arc {@code tail -> head} to the arc-flags {@code arcFlags}.
     * 
     * @param tail     the tail node of the arc.
     * @param head     the head node of the arc.
     * @param arcFlags the arc-flags object.
     */
    public void put(DirectedGraphNode tail, 
                    DirectedGraphNode head, 
                    ArcFlags arcFlags) {
        map.computeIfAbsent(tail, ign -> new HashMap<>()).put(head, arcFlags);
    }
    
    /**
     * Reads the arc-flags object of the input directed arc.
     * 
     * @param tail the tail node of the arc.
     * @param head the head node of the arc.
     * 
     * @return the associated arc-flags object. 
     */
    public ArcFlags get(DirectedGraphNode tail, DirectedGraphNode head) {
        return map.get(tail).get(head);
    }
}
