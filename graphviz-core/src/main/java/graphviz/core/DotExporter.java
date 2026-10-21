package graphviz.core;


@FunctionalInterface
public interface DotExporter<T> {

    String export(T object);
}
