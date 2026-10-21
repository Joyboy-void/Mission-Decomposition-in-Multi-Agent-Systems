package graphviz.core;

public final class GraphvizException extends RuntimeException{

    public GraphvizException(String message){
        super(message);
    }

    public GraphvizException(String message, Throwable cause){
        super(message, cause);
    }
}
