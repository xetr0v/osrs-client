package org.prop;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * oldrs
 * 2.3.2013
 */
public class Properties {

    private Map<String, Section> sections;
    private String filename;

    public Properties() {
        sections = new HashMap<String, Section>();
    }

    public void load(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String currentSectionName = "";
        Section currentSection = null;
        while((line = in.readLine()) != null) {// top lel
            if(line.startsWith("[") && line.endsWith("]")) {
                currentSectionName = line.substring(1, line.length() - 1);
                currentSection = new Section(currentSectionName);
                sections.put(currentSectionName, currentSection);
            }
            else if(!currentSectionName.equals("") &&
                    line.indexOf('=') > 0 && !line.startsWith("#")) {
                String key = line.substring(0, line.indexOf('='));
                String value = line.substring(line.indexOf('=') + 1, line.length());
                currentSection.putProperty(key, value);
            }
        }
        in.close();
    }

    /**
     * Load a property file
     * @param filename
     * @throws IOException
     */
    public void load(String filename) throws IOException {
        this.filename = filename;
        BufferedReader in = new BufferedReader(new FileReader(filename));
        String line;
        String currentSectionName = "";
        Section currentSection = null;
        while((line = in.readLine()) != null) {// top lel
            if(line.startsWith("[") && line.endsWith("]")) {
                currentSectionName = line.substring(1, line.length() - 1);
                currentSection = new Section(currentSectionName);
                sections.put(currentSectionName, currentSection);
            }
            else if(!currentSectionName.equals("") &&
                    line.indexOf('=') > 0 && !line.startsWith("#")) {
                String key = line.substring(0, line.indexOf('='));
                String value = line.substring(line.indexOf('=') + 1, line.length());
                currentSection.putProperty(key, value);
            }
        }
        in.close();
    }

    /**
     * Get a section by its name
     * @param sectionName
     * @throws IllegalArgumentException if no such section exists
     * @return
     */
    public Section getSection(String sectionName) {
        if(!sections.containsKey(sectionName))
            throw new IllegalArgumentException("No section: " + sectionName);
        return sections.get(sectionName);
    }

    /**
     * Write the property entries into a file
     * @param filename
     * @throws IOException
     */
    public void save(String filename) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(filename));
        for(Map.Entry<String, Section> sectionEntry : sections.entrySet()) {
            String sectionName = sectionEntry.getKey();
            Section section = sectionEntry.getValue();
            out.println("[" + sectionName + "]");
            for(Map.Entry<String, String> entry : section.getEntries().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                out.println(key + "=" + value);
            }
            out.println();
        }
        out.close();
    }

    /**
     * Adds a new section entry and replaces an already existing one with the same name if it exists
     * @param section
     */
    public void putSection(Section section) {
        putSection(section, true);
    }

    /**
     * Adds a new section entry, or replaces an already existing one with the same name if it exists
     * @param section
     * @param overwrite whether to overwrite or not if match is found
     */

    public void putSection(Section section, boolean overwrite) {
        if(!overwrite && sections.containsKey(section.getName()))
            throw new IllegalArgumentException("Section " + section.getName() + " already exists");
        sections.put(section.getName(), section);
    }

    public void removeSection(String sectionName) {
        sections.remove(sectionName);
    }

    public String getFilename() {
        return filename;
    }

    public void reload() throws IOException {
        for(Map.Entry<String, Section> entry : sections.entrySet())
            entry.getValue().clear();// probably not necessary
        sections.clear();
        load(filename);
    }
}
