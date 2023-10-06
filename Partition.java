class Partition {
    private int startAddress;
    private int size;
    private boolean allocated;
    private int fragmentation;

    public Partition(int startAddress, int size, boolean allocated) {
        this.startAddress = startAddress;
        this.size = size;
        this.allocated = allocated;
    }

    public Partition(int startAddress, int size, boolean allocated, int fragmentation) {
        this.startAddress = startAddress;
        this.size = size;
        this.allocated = allocated;
        this.fragmentation = fragmentation;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isAllocated() {
        return allocated;
    }

    public void setAllocated(boolean allocated) {
        this.allocated = allocated;
    }

    public int getFragmentation(){
        return fragmentation;
    }

    public void setFragmentation(int fragmentation){
        this.fragmentation = fragmentation;
    }
}