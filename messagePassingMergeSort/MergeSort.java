
//Compile & run:
// javac -cp ../libraries/scala.jar:../libraries/akka-actor.jar MergeSort.java
// java -cp ../libraries/scala.jar:../libraries/akka-actor.jar:../libraries/akka-config.jar:. MergeSort

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import akka.actor.*;

class MergeSort {
    public static void main(String[] args) {
        final var system = ActorSystem.create("MergeSortPipelineSystem");
        final var tester = system.actorOf(Props.create(TesterActor.class));
        final var sorter = system.actorOf(Props.create(SorterActor.class));
        tester.tell(new InitMessage(sorter), ActorRef.noSender());
    }
}

class SorterActor extends UntypedActor {
    public void onReceive(Object o) throws Exception {
        if (o instanceof SortMessage) {
            var list = ((SortMessage) o).list;
            var x = ((SortMessage) o).receiver;

            if (list.size() > 1) {
                var m = getContext().actorOf(Props.create(MergerActor.class));
                m.tell(new ResultMessage(x), ActorRef.noSender());

                var l1 = list.subList(0, list.size() / 2);
                var l2 = list.subList(list.size() / 2, list.size());

                var s1 = getContext().actorOf(Props.create(SorterActor.class));
                s1.tell(new SortMessage(l1, m), ActorRef.noSender());

                var s2 = getContext().actorOf(Props.create(SorterActor.class));
                s2.tell(new SortMessage(l2, m), ActorRef.noSender());
            }

            else {
                x.tell(new SortedMessage(list), ActorRef.noSender());
            }
        }
    }
}

class MergerActor extends UntypedActor {
    private ActorRef receiver;
    private List<Integer> l1;
    private List<Integer> l2;

    private List<Integer> merge(List<Integer> l1, List<Integer> l2) {
        var result = new ArrayList<Integer>();
        if (l1.stream().min(Integer::compare).get() < l2.stream().min(Integer::compare).get()) {
            result.addAll(l1);
            result.addAll(l2);
        } else {
            result.addAll(l2);
            result.addAll(l1);
        }
        return result;
    }

    public void onReceive(Object o) throws Exception {
        if (o instanceof ResultMessage) {
            this.receiver = ((ResultMessage) o).receiver;
        }

        else if (o instanceof SortedMessage) {
            // Since we can't do nested onReceive, we do stateful receiver and
            // list building on this actor.
            if (receiver == null)
                return;
            if (l1 == null) {
                l1 = ((SortedMessage) o).sorted;
            } else {
                l2 = ((SortedMessage) o).sorted;
                var sorted = merge(l1, l2);
                System.out.println("Merged: " + sorted);
                receiver.tell(new SortedMessage(sorted), ActorRef.noSender());
            }
        }
    }
}

class TesterActor extends UntypedActor {
    public void onReceive(Object o) throws Exception {
        if (o instanceof InitMessage) {
            var sorter = ((InitMessage) o).sorter;
            var list = Arrays.asList(new Integer[] { 8, 8, 8, 8, 1, 1, 1, 1 });
            System.out.println(list);
            sorter.tell(new SortMessage(list, getSelf()), ActorRef.noSender());
        } else if (o instanceof SortedMessage) {
            System.out.println("RESULT: " + ((SortedMessage) o).sorted);
        }
    }
}

class InitMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public final ActorRef sorter;

    public InitMessage(ActorRef sorter) {
        this.sorter = sorter;
    }
}

class SortMessage implements Serializable {
    private static final long serialVersionUID = 2L;
    public final List<Integer> list;
    public final ActorRef receiver;

    public SortMessage(List<Integer> list, ActorRef receiver) {
        this.list = list;
        this.receiver = receiver;
    }
}

class ResultMessage implements Serializable {
    private static final long serialVersionUID = 3L;
    public final ActorRef receiver;

    public ResultMessage(ActorRef receiver) {
        this.receiver = receiver;
    }
}

class SortedMessage implements Serializable {
    private static final long serialVersionUID = 4L;
    public final List<Integer> sorted;

    public SortedMessage(List<Integer> sorted) {
        this.sorted = sorted;
    }
}