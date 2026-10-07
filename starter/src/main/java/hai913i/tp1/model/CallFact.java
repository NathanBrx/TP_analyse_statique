package hai913i.tp1.model;

import java.util.Optional;

public class CallFact {
    private String callerId;
    private Optional<String> targetId; // Optional car peut être non résolu
    private String staticReceiverType;
    private int sourceLine;
    private boolean resolved;
    private boolean internal;
    private boolean isSuper;

    public CallFact(String callerId, Optional<String> targetId, String staticReceiverType, int sourceLine, boolean resolved, boolean internal, boolean isSuper) {
        this.callerId = callerId;
        this.targetId = targetId;
        this.staticReceiverType = staticReceiverType;
        this.sourceLine = sourceLine;
        this.resolved = resolved;
        this.internal = internal;
        this.isSuper = isSuper;
    }

    public String getCallerId() { return callerId; }
    public Optional<String> getTargetId() { return targetId; }
    public String getStaticReceiverType() { return staticReceiverType; }
    public int getSourceLine() { return sourceLine; }
    public boolean isResolved() { return resolved; }
    public boolean isInternal() { return internal; }
    public boolean isSuper() {
        return isSuper;
    }
}