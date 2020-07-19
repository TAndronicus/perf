package jb;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.LinkedList;

public class VavrDataStructuresTest {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(VavrDataStructuresTest.class.getSimpleName())
                .forks(1)
                .threads(8)
                .warmupIterations(1)
                .measurementIterations(1)
                .build();
        new Runner(options).run();
    }

    @Benchmark
    public void vavr_prepend(Blackhole blackhole) {
        io.vavr.collection.List list = io.vavr.collection.List.empty();
        for (int i = 0; i < 10_000; i++) {
            list = list.prepend(i);
        }
        blackhole.consume(list);
    }

    @Benchmark
    public void vavr_append(Blackhole blackhole) {
        io.vavr.collection.List list = io.vavr.collection.List.empty();
        for (int i = 0; i < 10_000; i++) {
            list = list.append(i);
        }
        blackhole.consume(list);
    }

    @Benchmark
    public void arraylist_add(Blackhole blackhole) {
        ArrayList list = new ArrayList();
        for (int i = 0; i < 10_000; i++) {
            list.add(i);
        }
        blackhole.consume(list);
    }

    @Benchmark
    public void linkedlist_add(Blackhole blackhole) {
        LinkedList list = new LinkedList();
        for (int i = 0; i < 10_000; i++) {
            list.add(i);
        }
        blackhole.consume(list);
    }

    @Benchmark
    public void linkedlist_offerfirst(Blackhole blackhole) {
        LinkedList list = new LinkedList();
        for (int i = 0; i < 10_000; i++) {
            list.offerFirst(i);
        }
        blackhole.consume(list);
    }

}
