package de.lekse.ant.typesetting.messages;

/**
 *
 * @author Lekse
 */
public abstract class AbstractMessage {
    
    protected abstract String getPrefix();
    
    private String message;

    public AbstractMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return String.format("%1$s: %2$s", getPrefix(), getMessage());
    }
    
}
