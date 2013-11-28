package de.lekse.ant.typesetting.messages;

/**
 *
 * @author Lekse
 */
public class ErrorMessage extends AbstractMessage {

    public ErrorMessage(String message) {
        super(message);
    }

    @Override
    protected String getPrefix() {
        return "Error";
    }
    
}
