package com.barangay.scholarship.algorithms;

import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.model.ApplicationStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom sorting and searching algorithms implemented from scratch.
 * Satisfies Section 3 of project rubric (Merge Sort, Quick Sort, Binary Search, Linear Search).
 */
public class AlgorithmUtils {

    // =========================================================================
    // 1. MERGE SORT (By GWA / Academic Merit)
    // =========================================================================

    /**
     * Sorts applicants by GWA (Academic Merit) using Divide-and-Conquer Merge Sort (O(n log n)).
     *
     * @param list The list of applicants to sort (in-place).
     * @param bestFirst If true, ranks top academic performers first (taking scale into account).
     */
    public static void mergeSortByGWA(List<Applicant> list, boolean bestFirst) {
        if (list == null || list.size() <= 1) return;
        List<Applicant> temp = new ArrayList<>(list);
        mergeSortGWAHelper(list, temp, 0, list.size() - 1, bestFirst);
    }

    private static void mergeSortGWAHelper(List<Applicant> list, List<Applicant> temp,
                                           int left, int right, boolean bestFirst) {
        if (left >= right) return;
        int mid = left + (right - left) / 2;
        mergeSortGWAHelper(list, temp, left, mid, bestFirst);
        mergeSortGWAHelper(list, temp, mid + 1, right, bestFirst);
        mergeGWA(list, temp, left, mid, right, bestFirst);
    }

    private static void mergeGWA(List<Applicant> list, List<Applicant> temp,
                                 int left, int mid, int right, boolean bestFirst) {
        for (int i = left; i <= right; i++) {
            temp.set(i, list.get(i));
        }

        int i = left;
        int j = mid + 1;
        int k = left;

        while (i <= mid && j <= right) {
            Applicant a = temp.get(i);
            Applicant b = temp.get(j);
            int cmp = compareMerit(a, b);
            if (!bestFirst) cmp = -cmp;

            if (cmp <= 0) {
                list.set(k++, a);
                i++;
            } else {
                list.set(k++, b);
                j++;
            }
        }

        while (i <= mid) {
            list.set(k++, temp.get(i++));
        }
    }

    /**
     * Compares academic merit:
     * - In collegiate scale (<= 5.0), 1.00 is better than 2.00 (lower number wins).
     * - In percentage scale (> 5.0), 95.0% is better than 85.0% (higher number wins).
     */
    public static int compareMerit(Applicant a, Applicant b) {
        double gwaA = a.getGwa();
        double gwaB = b.getGwa();

        // Convert percentage scale (75-100) to equivalent 1.0 - 5.0 relative score for uniform comparison
        // e.g., 100% -> 1.0, 75% -> 3.0
        double scoreA = (gwaA > 5.0) ? (5.0 - ((gwaA - 75.0) / 25.0) * 4.0) : gwaA;
        double scoreB = (gwaB > 5.0) ? (5.0 - ((gwaB - 75.0) / 25.0) * 4.0) : gwaB;

        return Double.compare(scoreA, scoreB);
    }

    // =========================================================================
    // 2. QUICK SORT (By Household Income / Financial Need)
    // =========================================================================

    /**
     * Sorts applicants by Monthly Family Income using Quick Sort (O(n log n)).
     *
     * @param list The list to sort in-place.
     * @param lowestFirst If true, lowest family income (greatest financial need) comes first.
     */
    public static void quickSortByIncome(List<Applicant> list, boolean lowestFirst) {
        if (list == null || list.size() <= 1) return;
        quickSortIncomeHelper(list, 0, list.size() - 1, lowestFirst);
    }

    private static void quickSortIncomeHelper(List<Applicant> list, int low, int high, boolean lowestFirst) {
        if (low < high) {
            int pi = partitionIncome(list, low, high, lowestFirst);
            quickSortIncomeHelper(list, low, pi - 1, lowestFirst);
            quickSortIncomeHelper(list, pi + 1, high, lowestFirst);
        }
    }

