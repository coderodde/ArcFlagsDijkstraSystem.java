package io.github.coderodde.graph.pathfinding.arcdijkstra;

import java.util.List;

/**
 * This interface specifies the API for the algorithms assigning region IDs to 
 * the graph nodes.
 */
public sealed interface NodeRegionIDMapBuilder
    permits KdTreeNodeRegionIDMapBuilder {
    
    /**
     * Builds a node region ID map.
     * 
     * @param nodeList    the list of nodes to consider.
     * @param coordinates the graph node coordinate map.
     * @param regions     the desired number of regions.
     * 
     * @return the node region ID map.
     */
    public NodeRegionIDMap build(List<DirectedGraphNode> nodeList,
                                 DirectedGraphNodeCoordinatesMap coordinates,
                                 int regions);
}
