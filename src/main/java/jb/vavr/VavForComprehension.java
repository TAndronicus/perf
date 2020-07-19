package jb.vavr;

import io.vavr.API;
import io.vavr.Tuple2;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;

import static io.vavr.API.For;
import static io.vavr.API.Tuple;

public class VavForComprehension {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(VavForComprehension.class.getSimpleName())
                .forks(1)
                .threads(8)
                .warmupIterations(1)
                .measurementIterations(1)
                .build();
        new Runner(options).run();
    }

    @Benchmark
    public void java_forLoop(Blackhole blackhole) {
        List<List<Integer>> res = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                List<Integer> inter = new ArrayList<>();
                inter.add(i);
                inter.add(j);
                res.add(inter);
            }
        }
        blackhole.consume(res);
    }

    @Benchmark
    public void vavr_forComprehension(Blackhole blackhole) {
        io.vavr.collection.List<Tuple2<Integer, Integer>> res =
                For(io.vavr.collection.List.range(0, 100), io.vavr.collection.List.range(0, 100))
                        .yield(API::Tuple);
        blackhole.consume(res);
    }

    @Benchmark
    public void vavr_forComprehensionNested(Blackhole blackhole) {
        io.vavr.collection.List<Tuple2<Integer, Integer>> res =
                For(io.vavr.collection.List.range(0, 100), x ->
                        For(io.vavr.collection.List.range(0, 100))
                                .yield(y -> Tuple(x, y)))
                        .toList();
        blackhole.consume(res);
    }

}
