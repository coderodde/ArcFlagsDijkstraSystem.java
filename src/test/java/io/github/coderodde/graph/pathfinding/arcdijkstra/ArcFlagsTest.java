package io.github.coderodde.graph.pathfinding.arcdijkstra;

import org.junit.Test;
import static org.junit.Assert.*;

public final class ArcFlagsTest {
    
    @Test
    public void test1() {
        ArcFlags af = new ArcFlags(1);
        
        for (int i = 0; i < Byte.SIZE; ++i) {
            assertFalse(af.readFlag(i));
        }
        
        af.writeFlag(2);
        af.writeFlag(5);
        
        assertTrue(af.readFlag(2));
        assertTrue(af.readFlag(5));
        
        assertFalse(af.readFlag(0));
        assertFalse(af.readFlag(1));
        assertFalse(af.readFlag(3));
        assertFalse(af.readFlag(4));
        assertFalse(af.readFlag(6));
        assertFalse(af.readFlag(7));
    }
    
    @Test
    public void test2() {
        ArcFlags af = new ArcFlags(30);
        
        assertFalse(af.readFlag(4));
        assertFalse(af.readFlag(11));
        assertFalse(af.readFlag(22));
        assertFalse(af.readFlag(29));
        assertFalse(af.readFlag(31));
        
        af.writeFlag(4);
        af.writeFlag(11);
        af.writeFlag(22);
        af.writeFlag(29);
        af.writeFlag(31);
        
        assertTrue(af.readFlag(4));
        assertTrue(af.readFlag(11));
        assertTrue(af.readFlag(22));
        assertTrue(af.readFlag(29));
        assertTrue(af.readFlag(31));
    }
    
    @Test
    public void test3() {
        new ArcFlags(16); // Tight bit vector.
    }
}
