package io.github.coderodde.graph.pathfinding.arcdijkstra;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class implements a map mapping graph nodes to their respective region
 * IDs.
 */
public final class NodeRegionIDMap {
    
    private final Map<DirectedGraphNode, Integer> map = new HashMap<>();
    private final Set<Integer> regionIdSet            = new HashSet<>();
    
    public void put(DirectedGraphNode node, Integer regionId) {
        map.put(node, regionId);
        regionIdSet.add(regionId);
    }
    
    public int get(DirectedGraphNode node) {
        return map.get(node);
    }
    
    public int numberOfRegions() {
        return regionIdSet.size();
    }
}
