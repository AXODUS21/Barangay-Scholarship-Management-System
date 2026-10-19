package com.barangay.scholarship.datastructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Custom singly-linked FIFO Queue implementation.
 * Used for Document Screening queue and Waitlist management.
 *
 * @param <T> The type of elements held in this queue.
 */
public class ScholarshipQueue<T> implements Iterable<T>, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Internal singly-linked node.
     */
    private static class Node<E> implements Serializable {
        private static final long serialVersionUID = 1L;
        E data;
        Node<E> next;

        Node(E data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> front;
    private Node<T> rear;
    private int size;

    public ScholarshipQueue() {
        this.front = null;
        this.rear = null;
        this.size = 0;
    }

    /**
     * Enqueues an item to the rear of the queue (O(1)).
     */
    public synchronized void enqueue(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot enqueue null item into ScholarshipQueue");
        }
        Node<T> newNode = new Node<>(item);
        if (isEmpty()) {
            front = newNode;
            rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    /**
     * Dequeues and returns the front item of the queue (O(1)).
     */
    public synchronized T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Cannot dequeue from an empty ScholarshipQueue");
        }
        T item = front.data;
        front = front.next;
        if (front == null) {
            rear = null;
        }
        size--;
        return item;
    }

    /**
     * Peeks at the front item without removing it (O(1)).
     */
    public synchronized T peek() {
        if (isEmpty()) {
            return null;
        }
        return front.data;
    }

    /**
     * Checks if the queue is empty.
     */
    public synchronized boolean isEmpty() {
        return front == null;
    }

    /**
     * Returns the number of items in the queue.
     */
    public synchronized int size() {
        return size;
    }

    /**
     * Clears all elements from the queue.
     */
    public synchronized void clear() {
        front = null;
        rear = null;
        size = 0;
    }

    /**
     * Removes a specific item from the queue if present.
     * Useful when a waitlisted applicant is manually cancelled or promoted out-of-band.
     */
    public synchronized boolean remove(T item) {
        if (isEmpty() || item == null) return false;

        if (front.data.equals(item)) {
            dequeue();
            return true;
        }

        Node<T> current = front;
        while (current.next != null && !current.next.data.equals(item)) {
            current = current.next;
        }

        if (current.next != null) {
            if (current.next == rear) {
                rear = current;
            }
            current.next = current.next.next;
            size--;
            return true;
        }
        return false;
    }

    /**
     * Returns a snapshot of elements in queue order from front to rear.
     */
    public synchronized List<T> toList() {
        List<T> list = new ArrayList<>();
        Node<T> current = front;
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
        return "ScholarshipQueue(size=" + size + ", front=" + (peek() != null ? peek() : "none") + ")";
    }
}