    private static int partitionIncome(List<Applicant> list, int low, int high, boolean lowestFirst) {
        double pivot = list.get(high).getMonthlyFamilyIncome();
        int i = low - 1;

        for (int j = low; j < high; j++) {
            double currentIncome = list.get(j).getMonthlyFamilyIncome();
            boolean shouldSwap = lowestFirst ? (currentIncome <= pivot) : (currentIncome >= pivot);
            if (shouldSwap) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    // =========================================================================
    // 3. QUICK SORT (By Full Name / Last Name for Masterlist & Attendance)
    // =========================================================================

    /**
     * Sorts applicants alphabetically by Name using Quick Sort (O(n log n)).
     */
    public static void quickSortByName(List<Applicant> list, boolean ascending) {
        if (list == null || list.size() <= 1) return;
        quickSortNameHelper(list, 0, list.size() - 1, ascending);
    }

    private static void quickSortNameHelper(List<Applicant> list, int low, int high, boolean ascending) {
        if (low < high) {
            int pi = partitionName(list, low, high, ascending);
            quickSortNameHelper(list, low, pi - 1, ascending);
            quickSortNameHelper(list, pi + 1, high, ascending);
        }
    }

    private static int partitionName(List<Applicant> list, int low, int high, boolean ascending) {
        String pivot = list.get(high).getFullName();
        int i = low - 1;

        for (int j = low; j < high; j++) {
            String current = list.get(j).getFullName();
            int cmp = current.compareToIgnoreCase(pivot);
            boolean shouldSwap = ascending ? (cmp <= 0) : (cmp >= 0);
            if (shouldSwap) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    // =========================================================================
    // 4. QUICK SORT (By Applicant ID)
    // =========================================================================

    public static void quickSortById(List<Applicant> list) {
        if (list == null || list.size() <= 1) return;
        quickSortIdHelper(list, 0, list.size() - 1);
    }

    private static void quickSortIdHelper(List<Applicant> list, int low, int high) {
        if (low < high) {
            int pi = partitionId(list, low, high);
            quickSortIdHelper(list, low, pi - 1);
            quickSortIdHelper(list, pi + 1, high);
        }
    }

    private static int partitionId(List<Applicant> list, int low, int high) {
        String pivot = list.get(high).getApplicantId();
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (list.get(j).getApplicantId().compareToIgnoreCase(pivot) <= 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    private static void swap(List<Applicant> list, int i, int j) {
        Applicant temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    // =========================================================================
    // 5. BINARY SEARCH (O(log n))
    // =========================================================================

    /**
     * Binary Search by Applicant ID. Pre-condition: list must be sorted by Applicant ID.
     *
     * @return Index of matching applicant, or -1 if not found.
     */
    public static int binarySearchById(List<Applicant> sortedList, String targetId) {
        if (sortedList == null || targetId == null) return -1;
        int low = 0;
        int high = sortedList.size() - 1;
        String target = targetId.trim();

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int cmp = sortedList.get(mid).getApplicantId().compareToIgnoreCase(target);
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    /**
     * Binary Search by Full Name. Pre-condition: list must be sorted by Full Name.
     */
    public static int binarySearchByName(List<Applicant> sortedList, String targetName) {
        if (sortedList == null || targetName == null) return -1;
        int low = 0;
        int high = sortedList.size() - 1;
        String target = targetName.trim();

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int cmp = sortedList.get(mid).getFullName().compareToIgnoreCase(target);
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    // =========================================================================
    // 6. LINEAR SEARCH & MULTI-FILTERING (O(n))
    // =========================================================================

    /**
     * Filters applicants residing in a specific Purok/Zone using Linear Search.
     */
    public static List<Applicant> linearSearchByPurok(List<Applicant> list, String purok) {
        List<Applicant> matches = new ArrayList<>();
        if (list == null || purok == null) return matches;
        String target = purok.trim().toLowerCase();

        for (Applicant applicant : list) {
            if (applicant.getPurok() != null && applicant.getPurok().toLowerCase().contains(target)) {
                matches.add(applicant);
            }
        }
        return matches;
    }

    /**
     * Filters applicants enrolled in a specific School using Linear Search.
     */
    public static List<Applicant> linearSearchBySchool(List<Applicant> list, String school) {
        List<Applicant> matches = new ArrayList<>();
        if (list == null || school == null) return matches;
        String target = school.trim().toLowerCase();

        for (Applicant applicant : list) {
            if (applicant.getSchool() != null && applicant.getSchool().toLowerCase().contains(target)) {
                matches.add(applicant);
            }
        }
        return matches;
    }

    /**
     * Filters applicants by ApplicationStatus using Linear Search.
     */
    public static List<Applicant> linearSearchByStatus(List<Applicant> list, ApplicationStatus status) {
        List<Applicant> matches = new ArrayList<>();
        if (list == null || status == null) return matches;

        for (Applicant applicant : list) {
            if (applicant.getStatus() == status) {
                matches.add(applicant);
            }
        }
        return matches;
    }

    /**
     * Universal keyword linear search matching ID, Name, School, or Purok.
     */
    public static List<Applicant> linearSearchKeyword(List<Applicant> list, String query) {
        List<Applicant> matches = new ArrayList<>();
        if (list == null) return matches;
        if (query == null || query.trim().isEmpty()) return new ArrayList<>(list);

        String q = query.trim().toLowerCase();
        for (Applicant a : list) {
            if (a.getApplicantId().toLowerCase().contains(q)
                    || a.getFullName().toLowerCase().contains(q)
                    || a.getSchool().toLowerCase().contains(q)
                    || a.getPurok().toLowerCase().contains(q)) {
                matches.add(a);
            }
        }
        return matches;
    }
}
