package de.lekse.ant.typesetting.tasks;

import de.lekse.ant.typesetting.messages.AbstractMessage;
import de.lekse.ant.typesetting.messages.ErrorMessage;
import de.lekse.ant.typesetting.messages.WarningMessage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Environment.Variable;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;

public class TypesetTask extends Task {
    
    private static final String TYPE_DEFAULT = "default";
    
    private static final String TYPE_ARTICLE = "article";
    
    private static final String TYPE_BEAMER = "beamer";
    
    private static final String TYPE_BEAMER_HANDOUT = "beamer-handout";
    
    private static final String TYPE_BEAMER_ARTICLE = "beamer-article";
    
    private static final String ENV_VARIABLE_TEXINPUTS = "TEXINPUTS";

    private static final String EXEC_PROPERTY_NAMESPACE = TypesetTask.class.getSimpleName().toLowerCase();
    
    /**
     * Defines the type of the compiled document. The type can be on of the
     * following values:
     * 
     * - default: Does not change the documentclass of the document
     * - article: Compiles using the documentclass article
     * - beamer: Compiles using the documentclass beamer
     * - beamer-handout: Compiles using the documentclass beamer with attribute
     *                   handout
     * - beamer-article: Compiles using the documentclass beamer and calls
     *                   imports the package beamerarticle directly afterwards.
     */
    private String type;
    
    /**
     * 
     */
    private String documentclass;
    
    /**
     * 
     */
    private String documentattributes;

    /**
     * Defines the working directory in which the pdflatex compiler is
     * executed. Defaults to the current ${basedir}.
     */
    private File basedir;
    
    /**
     * Defines the file containing the document to compile.
     */
    private File document;
    
    /**
     * Defines the directory in which auxiliary files are stored. The value of
     * this attribute is used as the value for the parameter -output-directory
     * when invoking pdflatex.
     */
    private File outputdir;
    
    /**
     * Defines the name of the generated output. The value of this attribute is
     * used as the value for the parameter -jobname when invoking pdflatex.
     */
    private String outputname;
    
    /**
     * Defines the language the document should be compiled with.
     * TODO explain this feature in more detail
     */
    private String language;
    
    /**
     * Defines a set of files, which should be watched concerning changes. If a related document is changes, the
     * typesetting job is executed.
     */
    private List<FileSet> relatedDocuments;
    
    /**
     * If true, the pdflatex compiler is invoked with the -draftmode option.
     * Defaults to false.
     */
    private boolean draft;
    
    /**
     * If true, the continuous compiler mode is enabled instead of the one-shot mode.
     */
    private boolean continuous;
    
    /**
     * If true, the pdflatex compiler is invoked with support for caching TikZ pictures.
     */
    private boolean cache;
    
    /**
     * Defines the path which should be used for caching TikZ pictures.
     */
    private File cachedir;
    
    /**
     * If true, the task outputs the log produced by the pdflatex invocation.
     * Defaults to false.
     */
    private boolean verbose;
    
    /**
     * Defines a set of paths which should be used for searching inputs. The
     * value of this attribute is used to define the TEXINPUTS environment
     * variable when invoking pdflatex.
     */
    private org.apache.tools.ant.types.Path inputPath;
    
    /**
     * Default constructor
     */
    public TypesetTask() {
        // Call parent constructor
        super();
        
        // Initialize lists
        this.relatedDocuments = new ArrayList<>();
        
        // Set default parameters
        this.type = TYPE_DEFAULT;
        
        this.draft = false;
        this.continuous = false;
        this.cache = false;
        this.verbose = false;
    }
    
