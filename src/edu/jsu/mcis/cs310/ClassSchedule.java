package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ClassSchedule {
    
    private final String CSV_FILENAME = "jsu_sp24_v1.csv";
    private final String JSON_FILENAME = "jsu_sp24_v1.json";
    
    private final String CRN_COL_HEADER = "crn";
    private final String SUBJECT_COL_HEADER = "subject";
    private final String NUM_COL_HEADER = "num";
    private final String DESCRIPTION_COL_HEADER = "description";
    private final String SECTION_COL_HEADER = "section";
    private final String TYPE_COL_HEADER = "type";
    private final String CREDITS_COL_HEADER = "credits";
    private final String START_COL_HEADER = "start";
    private final String END_COL_HEADER = "end";
    private final String DAYS_COL_HEADER = "days";
    private final String WHERE_COL_HEADER = "where";
    private final String SCHEDULE_COL_HEADER = "schedule";
    private final String INSTRUCTOR_COL_HEADER = "instructor";
    private final String SUBJECTID_COL_HEADER = "subjectid";
    
    public String convertCsvToJsonString(List<String[]> csv) {
        
        // Create an iterator for the CSV data.
        Iterator<String[]> iterator = csv.iterator();
       
        // Initialize JSON structures
        
        JsonObject rootObject = new JsonObject();
        JsonObject scheduleTypeMap = new JsonObject();
        JsonObject subjectMap = new JsonObject();
        JsonObject courseMap = new JsonObject();
        JsonArray sectionsArray = new JsonArray();

        
        

        

        // Assuming the first row contains headers, create a map for column indexes.
        Map<String, Integer> headers = new HashMap<>();
        String[] headerRow = iterator.next();
        
        for (int i = 0; i < headerRow.length; ++i) {
             headers.put(headerRow[i], i);   
        }
        // Initialize a Map to hold scheduleType as keys and lists of sections as values

       
        
        
        while (iterator.hasNext()) {
            
            String[] record = iterator.next();
            
            
            
            // Get ScheduleType Mappings
            
            String type = record[ headers.get(TYPE_COL_HEADER) ];
            String schedule = record[ headers.get(SCHEDULE_COL_HEADER) ];
            
            scheduleTypeMap.put(type, schedule);
            
            // Get Subject Mappings
            
            String num = record[ headers.get(NUM_COL_HEADER) ];
            String subject = record[ headers.get(SUBJECT_COL_HEADER) ];
            
            String[] numSplit = num.split(" ");
            
            subjectMap.put(numSplit[0], subject);
            
            // Get Course Mappings
            
            JsonObject courseRow = new JsonObject();
            
            courseRow.put(SUBJECTID_COL_HEADER, numSplit[0]);
            courseRow.put(NUM_COL_HEADER, numSplit[1]);
            courseRow.put(DESCRIPTION_COL_HEADER, record[ headers.get(DESCRIPTION_COL_HEADER) ]);
            courseRow.put(CREDITS_COL_HEADER, Integer.valueOf(record[ headers.get(CREDITS_COL_HEADER) ]));
            
            courseMap.put(num, courseRow);
            
            // Get Section Mappings
        
            JsonObject sectionRow = new JsonObject();
            sectionRow.put(CRN_COL_HEADER, Integer.valueOf(record[ headers.get(CRN_COL_HEADER) ]));
            sectionRow.put(SUBJECTID_COL_HEADER,numSplit[0]);
            sectionRow.put(NUM_COL_HEADER, numSplit[1]);
            sectionRow.put(SECTION_COL_HEADER,(record[ headers.get(SECTION_COL_HEADER) ]));
            sectionRow.put(TYPE_COL_HEADER, record[ headers.get(TYPE_COL_HEADER) ]);
            sectionRow.put(START_COL_HEADER, (record[ headers.get(START_COL_HEADER)] ));
            sectionRow.put(END_COL_HEADER, record[headers.get(END_COL_HEADER)]);
            sectionRow.put(DAYS_COL_HEADER, record[headers.get(DAYS_COL_HEADER)]);
            sectionRow.put(WHERE_COL_HEADER, record[headers.get(WHERE_COL_HEADER)]);
            String instructor = record[ headers.get(INSTRUCTOR_COL_HEADER) ];
            JsonArray instructors = new JsonArray();
            for (String inst : instructor.split(",")) {
            instructors.add(inst.trim()); // Use trim() to remove leading and trailing spaces
            }
            sectionRow.put("instructor", instructors);

            //instructors.addAll(Arrays.asList(instructor.split(",")));
            //sectionRow.put("instructors",instructors);
            sectionsArray.add(sectionRow);
       
            
        
        
        rootObject.put("scheduletype", scheduleTypeMap);
        rootObject.put("subject", subjectMap);
        rootObject.put("course", courseMap);
        rootObject.put("section", sectionsArray);
        }
        
        //Serialize JSON object to string
        
        return Jsoner.serialize(rootObject);
        
    }


        
    
    
    public String convertJsonToCsvString(JsonObject json) {
    
        StringBuilder csvBuilder = new StringBuilder();
        JsonObject scheduleTypes = (JsonObject) json.get("scheduletype");
        JsonObject subjects = (JsonObject) json.get("subject");
        JsonObject courses = (JsonObject) json.get("course");
        JsonArray sections = (JsonArray) json.get("section");

    // Define CSV headers
    List<String> headers = List.of(CRN_COL_HEADER, SUBJECT_COL_HEADER, NUM_COL_HEADER,DESCRIPTION_COL_HEADER,SECTION_COL_HEADER, TYPE_COL_HEADER, CREDITS_COL_HEADER, START_COL_HEADER, END_COL_HEADER, DAYS_COL_HEADER,WHERE_COL_HEADER, SCHEDULE_COL_HEADER, INSTRUCTOR_COL_HEADER);
    csvBuilder.append(String.join("\t", headers)).append("\n");

    // Iterate over sections
    for (Object sectionObj : sections) {
        JsonObject section = (JsonObject) sectionObj;
        String subjectId = (String) section.get("subjectid");
        String num = (String) section.get(NUM_COL_HEADER );
        String type = (String) section.get("type");
        
        String courseIdentifier = subjectId + " " + num;
        JsonObject course = (JsonObject) courses.get(courseIdentifier);

       
        
        List<String> row = new ArrayList<>();
        row.add(String.valueOf(section.get(CRN_COL_HEADER))); // Crn
        row.add(subjects.getOrDefault(subjectId, "").toString());  //subject: Accounting
        row.add(courseIdentifier);  // num: subjectid , num
        row.add(course != null ? course.get("description").toString() : ""); // description
        row.add((String) section.get("section")); // section
        row.add((String) section.get("type")); // type
        row.add(course != null ? course.get("credits").toString() : ""); //gets credits
        row.add((String) section.get("start"));
        row.add((String) section.get("end"));
        row.add((String) section.get("days"));
        row.add((String) section.get("where"));
        row.add(scheduleTypes.getOrDefault(type, "").toString()); //gives schedule on csv
        
        JsonArray instructors = (JsonArray) section.get("instructor");
        StringBuilder instructorsStr = new StringBuilder();
        if (instructors != null) {
            for (Object instructor : instructors) {
                if (instructorsStr.length() > 0) instructorsStr.append(", ");
                instructorsStr.append((String) instructor);
            }
        }
        row.add(instructorsStr.toString());

        csvBuilder.append(String.join("\t", row)).append("\n");
    }

    return csvBuilder.toString();
    }


    
    



    
    
    public JsonObject getJson() {
        
        JsonObject json = getJson(getInputFileData(JSON_FILENAME));
        return json;
        
    }
    
    public JsonObject getJson(String input) {
        
        JsonObject json = null;
        
        try {
            json = (JsonObject)Jsoner.deserialize(input);
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return json;
        
    }
    
    public List<String[]> getCsv() {
        
        List<String[]> csv = getCsv(getInputFileData(CSV_FILENAME));
        return csv;
        
    }
    
    public List<String[]> getCsv(String input) {
        
        List<String[]> csv = null;
        
        try {
            
            CSVReader reader = new CSVReaderBuilder(new StringReader(input)).withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
            csv = reader.readAll();
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return csv;
        
    }
    
    public String getCsvString(List<String[]> csv) {
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");
        
        csvWriter.writeAll(csv);
        
        return writer.toString();
        
    }
    
    private String getInputFileData(String filename) {
        
        StringBuilder buffer = new StringBuilder();
        String line;
        
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        
        try {
        
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream("resources" + File.separator + filename)));

            while((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return buffer.toString();
        
    }
    
}