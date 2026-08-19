package io.github.coderodde.graph.pathfinding.arcdijkstra.demo;

import io.github.coderodde.graph.pathfinding.arcdijkstra.ArcFlagsDijkstraSystem;
import io.github.coderodde.graph.pathfinding.arcdijkstra.DirectedGraphNode;
import java.util.List;
import java.util.Random;

/**
 *
 */
public final class Demo {
    
    private static final int NODES = 5000;
    private static final int ARCS = NODES * 4;
    private static final Random RANDOM = new Random(13L);
    
    public static void main(String[] args) {
        System.out.printf(
            "=== %s demo ===%n", 
            ArcFlagsDijkstraSystem.class.getSimpleName());
    }
    
    private static final class GraphData {
        List<DirectedGraphNode> nodeList;
        
        
    }
}
