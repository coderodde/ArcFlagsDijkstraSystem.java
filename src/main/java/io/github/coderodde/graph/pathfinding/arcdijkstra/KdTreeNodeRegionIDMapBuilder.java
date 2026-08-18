package io.github.coderodde.graph.pathfinding.arcdijkstra;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class implements the <b>k</b>-D tree algorithm for separating the nodes
 * into regions.
 */
public final class KdTreeNodeRegionIDMapBuilder 
        implements NodeRegionIDMapBuilder {
    
    private enum SortKey {
        SORT_X,
        SORT_Y;
    }
    
    /**
     * The minimum number of regions.
     */
    private static final int MINIMUM_REGIONS = 2;

    /**
     * Computes the node region ID map using the <b>k</b>-D tree algorithm.
     * 
     * @param nodeList       the list of nodes to consider.
     * @param coordinatesMap the graph node coordinates map.
     * @param regions        the desired number of regions.
     * 
     * @return the node region ID map. 
     */
    @Override
    public NodeRegionIDMap build(List<DirectedGraphNode> nodeList,
                                 DirectedGraphNodeCoordinatesMap coordinatesMap,
                                 int regions) {
        
        Objects.requireNonNull(nodeList, "The node list is null.");
        Objects.requireNonNull(coordinatesMap, "The coordinates map is null.");
        
        checkNodeList(nodeList);
        checkRegions(regions);
        
        // Do not mess with the original node list:
        nodeList = new ArrayList<>(nodeList);
        
        if (regions >= nodeList.size()) {
            return simpleRegionMap(nodeList, coordinatesMap);
        } else {
            return kdRegionMap(nodeList, 
                               coordinatesMap,
                               regions);
        }
    }
    
    private static NodeRegionIDMap 
        kdRegionMap(List<DirectedGraphNode> nodeList, 
                    DirectedGraphNodeCoordinatesMap coordinateMap,
                    int regions) {
        
        NodeRegionIDMap idMap = new NodeRegionIDMap(coordinateMap);
        
        kdRegionMapImpl(nodeList,
                        coordinateMap,
                        idMap,
                        0,
                        regions - 1,
                        SortKey.SORT_X);
        
        return idMap;
    }
        
    private static void 
        kdRegionMapImpl(List<DirectedGraphNode> nodeList,
                        DirectedGraphNodeCoordinatesMap coordinatesMap,
                        NodeRegionIDMap idMap,
                        int startRegionId,
                        int endRegionId,
                        SortKey sortKey) {
        
        if (startRegionId == endRegionId) {
            // Map the nodes to the region and end recursion:
            for (DirectedGraphNode node : nodeList) {
                idMap.put(node, startRegionId);
            }
            
            return;
        }
        
        // Sort the current node list:
        switch (sortKey) {
            case SortKey.SORT_X:
                sortByXkey(nodeList, coordinatesMap);
                break;
                
            case SortKey.SORT_Y:
                sortByYkey(nodeList, coordinatesMap);
                break;
                
            default:
                throw new EnumConstantNotPresentException(SortKey.class, 
                                                          sortKey.name());
        }
        
        int leftLength  = nodeList.size() / 2;
        int rightLength = nodeList.size() - leftLength;
        
        int totalRegions = endRegionId - startRegionId + 1;
        SortKey nextSortKey = sortKey == SortKey.SORT_X ? 
                              SortKey.SORT_Y :
                              SortKey.SORT_X;
        
        if (totalRegions % 2 == 0) {
            // Even split:
            int leftEndRegionId    = totalRegions / 2 - 1;
            int rightStartRegionId = totalRegions / 2;
            
            List<DirectedGraphNode> leftNodeList = 
                nodeList.subList(0, totalRegions / 2);
            
            List<DirectedGraphNode> rightNodeList = 
                nodeList.subList(totalRegions / 2, totalRegions);
            
            kdRegionMapImpl(leftNodeList,
                            coordinatesMap, 
                            idMap, 
                            0, 
                            leftEndRegionId, 
                            nextSortKey);
            
            kdRegionMapImpl(rightNodeList, 
                            coordinatesMap,
                            idMap, 
                            rightStartRegionId, 
                            endRegionId, 
                            nextSortKey);
        } else {
            // Slightly uneven split:
            int maxLength = Math.max(leftLength, rightLength);
            
            // The left sublist is one node larger than the right node list:
            List<DirectedGraphNode> leftNodeList = 
                nodeList.subList(0, maxLength);
            
            List<DirectedGraphNode> rightNodeList = 
                nodeList.subList(maxLength, nodeList.size());
            
            int leftNodeListRegions = totalRegions / 2 + 1;
            int leftEndRegionId     = leftNodeListRegions - 1;
            int rightStartRegionId  = leftEndRegionId + 1;
            
            kdRegionMapImpl(leftNodeList, 
                            coordinatesMap,
                            idMap, 
                            0, 
                            leftEndRegionId, 
                            nextSortKey);
            
            kdRegionMapImpl(rightNodeList, 
                            coordinatesMap, 
                            idMap, 
                            rightStartRegionId, 
                            endRegionId, 
                            sortKey);
        }
    }
    
    private static void 
        sortByXkey(List<DirectedGraphNode> nodeList,
                   DirectedGraphNodeCoordinatesMap coordinateMap) {
            
        nodeList.sort((u, v) -> {
            Coordinates2D uCoordinates = coordinateMap.get(u);
            Coordinates2D vCoordinates = coordinateMap.get(v);
            
            double ux = uCoordinates.x();
            double vx = vCoordinates.x();
            
            return Double.compare(ux, vx);
        });
    }
    
    private static void 
        sortByYkey(List<DirectedGraphNode> nodeList,
                   DirectedGraphNodeCoordinatesMap coordinateMap) {
            
        nodeList.sort((u, v) -> {
            Coordinates2D uCoordinates = coordinateMap.get(u);
            Coordinates2D vCoordinates = coordinateMap.get(v);
            
            double uy = uCoordinates.y();
            double vy = vCoordinates.y();
            
            return Double.compare(uy, vy);
        });
    }
    
    /**
     * Called when the each graph node constitutes a single-node region.
     * 
     * @param nodeList the list of graph nodes.
     * 
     * @return a node region ID map. 
     */
    private static NodeRegionIDMap 
        simpleRegionMap(List<DirectedGraphNode> nodeList,
                        DirectedGraphNodeCoordinatesMap coordinatesMap) {
            
        NodeRegionIDMap map = new NodeRegionIDMap(coordinatesMap);
        
        for (int i = 0; i < nodeList.size(); ++i) {
            map.put(nodeList.get(i), i);
        }
        
        return map;
    }
    
    private static void checkNodeList(List<DirectedGraphNode> nodeList) {
        if (nodeList.isEmpty()) {
            throw new IllegalArgumentException("The input node list is empty.");
        }
    }
    
    /**
     * Checks that the input number of regions is sensible.
     * 
     * @param regions the target number of regions to check.
     */
    private static void checkRegions(int regions) {
        if (regions < MINIMUM_REGIONS) {
            throw new IllegalArgumentException(
                String.format(
                    "Number or regions (%d) is too small. Must be at least %d.",
                    regions,
                    MINIMUM_REGIONS));
        }
    }
}
