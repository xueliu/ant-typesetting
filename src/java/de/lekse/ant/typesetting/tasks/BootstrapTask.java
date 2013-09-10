package de.lekse.ant.typesetting.tasks;

import de.lekse.ant.typesetting.Document;
import java.util.HashMap;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/**
 *
 * @author Lekse
 */
public class BootstrapTask extends Task {
    
    private static final String DEFAULT_PROPERTY_NAMESPACE = "documents.";
    
    private String namespace;

    @Override
    public void execute() throws BuildException {
        // Determine property namespace
        String _namespace = namespace;
        
        if (_namespace == null) {
            // Use default namespace
            _namespace = DEFAULT_PROPERTY_NAMESPACE;
        }
        
        // Scan properties and create an own target for each document type
        Map<String,Object> properties = this.getProject().getProperties();
        Map<String,Document> documents = new HashMap<>();
        
        // Iterate through properties in namespace PROPERTY_NAMESPACE
        for (Map.Entry<String,Object> property : properties.entrySet()) {
            String key = property.getKey();
            
            if (key.startsWith(_namespace)) {
                // Determine document identifier
                String documentIdentifier = key.substring(_namespace.length(), key.indexOf(".", _namespace.length()));
                
                // Get existing document
                Document document = documents.get(key);
                
                if (document == null) {
                    // Create new document if it does not exist
                    document = new Document();
                    documents.put(documentIdentifier, document);
                }
                
                // Map individual property
                Class<Document> documentClass = (Class<Document>) document.getClass();
                
                //documentClass.getField("key").s
                //document.getClass().
            }
        }
        
        // Create target for each document
        for (Map.Entry<String,Document> document : documents.entrySet()) {
            // Create target
            Target _target = new Target();
            _target.setName(String.format("build-%s", document.getKey()));
            
            // Add tasks to target
            // TODO
            
            // Add target to current project
            this.getProject().addTarget(_target);
        }
        
        super.execute();
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
}
