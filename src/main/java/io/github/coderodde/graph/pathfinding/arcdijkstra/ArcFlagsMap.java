package io.github.coderodde.graph.pathfinding.arcdijkstra;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public final class ArcFlagsMap {
    
    private final Map<DirectedGraphNode, Map<DirectedGraphNode, ArcFlags>> map =
        new HashMap<>();
    
    public void put(DirectedGraphNode tail, 
                    DirectedGraphNode head, 
                    ArcFlags arcFlags) {
        map.computeIfAbsent(tail, ign -> new HashMap<>()).put(head, arcFlags);
    }
    
    public ArcFlags get(DirectedGraphNode tail, DirectedGraphNode head) {
        return map.get(tail).get(head);
    }
}
