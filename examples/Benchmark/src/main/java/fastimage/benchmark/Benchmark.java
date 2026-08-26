package fastimage.benchmark;

import fastimage.FastImage;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class Benchmark {

    private FastImage srcImage;

    @Setup
    public void setup() {
        srcImage = FastImage.create(1920, 1080);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public Object benchmarkFastImageResize() {
        return srcImage.resize(1280, 720);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public Object benchmarkFastImageKawaseBlur() {
        return srcImage.blurKawase(3.0f, 2);
    }
}
