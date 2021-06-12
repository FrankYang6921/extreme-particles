package top.frankyang.exp.group;

import net.minecraft.client.particle.Particle;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

public class ParticleGroup implements List<Particle> {
    private final LinkedList<WeakReference<Particle>> delegate = new LinkedList<>();
    private final ReferenceQueue<Particle> referenceQueue = new ReferenceQueue<>();

    private WeakReference<Particle> createReference(Particle particle) {
        return new WeakReference<>(particle, referenceQueue);
    }

    private void cleanseReference() {
        Set<Reference<? extends Particle>> deadReferences = new HashSet<>();
        Reference<? extends Particle> reference;

        while ((reference = referenceQueue.poll()) != null) {
            deadReferences.add(reference);
        }
        delegate.removeAll(deadReferences);  // A hash set is used to improve performance
        delegate.removeIf(
                r -> !r.get().isAlive()
        );  // Made in order to get rid of dead particles caused by a deferred GC
    }

    @Override
    public int size() {
        cleanseReference();
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        cleanseReference();
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        cleanseReference();
        for (WeakReference<Particle> reference : delegate) {
            if (o.equals(reference.get())) return true;
        }
        return false;
    }

    @Override
    public Iterator<Particle> iterator() {
        cleanseReference();
        return delegate.stream()
                .map(WeakReference<Particle>::get)
                .iterator();
    }

    @Override
    public Object[] toArray() {
        cleanseReference();
        return delegate.stream()
                .map(WeakReference<Particle>::get)
                .toArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        cleanseReference();
        return (T[]) delegate.stream()
                .map(WeakReference<Particle>::get)
                .toArray();
    }

    @Override
    public boolean add(Particle particle) {
        return delegate.add(createReference(particle));
    }

    @Override
    public boolean remove(Object o) {
        cleanseReference();
        for (Iterator<WeakReference<Particle>> iterator = delegate.iterator(); iterator.hasNext(); ) {
            WeakReference<Particle> reference = iterator.next();
            if (o.equals(reference)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public boolean retain(Object o) {
        cleanseReference();
        for (Iterator<WeakReference<Particle>> iterator = delegate.iterator(); iterator.hasNext(); ) {
            WeakReference<Particle> reference = iterator.next();
            if (!o.equals(reference)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        cleanseReference();
        return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(Collection<? extends Particle> c) {
        for (Particle particle : c) {
            add(particle);
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Particle> c) {
        cleanseReference();
        for (Particle particle : c) {
            add(index, particle);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        cleanseReference();
        return c.stream().anyMatch(this::remove);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        cleanseReference();
        return c.stream().anyMatch(this::retain);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Particle get(int index) {
        cleanseReference();
        return delegate.get(index).get();
    }

    @Override
    public Particle set(int index, Particle element) {
        cleanseReference();
        return delegate.set(index, createReference(element)).get();
    }

    @Override
    public void add(int index, Particle element) {
        cleanseReference();
        delegate.add(index, createReference(element));
    }

    @Override
    public Particle remove(int index) {
        cleanseReference();
        return delegate.remove(index).get();
    }

    @Override
    public int indexOf(Object o) {
        cleanseReference();
        for (int s = delegate.size(), i = 0; i < s; i++) {
            WeakReference<Particle> reference = delegate.get(i);
            if (o.equals(reference)) return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        cleanseReference();
        for (int i = delegate.size() - 1; i >= 0; i--) {
            WeakReference<Particle> reference = delegate.get(i);
            if (o.equals(reference)) return i;
        }
        return -1;
    }

    @Override
    public ListIterator<Particle> listIterator() {
        cleanseReference();
        return delegate.stream()
                .map(WeakReference<Particle>::get)
                .collect(Collectors.toList())
                .listIterator();
    }

    @Override
    public ListIterator<Particle> listIterator(int index) {
        cleanseReference();
        return delegate.stream()
                .map(WeakReference<Particle>::get)
                .collect(Collectors.toList())
                .listIterator(index);
    }

    @Override
    public List<Particle> subList(int fromIndex, int toIndex) {
        cleanseReference();
        return delegate.stream()
                .map(WeakReference<Particle>::get)
                .collect(Collectors.toList())
                .subList(fromIndex, toIndex);
    }
}
