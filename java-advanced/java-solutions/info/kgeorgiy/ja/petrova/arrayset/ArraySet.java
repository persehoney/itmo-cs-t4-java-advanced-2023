package info.kgeorgiy.ja.petrova.arrayset;

import java.util.*;

public class ArraySet<E extends Comparable<? super E>> extends AbstractSet<E> implements SortedSet<E> {
    private final List<E> elements = new ArrayList<>();
    private Comparator<? super E> comparator;

    public ArraySet() {
        super();
    }

    public ArraySet(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }

    public ArraySet(Collection<E> collection) {
        Set<E> treeSet = new TreeSet<>(collection);
        elements.addAll(treeSet);
    }

    public ArraySet(Collection<E> collection, Comparator<? super E> comparator) {
        Set<E> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        elements.addAll(treeSet);
        this.comparator = comparator;
    }

    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) throws IllegalArgumentException {
        if (comparator.compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("fromKey > toKey");
        }
        int from = getPosition(fromElement);
        int to = getPosition(toElement);
        return getSet(from, to);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        int to = getPosition(toElement);
        return getSet(0, to);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        int from = getPosition(fromElement);
        return getSet(from, elements.size());
    }

    @Override
    public E first() {
        return getElement(0);
    }

    @Override
    public E last() {
        return getElement(elements.size() - 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return getPosition((E) o) >= 0;
    }

    private E getElement(int i) throws NoSuchElementException {
        if (size() != 0)
            return elements.get(i);
        throw new NoSuchElementException();
    }

    private int getPosition(E element) {
        return Collections.binarySearch(elements, element, comparator);
    }

    private int getBorder(int border) {
        if (border < 0)
            return -(border + 1);
        return border;
    }

    private SortedSet<E> getSet(int from, int to) throws IllegalArgumentException {
        from = getBorder(from);
        to = getBorder(to);
        if (from > to) {
            throw new IllegalArgumentException("fromKey > toKey");
        }
        TreeSet<E> result = new TreeSet<>();
        for (int i = from; i < to; i++)
            result.add(elements.get(i));
        return result;
    }
}
