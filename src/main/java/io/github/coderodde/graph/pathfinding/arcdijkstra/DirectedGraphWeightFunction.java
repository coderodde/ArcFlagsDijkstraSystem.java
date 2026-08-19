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
    
    public void put(DirectedGraphNode head,
                    DirectedGraphNode tail,
                    double weight) {
        checkWeight(weight);
        map.computeIfAbsent(head, ignored -> new HashMap<>()).put(tail, weight);
    }
    
    public double get(DirectedGraphNode head, DirectedGraphNode tail) {
        return map.get(head).get(tail);
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
