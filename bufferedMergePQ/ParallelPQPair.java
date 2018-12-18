import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class ParallelPQPair implements PQPair {
    PQ left, right;

    public PQPair clone() {
        return new ParallelPQPair();
    }

    public void createPairParam(Parameters param, Function<Parameters, PQ> instanceCreator) {
        var executor = Executors.newWorkStealingPool();
        var tasks = new ArrayList<Callable<Void>>();
        tasks.add(() -> {
            left = instanceCreator.apply(param.left());
            return null;
        });
        tasks.add(() -> {
            right = instanceCreator.apply(param.right());
            return null;
        });
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    };

    public PQ getLeft() {
        return left;
    }

    public PQ getRight() {
        return right;
    }
}