    /**
     * Validates the parameters passed to the task.
     * 
     * @throws BuildException 
     */
    private void validateAttributes() throws BuildException {
        // Require the document attribute
        if (this.document == null) {
            throw new BuildException(String.format("Document attribute has to be specified", this.document.getAbsoluteFile()));
        }
        
        // Require an existing document
        if (!this.document.exists()) {
            throw new BuildException(String.format("Document \"%1$s\" does not exist", this.document.getAbsoluteFile()));
        }
        
        // Require the document to have a .tex extension
        String documentName = this.document.getName();
        
        if (!documentName.endsWith(".tex")) {
            throw new BuildException(String.format("Document \"%1$s\" does not have a .tex extension", this.document.getAbsoluteFile()));
        }
        
        // Require an existing base directory
        if (this.basedir != null && !this.basedir.exists() ) {
            throw new BuildException(String.format("Base directory \"%1$s\" does not exist", this.basedir.getAbsolutePath()));
        }
        
        // Require an existing output directory
        if (this.outputdir != null && !this.outputdir.exists() ) {
            throw new BuildException(String.format("Output directory \"%1$s\" does not exist", this.outputdir.getAbsoluteFile()));
        }
        
        // Require an existing cache directory
        if (this.cachedir != null && !this.cachedir.exists() ) {
            throw new BuildException(String.format("Cache directory \"%1$s\" does not exist", this.cachedir.getAbsoluteFile()));
        }
    }

    /**
     * Verifies that the LateX compiler is available for execution.
     *
     * @throws BuildException
     */
    private void validateCompiler() throws BuildException {
        // TODO Implement validateCompiler
    }
    
    /**
     * Parses errors and warnings raised by the pdflatex invocation.
     * 
     * @param log Raw output from pdflatex process
     */
    private List<AbstractMessage> parseLog(String log) {
        List<AbstractMessage> messages = new ArrayList<>();
        String[] logs = log.replace("\r\n", "\n").split("\n");

        for (int i = 0; i < logs.length; i++) {
            String line = logs[i];

            // Capture error
            if (line.startsWith("!")) {
                // Concat error in current and subsequent line
                String error = String.format("%1$s %2$s", line.substring(2), logs[i + 1]);

                // Skip subsequent line
                i += 1;

                // Append error to list
                messages.add(new ErrorMessage(error));
            }
            // Capture warning
            else if (line.startsWith("LaTeX Warning")) {
                String warning = line.substring(15);

                // Append warning to list
                messages.add(new WarningMessage(warning));
            }
        }
        
        return messages;
    }
    
    private void displayLog(List<AbstractMessage> messages) {
        // Display errors and warnings
        for (AbstractMessage message : messages) {
            this.log(message.toString());
        }
    }
    
