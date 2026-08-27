package io.github.coderodde.graph.pathfinding.arcdijkstra;

import io.github.coderodde.graph.pathfinding.arcdijkstra.demo.Demo;
import io.github.coderodde.graph.pathfinding.arcdijkstra.demo.Demo.GraphData;
import static io.github.coderodde.graph.pathfinding.arcdijkstra.demo.Demo.REGIONS;
import static io.github.coderodde.graph.pathfinding.arcdijkstra.demo.Demo.choose;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public final class ArcFlagsDijkstraSystemTest {
    
    @Test
    public void exploreGraph() {
        DirectedGraphNode a = new DirectedGraphNode(0);
        DirectedGraphNode b = new DirectedGraphNode(1);
        DirectedGraphNode c = new DirectedGraphNode(2);
        DirectedGraphNode d = new DirectedGraphNode(3);
        DirectedGraphNode e = new DirectedGraphNode(4);
        DirectedGraphNode f = new DirectedGraphNode(5); // Unreachable from 
                                                        // other 5 nodes.
        
        b.connectTo(c);
        c.connectTo(d);
        d.connectTo(b);
        
        a.connectTo(b);
        d.connectTo(e);
        
        List<DirectedGraphNode> nodeList = List.of(c);
        
        List<DirectedGraphNode> exploredNodeList = 
            ArcFlagsDijkstraSystem.explore(nodeList);
        
        assertEquals(5, exploredNodeList.size());
        
        Set<DirectedGraphNode> exploredNodeSet = 
            new HashSet<>(exploredNodeList);
        
        assertTrue(exploredNodeSet.contains(a));
        assertTrue(exploredNodeSet.contains(b));
        assertTrue(exploredNodeSet.contains(c));
        assertTrue(exploredNodeSet.contains(d));
        assertTrue(exploredNodeSet.contains(e));
        
        assertFalse(exploredNodeSet.contains(f));
    }
    
    @Test
    public void runSearch() {
        GraphData graphData = Demo.createRandomGraph();
        
        DirectedGraphNode source = choose(graphData.nodeList());
        DirectedGraphNode target = choose(graphData.nodeList());
        
        ArcFlagsDijkstraSystem dijkstraSystem = 
            new ArcFlagsDijkstraSystem(
                graphData.nodeList(), 
                graphData.weightFunction(), 
                graphData.coordinatesMap(), 
                graphData.regionIdMap(),
                REGIONS);
        
        List<DirectedGraphNode> path1 = dijkstraSystem.queryViaDijkstra(source,
                                                                        target);
        
        List<DirectedGraphNode> path2 = dijkstraSystem.queryViaAStar(source,
                                                                     target);
        
        List<DirectedGraphNode> path3 = dijkstraSystem.queryViaArcFlags(source, 
                                                                        target);
        
        assertEquals(path1, path2);
        assertEquals(path1, path3);
    }
}
