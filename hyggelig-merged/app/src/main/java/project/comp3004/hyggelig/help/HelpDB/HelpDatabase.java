package project.comp3004.hyggelig.help.HelpDB;

import android.content.Context;

import androidx.room.Room;

import java.io.File;

public class HelpDatabase {
    /**
     * An implementation of the Singleton pattern to ensure that the Crypto activity is always
     * making use of the same, single Volley RequestQueue
     */
    private static Context context;
    private static HelpDB helpDB;
    private static HelpDatabase instance;

    private HelpDatabase(Context context){
        this.context = context;
        helpDB = getDB();
    }

    public HelpDB getDB(){
        // More lazy initialization
        if(helpDB==null) {
            //May God forgive me
            helpDB = Room.databaseBuilder(context,HelpDB.class, "helpDB.db")
                    .fallbackToDestructiveMigration()
                    .createFromAsset("HelpDB.db")
                    .allowMainThreadQueries()
                    .build();
        }
        return helpDB;

    }

    public static HelpDatabase getInstance(Context context){
        //Lazy Initialization
        if (instance == null){
            instance = new HelpDatabase(context.getApplicationContext());
        }

        return instance;
    }

    public TopicList[] getCategories(){
        return helpDB.topicListDao().getCategories();
    }

    public TopicList[] getTopics(String category){
        return helpDB.topicListDao().getTopics(category);
    }

    public String getHelpText(int topicID){
        Topic response = helpDB.topicDao().getTopic(new Integer(topicID).toString());
        if(response==null){
            return "Topic with topicID " + topicID+" not found.";
        }else {
            return response.helpText;
        }
    }
}
