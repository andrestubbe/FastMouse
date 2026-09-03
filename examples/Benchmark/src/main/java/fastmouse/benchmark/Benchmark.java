package fastmouse.benchmark;

import fastmouse.FastMouse;

import fastmouse.MouseDevice;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class Benchmark {

    private FastMouse mouse;

    @Setup
    public void setup() {
        try {
            mouse = FastMouse.open();
        } catch (Exception e) {
            mouse = null;
        }
    }

    @org.openjdk.jmh.annotations.Benchmark
    public List<MouseDevice> benchmarkGetConnectedDevices() {
        if (mouse != null) {
            return mouse.getConnectedDevices();
        }
        return null;
    }
}
