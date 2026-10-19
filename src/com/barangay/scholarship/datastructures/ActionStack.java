package com.barangay.scholarship.datastructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;

/**
 * Custom singly-linked LIFO Stack implementation.
 * Used for transaction auditing and 1-click Undo operations.
 *
 * @param <T> The type of elements held in this stack.
 */
public class ActionStack<T> implements Iterable<T>, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Internal singly-linked stack node.
     */
    private static class Node<E> implements Serializable {
        private static final long serialVersionUID = 1L;
        E data;
        Node<E> next;

        Node(E data, Node<E> next) {
            this.data = data;
            this.next = next;
        }
    }

    private Node<T> top;
    private int size;

    public ActionStack() {
        this.top = null;
        this.size = 0;
    }

    /**
     * Pushes an item onto the top of the stack (O(1)).
     */
    public synchronized void push(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot push null item onto ActionStack");
        }
        top = new Node<>(item, top);
        size++;
    }

    /**
     * Pops and returns the top item from the stack (O(1)).
     */
    public synchronized T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        T item = top.data;
        top = top.next;
        size--;
        return item;
    }

    /**
     * Peeks at the top item without removing it (O(1)).
     */
    public synchronized T peek() {
        if (isEmpty()) {
            return null;
        }
        return top.data;
    }

    /**
     * Checks if the stack is empty.
     */
    public synchronized boolean isEmpty() {
        return top == null;
    }

    /**
     * Returns current number of items on the stack.
     */
    public synchronized int size() {
        return size;
    }

    /**
     * Clears all items from the stack.
     */
    public synchronized void clear() {
        top = null;
        size = 0;
    }

    /**
     * Returns items in reverse chronological order (top to bottom).
     */
    public synchronized List<T> toList() {
        List<T> list = new ArrayList<>();
        Node<T> current = top;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }

    @Override
    public Iterator<T> iterator() {
        return toList().iterator();
    }

    @Override
    public String toString() {
        return "ActionStack(size=" + size + ", top=" + (peek() != null ? peek() : "none") + ")";
    }
}
