package io.github.coderodde.graph.pathfinding.arcdijkstra;

/**
 * This class implements a simple bit vector for marking the graph arcs with 
 * flags. Given the number of regions {@code R}, the flag indices are 
 * {@code 0, 1, ..., R - 1}.
 */
public final class ArcFlags {
    
    /**
     * The actual bit data.
     */
    private final byte[] flagBytes;
    
    /**
     * Constructs this arc flags object.
     * 
     * @param regions the number of regions.
     */
    public ArcFlags(int regions) {
        int numBytes = regions / Byte.SIZE;
        
        if (regions % Byte.SIZE != 0) {
            ++numBytes;
        }
        
        flagBytes = new byte[numBytes];
    }
    
    /**
     * Reads the {@code regionId}th flag.
     * 
     * @param regionId the region ID.
     * 
     * @return the binary flag associated with the region.
     */
    public boolean readFlag(int regionId) {
        int byteIndex = regionId / Byte.SIZE;
        int bitIndex  = regionId % Byte.SIZE;
        byte b = flagBytes[byteIndex];
        
        return (b & (0b1 << bitIndex)) != 0;
    }
    
    /**
     * Sets the {@code regionId}th flag.
     * 
     * @param regionId the region ID.
     */
    public void writeFlag(int regionId) {
        int byteIndex = regionId / Byte.SIZE;
        int bitIndex  = regionId % Byte.SIZE;
        flagBytes[byteIndex] |= (0b1 << bitIndex);
    }
}