    private void buildDocument() throws BuildException {
        // Get project
        Project _project = this.getProject();
        
        // Create buffer for preamble
        StringBuilder preamble = new StringBuilder();
        
        // Determine input document
        Path basePath = Paths.get((this.basedir != null ? this.basedir : _project.getBaseDir()).getAbsolutePath());
        Path documentPath = Paths.get(this.document.getParent());
        Path relativeDocumentPath = basePath.relativize(documentPath);
        
        String documentName = this.document.getName();
        String documentWithoutExt = documentName.substring(0, documentName.lastIndexOf("."));
        String documentSeparator = relativeDocumentPath.toString().isEmpty() ? "" : "/";
        
        String inputDocument = String.format("%1$s%3$s%2$s", relativeDocumentPath.toString().replaceAll("\\\\", "/"), documentWithoutExt, documentSeparator);
        
        // Determine relative cache path
        String externalPrefix = "";
        
        if (this.cachedir != null) {
            Path cachePath = Paths.get(this.cachedir.getPath());
            Path relativeCachePath = basePath.relativize(cachePath);
            externalPrefix = relativeCachePath.toString().replaceAll("\\\\", "/");
            
            // Ensure that the external prefix ends with a trailing slash
            if (!externalPrefix.endsWith("/")) {
                externalPrefix = externalPrefix.concat("/");
            }
        }
        
        // Document class
        String _documentclass = null;
        
        if (this.documentclass != null) {
            // Document class has been explicitly specified
            if (this.documentattributes != null) {
                _documentclass = String.format("\\documentclass[%2$s]{%1$s}", this.documentclass, this.documentattributes);
            }
            else {
                _documentclass = String.format("\\documentclass{%1$s}", this.documentclass);
            }
        }
        else {
            // Document class is derived from type
            if (type.equals(TYPE_DEFAULT)) {
                // Do not overwrite the documentclass
                // TODO read document class from document file
                _documentclass = "\\documentclass{article}";
            }
            else if (type.equals(TYPE_ARTICLE)) {
                // Use documentclass article
                _documentclass = "\\documentclass{article}";
            }
            else if (type.equals(TYPE_BEAMER)) {
                // Use documentclass beamer
                _documentclass = "\\documentclass{beamer}";
            }
            else if (type.equals(TYPE_BEAMER_HANDOUT)) {
                // Use documentclass beamer with handout attribute
                _documentclass = "\\documentclass[handout]{beamer}";
            }
            else if (type.equals(TYPE_BEAMER_ARTICLE)) {
                // Use documentclass beamer with importing the package beamerarticle 
                _documentclass = "\\documentclass{article}";
                _documentclass += "\\usepackage{beamerarticle}";
            }
        }
        
        if (_documentclass != null) {
            // Add document class to preamble
            preamble.append(_documentclass);

            // Overwrite the documentclass
            preamble.append("\\renewcommand\\documentclass[2][]{}");
        }
        
        // Ensure compatibility with TikZ externalize feature
        if (this.cache) {
            // Insert TikZ code into preamble
            preamble.append("\\usepackage{tikz}");
            preamble.append("\\usetikzlibrary{external}");
            
            // Insert placeholder for system call definition
            preamble.append("\\tikzsetsystemcall");
        }
        
        // Define tikz cache dir
        if (this.cache && this.cachedir != null) {
            preamble.append(String.format("\\tikzsetexternalprefix{%1$s}", externalPrefix));
        }
        
        // Language
        if (this.language != null) {
            preamble.append(String.format("\\newcommand\\locale{%1$s}", language));
        }

        // Set input document
        preamble.append(String.format("\\input{%1$s}", inputDocument));
        
        // Set TikZ externalize system call
        if (this.cache) {
            String filteredPreamble = preamble.toString().replace("\\tikzsetsystemcall", "");
            String tikzSetSystemCall = String.format("\\tikzset{external/system call={pdflatex \\tikzexternalcheckshellescape -halt-on-error -interaction=batchmode -jobname \"\\image\" \"\\string\\def\\string\\tikzexternalrealjob{%1$s}%2$s\"}}", inputDocument, filteredPreamble.replace("\\", "\\string\\"));
            
            preamble = new StringBuilder(preamble.toString().replace("\\tikzsetsystemcall", tikzSetSystemCall));
        }

        // Job name
        String jobname;
        
        if (this.outputname != null) {
            // Use the defined output name as job name
            jobname = this.outputname;
        }
        else {
            // Use the document name as job name
            jobname = documentWithoutExt;
        }
        
        // Create execution task
        ExecTask exec = (ExecTask) _project.createTask("exec");
        // TODO allow configurable latex compiler (e.g. luatex)
        exec.setExecutable("pdflatex");
        
        if (!this.verbose) {
            // Capture output and error stream of pdflatex

            exec.setOutputproperty(String.format("%1$s.out", EXEC_PROPERTY_NAMESPACE));
            exec.setErrorProperty(String.format("%1$s.err", EXEC_PROPERTY_NAMESPACE));
        }
        
        exec.setInputString(preamble.toString());

        // Set working directory
        if (this.basedir != null) {
            exec.setDir(this.basedir);
        }

        // Append arguments
        exec.createArg().setValue("-shell-escape");
        exec.createArg().setValue("-interaction=nonstopmode");
        exec.createArg().setValue(String.format("-jobname=%1$s", jobname));
        
        if (this.outputdir != null) {
            exec.createArg().setValue(String.format("-output-directory=%1$s", this.outputdir));
        }

        if (this.draft) {
            exec.createArg().setValue("-draftmode");
        }
        
        // Append environment variables
        if (this.inputPath != null) {
            // Determine complete input path path (prepend base path and append default texinputs)
            String texInputs = String.format("%1$s%2$s", this.inputPath.toString(), File.pathSeparator);
            
            // Create environment variable
            Variable texInputVar = new Variable();
            texInputVar.setKey(ENV_VARIABLE_TEXINPUTS);
            texInputVar.setValue(texInputs);
            
            exec.addEnv(texInputVar);
        }

        // Execute pdflatex
        exec.perform();

        // Get output and error stream
        String out = _project.getProperty(String.format("%1$s.out", EXEC_PROPERTY_NAMESPACE));

        if (!this.verbose) {
            // Parse errors and warnings
            List<AbstractMessage> messages = parseLog(out);

            // Display errors and warnings
            displayLog(messages);
        }
    }
    
