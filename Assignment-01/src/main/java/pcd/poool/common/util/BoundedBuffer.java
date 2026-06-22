package pcd.poool.common.util;

public interface BoundedBuffer<Item> {

    void put(Item item) throws InterruptedException;
    
    Item get() throws InterruptedException;

    Item poll(long timeoutMs) throws InterruptedException;
    
}
