package io.github.coderodde.graph.pathfinding.arcdijkstra;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public final class KdTreeNodeRegionIDMapBuilderTest {
    
    private final KdTreeNodeRegionIDMapBuilder builder = 
        new KdTreeNodeRegionIDMapBuilder();
    
    @Test
    public void lessOrEqualNodesThanRegions() {
        List<DirectedGraphNode> nodeList = new ArrayList<>();
        DirectedGraphNode a = new DirectedGraphNode(0);
        DirectedGraphNode b = new DirectedGraphNode(1);
        DirectedGraphNode c = new DirectedGraphNode(2);
        
        nodeList.addAll(List.of(a, b, c));
        
        DirectedGraphNodeCoordinatesMap coordMap = 
            new DirectedGraphNodeCoordinatesMap();
        
        coordMap.put(a, new Coordinates2D(1.0, 2.0));
        coordMap.put(b, new Coordinates2D(2.0, 1.5));
        coordMap.put(c, new Coordinates2D(2.0, 2.0));
        
        NodeRegionIDMap regionIdMap = builder.build(nodeList, coordMap, 4);
        
        assertEquals(3, regionIdMap.numberOfRegions());
        
        regionIdMap = builder.build(nodeList, coordMap, 3);
        
        assertEquals(3, regionIdMap.numberOfRegions());
    }
    
    @Test
    public void largerRegions() {
        List<DirectedGraphNode> nodeList = new ArrayList<>();
        DirectedGraphNodeCoordinatesMap coordMap = 
            new DirectedGraphNodeCoordinatesMap();
        
        Random random = new Random(13L);
        
        for (int i = 0; i < 10; ++i) {
            DirectedGraphNode node = new DirectedGraphNode(i);
            Coordinates2D coords = getRandomCoordinates(random);
            coordMap.put(node, coords);
            nodeList.add(node);
        }
        
        NodeRegionIDMap idMap = builder.build(nodeList, coordMap, 3);
        
        System.out.println("yeah");
    }
    
    private static final Coordinates2D getRandomCoordinates(Random random) {
        return new Coordinates2D(random.nextDouble(), random.nextDouble());
    }
}
