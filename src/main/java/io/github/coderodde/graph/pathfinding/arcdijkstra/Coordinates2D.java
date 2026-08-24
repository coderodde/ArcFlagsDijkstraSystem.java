package io.github.coderodde.graph.pathfinding.arcdijkstra;

/**
 * This class represents 2D-coordinates.
 */
public final record Coordinates2D(double x, double y) {
    
    @Override
    public String toString() {
        return "[x = %s, y = %s]".formatted(x, y);
    }
    
    /**
     * Returns the Euclidean distance between this and the {@code other} 
     * coordinates.
     * 
     * @param other another 2D-point coordinates.
     * 
     * @return Euclidean distance. 
     */
    public double euclideanDistance(Coordinates2D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
