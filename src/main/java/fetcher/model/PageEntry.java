package fetcher.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fetcher.controller.MainController;
import javafx.application.Platform;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * File name : PageEntry.java
 *
 * This class represents the abstraction for a single URL entry.
 * Using Jsoup it fetches all the needed informations from the web page.
 *
 */
public class PageEntry{
    String URL;
    String Name;
    String Description;
    Date DateAdded;
    String pageSnapshot;
    MainController controller;
    HashSet<String> tags;

    public PageEntry(String URLstr,MainController controller){
        this.URL = URLstr;
        this.controller = controller;
        this.pageSnapshot = "images/octopus.png";
        this.Description = "";
        this.tags = new HashSet<String>();
        //This is to 'reset' the filter by tags (Show all entries) .
        tags.add("all");
        Thread workerThread = new Thread(new Worker(true));
        workerThread.start();
    }

    /**
     * Constructor used just when loading from the json file.
     * @param controller the mainController.
     */
    public PageEntry(MainController controller){
        this.controller = controller;
        this.tags = new HashSet<String>();
        tags.add("all");
        Thread workerThread = new Thread(new Worker(false));
        workerThread.start();
    }

    /**
     * Loads the page provided by the URL parameter and uses Jsoup to retrieve its content.
     * @return true if the page loading has success
     */
    private boolean loadPage(){
        Document doc;
        try {
            doc = Jsoup.connect(URL).userAgent("Mozilla").get();
        } catch (HttpStatusException e){
            System.out.println("Failed to load page : " + URL + " , HTTP status code received was : " + e.getStatusCode());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        this.Name = doc.title();
        this.DateAdded = Calendar.getInstance().getTime();
        //Getting description
        if(doc.select("p").first() != null)
            Description = doc.select("p").first().text();
        //Getting the pad absolute path.
        final String padFolder = (new File(controller.pad.getpadName())).getParent();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String URL = getURL();
                String dir = padFolder + File.separator + Utils.IMAGES_SUBFOLDER + File.separator;
                if(!URL.contains("youtube")) {
                    pageSnapshot = "file:///" + dir + (new Snapshotter(getURL(), padFolder, controller)).getWebsiteSnapshot();
                }
                else{
                    pageSnapshot = "file:///" + dir + Snapshotter.getYoutubeThumbnail(getURL(),dir);

                }
            }
        });
        return true;
    }

    /**
     * Creates a JsonObject containing a json representation of this PageEntry.
     * @return savedJson is the JsonObject containing the fields of this PageEntry.
     */
    public JsonObject saveEntry(){
        JsonObject savedJson = new JsonObject();
        JsonArray savedTags = new JsonArray();
        savedJson.addProperty("URL",URL);
        savedJson.addProperty("Name",Name);
        savedJson.addProperty("Description",Description);
        savedJson.addProperty("DateAdded", new SimpleDateFormat("dd MMMM yy , hh:mm:ss" , Locale.getDefault()).format(this.DateAdded));
        savedJson.addProperty("pageSnapshot",pageSnapshot);
        for(String tag : tags)
            savedTags.add(new JsonPrimitive(tag));
        savedJson.add("Tags", savedTags);
        return savedJson;
    }

    /**
     * Loads an entry based on a json Object.
     * @param JsonObjectEntry the json object containing the entry.
     * @throws ParseException
     */
    public void loadEntry(JsonObject JsonObjectEntry) throws ParseException {
        setURL(JsonObjectEntry.get("URL").getAsString());
        setDescription(JsonObjectEntry.get("Description").getAsString());
        setName(JsonObjectEntry.get("Name").getAsString());
        setPageSnapshot(JsonObjectEntry.get("pageSnapshot").getAsString());
        setDateAdded(new SimpleDateFormat("dd MMMM yy , hh:mm:ss", Locale.getDefault()).parse(JsonObjectEntry.get("DateAdded").getAsString()));
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Date getDateAdded() {
        return DateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        DateAdded = dateAdded;
    }

    public String getPageSnapshot() {
        return pageSnapshot;
    }

    public void setPageSnapshot(String pageSnapshot) {
        this.pageSnapshot = pageSnapshot;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void addTag(String tag){
        this.tags.add(tag);
    }

    public HashSet<String> getTags() {
        return tags;
    }

    public void setTags(HashSet<String> tags) {
        this.tags = tags;
    }

    public void clearTags() {
        tags.clear();
        tags.add("all");
    }

    public void deleteTag(String selectedItem) {
        if(tags.contains(selectedItem) && !selectedItem.equalsIgnoreCase("all")) tags.remove(selectedItem);
    }

    /**
     * Worker class is used to load a single PageEntry from a Thread so that the main program doesn't get stuck.
     */
    class Worker implements Runnable{

        //The worker Thread is used both when loading a URL from the clipboard and when we are loading from json. This boolean makes the double-usage possible.
        boolean loadFromURL;

        public Worker(boolean loadFromUrl){
            this.loadFromURL = loadFromUrl;
        }

        @Override
        public void run() {
            //If we are loading from a url we try to load the page. We return if the loading fails (so the controller won't be notified).
            if(loadFromURL)
                if(!loadPage()) return;

            controller.notifyControllerNewEntry(PageEntry.this);
        }
    }
}

