package jb;

import one.util.streamex.EntryStream;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StreamExTest {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(StreamExTest.class.getSimpleName())
                .forks(1)
                .threads(8)
                .warmupIterations(1)
                .measurementIterations(1)
                .build();
        new Runner(options).run();
    }

    @State(Scope.Benchmark)
    public static class Provider {

        Map<Integer, String> map = new HashMap(){{
            put(1, "one");
            put(2, "two");
            put(3, "three");
            put(4, "four");
            put(5, "five");
        }};
        List<String> strings = new ArrayList<>(map.values());

    }

    @Benchmark
    public void streamEx_reverseMapImmutable(Provider provider, Blackhole blackhole) {
        Map<String, Integer> reversed = EntryStream.of(provider.map).invert().toImmutableMap();
        blackhole.consume(reversed);
    }

    @Benchmark
    public void streamEx_reverseMap(Provider provider, Blackhole blackhole) {
        Map<String, Integer> reversed = EntryStream.of(provider.map).invert().toMap();
        blackhole.consume(reversed);
    }

    @Benchmark
    public void stream_reverseMap(Provider provider, Blackhole blackhole) {
        Map<String, Integer> reversed = provider.map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        blackhole.consume(reversed);
    }

    @Benchmark
    public void streamEx_mapMappingImmutable(Provider provider, Blackhole blackhole) {
        Map<BigInteger, Integer> map = EntryStream.of(provider.map)
                .mapKeys(i -> BigInteger.valueOf(i).multiply(BigInteger.valueOf(i)))
                .mapValues(String::length)
                .toImmutableMap();
        blackhole.consume(map);
    }

    @Benchmark
    public void streamEx_mapMapping(Provider provider, Blackhole blackhole) {
        Map<BigInteger, Integer> map = EntryStream.of(provider.map)
                .mapKeys(i -> BigInteger.valueOf(i).multiply(BigInteger.valueOf(i)))
                .mapValues(String::length)
                .toMap();
        blackhole.consume(map);
    }

    @Benchmark
    public void stream_mapMapping(Provider provider, Blackhole blackhole) {
        Map<BigInteger, Integer> map = new HashMap<>();
        provider.map.entrySet().forEach(e -> map.put(BigInteger.valueOf(e.getKey()).multiply(BigInteger.valueOf(e.getKey())), e.getValue().length()));
        blackhole.consume(map);
    }

    @Benchmark
    public void streamEx_pairMap(Provider provider, Blackhole blackhole) {
        List<String> result = StreamEx.of(provider.strings).pairMap((s1, s2) -> s1 + " + " + s2).toList();
        blackhole.consume(result);
    }

    @Benchmark
    public void stream_pairMap(Provider provider, Blackhole blackhole) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < provider.strings.size() / 2; i++) {
            result.add(provider.strings.get(2 * i + 1) + " + " + provider.strings.get(2 * i + 1));
        }
        blackhole.consume(result);
    }

}
