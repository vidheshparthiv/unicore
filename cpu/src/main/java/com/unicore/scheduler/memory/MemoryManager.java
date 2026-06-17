package com.unicore.scheduler.memory;

import java.util.*;

/**
 * Simple memory management simulation.
 * Tracks allocated memory blocks and fragmentation.
 */
public class MemoryManager {
    private final int totalMemory;
    private final Map<Integer, MemoryBlock> allocatedBlocks;
    private final List<FreeBlock> freeBlocks;

    public MemoryManager(int totalMemory) {
        this.totalMemory = totalMemory;
        this.allocatedBlocks = new HashMap<>();
        this.freeBlocks = new ArrayList<>();
        this.freeBlocks.add(new FreeBlock(0, totalMemory));
    }

    /**
     * Allocate memory for a process.
     *
     * @param processId ID of the process
     * @param size Size of memory to allocate
     * @return Starting address of allocated block, or -1 if allocation failed
     */
    public int allocateMemory(int processId, int size) {
        if (size <= 0 || size > totalMemory) {
            return -1;
        }

        // First Fit Algorithm
        for (FreeBlock block : freeBlocks) {
            if (block.size >= size) {
                int startAddress = block.startAddress;
                allocatedBlocks.put(processId, new MemoryBlock(startAddress, size, processId));

                if (block.size == size) {
                    freeBlocks.remove(block);
                } else {
                    block.startAddress += size;
                    block.size -= size;
                }

                return startAddress;
            }
        }

        return -1; // Allocation failed
    }

    /**
     * Deallocate memory for a process.
     *
     * @param processId ID of the process
     */
    public void deallocateMemory(int processId) {
        MemoryBlock block = allocatedBlocks.remove(processId);
        if (block != null) {
            // Add back to free blocks and merge adjacent free blocks
            freeBlocks.add(new FreeBlock(block.startAddress, block.size));
            mergeFreeBlocks();
        }
    }

    /**
     * Merge adjacent free blocks to reduce fragmentation.
     */
    private void mergeFreeBlocks() {
        freeBlocks.sort(Comparator.comparingInt(b -> b.startAddress));

        for (int i = 0; i < freeBlocks.size() - 1; i++) {
            FreeBlock current = freeBlocks.get(i);
            FreeBlock next = freeBlocks.get(i + 1);

            if (current.startAddress + current.size == next.startAddress) {
                current.size += next.size;
                freeBlocks.remove(i + 1);
                i--;
            }
        }
    }

    /**
     * Get total free memory.
     */
    public int getTotalFreeMemory() {
        return freeBlocks.stream().mapToInt(b -> b.size).sum();
    }

    /**
     * Get total allocated memory.
     */
    public int getTotalAllocatedMemory() {
        return allocatedBlocks.values().stream().mapToInt(b -> b.size).sum();
    }

    /**
     * Get fragmentation percentage.
     */
    public double getFragmentationPercentage() {
        if (freeBlocks.isEmpty()) return 0;
        int largestBlock = freeBlocks.stream().mapToInt(b -> b.size).max().orElse(0);
        return ((double) (getTotalFreeMemory() - largestBlock) / totalMemory) * 100;
    }

    /**
     * Represents a block of allocated memory.
     */
    public static class MemoryBlock {
        public final int startAddress;
        public final int size;
        public final int processId;

        public MemoryBlock(int startAddress, int size, int processId) {
            this.startAddress = startAddress;
            this.size = size;
            this.processId = processId;
        }
    }

    /**
     * Represents a block of free memory.
     */
    public static class FreeBlock {
        public int startAddress;
        public int size;

        public FreeBlock(int startAddress, int size) {
            this.startAddress = startAddress;
            this.size = size;
        }
    }
}
