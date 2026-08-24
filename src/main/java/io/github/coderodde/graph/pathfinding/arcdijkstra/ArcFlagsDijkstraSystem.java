package io.github.coderodde.graph.pathfinding.arcdijkstra;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 */
public final class ArcFlagsDijkstraSystem {

    /**
     * The minimum number of regions allowed.
     */
    private static final int MINIMUM_REGIONS = 2;
    
    private final DirectedGraphWeightFunction weightFunction;
    private final DirectedGraphNodeCoordinatesMap coordinatesMap;
    private final NodeRegionIDMap regionIdMap;
    
    public ArcFlagsDijkstraSystem(
            List<DirectedGraphNode> nodeList,
            DirectedGraphWeightFunction weightFunction,
            DirectedGraphNodeCoordinatesMap coordinatesMap,
            int regions) {
        
        Objects.requireNonNull(nodeList, "The input node list is null.");
        
        Objects.requireNonNull(weightFunction,
                               "The input weight function is null.");
        
        this.weightFunction = weightFunction;
        
        this.coordinatesMap =
            Objects.requireNonNull(
                coordinatesMap, 
                "The input coordinate map is null.");
        
        this.regionIdMap = new NodeRegionIDMap(coordinatesMap);
        
        if (nodeList.isEmpty()) {
            throw new IllegalArgumentException("The input node list is empty.");
        }
        
        checkRegions(regions);
        
        // Explore the entire graph with all the reachable nodes:
        nodeList = explore(nodeList);
        
        if (nodeList.size() != coordinatesMap.size()) {
            throw new IllegalArgumentException();
        }
    }
    
    public static List<DirectedGraphNode> 
        explore(List<DirectedGraphNode> nodeList) {
        
        Deque<DirectedGraphNode> frontier = 
            new ArrayDeque<>(new HashSet<>(nodeList));
        
        Set<DirectedGraphNode> visited = new HashSet<>();
        
        while (!frontier.isEmpty()) {
            DirectedGraphNode current = frontier.removeFirst();
            visited.add(current);
            
            for (DirectedGraphNode child : current.children()) {
                if (!visited.contains(child)) {
                    frontier.addLast(child);
                }
            }
            
            for (DirectedGraphNode parent : current.parents()) {
                if (!visited.contains(parent)) {
                    frontier.addLast(parent);
                }
            }
        }
        
        return new ArrayList<>(visited);
    }
        
    public List<DirectedGraphNode> queryViaDijkstra(DirectedGraphNode source,
                                                    DirectedGraphNode target) {
        
        Map<DirectedGraphNode, DirectedGraphNode> parentsMap = new HashMap<>();
        Map<DirectedGraphNode, Double> distancesMap          = new HashMap<>();
        DoublePriorityBinaryHeap<DirectedGraphNode> frontierHeap =
            new DoublePriorityBinaryHeap<>();
        
        parentsMap.put(source, null);
        distancesMap.put(source, 0.0);
        
        // The initial score of the source node is not important since it will
        // be popped first:
        frontierHeap.insert(source, 0.0);
        
        while (!frontierHeap.isEmpty()) {
            DirectedGraphNode currentNode = frontierHeap.extractTop();
            
            if (currentNode.equals(target)) {
                return tracebackPath(target, parentsMap);
            }
            
            for (DirectedGraphNode childNode : currentNode.children()) {
                if (!distancesMap.containsKey(childNode)) {
                    double distance = distancesMap.get(currentNode) 
                                    + weightFunction.get(currentNode, 
                                                         childNode);
                    
                    distancesMap.put(childNode, distance);
                    parentsMap.put(childNode, currentNode);
                    frontierHeap.insert(childNode, distance);
                } else {
                    double distance = distancesMap.get(currentNode) 
                                    + weightFunction.get(currentNode,
                                                         childNode);
                    
                    if (distance < distancesMap.get(childNode)) {
                        distancesMap.put(childNode, distance);
                        parentsMap.put(childNode, currentNode);
                        frontierHeap.changePriority(childNode, distance);
                    }
                }
            }
        }
        
        return List.of();
    }
    
    public List<DirectedGraphNode> queryViaAStar(DirectedGraphNode source,
                                                 DirectedGraphNode target) {
        Map<DirectedGraphNode, DirectedGraphNode> parentsMap = new HashMap<>();
        Map<DirectedGraphNode, Double> distancesMap          = new HashMap<>();
        DoublePriorityBinaryHeap<DirectedGraphNode> frontierHeap =
            new DoublePriorityBinaryHeap<>();
        
        parentsMap.put(source, null);
        distancesMap.put(source, 0.0);
        
        // The initial score of the source node is not important since it will
        // be popped first:
        frontierHeap.insert(source, 0.0);
        
        while (!frontierHeap.isEmpty()) {
            DirectedGraphNode currentNode = frontierHeap.extractTop();
            
            if (currentNode.equals(target)) {
                return tracebackPath(target, parentsMap);
            }
            
            for (DirectedGraphNode childNode : currentNode.children()) {
                if (!distancesMap.containsKey(childNode)) {
                    double distance = distancesMap.get(currentNode) 
                                    + weightFunction.get(currentNode, 
                                                         childNode);
                    
                    distancesMap.put(childNode, distance);
                    parentsMap.put(childNode, currentNode);
                    frontierHeap.insert(childNode, 
                                        distance + heuristicEstimate(childNode,
                                                                     target));
                } else {
                    double distance = distancesMap.get(currentNode) 
                                    + weightFunction.get(currentNode,
                                                         childNode);
                    
                    if (distance < distancesMap.get(childNode)) {
                        distancesMap.put(childNode, distance);
                        parentsMap.put(childNode, currentNode);
                        
                        frontierHeap.changePriority(
                            childNode, 
                            distance + heuristicEstimate(childNode, target));
                    }
                }
            }
        }
        
        return List.of();
    }
    
    private double heuristicEstimate(DirectedGraphNode node, 
                                     DirectedGraphNode target) {
        Coordinates2D nodeCoords   = coordinatesMap.get(node);
        Coordinates2D targetCoords = coordinatesMap.get(target);
        return nodeCoords.euclideanDistance(targetCoords);
    }
    
    private static List<DirectedGraphNode> 
        tracebackPath(DirectedGraphNode target, 
                      Map<DirectedGraphNode, DirectedGraphNode> parentsMap) {
        DirectedGraphNode current = target;
        List<DirectedGraphNode> path = new ArrayList<>();

        while (current != null) {
            path.addLast(current);
            current = parentsMap.get(current);
        } 

        Collections.reverse(path);
        return path;
    }
    
    private static void checkRegions(int regions) {
        if (regions < MINIMUM_REGIONS) {
            throw new IllegalArgumentException(
                String.format(
                    "The input number of regions (%d) is too small. " + 
                        "Must be at least %d.", 
                    regions, 
                    MINIMUM_REGIONS));
        }
    }
    
    private final record HeapNode(DirectedGraphNode node, double score) {
        
    }
}
