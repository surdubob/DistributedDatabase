package ro.fmi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Message {
    public List<Element> view;

    public Map<Integer, Integer> t;

    public Map<Element, Integer> del;

    public Message(List<Element> view, Map<Integer, Integer> t, Map<Element, Integer> del) {
        this.view = view;
        this.t = t;
        this.del = del;
    }
}
