package top.frankyang.exp.internal;

public abstract class RendererContext {
    private String feedback;

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String s) {
        feedback = s;
    }

    public abstract String getMessage();
}
