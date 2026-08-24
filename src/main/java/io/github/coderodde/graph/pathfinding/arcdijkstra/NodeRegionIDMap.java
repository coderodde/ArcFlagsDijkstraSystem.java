package io.github.coderodde.graph.pathfinding.arcdijkstra;

import java.util.Collection;
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
    
    private int decimals = 3;
    
    public NodeRegionIDMap(DirectedGraphNodeCoordinatesMap coordinatesMap) {
        this.coordinatesMap = 
            Objects.requireNonNull(
                coordinatesMap, 
                "The coordinate map is null.");
    }
    
    public void setDecimals(int decimals) {
        this.decimals = Math.max(0, decimals);
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
    
    public Set<DirectedGraphNode> getBoundaryNodes() {
        Set<DirectedGraphNode> boundaryNodesSet = new HashSet<>();
        
        for (Set<DirectedGraphNode> region : inverseMap.values()) {
            loop:
            for (DirectedGraphNode node : region) {
                int nodeRegion = map.get(node);
                
                for (DirectedGraphNode child : node.children()) {
                    int childRegion = map.get(child);
                    
                    if (nodeRegion != childRegion) {
                        boundaryNodesSet.add(node);
                        continue loop;
                    }
                }
                
                for (DirectedGraphNode parent : node.parents()) {
                    int parentRegion = map.get(parent);
                    
                    if (nodeRegion != parentRegion) {
                        boundaryNodesSet.add(node);
                        continue loop;
                    }
                }
            }
        }
        
        return boundaryNodesSet;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[NodeRegionIDMap]\n");
        
        for (Map.Entry<Integer, Set<DirectedGraphNode>> e 
            : inverseMap.entrySet()) {
            
            sb.append(e.getKey())
              .append(" -> {");
            
            nodeSetToString(sb, e.getValue());
            
            sb.append("}\n");
        }
        
        return sb.append("\n").toString();
    }
    
    private void 
        nodeSetToString(StringBuilder sb, Set<DirectedGraphNode> nodes) {
        if (nodes.isEmpty()) {
            return;
        }
        
        Iterator<DirectedGraphNode> iter = nodes.iterator();
        String format = "%." + decimals + "f";
        DirectedGraphNode initialNode = iter.next();
        Coordinates2D initialNodeCoords = coordinatesMap.get(initialNode);
          
        sb.append(initialNode)
          .append("(")
          .append(String.format(format, initialNodeCoords.x()))
          .append(", ")
          .append(String.format(format, initialNodeCoords.y()))
          .append(") ");
        
        while (iter.hasNext()) {
            DirectedGraphNode node = iter.next();
            Coordinates2D coordinates = coordinatesMap.get(node);
            double x = coordinates.x();
            double y = coordinates.y();
            
            sb.append(", ")
              .append(node)
              .append("(")
              .append(String.format(format, x))
              .append(", ")
              .append(String.format(format, y))
              .append(")");
        }
    }
}
