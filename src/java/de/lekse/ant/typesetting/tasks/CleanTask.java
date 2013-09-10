package de.lekse.ant.typesetting.tasks;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.taskdefs.Delete;

/**
 *
 * @author Lekse
 */
public class CleanTask extends Task {
    
    private static String AUXILIARY_TEX_FILES = "*.aux,*.auxlock,*.gz,*.log,*.nav,*.out,*.pdf,*.snm,*.toc";
    
    private File dir;

    @Override
    public void execute() throws BuildException {
        // Define set of included files
        FileSet fileset = new FileSet();
        fileset.setDir(this.dir);
        fileset.setIncludes(AUXILIARY_TEX_FILES);

        Delete delete = (Delete) this.getProject().createTask("delete");
        delete.addFileset(fileset);
        delete.perform();
    }

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }
    
}
