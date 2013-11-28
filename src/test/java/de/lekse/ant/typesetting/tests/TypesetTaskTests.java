package de.lekse.ant.typesetting.tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;
import de.lekse.ant.typesetting.tasks.TypesetTask;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;

/**
 * Unit tests for the typeset task.
 */
@RunWith(JUnit4.class)
public class TypesetTaskTests {
    private Project antProject;
    
    private Target defaultTarget;
    
    private File outputFolder;
    
    private File cacheFolder;
    
    private static final String DEFAULT_TARGET_NAME = "typeset-test";
    
    private static final String OUTPUT_FOLDER = "tex-output";
    
    private static final String CACHE_FOLDER = "tex-cache";
    
    private File createFolder(File folder) {
        if (folder.exists()) {
            if (!folder.isDirectory()) {
                folder.delete();
            }
            else {
                return folder;
            }
        }
        
        folder.mkdir();
        
        return folder;
    }
    
    private File getTestClassesFolder() {
        try {
            return new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            fail();
            
            return null;
        }
    }
    
    private File getTargetFolder() {
        return getTestClassesFolder().getParentFile();
    }
    
    private File getResourceFolder() {
        String testsPackageName = getClass().getPackage().getName();
        String resourcesPackageName = testsPackageName.concat(".resources");
        File resourcesPackagePath = new File(getTestClassesFolder(), resourcesPackageName.replace(".", "/"));
        
        return resourcesPackagePath;
    }
    
    private File getOutputFolder() {
        return new File(getTargetFolder(), OUTPUT_FOLDER);
    }
    
    private File getCacheFolder() {
        return new File(getTargetFolder(), CACHE_FOLDER);
    }
    
    @Before 
    public void initialize() {
        // Create and initialize an ant project
        antProject = new Project();
        antProject.setBaseDir(getTargetFolder());
        antProject.init();
        
        // Create default target
        defaultTarget = new Target();
        defaultTarget.setName(DEFAULT_TARGET_NAME);
        antProject.addTarget(defaultTarget);
        
        // Create temporary folders
        outputFolder = createFolder(getOutputFolder());
        cacheFolder = createFolder(getCacheFolder());
    }

    /**
     * Test for compiling a basic document
     */
    @Test
    public void simpleDocument() {
        // Create task
        TypesetTask typesetTask = new TypesetTask();
        typesetTask.setProject(antProject);
        defaultTarget.addTask(typesetTask);
        
        // Set attributes
        typesetTask.setDocument(new File(getResourceFolder(), "simple.tex"));
        typesetTask.setOutputdir(getOutputFolder());
        typesetTask.setCachedir(getCacheFolder());
        typesetTask.setVerbose(false);
        
        // Execute target
        defaultTarget.execute();
        
        // Verify existence of simple document
        assertTrue(new File(getOutputFolder(), "simple.pdf").exists());
    }
    
    /**
     * Test for compiling a document with cached contents
     */
    @Test
    public void cacheDocument() {
        // Create task
        TypesetTask typesetTask = new TypesetTask();
        typesetTask.setProject(antProject);
        defaultTarget.addTask(typesetTask);
        
        // Set attributes
        typesetTask.setDocument(new File(getResourceFolder(), "tikzpicture.tex"));
        typesetTask.setOutputdir(getOutputFolder());
        typesetTask.setCache(true);
        typesetTask.setCachedir(getCacheFolder());
        typesetTask.setVerbose(false);
        
        // Execute target
        defaultTarget.execute();
        
        // Verify existence of simple document
        assertTrue(new File(getOutputFolder(), "tikzpicture.pdf").exists());
    }
}
