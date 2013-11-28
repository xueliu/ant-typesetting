package de.lekse.ant.typesetting.messages;

/**
 *
 * @author Lekse
 */
public class WarningMessage extends AbstractMessage {

    public WarningMessage(String message) {
        super(message);
    }

    @Override
    protected String getPrefix() {
        return "Warning";
    }
    
}
