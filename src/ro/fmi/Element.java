package ro.fmi;

import java.util.Objects;

public class Element {
    public int cre;
    public int T;
    public int value;

    public Element(int cre, int t, int value) {
        this.cre = cre;
        T = t;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Element{" +
                "cre=" + cre +
                ", T=" + T +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        Element e = (Element) obj;
        return e.value == value && e.cre == cre && e.T == T;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cre, T, value);
    }
}
