package project.comp3004.hyggelig.help.HelpDB;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities= {TopicList.class, Topic.class}, version = 2)

public abstract class HelpDB extends RoomDatabase {
    public abstract TopicListDao topicListDao();
    public abstract TopicDao topicDao();
}
