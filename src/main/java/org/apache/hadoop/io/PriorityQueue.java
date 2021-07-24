package org.apache.hadoop.io;

public abstract class PriorityQueue {
    private Object[] heap;
    private int size;
    private int maxSize;

    public PriorityQueue() {
    }

    protected abstract boolean lessThan(Object var1, Object var2);

    protected final void initialize(int maxSize) {
        this.size = 0;
        int heapSize = maxSize + 1;
        this.heap = new Object[heapSize];
        this.maxSize = maxSize;
    }

    public final void put(Object element) {
        ++this.size;
        this.heap[this.size] = element;
        this.upHeap();
    }

    public boolean insert(Object element) {
        if (this.size < this.maxSize) {
            this.put(element);
            return true;
        } else if (this.size > 0 && !this.lessThan(element, this.top())) {
            this.heap[1] = element;
            this.adjustTop();
            return true;
        } else {
            return false;
        }
    }

    public final Object top() {
        return this.size > 0 ? this.heap[1] : null;
    }

    public final Object pop() {
        if (this.size > 0) {
            Object result = this.heap[1];
            this.heap[1] = this.heap[this.size];
            this.heap[this.size] = null;
            --this.size;
            this.downHeap();
            return result;
        } else {
            return null;
        }
    }

    public final void adjustTop() {
        this.downHeap();
    }

    public final int size() {
        return this.size;
    }

    public final void clear() {
        for(int i = 0; i <= this.size; ++i) {
            this.heap[i] = null;
        }

        this.size = 0;
    }

    private final void upHeap() {
        int i = this.size;
        Object node = this.heap[i];

        for(int j = i >>> 1; j > 0 && this.lessThan(node, this.heap[j]); j >>>= 1) {
            this.heap[i] = this.heap[j];
            i = j;
        }

        this.heap[i] = node;
    }

    private final void downHeap() {
        int i = 1;
        Object node = this.heap[i];
        int j = i << 1;
        int k = j + 1;
        if (k <= this.size && this.lessThan(this.heap[k], this.heap[j])) {
            j = k;
        }

        while(j <= this.size && this.lessThan(this.heap[j], node)) {
            this.heap[i] = this.heap[j];
            i = j;
            j <<= 1;
            k = j + 1;
            if (k <= this.size && this.lessThan(this.heap[k], this.heap[j])) {
                j = k;
            }
        }

        this.heap[i] = node;
    }
}

