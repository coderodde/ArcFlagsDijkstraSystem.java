package io.github.coderodde.graph.pathfinding.arcdijkstra;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This class implements a map mapping graph nodes to their respective region
 * IDs.
 */
public final class NodeRegionIDMap {
    
    private final Map<DirectedGraphNode, Integer> map = new HashMap<>();
    private final Set<Integer> regionIdSet            = new HashSet<>();
    
    private final Map<Integer, Set<DirectedGraphNode>> inverseMap = 
            new HashMap<>();
    
    private final DirectedGraphNodeCoordinatesMap coordinatesMap;
    
    public NodeRegionIDMap(DirectedGraphNodeCoordinatesMap coordinatesMap) {
        this.coordinatesMap = 
            Objects.requireNonNull(
                coordinatesMap, 
                "The coordinate map is null.");
    }
    
    public void put(DirectedGraphNode node, Integer regionId) {
        map.put(node, regionId);
        regionIdSet.add(regionId);
        
        if (!inverseMap.containsKey(regionId)) {
            inverseMap.put(regionId, new HashSet<>());
        }
        
        inverseMap.get(regionId).add(node);
    }
    
    public int get(DirectedGraphNode node) {
        return map.get(node);
    }
    
    public int numberOfRegions() {
        return regionIdSet.size();
    }
    
    public Set<DirectedGraphNode> getRegionNodes(int region) {
        return Collections.unmodifiableSet(inverseMap.get(region));
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[NodeRegionIDMap ")
          .append(this)
          .append("]\n");
        
        for (Map.Entry<Integer, Set<DirectedGraphNode>> e 
            : inverseMap.entrySet()) {
            
            sb.append(e.getKey())
              .append(" -> {");
            
            nodeSetToString(sb, e.getValue());
            
            sb.append("}\n");
        }
        
        return sb.append("\n").toString();
    }
    
    private static void 
        nodeSetToString(StringBuilder sb, Set<DirectedGraphNode> nodes) {
        if (nodes.isEmpty()) {
            return;
        }
        
        Iterator<DirectedGraphNode> iter = nodes.iterator();
        
        sb.append(iter.next());
        
        while (iter.hasNext()) {
            sb.append(", ").append(iter.next());
        }
    }
}
