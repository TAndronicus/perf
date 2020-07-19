package jb.vavr;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

public class VavrPatternMatching {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(VavrPatternMatching.class.getSimpleName())
                .forks(1)
                .threads(8)
                .warmupIterations(1)
                .measurementIterations(1)
                .build();
        new Runner(options).run();
    }

    @Benchmark
    public void java_mappingWithIfElse(Provider provider, Blackhole blackhole) {
        List<String> res = provider.javaList
                .stream()
                .map(i -> {
                    if (i == 0) return "a";
                    else if (i == 1) return "b";
                    else if (i == 2) return "c";
                    else return "d";
                })
                .collect(Collectors.toList());
        blackhole.consume(res);
    }

    @Benchmark
    public void vavr_mappingWithPatternMatching(Provider provider, Blackhole blackhole) {
        io.vavr.collection.List<String> res = provider.vavrList
                .map(i -> Match(i).of(
                        Case($(0), "a"),
                        Case($(1), "b"),
                        Case($(2), "c"),
                        Case($(), "d")
                ));
        blackhole.consume(res);
    }

    @Benchmark
    public void java_javaIfElse(Provider provider, Blackhole blackhole) {
        String res;
        if (provider.i == 0) res = "a";
        else if (provider.i == 1) res = "b";
        else if (provider.i == 2) res = "c";
        else res = "d";
        blackhole.consume(res);
    }

    @Benchmark
    public void vavr_patternMatching(Provider provider, Blackhole blackhole) {
        String res = Match(provider.i).of(
                Case($(0), "a"),
                Case($(1), "b"),
                Case($(2), "c"),
                Case($(), "d")
        );
        blackhole.consume(res);
    }

    @State(Scope.Benchmark)
    public static class Provider {

        List<Integer> javaList = IntStream.range(0, 1000)
                .boxed()
                .map(i -> i % 4)
                .collect(Collectors.toList());
        io.vavr.collection.List<Integer> vavrList = io.vavr.collection.List.range(0, 1000)
                .map(i -> i % 4);
        int i = new Random().nextInt(5);

    }

}
