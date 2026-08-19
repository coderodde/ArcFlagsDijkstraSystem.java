package io.github.coderodde.graph.pathfinding.arcdijkstra;

import java.util.NoSuchElementException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public final class DoublePriorityBinaryHeapTest {
    
    private DoublePriorityBinaryHeap<Integer> heap;
    
    @Before
    public void before() {
        heap = new DoublePriorityBinaryHeap<>();
    }
    
    @Test
    public void test1() {
        
        assertEquals(0, heap.size());
        assertTrue(heap.isEmpty());
        
        heap.insert(3, 3.0);
        
        assertEquals(Integer.valueOf(3), heap.top());
        
        assertTrue(heap.containsDatum(3));
        assertFalse(heap.containsDatum(10));
        
        assertEquals(1, heap.size());
        assertFalse(heap.isEmpty());
        
        heap.insert(5, 5.0);
        
        heap.changePriority(5, 2.0);
        
        assertEquals(Integer.valueOf(5), heap.extractTop());
        assertEquals(Integer.valueOf(3), heap.extractTop());
        
        for (int i = 0; i < 100; ++i) {
            heap.insert(i, 100 - i);
        }
        
        for (int i = 99; i >= 0; --i) {
            assertEquals(Integer.valueOf(i), heap.extractTop());
        }
        
        for (int i = 0; i < 20; ++i) {
            heap.insert(i, i);
        }
        
        heap.changePriority(0, 100);
        heap.changePriority(19, -1);
        
        assertEquals(Integer.valueOf(19), heap.extractTop());
        
        for (int i = 1; i < 19; ++i) {
            assertEquals(Integer.valueOf(i), heap.extractTop());
        }
        
        assertEquals(Integer.valueOf(0), heap.extractTop());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void throwsOnInsertingDuplicate() {
        heap.insert(1, 0);
        heap.insert(1, 2);
    }
    
    @Test(expected = NoSuchElementException.class)
    public void throwsOnExtractingFromEmptyHeap() {
        heap.extractTop();
    }
    
    @Test(expected = NoSuchElementException.class)
    public void throwsOnPeekingFromEmptyHeap() {
        heap.top();
    }
    
    @Test(expected = NoSuchElementException.class)
    public void throwsOnChangingPriorityOfAbsentElement() {
        try {
            heap.insert(1, 1);
            heap.insert(2, 2);

            heap.changePriority(1, -1);
            heap.changePriority(2, -2);
        } catch (Exception ex) {
            fail("Premature exception.");
        }
        
        heap.changePriority(3, 1); // Must throw
    }
    
    @Test
    public void idempotentPriorityChange() {
        heap.insert(1, 1);
        heap.insert(2, 2);
        heap.insert(3, 3);
        
        heap.changePriority(2, 2);
    }
}
