package io.github.coderodde.graph.pathfinding.arcdijkstra;

/**
 * This class represents 2D-coordinates.
 */
public final record Coordinates2D(double x, double y) {
    
    @Override
    public String toString() {
        return "[x = %s, y = %s]".formatted(x, y);
    }
}
