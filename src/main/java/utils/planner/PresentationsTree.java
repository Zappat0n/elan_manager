package utils.planner;

import main.ApplicationLoader;
import utils.data.Presentation;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PresentationsTree {
    private static final String TAG = PresentationsTree.class.getSimpleName();
    int student;
    public int size;
    double age;
    Boolean goOn = true;
    LinkedHashMap <Integer, ArrayList<Integer>> initialBlock;
    Node root;

    public PresentationsTree(int student, double age) {
        this.student = student;
        this.age = age;
        size = 0;
        root = new Node(0,null);
        initialize();
    }

    private void initialize () {
        double target = Math.floor(age * 2)/2 - 0.5;
        initialBlock = ApplicationLoader.cacheManager.presentationsPerYearAndSubarea.get(target);
        for (int subarea : initialBlock.keySet()) {
            ArrayList<Integer> presentations = initialBlock.get(subarea);
            int presentation = presentations.get(0);
            ArrayList<Integer> subs = ApplicationLoader.cacheManager.presentationsSubPerPresentation.get(presentation);
            Node first = new Node(presentation, subs != null ? subs.get(0) : null);
            root.addChild(first);
            size++;
            createNodesForBranch(presentations, presentation, first);
        }
    }

     private void createNodesForBranch(ArrayList<Integer> presentations, int lastPres, Node root) {
        checkRootToFollowSubs(root);
        int index = presentations.indexOf(lastPres);
        if (index < presentations.size() - 1) {
            int presentation = presentations.get(index + 1);
            ArrayList<Integer> subs = ApplicationLoader.cacheManager.presentationsSubPerPresentation.get(presentation);
            Node node = new Node(presentation, subs != null ? subs.get(0) : null);
            root.addChild(node);
            size++;
            createNodesForBranch(presentations, presentation, node);
        }
    }

    private void checkRootToFollowSubs(Node root) {
        if (root.presentationSub == null) return;
        ArrayList<Integer> subs = ApplicationLoader.cacheManager.presentationsSubPerPresentation.get(root.presentation);
        int index = subs.indexOf(root.presentationSub);
        if (index < subs.size() - 1) {
            root.addChild(new Node(root.presentation, subs.get(index+1)));
            size++;
            goOn = true;
        }
    }

    // List Tree
    public ArrayList<Presentation> listPresentations (int amount) {
        return processQueue(new ArrayList<>(root.getChildren()), new ArrayList<>(), 0, amount);
    }

    private ArrayList<Presentation> processQueue(ArrayList<Node> queue, ArrayList<Presentation> result, int count,
                                                 int amount) {
        if (queue.size() == 0 || count >= amount) return  result;
        Node node = queue.remove(0);
        result.add(new Presentation(node.presentation, node.presentationSub));
        queue.addAll(node.getChildren());
        return processQueue(queue, result, ++count, amount);
    }



    // Class Node of the tree

    static class Node {
        int presentation;
        Integer presentationSub;
        ArrayList<Node> children;

        protected Node (int presentation, Integer sub) {
            this.presentation = presentation;
            this.presentationSub = sub;
            children = new ArrayList<>();
        }

        protected void addChild(Node node) {
            children.add(node);
        }

        protected ArrayList<Node> getChildren() {
            return children;
        }
    }
}
