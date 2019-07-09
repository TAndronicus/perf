/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package jb;

import com.pivovarit.collectors.ParallelCollectors;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class ParallelCollectorsTest {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(ParallelCollectorsTest.class.getSimpleName())
                .forks(1)
                .warmupIterations(1)
                .measurementIterations(1)
                .build();
        new Runner(options).run();
    }

    @State(Scope.Benchmark)
    public static class Squarer implements Function<Integer, BigInteger> {

        @Override
        public BigInteger apply(Integer value) {
            return BigInteger.valueOf(value).multiply(BigInteger.valueOf(value));
        }

        final List<Integer> list = IntStream.range(0, 1_000_000).boxed().collect(Collectors.toList());

        UnaryOperator<BigInteger> plusTwo = i -> i.add(BigInteger.valueOf(2));
        UnaryOperator<BigInteger> timesFive = i -> i.multiply(BigInteger.valueOf(5));
        UnaryOperator<BigInteger> minusHundred = i -> i.subtract(BigInteger.valueOf(100));

    }

    @State(Scope.Benchmark)
    public static class Sleeper implements Function<Integer, Integer> {

        @Override
        public Integer apply(Integer integer) {
            try {
                Thread.sleep(1L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return integer;
        }

        final List<Integer> list = IntStream.range(0, 1_000).boxed().collect(Collectors.toList());

    }

    //    @Benchmark
    public void parallelStream_blocking_cpu(Squarer squarer, Blackhole blackhole) {
        List<BigInteger> list = squarer.list.parallelStream().map(squarer).collect(Collectors.toList());
        blackhole.consume(list);
    }

    //    @Benchmark
    public void parallelCollector_blocking_cpu(Squarer squarer, Blackhole blackhole) {
        Executor executor = new ForkJoinPool(8);
        List<BigInteger> list = squarer.list.parallelStream().collect(ParallelCollectors.parallelToList(squarer, executor)).join();
        blackhole.consume(list);
    }

    //    @Benchmark
    public void parallelStream_blocking_io(Sleeper sleeper, Blackhole blackhole) {
        List<Integer> list = sleeper.list.parallelStream().map(sleeper).collect(Collectors.toList());
        blackhole.consume(list);
    }

    //    @Benchmark
    public void parallelCollector_blocking_io(Sleeper sleeper, Blackhole blackhole) {
        Executor executor = new ForkJoinPool(8);
        List<Integer> list = sleeper.list.parallelStream().collect(ParallelCollectors.parallelToList(sleeper, executor)).join();
        blackhole.consume(list);
    }

    @Benchmark
    public void parallelStream_async_cpu(Squarer squarer, Blackhole blackhole) {
        List<BigInteger> list = squarer.list.parallelStream()
                .map(squarer)
                .map(squarer.plusTwo).collect(Collectors.toList());
        blackhole.consume(list);
        list = squarer.list.parallelStream()
                .map(squarer)
                .map(squarer.timesFive).collect(Collectors.toList());
        blackhole.consume(list);
        list = squarer.list.parallelStream()
                .map(squarer)
                .map(squarer.minusHundred).collect(Collectors.toList());
        blackhole.consume(list);
    }

    @Benchmark
    public void parallelCollector_async_cpu(Squarer squarer, Blackhole blackhole) {
        Executor executor1 = Executors.newFixedThreadPool(2);
        Executor executor2 = Executors.newFixedThreadPool(2);
        Executor executor3 = Executors.newFixedThreadPool(2);
        CompletableFuture<List<BigInteger>> list1 = squarer.list.parallelStream().collect(ParallelCollectors.parallelToList(squarer.andThen(squarer.plusTwo), executor1));
        CompletableFuture<List<BigInteger>> list2 = squarer.list.parallelStream().collect(ParallelCollectors.parallelToList(squarer.andThen(squarer.timesFive), executor2));
        CompletableFuture<List<BigInteger>> list3 = squarer.list.parallelStream().collect(ParallelCollectors.parallelToList(squarer.andThen(squarer.minusHundred), executor3));
        while (!(list1.isCompletedExceptionally() && list2.isCompletedExceptionally() && list3.isCompletedExceptionally())) {}
        blackhole.consume(list1.join());
        blackhole.consume(list2.join());
        blackhole.consume(list3.join());
    }

}
