package axl.compiler.analysis.common.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TreeAnalyzer {

    private final List<Visitor> visitors;

    private final List<Object> tempChildren = new ArrayList<>();

    public TreeAnalyzer(List<Visitor> visitors) {
        this.visitors = visitors;
    }

    public void analyze(Object root) {
        record Frame(Object node, boolean exit) {}

        Deque<Frame> stack = new ArrayDeque<>();
        stack.push(new Frame(root, false));

        while (!stack.isEmpty()) {
            Frame frame = stack.pop();
            Object node = frame.node();

            if (!frame.exit()) {
                tempChildren.clear();
                for (Visitor v : visitors) {
                    v.enter(this, node);
                }

                List<Object> children = List.copyOf(tempChildren);
                stack.push(new Frame(node, true));

                for (int i = children.size() - 1; i >= 0; i--) {
                    stack.push(new Frame(children.get(i), false));
                }
            } else {
                for (Visitor v : visitors) {
                    v.exit(this, node);
                }
            }
        }
    }

    public void enqueue(Object child) {
        if (child == null)
            return;

        tempChildren.add(child);
    }
}