    private void continuousExecute() throws BuildException {
        try {
            // Build list of distinct paths to watch
            // TODO implement continuous feature
            
            // Create watch service
            // (see http://docs.oracle.com/javase/tutorial/essential/io/notification.html#overview)
            WatchService watcher = FileSystems.getDefault().newWatchService();

            // Register event listening for document changes
            WatchKey registeredKey = Paths.get(this.document.getParent()).register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

            // Event loop
            while (true) {
                // Wait for key to be signaled
                WatchKey key;

                try {
                    key = watcher.take();
                } catch (InterruptedException e) {
                    break;
                }
                
                // TODO modified events are fired twice (content change and last modified timestamp changed!)
                // TODO include related documents

                boolean changed = false;

                for (WatchEvent<?> event: key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // Handle overflow event kind
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    // Determine the file name
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path filename = pathEvent.context();

                    this.log(String.format("%1$s has been changed", filename.toAbsolutePath()));
                    changed = true;
                }

                // Reset the key and cancel if the watcher is no longer valid
                if (!key.reset()) {
                    break;
                }

                // Compile document if changes occurred
                if (changed) {
                    this.verbose = true;
                    //this.buildDocument();
                }
            }

        } catch (IOException e) {
            // Do nothing
        }
    }
    
    @Override
    public void execute() throws BuildException {
        // Validate attributes of the task
        this.validateAttributes();

        // Verify availability of LateX compiler
        this.validateCompiler();
        
        if (!this.continuous) {
            // One-shot mode
        
            // Build the document once
            this.buildDocument();
        }
        else {
            // Continuous mode
            this.continuousExecute();
        }
    }
    
    public void addFileset(FileSet fileSet) {
        this.relatedDocuments.add(fileSet);
    }

    public File getBasedir() {
        return basedir;
    }

    public void setBasedir(File basedir) {
        this.basedir = basedir;
    }

    public File getDocument() {
        return document;
    }

    public void setDocument(File document) {
        this.document = document;
    }

    public File getOutputdir() {
        return outputdir;
    }

    public void setOutputdir(File outputdir) {
        this.outputdir = outputdir;
    }

    public String getVersion() {
        return type;
    }

    public void setVersion(String version) {
        this.type = version;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getDraft() {
        return draft;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    public Boolean getTikzcompatibility() {
        return cache;
    }

    public void setTikzcompatibility(Boolean tikzcompatibility) {
        this.cache = tikzcompatibility;
    }

    public String getOutputname() {
        return outputname;
    }

    public void setOutputname(String outputname) {
        this.outputname = outputname;
    }

    public Boolean getContinuous() {
        return continuous;
    }

    public void setContinuous(Boolean continuous) {
        this.continuous = continuous;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getDocumentclass() {
        return documentclass;
    }

    public void setDocumentclass(String documentclass) {
        this.documentclass = documentclass;
    }

    public String getDocumentattributes() {
        return documentattributes;
    }

    public void setDocumentattributes(String documentattributes) {
        this.documentattributes = documentattributes;
    }

    public boolean isCache() {
        return cache;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }

    public File getCachedir() {
        return cachedir;
    }

    public void setCachedir(File cachedir) {
        this.cachedir = cachedir;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    public void setInputPathRef(Reference r) {
        if (this.inputPath == null) {
            this.inputPath = new org.apache.tools.ant.types.Path(getProject());
        }
        
        this.inputPath.setRefid(r);
    }
    
}
