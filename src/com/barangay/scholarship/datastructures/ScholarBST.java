package com.barangay.scholarship.datastructures;

import com.barangay.scholarship.model.Scholar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Binary Search Tree (BST) for fast scholar lookups and ordered traversals.
 * Keyed by Scholar ID (e.g. "SCH-2026-001") or Applicant ID.
 */
public class ScholarBST implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Binary Search Tree node.
     */
    public static class BSTNode implements Serializable {
        private static final long serialVersionUID = 1L;
        public Scholar scholar;
        public BSTNode left;
        public BSTNode right;

        public BSTNode(Scholar scholar) {
            this.scholar = scholar;
            this.left = null;
            this.right = null;
        }

        public String getKey() {
            if (scholar == null) return "";
            return scholar.getScholarId() != null ? scholar.getScholarId() : scholar.getApplicantId();
        }
    }

    private BSTNode root;
    private int size;

    public ScholarBST() {
        this.root = null;
        this.size = 0;
    }

    public synchronized BSTNode getRoot() {
        return root;
    }

    public synchronized int size() {
        return size;
    }

    public synchronized boolean isEmpty() {
        return root == null;
    }

    public synchronized void clear() {
        root = null;
        size = 0;
    }

    /**
     * Inserts a scholar into the BST. If a scholar with the same key exists, updates it.
     */
    public synchronized void insert(Scholar scholar) {
        if (scholar == null) {
            throw new IllegalArgumentException("Cannot insert null scholar into ScholarBST");
        }
        root = insertRecursive(root, scholar);
    }

    private BSTNode insertRecursive(BSTNode current, Scholar scholar) {
        if (current == null) {
            size++;
            return new BSTNode(scholar);
        }

        String newKey = scholar.getScholarId() != null ? scholar.getScholarId() : scholar.getApplicantId();
        String currentKey = current.getKey();
        int cmp = newKey.compareToIgnoreCase(currentKey);

        if (cmp < 0) {
            current.left = insertRecursive(current.left, scholar);
        } else if (cmp > 0) {
            current.right = insertRecursive(current.right, scholar);
        } else {
            // Replace existing scholar with updated data
            current.scholar = scholar;
        }
        return current;
    }

    /**
     * Searches for a scholar by Scholar ID or Applicant ID.
     */
    public synchronized Scholar searchById(String id) {
        if (id == null || root == null) return null;
        BSTNode result = searchRecursive(root, id.trim());
        return result != null ? result.scholar : null;
    }

    private BSTNode searchRecursive(BSTNode current, String targetId) {
        if (current == null) return null;

        int cmp = targetId.compareToIgnoreCase(current.getKey());
        if (cmp == 0) {
            return current;
        } else if (cmp < 0) {
            return searchRecursive(current.left, targetId);
        } else {
            return searchRecursive(current.right, targetId);
        }
    }

    /**
     * Deletes a scholar by ID from the BST.
     */
    public synchronized boolean delete(String id) {
        if (id == null || root == null) return false;
        int oldSize = size;
        root = deleteRecursive(root, id.trim());
        return size < oldSize;
    }

    private BSTNode deleteRecursive(BSTNode current, String targetId) {
        if (current == null) return null;

        int cmp = targetId.compareToIgnoreCase(current.getKey());
        if (cmp < 0) {
            current.left = deleteRecursive(current.left, targetId);
        } else if (cmp > 0) {
            current.right = deleteRecursive(current.right, targetId);
        } else {
            // Found node to delete
            size--;

            // Case 1: Leaf or single child
            if (current.left == null) return current.right;
            if (current.right == null) return current.left;

            // Case 2: Two children -> find in-order successor (smallest in right subtree)
            BSTNode successor = findMin(current.right);
            current.scholar = successor.scholar;
            // Delete successor from right subtree (adjust size back since recursive call will decrement)
            size++;
            current.right = deleteRecursive(current.right, successor.getKey());
        }
        return current;
    }

    private BSTNode findMin(BSTNode node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    /**
     * In-Order Traversal (Left, Root, Right).
     * Yields scholars sorted ascendingly by ID.
     */
    public synchronized List<Scholar> inOrderTraversal() {
        List<Scholar> result = new ArrayList<>();
        inOrderHelper(root, result);
        return result;
    }

    private void inOrderHelper(BSTNode node, List<Scholar> list) {
        if (node != null) {
            inOrderHelper(node.left, list);
            list.add(node.scholar);
            inOrderHelper(node.right, list);
        }
    }

    /**
     * Pre-Order Traversal (Root, Left, Right).
     */
    public synchronized List<Scholar> preOrderTraversal() {
        List<Scholar> result = new ArrayList<>();
        preOrderHelper(root, result);
        return result;
    }

    private void preOrderHelper(BSTNode node, List<Scholar> list) {
        if (node != null) {
            list.add(node.scholar);
            preOrderHelper(node.left, list);
            preOrderHelper(node.right, list);
        }
    }

    /**
     * Post-Order Traversal (Left, Right, Root).
     */
    public synchronized List<Scholar> postOrderTraversal() {
        List<Scholar> result = new ArrayList<>();
        postOrderHelper(root, result);
        return result;
    }

    private void postOrderHelper(BSTNode node, List<Scholar> list) {
        if (node != null) {
            postOrderHelper(node.left, list);
            postOrderHelper(node.right, list);
            list.add(node.scholar);
        }
    }
}
