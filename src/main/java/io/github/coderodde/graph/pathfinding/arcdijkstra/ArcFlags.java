package io.github.coderodde.graph.pathfinding.arcdijkstra;

/**
 * This class implements a simple bit vector for marking the graph arcs with 
 * flags.
 */
public final class ArcFlags {
    
    private final byte[] flagBytes;
    
    public ArcFlags(int regions) {
        int numBytes = regions / Byte.SIZE;
        
        if (regions % Byte.SIZE != 0) {
            ++numBytes;
        }
        
        flagBytes = new byte[numBytes];
    }
    
    public boolean readFlag(int regionId) {
        int byteIndex = regionId / Byte.SIZE;
        int bitIndex  = regionId % Byte.SIZE;
        byte b = flagBytes[byteIndex];
        
        return (b & (0b1 << bitIndex)) != 0;
    }
    
    public void writeFlag(int regionId) {
        int byteIndex = regionId / Byte.SIZE;
        int bitIndex  = regionId % Byte.SIZE;
        flagBytes[byteIndex] |= (0b1 << bitIndex);
    }
}
