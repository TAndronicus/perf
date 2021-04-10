package jb.collections;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RoaringBitmapTest {

    private static final int SMALL_SET_SIZE = 1000;
    private static final int LARGE_SET_SIZE = 1000000;
    private static final int LOOKUP_SIZE = 1000;

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(RoaringBitmapTest.class.getSimpleName())
                .forks(1)
                .threads(8)
                .warmupIterations(1)
                .measurementIterations(1)
                .build();
        new Runner(options).run();
    }

    @State(Scope.Benchmark)
    public static class Provider {

        Set<Integer> smallSet = new HashSet<>(SMALL_SET_SIZE);
        Set<Integer> largeSet = new HashSet<>(LARGE_SET_SIZE);
        RoaringBitmap smallRoaringBitmap = new RoaringBitmap();
        RoaringBitmap largeRoaringBitmap = new RoaringBitmap();
        Set<Integer> lookupSmallNeg;
        Set<Integer> lookupLargeNeg;
        ImmutableRoaringBitmap immutableRoaringBitmap;

        @Setup
        public void setup() {
            fillCollections(smallSet, smallRoaringBitmap, SMALL_SET_SIZE);
            fillCollections(largeSet, largeRoaringBitmap, LARGE_SET_SIZE);
            lookupSmallNeg = IntStream.range(0, 2 * SMALL_SET_SIZE)
                            .boxed()
                            .filter(i -> !smallSet.contains(i))
                            .limit(LOOKUP_SIZE)
                            .collect(Collectors.toSet());
            lookupLargeNeg = IntStream.range(0, 2 * LARGE_SET_SIZE)
                            .boxed()
                            .filter(i -> !largeSet.contains(i))
                            .limit(LOOKUP_SIZE)
                            .collect(Collectors.toSet());
        }

        private void fillCollections(Set<Integer> set, RoaringBitmap roaringBitmap, int count) {
            Random random = new Random();
            for (int i = 0; i < count; i++) {
                int next = random.nextInt(Integer.MAX_VALUE);
                set.add(next);
                roaringBitmap.add(next);
            }
        }

    }

    @Benchmark
    public void native_or(Provider provider, Blackhole blackhole) {
        Set<Integer> or = new HashSet<>(provider.largeSet.size() + provider.smallSet.size());
        or.addAll(provider.smallSet);
        or.addAll(provider.largeSet);
        blackhole.consume(or);
    }

    @Benchmark
    public void roaring_or(Provider provider, Blackhole blackhole) {
        RoaringBitmap or = new RoaringBitmap();
        or.or(provider.smallRoaringBitmap);
        or.or(provider.largeRoaringBitmap);
        blackhole.consume(or);
    }

    @Benchmark
    public void small_set_lookup_roaring(Provider provider, Blackhole blackhole) {
        long cnt = provider.lookupSmallNeg.stream()
                .map(i -> provider.smallRoaringBitmap.contains(i))
                .count();
        blackhole.consume(cnt);
    }

    @Benchmark
    public void large_set_lookup_roaring(Provider provider, Blackhole blackhole) {
        long cnt = provider.lookupLargeNeg.stream()
                .map(i -> provider.largeRoaringBitmap.contains(i))
                .count();
        blackhole.consume(cnt);
    }

    @Benchmark
    public void small_set_lookup_native(Provider provider, Blackhole blackhole) {
        long cnt = provider.lookupSmallNeg.stream()
                .map(i -> provider.smallSet.contains(i))
                .count();
        blackhole.consume(cnt);
    }

    @Benchmark
    public void large_set_lookup_native(Provider provider, Blackhole blackhole) {
        long cnt = provider.lookupLargeNeg.stream()
                .map(i -> provider.largeSet.contains(i))
                .count();
        blackhole.consume(cnt);
    }

}
