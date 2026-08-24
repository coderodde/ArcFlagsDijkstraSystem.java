package io.github.coderodde.graph.pathfinding.arcdijkstra;

import java.util.Map;
import java.util.HashMap;

/**
 * This class implements directed graph weight functions. This class 
 * <b>does not allow negative weights</b>.
 */
public final class DirectedGraphWeightFunction {
    
    private final Map<DirectedGraphNode, Map<DirectedGraphNode, Double>> map = 
        new HashMap<>();
    
    public void put(DirectedGraphNode tail,
                    DirectedGraphNode head,
                    double weight) {
        checkWeight(weight);
        map.computeIfAbsent(tail, ignored -> new HashMap<>()).put(head, weight);
    }
    
    public double get(DirectedGraphNode tail, DirectedGraphNode head) {
        return map.get(tail).get(head);
    }
    
    public boolean containsArcWeight(DirectedGraphNode tail, 
                                     DirectedGraphNode head) {
        if (!map.containsKey(tail)) {
            return false;
        }
        
        return map.get(tail).containsKey(head);
    }
    
    private static void checkWeight(double weight) {
        if (Double.isNaN(weight)) {
            throw new IllegalArgumentException("The input weight is NaN.");
        }
        
        if (weight < 0.0) {
            throw new IllegalArgumentException(
                String.format("The input weight is negative: %f", weight));
        }
    }
}
