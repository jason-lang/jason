package jason.jeditplugin;


/** based on FileImporter of ProjectView plugin */
public class JasonProjectImporter { //extends Importer {
/*
    public JasonProjectImporter(VPTProject node, ProjectViewer viewer) {
        super(node, viewer);
    }
    
    protected Collection internalDoImport() {
        String state = null;
        if (viewer != null) {
            state = viewer.getFolderTreeState(project);
        }
        
        addTree(new File(project.getRootPath()), 
                project, 
                new GlobFilter("*.asl *.xml *.mas2j *.java log4j.configuration",""), //GlobFilter.getImportSettingsFilter(),
                false); // filtro: 
        
        if (state != null) {
            postAction = new NodeStructureChange(project, state);
        }
        return null;
    }
*/
    /**
     *  Adds a directory tree to the given node.
     *
     *  @param  root    The root directory from where to look for files.
     *  @param  where   The node to where the new files will be added.
     *  @param  filter  The filter to use to select files.
     *  @param  flatten Whether to "flat import" (add all files to top directory).
     */
    /*
    protected void addTree(File root, VPTNode where,
                           FilenameFilter filter, boolean flatten)
    {
        File[] children;

        if (filter != null){
            children = root.listFiles(filter);
        } else {
            children = root.listFiles();
        }

        if (children == null || children.length == 0) return;

        for (int i = 0; i < children.length; i++) {
            if (!children[i].exists()) {
                continue;
            }

            VPTNode child;
            if (children[i].isDirectory()) {
                child = (flatten) ? where : findDirectory(children[i], where, true);
                addTree(children[i], child, filter, flatten);
            } else {
                child = new VPTFile(children[i]);
                if (where.getIndex(child) != -1) {
                    continue;
                }
                registerFile((VPTFile) child);
            }

            if ((!child.isDirectory() || child.getChildCount() != 0)
                && child.getParent() == null
                && child != where
            ) {
                where.add(child);
            }
        }

        where.sortChildren();
    }
    */
}

