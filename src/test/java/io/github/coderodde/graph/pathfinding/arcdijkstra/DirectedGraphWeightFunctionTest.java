package io.github.coderodde.graph.pathfinding.arcdijkstra;

import org.junit.Test;
import static org.junit.Assert.*;

public final class DirectedGraphWeightFunctionTest {
    
    private static double E = 0.001;
    
    @Test
    public void test1() {
        DirectedGraphWeightFunction f = new DirectedGraphWeightFunction();
        
        DirectedGraphNode a = new DirectedGraphNode(0);
        DirectedGraphNode b = new DirectedGraphNode(1);
        DirectedGraphNode c = new DirectedGraphNode(2);
        
        f.put(a, b, 1.0);
        f.put(b, a, 2.0);
        f.put(c, a, 0.0);
        f.put(a, c, Double.POSITIVE_INFINITY);
        
        assertEquals(1.0, f.get(a, b), E);
        assertEquals(2.0, f.get(b, a), E);
        assertEquals(0.0, f.get(c, a), E);
        assertEquals(Double.POSITIVE_INFINITY, f.get(a, c), E);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void throwsOnNegativeWeight() {
        new DirectedGraphWeightFunction()
            .put(new DirectedGraphNode(0), 
                 new DirectedGraphNode(1), 
                 -0.1);
    }
}
