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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * This class implements a system for point-to-point shortest path queries in
 * digraphs. The main algorithm {@link ArcFlagsDijkstraSystem#queryViaArcFlags}
 * is discussed in the paper
 * <b><i>Fast Point-to-Point Shortest Path Computations with Arc-Flags</i></b>.
 */
public final class ArcFlagsDijkstraSystem {
    
    /**
     * The epsilon value for checking equality of two floating-point numbers.
     */
    private static final double E = 1E-9;

    /**
     * The minimum number of regions allowed.
     */
    private static final int MINIMUM_REGIONS = 2;
    
    /**
     * Holds the weight function.
     */
    private final DirectedGraphWeightFunction weightFunction;
    
    /**
     * Maps each directed graph node to its 2D-coordinates.
     */
    private final DirectedGraphNodeCoordinatesMap coordinatesMap;
    
    /**
     * Maps each graph node to its region number.
     */
    private final NodeRegionIDMap regionIdMap;
    
    /**
     * Maps each arc to its arc flags.
     */
    private final ArcFlagsMap arcFlagsMap = new ArcFlagsMap();
    
    /**
     * Constructs this shortest path query engine by preprocessing the input
     * graph.
     * 
     * @param nodeList       the list of graph nodes.
     * @param weightFunction the weight function.
     * @param coordinatesMap the map mapping nodes to 2D-coordinates.
     * @param regionIdMap    the map mapping nodes to region numbers.
     * @param regions        the number of regions.
     */
    public ArcFlagsDijkstraSystem(
            List<DirectedGraphNode> nodeList,
            DirectedGraphWeightFunction weightFunction,
            DirectedGraphNodeCoordinatesMap coordinatesMap,
            NodeRegionIDMap regionIdMap,
            int regions) {
        
        this(nodeList,
             weightFunction, 
             coordinatesMap, 
             regionIdMap, 
             regions, 
             false);
    }
    
    /**
     * Constructs this shortest path query engine by preprocessing the input
     * graph.
     * 
     * @param nodeList       the list of graph nodes.
     * @param weightFunction the weight function.
     * @param coordinatesMap the map mapping nodes to 2D-coordinates.
     * @param regionIdMap    the map mapping nodes to region numbers.
     * @param regions        the number of regions.
     * @param parallel       the flag for indicating whether to perform parallel
     *                       preprocessing.
     */
    public ArcFlagsDijkstraSystem(
            List<DirectedGraphNode> nodeList,
            DirectedGraphWeightFunction weightFunction,
            DirectedGraphNodeCoordinatesMap coordinatesMap,
            NodeRegionIDMap regionIdMap,
            int regions,
            boolean parallel) {
        
        Objects.requireNonNull(nodeList, "The input node list is null.");
        
        this.weightFunction = Objects.requireNonNull(weightFunction,
                               "The input weight function is null.");
        
        this.coordinatesMap =
            Objects.requireNonNull(
                coordinatesMap, 
                "The input coordinate map is null.");
        
        if (nodeList.isEmpty()) {
            throw new IllegalArgumentException("The input node list is empty.");
        }
        
        checkRegions(regions);
        
        // Explore the entire graph with all the reachable nodes:
        nodeList = explore(nodeList);
        
        if (nodeList.size() > coordinatesMap.size()) {
            throw new IllegalArgumentException(
                String.format(
                    "Not all nodes have coordinates. " + 
                        "Nodes: %d, coordinates: %d.",
                    nodeList.size(), 
                    coordinatesMap.size()));
        }
        
        this.regionIdMap = 
            Objects.requireNonNull(
                regionIdMap, 
                "The input region ID map is null.");
        
        Set<DirectedGraphNode> boundaryNodesSet = 
                regionIdMap.getBoundaryNodes();
        
        if (parallel) {
            parallelPreprocess(nodeList,
                               boundaryNodesSet.size(),
                               regions);
        } else {
            preprocess(nodeList,
                       boundaryNodesSet, 
                       regions);
        }
    }
    
    /**
     * Expands the graph represented by nodes in {@code nodeList}, i.e., returns
     * the list of all nodes reachable via child/parent link from any node in
     * {@code nodeList}.
     * 
     * @param nodeList the source node list.
     * 
     * @return the fully expanded graph.
     */
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
        
    /**
     * Returns a shortest {@code source - target} path via Dijkstra's algorithm
     * that prunes some arcs from consideration.
     * 
     * @param source the source node.
     * @param target the target node.
     * 
     * @return the shortest path from source to target. 
     */
    public List<DirectedGraphNode> queryViaArcFlags(DirectedGraphNode source,
                                                    DirectedGraphNode target) {
        Map<DirectedGraphNode, DirectedGraphNode> parentsMap = new HashMap<>();
        Map<DirectedGraphNode, Double> distancesMap          = new HashMap<>();
        DoublePriorityBinaryHeap<DirectedGraphNode> frontierHeap =
            new DoublePriorityBinaryHeap<>();
        
        int targetRegion = regionIdMap.get(target);
        
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
                ArcFlags arcFlags = arcFlagsMap.get(currentNode, childNode);
                
                if (!arcFlags.readFlag(targetRegion)) {
                    // The arc (currentNode, childNode) does not lead to the
                    // target along a shortest path; omit it.
                    continue;
                }
                
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
        
    /**
     * Returns the shortest path from the input source node to the target node
     * using traditional Dijkstra's algorithm.
     * 
     * @param source the source node.
     * @param target the target node.
     * 
     * @return the shortest node from the source node to the target node. 
     */
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
    
    /**
     * Computes the shortest path from {@code source} to {@code target} via A*.
     * 
     * @param source the source node.
     * @param target the target node.
     * 
     * @return the shortest path from the source node and to the target node. 
     */
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
    
    /**
     * Preprocesses the graph and returns the map mapping each directed arc to
     * its arc-flags.
     * 
     * @param nodeList         the list of all nodes in the graph.
     * @param boundaryNodesSet the set of boundary nodes.
     * @param weightFunction   the weight function.
     * @param regions          the number of regions.
     * 
     * @return the arc-flags map.
     */
    private void preprocess(List<DirectedGraphNode> nodeList,
                            Set<DirectedGraphNode> boundaryNodesSet,
                            int regions) {
            
        buildArcFlagsMap(nodeList, regions);
        int iteration = 1;
        long t = System.currentTimeMillis();
        
        for (DirectedGraphNode boundaryNode : boundaryNodesSet) {
            System.out.printf("Preprocessing: %d/%d.%n",
                              iteration++, 
                              boundaryNodesSet.size());
            
            preprocess(boundaryNode, regionIdMap.get(boundaryNode));
        }
        
        System.out.printf("[STATUS] Preprocessed sequentially in %d ms.%n", 
                          System.currentTimeMillis() - t);
        
        writeInternalArcFlags();
    }
        
    /**
     * Runs the reverse Dijkstra's algorithm on the boundary node 
     * {@code boundaryNode} and sets all relevant arc-flags.
     * 
     * @param boundaryNode   the boundary node from which to start the 
     *                       traversal backwards.
     * @param region         the current region number.
     */    
    private void preprocess(DirectedGraphNode boundaryNode, int region) {
        
        Map<DirectedGraphNode, Double> distancesMap = new HashMap<>();
        DoublePriorityBinaryHeap<DirectedGraphNode> frontierHeap =
            new DoublePriorityBinaryHeap<>();
        
        distancesMap.put(boundaryNode, 0.0);
        
        // The initial score of the source node is not important since it will
        // be popped first:
        frontierHeap.insert(boundaryNode, 0.0);
        
        while (!frontierHeap.isEmpty()) {
            DirectedGraphNode currentNode = frontierHeap.extractTop();
            
            double currentDistance = distancesMap.get(currentNode);
            
            for (DirectedGraphNode parentNode : currentNode.parents()) {
                
                double candidate = currentDistance
                                 + weightFunction.get(parentNode, 
                                                      currentNode);
                
                double oldDistance =
                    distancesMap.getOrDefault(parentNode, 
                                              Double.POSITIVE_INFINITY);
                
                if (candidate < oldDistance) {
                    distancesMap.put(parentNode, candidate);
                    
                    if (frontierHeap.containsDatum(parentNode)) {
                        frontierHeap.changePriority(parentNode, candidate);
                    } else {
                        frontierHeap.insert(parentNode, candidate);
                    }
                }
            }
        }
        
        for (Map.Entry<DirectedGraphNode, Double> entry
            : distancesMap.entrySet()) {
            
            DirectedGraphNode tailNode = entry.getKey();
            double tailDistance        = entry.getValue();
            
            for (DirectedGraphNode headNode : tailNode.children()) {
                
                Double headDistance = distancesMap.get(headNode);
                
                if (headDistance == null) {
                    continue;
                }
                
                double candidate = weightFunction.get(tailNode, headNode) 
                                 + headDistance;
                
                if (approximatelyEqual(tailDistance, candidate)) {
                    arcFlagsMap.get(tailNode, headNode).writeFlag(region);
                }
            }
        }
    }
        
    /**
     * Returns {@code true} if and only if the two arguments are within
     * {@code E} from each other.
     * 
     * @param a the first floating-point value.
     * @param b the second floating-point value.
     * 
     * @return {@code true} if and only if the two input numbers are 
     *         sufficiently close to each other.
     */
    private static boolean approximatelyEqual(double a, double b) {
        return Math.abs(a - b) < E;
    }
    
    /**
     * Builds all the empty arc-flags for the arc-flags map.
     * 
     * @param arcFlagsMap the target arc-flags map.
     * @param nodeList    the list of all graph nodes.
     * @param regions     the total number of regions.
     */
    private void buildArcFlagsMap(List<DirectedGraphNode> nodeList,
                                  int regions) {
        
        for (DirectedGraphNode node : nodeList) {
            for (DirectedGraphNode child : node.children()) {
                arcFlagsMap.put(node, child, new ArcFlags(regions));
            }
        }
    }
    
    /**
     * Computes the Euclidean distance between {@code node} and {code target}.
     * 
     * @param node   the first node.
     * @param target the second node.
     * 
     * @return the Euclidean distance between the first and the second node.
     */
    private double heuristicEstimate(DirectedGraphNode node, 
                                     DirectedGraphNode target) {
        Coordinates2D nodeCoords   = coordinatesMap.get(node);
        Coordinates2D targetCoords = coordinatesMap.get(target);
        return nodeCoords.euclideanDistance(targetCoords);
    }
    
    /**
     * Builds the shortest path.
     *
     * @param target     the target node.
     * @param parentsMap the map mapping each graph node to its parent node on
     *                   the shortest path.
     * 
     * @return the shortest path to target. 
     */
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
   
    /**
     * Checks the number of regions.
     * 
     * @param regions the argument.
     */    
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

    private void parallelPreprocess(
        List<DirectedGraphNode> nodeList, 
        int totalBoundaryNodes,
        int regions) {
        
        buildArcFlagsMap(nodeList, regions);

        List<PreprocessingAction> actions = new ArrayList<>(regions);
        ForkJoinPool pool = ForkJoinPool.commonPool();
        int boundaryNodeId = 1;
        
        long t = System.currentTimeMillis();
        
        for (int region = 0; region < regions; ++region) {
            Set<DirectedGraphNode> regionBoundaryNodeSet =
                regionIdMap.getBoundaryNodes(region);
            
            for (DirectedGraphNode boundaryNode : regionBoundaryNodeSet) {
                PreprocessingAction action = 
                    new PreprocessingAction(
                        boundaryNode, 
                        boundaryNodeId, 
                        totalBoundaryNodes, 
                        region);

                pool.submit(action);
                actions.add(action);
                ++boundaryNodeId;
            }
        }
        
        for (PreprocessingAction action : actions) {
            action.join();
        }
        
        System.out.printf("[STATUS] Preprocessed in parallel in %d ms.%n", 
                          System.currentTimeMillis() - t);

        writeInternalArcFlags();
    }
    
    private final class PreprocessingAction extends RecursiveAction {

        private final DirectedGraphNode boundaryNode;
        private final int boundaryNodeId;
        private final int boundaryNodes;
        private final int region;
        
        PreprocessingAction(DirectedGraphNode boundaryNode,
                            int boundaryNodeId,
                            int boundaryNodes,
                            int region) {
            
            this.boundaryNode   = boundaryNode;
            this.boundaryNodeId = boundaryNodeId;
            this.boundaryNodes  = boundaryNodes;
            this.region         = region;
        }
        
        @Override
        protected void compute() {
            System.out.printf("Preprocessing boundary node %d/%d.%n", 
                              boundaryNodeId,
                              boundaryNodes);
            
            preprocess(boundaryNode, region);
        }
    }

    private void writeInternalArcFlags() {
        for (int region = 0;
                 region < regionIdMap.numberOfRegions();
                 ++region) {
            for (DirectedGraphNode node : regionIdMap.getRegionNodes(region)) {
                for (DirectedGraphNode child : node.children()) {
                    if (regionIdMap.get(child) == region) {
                        arcFlagsMap.get(node, child).writeFlag(region);
                    }
                }
            }
        }
    }
}
