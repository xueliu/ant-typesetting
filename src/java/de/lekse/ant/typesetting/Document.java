package de.lekse.ant.typesetting;

import java.io.File;

/**
 *
 * @author Lekse
 */
public class Document {
    private String type;

    private File dir;
    
    private String document;
    
    private File outputdir;
    
    private File outputfile;
    
    private String language;
    
    private boolean draft;
    
    private boolean continuous;
    
    private boolean tikzcompatibility;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public File getOutputdir() {
        return outputdir;
    }

    public void setOutputdir(File outputdir) {
        this.outputdir = outputdir;
    }

    public File getOutputfile() {
        return outputfile;
    }

    public void setOutputfile(File outputfile) {
        this.outputfile = outputfile;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public boolean isContinuous() {
        return continuous;
    }

    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }

    public boolean isTikzcompatibility() {
        return tikzcompatibility;
    }

    public void setTikzcompatibility(boolean tikzcompatibility) {
        this.tikzcompatibility = tikzcompatibility;
    }
    
}
