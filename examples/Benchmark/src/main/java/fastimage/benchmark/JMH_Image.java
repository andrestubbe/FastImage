package fastimage.benchmark;

import fastimage.FastImage;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 2, time = 1)
@Fork(1)
public class JMH_Image {

    private FastImage srcImage;

    @Setup
    public void setup() {
        srcImage = FastImage.create(1920, 1080);
    }

    @Benchmark
    public Object benchmarkFastImageResize() {
        return srcImage.resize(1280, 720);
    }

    @Benchmark
    public Object benchmarkFastImageKawaseBlur() {
        return srcImage.blurKawase(3.0f, 2);
    }
}
